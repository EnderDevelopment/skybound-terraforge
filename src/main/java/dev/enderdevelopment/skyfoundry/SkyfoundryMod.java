package dev.enderdevelopment.skyfoundry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class SkyfoundryMod implements ModInitializer {
    public static final String MOD_ID = "skyfoundry";
    public static BlockEntityType<FoundryBlockEntity> FOUNDRY_ENTITY;
    public static net.minecraft.screen.ScreenHandlerType<FoundryScreenHandler> FOUNDRY_SCREEN;

    @Override
    public void onInitialize() {
        FoundryBlock foundry = new FoundryBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f).requiresTool());
        Registry.register(Registries.BLOCK, id("foundry"), foundry);
        Registry.register(Registries.ITEM, id("foundry"), new BlockItem(foundry, new Item.Settings()));
        FOUNDRY_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("foundry"),
                BlockEntityType.Builder.create(FoundryBlockEntity::new, foundry).build());
        FOUNDRY_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("foundry"),
                new net.minecraft.screen.ScreenHandlerType<>(FoundryScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("island")
                .then(CommandManager.literal("steer").then(CommandManager.argument("direction", Vec3ArgumentType.vec3()).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    FoundryBlockEntity entity = findNearest(player);
                    if (entity == null) return 0;
                    entity.setVelocity(Vec3ArgumentType.getVec3(context, "direction").normalize().multiply(0.65));
                    context.getSource().sendFeedback(() -> Text.literal("Island trajectory set."), false);
                    return 1;
                })))
                .then(CommandManager.literal("throw").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    FoundryBlockEntity entity = findNearest(player);
                    if (entity == null) return 0;
                    entity.setVelocity(player.getRotationVec(1).multiply(1.1).add(0, 0.35, 0));
                    context.getSource().sendFeedback(() -> Text.literal("Island launched."), false);
                    return 1;
                }))));
    }

    private static FoundryBlockEntity findNearest(ServerPlayerEntity player) {
        for (BlockPos pos : BlockPos.iterateOutwards(player.getBlockPos(), 48, 20, 48)) {
            if (player.getWorld().getBlockEntity(pos) instanceof FoundryBlockEntity entity && entity.isMoving()) return entity;
        }
        player.sendMessage(Text.literal("No moving Skyfoundry island nearby."), false);
        return null;
    }

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }
}
