package org.yanbwe.searchcarefully.mixin;

import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestMenu.class)
public interface ChestMenuAccessor {
    
    // Add accessors for private fields if needed
    // For now, we're not adding specific accessors as our implementation doesn't require them
    // This is created in case we need to access private fields of ChestMenu in the future
}