package oas.work.vein_vantage.procedures;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
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
import net.neoforged.bus.api.Event;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.level.ServerLevel;

@EventBusSubscriber
public class VeinSweepStartProcedure {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4); // Ajuste le nombre de threads si nécessaire

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getState(), event.getPlayer());
    }

    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        execute(null, world, x, y, z, blockstate, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        if (entity == null || !(entity instanceof ServerPlayer) || !entity.isShiftKeyDown()) return;

        // Récupérer le niveau de l'enchantement "vein_sweep" sur l'objet principal de l'entité
        int enchantmentLevel = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:vein_sweep"))));

        if (enchantmentLevel == 0) return;

        Set<BlockPos> visitedPositions = new HashSet<>();
        BlockPos startPos = BlockPos.containing(x, y, z);

        // Augmentation de la puissance en fonction du niveau de l'enchantement
        // Maintenant, le nombre de blocs détruits est directement proportionnel au niveau de l'enchantement.
        // Tu peux ajuster le multiplicateur (15 dans cet exemple) pour contrôler la puissance de l'enchantement.
        int blocksPerLevel = 15; // Nombre de blocs détruits par niveau d'enchantement
        int maxBlocksToDestroy = enchantmentLevel * blocksPerLevel;

        int baseDelay = 150; // Delai de base en millisecondes (150ms = 0.15 secondes)

        floodFillDestroyWithDelay(world, startPos, blockstate, visitedPositions, maxBlocksToDestroy, baseDelay);
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

            BlockPos finalPos = pos;

            executor.submit(() -> {
                try {
                    Thread.sleep(delay); // Attendre le délai
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return; // Sortir si le thread est interrompu
                }

                if (world.getBlockState(finalPos).equals(targetBlockState)) {
                    if (world instanceof ServerLevel _level) {
                        _level.getServer().execute(() -> { // Assure que la destruction se fait sur le thread principal
                            Block.dropResources(world.getBlockState(finalPos), world, finalPos, null);
                            world.destroyBlock(finalPos, false);
                        });
                    }
                }
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