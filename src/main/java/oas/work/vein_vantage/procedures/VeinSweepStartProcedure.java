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

/**
 * This class implements the vein mining procedure triggered when a player breaks a block
 * while sneaking and holding an item with the "Vein Sweep" enchantment.
 */
@EventBusSubscriber
public class VeinSweepStartProcedure {

    /**
     *  Executor service used to handle block destruction asynchronously with delays,
     *  preventing server thread blocking and allowing for a smoother vein mining effect.
     *  Using a fixed thread pool of 4 to limit concurrent tasks.
     */
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     *  This method is subscribed to the `BlockEvent.BreakEvent`, meaning it will be executed
     *  whenever a block is broken in the game.
     *  It checks if the conditions for vein mining are met and initiates the process.
     *  @param event The BlockBreakEvent provided by Forge.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getState(), event.getPlayer());
    }

    /**
     *  First `execute` method overload. This method is designed to be called directly from other parts of the mod,
     *  providing flexibility in triggering the vein mining procedure.
     *  @param world The world in which the block is broken.
     *  @param x The x-coordinate of the broken block.
     *  @param y The y-coordinate of the broken block.
     *  @param z The z-coordinate of the broken block.
     *  @param blockstate The BlockState of the broken block.
     *  @param entity The entity that broke the block (ideally a player).
     */
    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        execute(null, world, x, y, z, blockstate, entity);
    }

    /**
     *  Second and main `execute` method overload. This method contains the core logic for starting the vein mining.
     *  It checks for player conditions (is a ServerPlayer, is sneaking), enchantment level, and then initiates
     *  the flood fill algorithm to destroy connected blocks.
     *  @param event The event that triggered this procedure (can be null if called directly).
     *  @param world The world in which the block is broken.
     *  @param x The x-coordinate of the broken block.
     *  @param y The y-coordinate of the broken block.
     *  @param z The z-coordinate of the broken block.
     *  @param blockstate The BlockState of the broken block.
     *  @param entity The entity that broke the block (must be a ServerPlayer).
     */
    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
        // Check if the entity is a ServerPlayer, not null, and is sneaking (shift-key down). If not, exit.
        if (entity == null || !(entity instanceof ServerPlayer) || !entity.isShiftKeyDown()) return;

        // Get the level of the "vein_sweep" enchantment on the item in the player's main hand.
        int enchantmentLevel = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:vein_sweep"))));

        // If the enchantment level is 0, the vein mining effect should not activate, so exit.
        if (enchantmentLevel == 0) return;

        // Initialize a Set to keep track of visited block positions to prevent infinite loops and re-processing.
        Set<BlockPos> visitedPositions = new HashSet<>();
        // Create a BlockPos object representing the starting position of the vein mining.
        BlockPos startPos = BlockPos.containing(x, y, z);

        // Define how many blocks can be destroyed per enchantment level.
        int blocksPerLevel = 15;
        // Calculate the maximum number of blocks to destroy based on the enchantment level.
        int maxBlocksToDestroy = enchantmentLevel * blocksPerLevel;

        // Set a base delay (in milliseconds) between each block destruction to create a sequential effect.
        int baseDelay = 150;

        // Initiate the flood fill algorithm with delay to start the vein mining process.
        floodFillDestroyWithDelay(world, startPos, blockstate, visitedPositions, maxBlocksToDestroy, baseDelay);
    }

    /**
     *  This method implements the flood fill algorithm to find and destroy connected blocks of the same type
     *  as the starting block. It uses a queue for Breadth-First Search (BFS) and destroys blocks with a delay.
     *  @param world The world in which to perform block destruction.
     *  @param startPos The starting BlockPos for the flood fill.
     *  @param targetBlockState The BlockState of the blocks to be destroyed.
     *  @param visitedPositions A Set to keep track of visited positions.
     *  @param maxBlocksToDestroy The maximum number of blocks to destroy in this vein mining operation.
     *  @param delay The delay in milliseconds between each block destruction.
     */
    private static void floodFillDestroyWithDelay(LevelAccessor world, BlockPos startPos, BlockState targetBlockState, Set<BlockPos> visitedPositions, int maxBlocksToDestroy, int delay) {
        // Initialize a Queue for BFS to explore neighboring blocks.
        Queue<BlockPos> queue = new LinkedList<>();
        // Add the starting position to the queue.
        queue.add(startPos);
        // Initialize a counter for the number of blocks destroyed.
        int blocksDestroyed = 0;
        // Create a Random object for shuffling neighbor exploration.
        Random random = new Random();

        // While the queue is not empty and the maximum number of blocks to destroy has not been reached.
        while (!queue.isEmpty() && blocksDestroyed < maxBlocksToDestroy) {
            // Get the next BlockPos from the queue.
            BlockPos pos = queue.poll();

            // Check if the position has already been visited or if the block at this position is not the target block.
            if (visitedPositions.contains(pos) || !world.getBlockState(pos).equals(targetBlockState)) {
                continue; // Skip to the next position in the queue.
            }

            // Mark the current position as visited.
            visitedPositions.add(pos);

            // Create a final BlockPos for use in the lambda expression (required for accessing within the executor).
            BlockPos finalPos = pos;

            // Submit a task to the executor service to handle block destruction asynchronously.
            executor.submit(() -> {
                try {
                    // Introduce a delay before destroying the block.
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // If the thread is interrupted, interrupt the current thread and return.
                    Thread.currentThread().interrupt();
                    return;
                }

                // Re-check if the block at the final position is still the target block before destroying.
                if (world.getBlockState(finalPos).equals(targetBlockState)) {
                    // Ensure block destruction and drop logic is executed on the server thread.
                    if (world instanceof ServerLevel _level) {
                        _level.getServer().execute(() -> {
                            // Drop block resources as if broken by a player.
                            Block.dropResources(world.getBlockState(finalPos), world, finalPos, null);
                            // Destroy the block without further events (preventing infinite loops).
                            world.destroyBlock(finalPos, false);
                        });
                    }
                }
            });

            // Increment the count of destroyed blocks.
            blocksDestroyed++;

            // Get a list of neighboring BlockPos (North, South, East, West, Up, Down).
            List<BlockPos> neighbors = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below());
            // Shuffle the neighbors randomly to create a more natural vein mining pattern.
            Collections.shuffle(neighbors, random);

            // Add the shuffled neighbors to the queue to explore them in the next iterations,
            // ensuring we do not exceed the maximum blocks to destroy.
            for (BlockPos neighbor : neighbors) {
                if (blocksDestroyed < maxBlocksToDestroy) {
                    queue.add(neighbor);
                }
            }
        }
    }
}