package org.yanbwe.searchcarefully.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
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
        
        // 使用封装的工具方法检查搜索时间
        if (ItemStackHelper.hasRemainingSearchTime(stack)) {
            int searchTime = (int) ItemStackHelper.getRemainingSearchTime(stack);
            
            // 如果搜索时间大于 0，阻止玩家拿起物品
            if (searchTime > 0) {
                cir.cancel();
                cir.setReturnValue(false);
            }
        }
    }
}