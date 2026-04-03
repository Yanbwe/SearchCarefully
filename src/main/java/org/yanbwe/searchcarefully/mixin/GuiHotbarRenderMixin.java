package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// GuiHotbarRenderMixin no longer needs to directly call RarityRegistry
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(Gui.class)
public class GuiHotbarRenderMixin {

    // Collect data for search overlay rendering before hotbar rendering
    @Inject(
        method = "renderHotbarAndDecorations", 
        at = @At("HEAD")
    )
    private void collectHotbarSearchOverlayData(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player != null) {
            Inventory inventory = mc.player.getInventory();
            
            // Iterate through all hotbar slots (index 0-8)
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = inventory.getItem(i);
                            
                if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                    int searchTime = (int) ItemStackHelper.getRemainingSearchTime(itemStack);
                    
                    if (searchTime > 0) {
                        // Calculate position of corresponding slot in hotbar
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        int hotbarX = (screenWidth - 182) / 2; // Hotbar texture starting X coordinate
                        int hotbarY = screenHeight - 22; // Hotbar Y coordinate
                        int x = hotbarX + i * 20 + 3; // Each slot is 20 pixels wide, internal offset 3 pixels
                        int y = hotbarY + 3; // Slot internal offset
                        
                        // Add hotbar item information that needs mask rendering to cache
                        SlotRenderCache.addHotbarOverlay(x, y, itemStack, searchTime);
                    }
                }
            }
        }
    }

    // Unified rendering of search overlays after hotbar rendering completes
    @Inject(
        method = "renderHotbarAndDecorations", 
        at = @At("TAIL")
    )
    private void renderHotbarSearchOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Render all hotbar overlays from cache
        for (SlotRenderCache.HotbarOverlayInfo overlayInfo : SlotRenderCache.getPendingHotbarOverlays()) {
            // Get unified mask texture
            var maskTexture = CustomTextureHandler.getMaskTexture();
            
            // Try to render custom texture mask, fallback to black fill if texture doesn't exist
            try {
                // Use blit method to render custom mask texture, ensure it displays above items
                guiGraphics.blit(maskTexture, overlayInfo.x, overlayInfo.y, 0, 0, 16, 16, 16, 16);
            } catch (Exception e) {
                // If texture loading fails, fallback to black fill
                guiGraphics.fill(overlayInfo.x, overlayInfo.y, overlayInfo.x + 16, overlayInfo.y + 16, 999, 0xFF000000); // Pure black completely opaque, Z value set to 400 to ensure top layer
            }
            
            // Render rotation animation texture
            try {
                var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                long currentTime = System.currentTimeMillis();
                
                // Calculate rotation texture position
                float[] position = RotationAnimationHandler.getRotatingPosition(
                    overlayInfo.searchTime * 1000L, // Use remaining search time as base for start time
                    currentTime
                );
                
                // Render rotation animation texture, placed above mask (higher Z value)
                guiGraphics.blit(rotationTexture, 
                    (int)(overlayInfo.x + position[0]), 
                    (int)(overlayInfo.y + position[1]), 
                    0, 0, 16, 16, 16, 16); // 16x16 texture
            } catch (Exception e) {
                // If rotation texture loading fails, ignore
            }
        }
        
        // Clear hotbar overlay cache
        SlotRenderCache.clearHotbarCache();
    }
}