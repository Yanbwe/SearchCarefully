package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerAccessMixin {
    @Accessor("hoveredSlot")
    Slot getHoveredSlot();
    
    @Accessor("leftPos")
    int getLeftPos();
    
    @Accessor("topPos")
    int getTopPos();
}