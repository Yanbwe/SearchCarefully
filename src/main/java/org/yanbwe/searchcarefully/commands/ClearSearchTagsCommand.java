package org.yanbwe.searchcarefully.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

public class ClearSearchTagsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("searchcarefully")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                    .executes(ClearSearchTagsCommand::clearSearchTags)));
    }

    private static int clearSearchTags(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            int clearedCount = clearPlayerSearchTags(player);
            source.sendSuccess(() -> Component.translatable(
                "commands.searchcarefully.clear.success"), true);
            return clearedCount;
        } else {
            source.sendFailure(Component.translatable("commands.searchcarefully.clear.not_player"));
            return 0;
        }
    }

    private static int clearPlayerSearchTags(ServerPlayer player) {
        int clearedCount = 0;
        Inventory inventory = player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (clearItemSearchTag(stack)) {
                clearedCount++;
                inventory.setItem(i, stack);
            }
        }
        for (int i = 100; i < 104; i++) {
            ItemStack stack = inventory.getItem(i);
            if (clearItemSearchTag(stack)) {
                clearedCount++;
                inventory.setItem(i, stack);
            }
        }
        ItemStack offhandStack = inventory.getItem(-106);
        if (clearItemSearchTag(offhandStack)) {
            clearedCount++;
            inventory.setItem(-106, offhandStack);
        }
        return clearedCount;
    }

    private static boolean clearItemSearchTag(ItemStack stack) {
        if (stack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(stack)) return false;
        ItemStackHelper.completeSearch(stack);
        return true;
    }
}
