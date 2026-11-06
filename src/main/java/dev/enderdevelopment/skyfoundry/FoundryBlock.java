package dev.enderdevelopment.skyfoundry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FoundryBlock extends Block implements BlockEntityProvider {
    public static final IntProperty RADIUS = IntProperty.of("radius", 4, 16);
    public FoundryBlock(Settings settings) { super(settings); setDefaultState(getStateManager().getDefaultState().with(RADIUS, 8)); }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(RADIUS); }
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new FoundryBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, SkyfoundryMod.FOUNDRY_ENTITY, (serverWorld, pos, blockState, entity) -> FoundryBlockEntity.tick(serverWorld, pos, blockState, entity));
    }
    @Override public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof FoundryBlockEntity foundry) {
            if (player.isSneaking()) {
                int next = state.get(RADIUS) >= 16 ? 4 : state.get(RADIUS) + 2;
                world.setBlockState(pos, state.with(RADIUS, next), Block.NOTIFY_ALL);
                player.sendMessage(Text.literal("Island radius: " + next + " blocks."), true);
            } else if (!foundry.isMoving()) {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, user) ->
                        new FoundryScreenHandler(syncId, inventory, pos), Text.literal("Skyfoundry controls")));
            } else {
                player.sendMessage(Text.literal("Use /island throw to launch the island."), true);
            }
        }
        return ActionResult.CONSUME;
    }
}
