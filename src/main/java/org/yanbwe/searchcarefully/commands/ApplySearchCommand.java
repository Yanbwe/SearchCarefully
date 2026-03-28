package org.yanbwe.searchcarefully.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.searchcarefully.Config;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.raritycore.registry.RarityRegistry;

import java.util.Random;

public class ApplySearchCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("searchcarefully")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("applysearch")
                    .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                            .then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(ApplySearchCommand::applySearch)
                            )
                        )
                    )
                )
        );
    }

    private static int applySearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getServer().getLevel(Level.OVERWORLD);

        if (level == null) {
            source.sendFailure(Component.translatable("commands.searchcarefully.applysearch.no_level"));
            return 0;
        }

        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");

        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof Container container)) {
            source.sendFailure(Component.translatable("commands.searchcarefully.applysearch.not_container"));
            return 0;
        }

        int[] processedCount = {0};
        Random random = new Random();

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());
            double baseSearchTime = SearchConstants.getSearchTimeByRarity(rarity);

            if (baseSearchTime > 0 && rarity >= 1 && rarity <= 7) {
                double randomTimeRange = Config.RARITY_RANDOM_TIMES[rarity].get();
                double randomAddition = (random.nextDouble() * 2 - 1) * randomTimeRange;
                double finalSearchTime = Math.max(1.0, baseSearchTime + randomAddition);

                ItemStackHelper.setRemainingSearchTime(stack, finalSearchTime);
                container.setItem(i, stack);
                processedCount[0]++;
            }
        }

        if (processedCount[0] > 0) {
            source.sendSuccess(() -> Component.translatable(
                "commands.searchcarefully.applysearch.success",
                processedCount[0],
                x, y, z
            ), true);
        } else {
            source.sendFailure(Component.translatable("commands.searchcarefully.applysearch.no_items"));
        }

        return processedCount[0];
    }
}
