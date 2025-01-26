package oas.work.veinvantage.procedures;

import oas.work.veinvantage.network.VeinVantageModVariables;
import oas.work.veinvantage.VeinVantageMod;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber
public class VeinSweepStartProcedure {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getState(), event.getPlayer());
    }

    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        execute(null, world, x, y, z, blockstate, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        if (entity == null || !(entity instanceof ServerPlayer) || !entity.isShiftKeyDown()) return;

        int enchantmentLevel = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:vein_sweep"))));

        if (enchantmentLevel == 0) return;

        Set<BlockPos> visitedPositions = new HashSet<>();
        BlockPos startPos = BlockPos.containing(x, y, z);

        int maxBlocksToDestroy = enchantmentLevel * 10;

        floodFillDestroyWithDelay(world, startPos, blockstate, visitedPositions, maxBlocksToDestroy, 2);
    }

    private static void floodFillDestroyWithDelay(LevelAccessor world, BlockPos startPos, BlockState targetBlockState, Set<BlockPos> visitedPositions, int maxBlocksToDestroy, int delay) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        int blocksDestroyed = 0;
        Random random = new Random();

        while (!queue.isEmpty() && blocksDestroyed < maxBlocksToDestroy) {
            BlockPos pos = queue.poll();

            if (visitedPositions.contains(pos) || !world.getBlockState(pos).equals(targetBlockState)) {
                continue;
            }

            visitedPositions.add(pos);

            VeinVantageMod.queueServerWork(delay * blocksDestroyed, () -> {
                Block.dropResources(world.getBlockState(pos), world, pos, null);
                world.destroyBlock(pos, false);
            });
            blocksDestroyed++;

            List<BlockPos> neighbors = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below());
            Collections.shuffle(neighbors, random);

            for (BlockPos neighbor : neighbors) {
                if (blocksDestroyed < maxBlocksToDestroy) {
                    queue.add(neighbor);
                }
            }
        }
    }
}