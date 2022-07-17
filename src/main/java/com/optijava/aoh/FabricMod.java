package com.optijava.aoh;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    private static boolean isAutoWalk = false;

    @Override
    public void onInitialize() {
        LOGGER.info("[AOH] Auto On Hook Mod is already loaded.");
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            // Auto Attach
            try {
                if (minecraftClient.player != null && minecraftClient.options.keyAttack.isPressed() && minecraftClient.player.getAttackCooldownProgress(0) >= 1) {
                    if (minecraftClient.crosshairTarget != null && minecraftClient.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                        Entity entity = (((EntityHitResult) minecraftClient.crosshairTarget).getEntity());
                        if (entity.isAlive() && entity.isAttackable()) {
                            if (minecraftClient.interactionManager != null) {
                                minecraftClient.interactionManager.attackEntity(minecraftClient.player, entity);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("[AOH] Auto On Hook Mod:Unexpect Exception in auto attack.", e);
            }

            // Auto Walk
            if (!FabricMod.isAutoWalk) {
                if (minecraftClient.options.keyForward.isPressed() && minecraftClient.options.keyBack.isPressed()) {
                    FabricMod.isAutoWalk = true;
                    LOGGER.info("[AOH] Auto Walk start.");
                }
            }
            if (FabricMod.isAutoWalk) {
                if (minecraftClient.options.keyForward.isPressed() && minecraftClient.options.keyBack.isPressed()) {
                    FabricMod.isAutoWalk = false;
                    LOGGER.info("[AOH] Auto Walk stop.");
                }
            }
            if (FabricMod.isAutoWalk) {
                minecraftClient.player.setMovementSpeed(5.0f);
            }
        });
    }
}
