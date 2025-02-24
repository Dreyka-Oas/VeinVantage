package oas.work.veinvantage.procedures;

import oas.work.vein_vantage.VeinVantageMod;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
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

@EventBusSubscriber
public class ClearingStrikeStartProcedure {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event.getLevel(), event.getPos(), event.getState(), event.getPlayer());
    }

    public static void execute(LevelAccessor world, BlockPos origin, BlockState blockstate, Entity entity) {
        if (!(entity instanceof Player player)) return;

        int enchantmentLevel = player.getMainHandItem().getEnchantmentLevel(
            world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:clearing_strike"))));
        if (enchantmentLevel == 0) return;

        int depth = enchantmentLevel;

        Direction facing;
        float pitch = player.getXRot();
        if (pitch <= -60) {
            facing = Direction.UP;
        } else if (pitch >= 60) {
            facing = Direction.DOWN;
        } else {
            facing = player.getDirection();
        }

        Set<BlockPos> blocksToDestroy = getSurroundingBlocks(origin, blockstate, facing, depth, world);

        for (BlockPos pos : blocksToDestroy) {
            world.destroyBlock(pos, true);
        }
    }

    private static Set<BlockPos> getSurroundingBlocks(BlockPos origin, BlockState blockstate, Direction facing, int depth, LevelAccessor world) {
        Set<BlockPos> positions = new HashSet<>();

        if (facing == Direction.UP || facing == Direction.DOWN) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int d = 0; d < depth; d++) {
                        BlockPos targetPos = (facing == Direction.UP) 
                                ? origin.offset(x, d, y)
                                : origin.offset(x, -d, y);

                        if (world.getBlockState(targetPos).equals(blockstate)) {
                            positions.add(targetPos);
                        }
                    }
                }
            }
        } else {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int d = 0; d < depth; d++) {
                        BlockPos targetPos;

                        switch (facing) {
                            case NORTH -> targetPos = origin.offset(i, j, -d);
                            case SOUTH -> targetPos = origin.offset(i, j, d);
                            case EAST -> targetPos = origin.offset(d, i, j);
                            case WEST -> targetPos = origin.offset(-d, i, j);
                            default -> throw new IllegalStateException("Unexpected value: " + facing);
                        }

                        if (world.getBlockState(targetPos).equals(blockstate)) {
                            positions.add(targetPos);
                        }
                    }
                }
            }
        }

        return positions;
    }
}