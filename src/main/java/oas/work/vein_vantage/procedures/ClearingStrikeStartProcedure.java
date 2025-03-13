package oas.work.veinvantage.procedures;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashSet;
import java.util.Set;

/**
 * This class implements the "Clearing Strike" enchantment effect.
 * When a player breaks a block with this enchantment, it destroys a line of blocks in the direction the player is facing.
 */
@EventBusSubscriber
public class ClearingStrikeStartProcedure {

    /**
     *  This method is subscribed to the `BlockEvent.BreakEvent`.
     *  It is called whenever a block is broken by a player in the game.
     *  It retrieves necessary information from the event and calls the main `execute` method.
     *  @param event The BlockBreakEvent provided by Forge.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event.getLevel(), event.getPos(), event.getState(), event.getPlayer());
    }

    /**
     *  This method is an entry point to trigger the clearing strike effect.
     *  It is designed to be called from other parts of the mod if needed, providing flexibility.
     *  @param world The world in which the block is broken.
     *  @param origin The position of the block that was initially broken.
     *  @param blockstate The BlockState of the block that was broken.
     *  @param entity The entity that broke the block (must be a Player).
     */
    public static void execute(LevelAccessor world, BlockPos origin, BlockState blockstate, Entity entity) {
        // Check if the entity is a Player, if not, the procedure should not execute.
        if (!(entity instanceof Player player)) return;

        // Retrieve the enchantment level of "clearing_strike" from the player's main hand item.
        int enchantmentLevel = player.getMainHandItem().getEnchantmentLevel(
            world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:clearing_strike"))));
        // If the enchantment level is 0, the effect should not activate, so return.
        if (enchantmentLevel == 0) return;

        // Define a multiplier for the reach of the clearing strike effect based on enchantment level.
        double reachMultiplier = 1.5;

        // Define the base depth of the clearing strike and calculate the final depth based on enchantment level and multiplier.
        int baseDepth = 1;
        int depth = baseDepth + (int) (enchantmentLevel * reachMultiplier);

        // Determine the direction the player is facing to define the direction of the clearing strike.
        Direction facing;
        float pitch = player.getXRot();
        // If the player is looking steeply upwards, set direction to UP.
        if (pitch <= -60) {
            facing = Direction.UP;
        // If the player is looking steeply downwards, set direction to DOWN.
        } else if (pitch >= 60) {
            facing = Direction.DOWN;
        // Otherwise, use the player's horizontal facing direction.
        } else {
            facing = player.getDirection();
        }

        // Get a set of BlockPos representing the blocks to be destroyed based on origin, blockstate, facing direction, and depth.
        Set<BlockPos> blocksToDestroy = getSurroundingBlocks(origin, blockstate, facing, depth, world);

        // Iterate through the set of blocks to destroy and destroy each block in the world.
        for (BlockPos pos : blocksToDestroy) {
            world.destroyBlock(pos, true);
        }
    }

    /**
     *  This method calculates and returns a set of BlockPos that should be destroyed by the clearing strike effect.
     *  It determines the blocks based on the origin block, the target block state, the facing direction, and the desired depth.
     *  @param origin The starting BlockPos from where the clearing strike originates.
     *  @param blockstate The BlockState of the target blocks to be destroyed.
     *  @param facing The Direction in which the clearing strike is oriented.
     *  @param depth The depth or reach of the clearing strike effect.
     *  @param world The LevelAccessor representing the game world.
     *  @return A Set of BlockPos representing the blocks to be destroyed.
     */
    private static Set<BlockPos> getSurroundingBlocks(BlockPos origin, BlockState blockstate, Direction facing, int depth, LevelAccessor world) {
        Set<BlockPos> positions = new HashSet<>();

        // Handle cases where the player is facing UP or DOWN. Clearing strike will be a vertical pillar.
        if (facing == Direction.UP || facing == Direction.DOWN) {
            // Iterate in a 3x3 area around the origin in the horizontal plane (x and y).
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    // Iterate through the depth in the vertical direction (d).
                    for (int d = 0; d < depth; d++) {
                        // Calculate the target BlockPos based on facing direction (UP or DOWN) and current offsets.
                        BlockPos targetPos = (facing == Direction.UP)
                                ? origin.offset(x, d, y) // UP: offset upwards
                                : origin.offset(x, -d, y); // DOWN: offset downwards

                        // Check if the block at the target position has the same BlockState as the initial block.
                        if (world.getBlockState(targetPos).equals(blockstate)) {
                            positions.add(targetPos); // Add the position to the set of blocks to destroy.
                        }
                    }
                }
            }
        // Handle cases where the player is facing horizontally (NORTH, SOUTH, EAST, WEST). Clearing strike will be a horizontal line.
        } else {
            // Iterate in a 3x3 area around the origin in the plane perpendicular to the facing direction (i and j).
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    // Iterate through the depth in the facing direction (d).
                    for (int d = 0; d < depth; d++) {
                        BlockPos targetPos;

                        // Calculate the target BlockPos based on the horizontal facing direction.
                        switch (facing) {
                            case NORTH -> targetPos = origin.offset(i, j, -d); // NORTH: offset in negative Z
                            case SOUTH -> targetPos = origin.offset(i, j, d);  // SOUTH: offset in positive Z
                            case EAST -> targetPos = origin.offset(d, i, j);   // EAST: offset in positive X
                            case WEST -> targetPos = origin.offset(-d, i, j);  // WEST: offset in negative X
                            default -> throw new IllegalStateException("Unexpected value: " + facing); // Should not happen, but for safety.
                        }

                        // Check if the block at the target position has the same BlockState as the initial block.
                        if (world.getBlockState(targetPos).equals(blockstate)) {
                            positions.add(targetPos); // Add the position to the set of blocks to destroy.
                        }
                    }
                }
            }
        }

        return positions; // Return the set of BlockPos to be destroyed.
    }
}