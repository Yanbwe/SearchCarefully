package org.yanbwe.searchcarefully.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SearchConstants;

public class ClearSearchTagsCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("searchcarefully")
                .requires(source -> source.hasPermission(2)) // OP权限等级2
                .then(Commands.literal("clear")
                    .executes(ClearSearchTagsCommand::clearSearchTags)
                )
        );
    }
    
    private static int clearSearchTags(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer player) {
            int clearedCount = clearPlayerSearchTags(player);
            source.sendSuccess(() -> Component.translatable(
                "commands.searchcarefully.clear.success"
            ), true);
            return clearedCount;
        } else {
            source.sendFailure(Component.translatable("commands.searchcarefully.clear.not_player"));
            return 0;
        }
    }
    
    private static int clearPlayerSearchTags(ServerPlayer player) {
        int clearedCount = 0;
        
        // 清理玩家物品栏
        Inventory inventory = player.getInventory();
        
        // 遍历主物品栏（索引0-35）
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (clearItemSearchTag(stack)) {
                clearedCount++;
                inventory.setItem(i, stack); // 更新物品
            }
        }
        
        // 清理盔甲槽（索引100-103）
        for (int i = 100; i < 104; i++) {
            ItemStack stack = inventory.getItem(i);
            if (clearItemSearchTag(stack)) {
                clearedCount++;
                inventory.setItem(i, stack); // 更新物品
            }
        }
        
        // 清理副手槽（索引-106）
        ItemStack offhandStack = inventory.getItem(-106);
        if (clearItemSearchTag(offhandStack)) {
            clearedCount++;
            inventory.setItem(-106, offhandStack); // 更新物品
        }
        
        return clearedCount;
    }
    
    private static boolean clearItemSearchTag(ItemStack stack) {
        if (stack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(stack)) {
            return false;
        }
        
        // 使用封装的工具方法完成搜索并清理标签
        ItemStackHelper.completeSearch(stack);
        return true;
    }
}