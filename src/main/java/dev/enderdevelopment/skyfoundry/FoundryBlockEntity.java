package dev.enderdevelopment.skyfoundry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class FoundryBlockEntity extends BlockEntity {
    private final List<CapturedBlock> island = new ArrayList<>();
    private Vec3d velocity = Vec3d.ZERO;
    private BlockPos offset = BlockPos.ORIGIN;
    private Vec3d preciseOffset = Vec3d.ZERO;
    private boolean moving;
    private int age;
    private final Random random = new Random();
    private final Set<BlockPos> islandPositions = new HashSet<>();

    public FoundryBlockEntity(BlockPos pos, BlockState state) { super(SkyfoundryMod.FOUNDRY_ENTITY, pos, state); }
    public boolean isMoving() { return moving; }
    public void setVelocity(Vec3d velocity) { this.velocity = velocity; markDirty(); }

    public boolean formIsland(int requestedRadius) {
        if (!(world instanceof ServerWorld server) || moving || requestedRadius < 4 || requestedRadius > 16) return false;
        BlockPos origin = getPos();
        List<CapturedBlock> captured = new ArrayList<>();
        for (int x = -requestedRadius; x <= requestedRadius; x++) {
            for (int z = -requestedRadius; z <= requestedRadius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                double edge = requestedRadius - 0.45 + Math.sin(x * 1.7 + z) * 0.8 + Math.cos(z * 1.3 - x) * 0.6;
                if (distance > edge) continue;
                int depth = Math.max(1, (int) ((1.0 - distance / requestedRadius) * 5.0) + random.nextInt(3));
                for (int y = 0; y >= -depth; y--) {
                    BlockPos source = origin.add(x, y, z);
                    BlockState sourceState = server.getBlockState(source);
                    if (source.equals(origin) || sourceState.isAir() || sourceState.isOf(Blocks.BEDROCK) || sourceState.hasBlockEntity()) continue;
                    captured.add(new CapturedBlock(new BlockPos(x, y, z), sourceState));
                }
            }
        }
        if (captured.isEmpty()) return false;
        island.clear();
        islandPositions.clear();
        island.addAll(captured);
        for (CapturedBlock block : captured) islandPositions.add(block.relative().toImmutable());
        moving = true;
        age = 0;
        offset = BlockPos.ORIGIN;
        preciseOffset = Vec3d.ZERO;
        velocity = new Vec3d(0, 0.12, 0);
        for (CapturedBlock block : captured) server.setBlockState(origin.add(block.relative()), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        markDirty();
        return true;
    }

    public static void tick(ServerWorld world, BlockPos pos, BlockState state, FoundryBlockEntity entity) {
        if (!entity.moving || entity.island.isEmpty()) return;
        entity.age++;
        if (entity.age % 3 == 0) world.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, pos.getX() + 0.5 + entity.offset.getX(), pos.getY() + entity.offset.getY() - 0.4, pos.getZ() + 0.5 + entity.offset.getZ(), 3, 0.7, 0.2, 0.7, 0.02);
        if (entity.age % 8 == 0) world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, pos.getX() + 0.5 + entity.offset.getX(), pos.getY() + entity.offset.getY() + 1.0, pos.getZ() + 0.5 + entity.offset.getZ(), 2, 0.8, 0.5, 0.8, 0.01);
        entity.velocity = entity.velocity.add(0, -0.006, 0).multiply(0.995);
        Vec3d desired = entity.preciseOffset.add(entity.velocity);
        BlockPos next = new BlockPos((int) Math.round(desired.x), (int) Math.round(desired.y), (int) Math.round(desired.z));
        if (!entity.canOccupy(world, pos, next)) {
            entity.velocity = new Vec3d(entity.velocity.x * 0.72, entity.velocity.y < 0 ? 0 : entity.velocity.y * 0.35, entity.velocity.z * 0.72);
            desired = entity.preciseOffset;
            next = entity.offset;
        }
        entity.moveBlocks(world, pos, next);
        entity.preciseOffset = desired;
        entity.offset = next;
        if (entity.velocity.lengthSquared() < 0.0004 && entity.age > 30) {
            entity.velocity = Vec3d.ZERO;
            entity.moving = false;
        }
        entity.markDirty();
    }

    private boolean canOccupy(ServerWorld world, BlockPos origin, BlockPos at) {
        Set<BlockPos> oldPositions = new HashSet<>();
        for (CapturedBlock block : island) oldPositions.add(origin.add(block.relative()).add(offset).toImmutable());
        Set<BlockPos> destinationPositions = new HashSet<>();
        for (CapturedBlock block : island) {
            BlockPos target = origin.add(block.relative()).add(at);
            if (!destinationPositions.add(target.toImmutable())) continue;
            if (oldPositions.contains(target)) continue;
            if (!world.getBlockState(target).getCollisionShape(world, target).isEmpty()) return false;
        }
        return true;
    }


    private void moveBlocks(ServerWorld world, BlockPos origin, BlockPos at) {
        if (offset.equals(at)) return;
        for (CapturedBlock block : island) world.setBlockState(origin.add(block.relative()).add(offset), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        for (CapturedBlock block : island) world.setBlockState(origin.add(block.relative()).add(at), block.state(), Block.NOTIFY_ALL);
    }

    @Override protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        nbt.putBoolean("Moving", moving);
        nbt.putDouble("VX", velocity.x); nbt.putDouble("VY", velocity.y); nbt.putDouble("VZ", velocity.z);
        nbt.putInt("OX", offset.getX()); nbt.putInt("OY", offset.getY()); nbt.putInt("OZ", offset.getZ());
        nbt.putDouble("PX", preciseOffset.x); nbt.putDouble("PY", preciseOffset.y); nbt.putDouble("PZ", preciseOffset.z); nbt.putInt("Age", age);
        NbtList blocks = new NbtList();
        for (CapturedBlock block : island) {
            NbtCompound item = new NbtCompound();
            item.putInt("X", block.relative().getX()); item.putInt("Y", block.relative().getY()); item.putInt("Z", block.relative().getZ());
            NbtElement encoded = BlockState.CODEC.encodeStart(lookup.getOps(NbtOps.INSTANCE), block.state()).result().orElse(new NbtCompound());
            item.put("State", encoded); blocks.add(item);
        }
        nbt.put("Blocks", blocks);
    }

    @Override protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        moving = nbt.getBoolean("Moving"); age = nbt.getInt("Age");
        offset = new BlockPos(nbt.getInt("OX"), nbt.getInt("OY"), nbt.getInt("OZ"));
        preciseOffset = new Vec3d(nbt.getDouble("PX"), nbt.getDouble("PY"), nbt.getDouble("PZ"));
        velocity = new Vec3d(nbt.getDouble("VX"), nbt.getDouble("VY"), nbt.getDouble("VZ"));
        island.clear();
        islandPositions.clear();
        NbtList blocks = nbt.getList("Blocks", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < blocks.size(); i++) {
            NbtCompound item = blocks.getCompound(i);
            BlockPos relative = new BlockPos(item.getInt("X"), item.getInt("Y"), item.getInt("Z"));
            BlockState.CODEC.parse(lookup.getOps(NbtOps.INSTANCE), item.get("State")).result().ifPresent(blockState -> {
                island.add(new CapturedBlock(relative, blockState));
                islandPositions.add(relative.toImmutable());
            });
        }
    }

    private record CapturedBlock(BlockPos relative, BlockState state) {}
}
