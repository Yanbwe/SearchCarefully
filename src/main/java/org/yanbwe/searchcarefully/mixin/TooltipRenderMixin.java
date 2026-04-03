package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.client.ClientOverlayRenderer;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SearchConstants;

import java.util.List;

/**
 * Low-level tooltip rendering interception Mixin
 * Directly intercepts Minecraft's native tooltip rendering methods to ensure compatibility with mods like Obscure Tooltips
 */
@Mixin(GuiGraphics.class)
public abstract class TooltipRenderMixin {
    
    // Slot cache optimization
    @Unique
    private static Slot lastHoveredSlot = null;
    @Unique
    private static long lastCheckTime = 0;
    @Unique
    private static final long CACHE_DURATION = 50; // 50ms cache
    
    // Global interception state flag - now managed by ClientOverlayRenderer
    // @Unique
    // private static boolean tooltipBlockedThisFrame = false;
    
    // Cache commonly used object references
    @Unique
    private static Minecraft cachedMc = null;
    @Unique
    private static AbstractContainerScreen<?> cachedScreen = null;

    /**
     * Remove tooltip interception logic, rely entirely on event system
     * Avoid conflicts with Obscure Tooltips
     */
    /*
    @Inject(
        method = "renderTooltipInternal",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 1
    )
    private void onRenderTooltipInternal(
            Font font, 
            List<ClientTooltipComponent> components, 
            int mouseX, 
            int mouseY, 
            ClientTooltipPositioner positioner, 
            CallbackInfo ci) {
        // Mixin interception logic removed, handled by event system
    }
    */

    /**
     * Use cache-optimized slot detection
     */
    @Unique
    private Slot findSlotUnderMouseWithCache(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        long currentTime = System.currentTimeMillis();
        
        // Check if cache is valid
        if (lastHoveredSlot != null && 
            currentTime - lastCheckTime < CACHE_DURATION) {
            // Quick check if last slot still matches
            if (isMouseStillOverSlot(lastHoveredSlot, mouseX, mouseY)) {
                return lastHoveredSlot;
            }
        }
        
        // Perform full search
        Slot foundSlot = findSlotUnderMouse(screen, mouseX, mouseY);
        
        // Update cache
        lastHoveredSlot = foundSlot;
        lastCheckTime = currentTime;
        
        return foundSlot;
    }
    
    /**
     * Check if mouse is still over the specified slot
     */
    @Unique
    private boolean isMouseStillOverSlot(Slot slot, int mouseX, int mouseY) {
        if (cachedScreen == null || cachedScreen.getMenu() == null) {
            return false;
        }
        
        int guiLeft = ((ContainerAccessMixin) cachedScreen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) cachedScreen).getTopPos();
        
        int slotX = guiLeft + slot.x;
        int slotY = guiTop + slot.y;
        
        return mouseX >= slotX && mouseY >= slotY && 
               mouseX < slotX + 16 && mouseY < slotY + 16;
    }
    
    /**
     * Optimized condition checking
     */
    @Unique
    private boolean shouldBlockTooltipOptimized(Slot slot) {
        if (slot == null || !slot.hasItem()) return false;
        
        ItemStack stack = slot.getItem();
        
        // Use encapsulated utility methods for checking, simplifying logic
        return ItemStackHelper.hasRemainingSearchTime(stack) &&
               ItemStackHelper.getRemainingSearchTime(stack) > 0;
    }
    
    /**
     * Rendering timing optimization: check if tooltip rendering should be processed
     */
    @Unique
    private boolean shouldProcessTooltipRendering() {
        if (cachedMc == null) return false;
        
        // Only process when game is running and has focus
        if (!cachedMc.isWindowActive() || cachedMc.getOverlay() != null) {
            return false;
        }
        
        // Only process in container screens
        return cachedScreen != null;
    }
    
    /**
     * Update cached object references
     */
    @Unique
    private void updateCachedReferences() {
        Minecraft mc = Minecraft.getInstance();
        if (cachedMc != mc) {
            cachedMc = mc;
            cachedScreen = mc.screen instanceof AbstractContainerScreen ? 
                          (AbstractContainerScreen<?>) mc.screen : null;
        }
    }
    
    /**
     * Original slot finding method (retained for full search)
     */
    @Unique
    private Slot findSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen.getMenu() == null) {
            return null;
        }

        // Use ContainerAccessMixin to get GUI position information
        int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) screen).getTopPos();
        
        // Data structure optimization: use spatial partitioning for large containers
        if (screen.getMenu().slots.size() > 20) {
            return findSlotUsingSpatialPartition(screen, mouseX, mouseY, guiLeft, guiTop);
        }
        
        // Use linear search for small containers
        for (Slot slot : screen.getMenu().slots) {
            int slotX = guiLeft + slot.x;
            int slotY = guiTop + slot.y;
            
            if (mouseX >= slotX && mouseY >= slotY && 
                mouseX < slotX + 16 && mouseY < slotY + 16) {
                return slot;
            }
        }
        
        return null;
    }
    
    /**
     * Data structure optimization: use spatial partitioning to accelerate slot finding in large containers
     */
    @Unique
    private Slot findSlotUsingSpatialPartition(AbstractContainerScreen<?> screen, int mouseX, int mouseY, int guiLeft, int guiTop) {
        // Simplified spatial partitioning implementation
        // Calculate approximate area where mouse is located
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;
        
        // Only check slot rows that might contain the mouse
        int startRow = Math.max(0, (relY / 18) - 1); // 18 is slot spacing
        int endRow = Math.min(screen.getMenu().slots.size() / 9 + 1, (relY / 18) + 2);
        
        // Search within limited range
        for (int i = startRow * 9; i < Math.min(endRow * 9, screen.getMenu().slots.size()); i++) {
            if (i >= 0 && i < screen.getMenu().slots.size()) {
                Slot slot = screen.getMenu().slots.get(i);
                int slotX = guiLeft + slot.x;
                int slotY = guiTop + slot.y;
                
                if (mouseX >= slotX && mouseY >= slotY && 
                    mouseX < slotX + 16 && mouseY < slotY + 16) {
                    return slot;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Reset per-frame state flags
     */
    @Unique
    private static void resetFrameState() {
        org.yanbwe.searchcarefully.client.ClientOverlayRenderer.setTooltipBlocked(false);
    }
    

}