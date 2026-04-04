package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

@Mixin(Gui.class)
public class GuiHotbarRenderMixin {

    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("TAIL"))
    private void renderSlotWithSearchMask(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int slotIndex, CallbackInfo ci) {
        if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
            double remainingTime = ItemStackHelper.getRemainingSearchTime(itemStack);
            
            if (remainingTime > 0) {
                renderSearchMask(guiGraphics, x, y);
                renderRotationAnimation(guiGraphics, x, y, remainingTime);
            }
        }
    }

    private void renderSearchMask(GuiGraphics guiGraphics, int x, int y) {
        var maskTexture = CustomTextureHandler.getMaskTexture();
        
        try {
            guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
        } catch (Exception e) {
            guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000);
        }
    }

    private void renderRotationAnimation(GuiGraphics guiGraphics, int x, int y, double remainingTime) {
        try {
            var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
            long currentTime = System.currentTimeMillis();
            
            float[] position = RotationAnimationHandler.getRotatingPosition(
                (long)(remainingTime * 1000L),
                currentTime
            );
            
            guiGraphics.blit(rotationTexture, 
                (int)(x + position[0]), 
                (int)(y + position[1]), 
                450, 0, 0, 16, 16, 16, 16);
        } catch (Exception e) {
        }
    }
}