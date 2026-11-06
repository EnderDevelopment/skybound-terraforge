package dev.enderdevelopment.skyfoundry;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class SkyfoundryClient implements ClientModInitializer {
    @Override public void onInitializeClient() { HandledScreens.register(SkyfoundryMod.FOUNDRY_SCREEN, FoundryScreen::new); }
}
