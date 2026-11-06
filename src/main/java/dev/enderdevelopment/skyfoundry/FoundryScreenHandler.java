package dev.enderdevelopment.skyfoundry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public final class FoundryScreenHandler extends ScreenHandler {
    private final BlockPos position;

    public FoundryScreenHandler(int syncId, PlayerInventory inventory) { this(syncId, inventory, BlockPos.ORIGIN); }
    public FoundryScreenHandler(int syncId, PlayerInventory inventory, BlockPos position) {
        super(SkyfoundryMod.FOUNDRY_SCREEN, syncId);
        this.position = position;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public BlockPos position() { return position; }

    @Override public boolean canUse(PlayerEntity player) { return player.squaredDistanceTo(position.getX() + .5, position.getY() + .5, position.getZ() + .5) < 64; }
    @Override public net.minecraft.item.ItemStack quickMove(PlayerEntity player, int slot) { return net.minecraft.item.ItemStack.EMPTY; }

    @Override public boolean onButtonClick(PlayerEntity player, int id) {
        if (!canUse(player) || !(player.getWorld().getBlockEntity(position) instanceof FoundryBlockEntity foundry)) return false;
        if (id == 0) {
            int radius = player.getWorld().getBlockState(position).get(FoundryBlock.RADIUS);
            int next = radius >= 16 ? 4 : radius + 2;
            player.getWorld().setBlockState(position, player.getWorld().getBlockState(position).with(FoundryBlock.RADIUS, next));
            return true;
        }
        if (id == 1) {
            int radius = player.getWorld().getBlockState(position).get(FoundryBlock.RADIUS);
            if (foundry.formIsland(radius)) player.closeHandledScreen();
            return true;
        }
        return false;
    }
}
