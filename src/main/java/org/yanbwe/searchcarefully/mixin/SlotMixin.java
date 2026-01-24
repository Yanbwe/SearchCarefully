package org.yanbwe.searchcarefully.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yanbwe.searchcarefully.util.SearchConstants;

@Mixin(Slot.class)
public class SlotMixin {

    @Shadow
    public ItemStack getItem() {
        return null; // Shadow method, implementation not needed
    }

    /**
     * 拦截mayPickup方法以防止玩家拿起仍有搜索时间的物品
     * 在Mojang官方映射中，该方法名为mayPickup
     */
    @Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z", 
            at = @At("HEAD"), 
            cancellable = true, 
            remap = true)
    private void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getItem();
        
        // Check if the item has a remaining search time
        if (stack.hasTag() && stack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING)) {
            int searchTime = stack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING);
            
            // If search time is greater than 0, prevent the player from picking up the item
            if (searchTime > 0) {
                cir.cancel();
                cir.setReturnValue(false);
            }
        }
    }
}