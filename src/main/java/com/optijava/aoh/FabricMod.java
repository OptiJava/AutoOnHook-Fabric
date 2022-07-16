package com.optijava.aoh;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Auto On Hook Mod is already loaded.");
        ClientTickCallback.EVENT.register(minecraftClient -> {
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
                throw new RuntimeException("Auto On Hook Mod:Unexpect Exception in auto attack.", e);
            }
        });
    }
}
