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

@Mixin(value = Slot.class, priority = 500)
public class SlotMixin {

    @Shadow
    public ItemStack getItem() {
        return null;
    }

    /**
     * 拦截mayPickup阻止玩家拿起仍有搜索时间的物品
     */
    @Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = true)
    private void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getItem();
        if (ItemStackHelper.hasRemainingSearchTime(stack)) {
            double searchTime = ItemStackHelper.getRemainingSearchTime(stack);
            if (searchTime > 0.0) {
                cir.cancel();
                cir.setReturnValue(false);
            }
        }
    }
}
