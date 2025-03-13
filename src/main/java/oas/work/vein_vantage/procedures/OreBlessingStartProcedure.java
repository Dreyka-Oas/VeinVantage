package oas.work.vein_vantage.procedures;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Holder;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class implements the "Ore Blessing" enchantment effect.
 * When a player breaks an ore block with this enchantment, it has a chance to grant them beneficial status effects like Haste, Speed, and Regeneration.
 */
@EventBusSubscriber
public class OreBlessingStartProcedure {

    /**
     *  This method is subscribed to the `BlockEvent.BreakEvent`.
     *  It is called whenever a block is broken by a player in the game.
     *  It retrieves the necessary context from the event and calls the main `execute` method.
     *  @param event The BlockBreakEvent provided by Forge.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event.getLevel(), event.getPos(), event.getState(), event.getPlayer());
    }

    /**
     *  This method serves as an entry point to trigger the ore blessing effect.
     *  It is designed to be called from other parts of the mod if needed, providing flexibility in triggering the effect.
     *  @param world The world in which the block is broken.
     *  @param pos The position of the broken block.
     *  @param blockstate The BlockState of the broken block.
     *  @param entity The entity that broke the block (must be a LivingEntity, ideally a Player).
     */
    public static void execute(LevelAccessor world, BlockPos pos, BlockState blockstate, Entity entity) {
        execute(null, world, pos, blockstate, entity);
    }

    /**
     *  This is the main `execute` method that contains the core logic for the Ore Blessing enchantment.
     *  It checks for the enchantment level, determines if the broken block is an ore,
     *  calculates probabilities for applying buffs, and applies them to the player if conditions are met.
     *  @param event The event that triggered this procedure (can be null if called directly).
     *  @param world The world in which the block is broken.
     *  @param pos The position of the broken block.
     *  @param blockstate The BlockState of the broken block.
     *  @param entity The entity that broke the block (must be a LivingEntity).
     */
    private static void execute(@Nullable Event event, LevelAccessor world, BlockPos pos, BlockState blockstate, Entity entity) {
        // Check if the entity is a LivingEntity. If not, the buffs cannot be applied, so exit.
        if (!(entity instanceof LivingEntity livingEntity)) return;

        // Retrieve the enchantment level of "ore_blessing" from the item in the player's main hand.
        int enchantmentLevel = ((livingEntity instanceof LivingEntity _livEnt) ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:ore_blessing"))));

        // If the enchantment level is 0, the effect should not activate, so exit.
        if (enchantmentLevel == 0) return;

        // Get the name of the broken block to check if it's an ore.
        String blockName = blockstate.getBlock().getDescriptionId();
        // Check if the block name contains "ore" (case-insensitive) to determine if it's an ore block.
        if (!blockName.toLowerCase().contains("ore")) return;

        // Define base chances for each buff and the increase in chance per enchantment level.
        double baseChance = 0.3;
        double chancePerLevel = 0.1;
        // Calculate the chance for Haste effect, capped at 1.0 (100%).
        double hasteChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);
        // Calculate the chance for Speed effect, capped at 1.0 (100%).
        double speedChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);
        // Calculate the chance for Regeneration effect, capped at 1.0 (100%).
        double regenerationChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);

        // Define base durations for each buff in ticks (20 ticks = 1 second).
        int baseHasteDuration = 100;
        int baseSpeedDuration = 100;
        int baseRegenerationDuration = 100;

        // Calculate the duration for Haste, increasing with enchantment level.
        int hasteDuration = baseHasteDuration + (enchantmentLevel * 20);
        // Set the amplifier for Haste to 0 (Haste I).
        int hasteAmplifier = 0;

        // Calculate the duration for Speed, increasing with enchantment level.
        int speedDuration = baseSpeedDuration + (enchantmentLevel * 20);
        // Set the amplifier for Speed, scaling with enchantment level (Speed I, Speed II, etc.).
        int speedAmplifier = enchantmentLevel - 1;

        // Calculate the duration for Regeneration, increasing with enchantment level.
        int regenerationDuration = baseRegenerationDuration + (enchantmentLevel * 20);
        // Set the amplifier for Regeneration, scaling with enchantment level (Regeneration I, Regeneration II, etc.).
        int regenerationAmplifier = enchantmentLevel - 1;

        // Create a Random object to determine if buffs are applied based on calculated chances.
        Random random = new Random();

        // Check if a random number is less than the calculated haste chance.
        if (random.nextDouble() < hasteChance) {
            // Apply the Haste effect to the player.
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, hasteDuration, hasteAmplifier, false, false));
        }

        // Check if a random number is less than the calculated speed chance.
        if (random.nextDouble() < speedChance) {
            // Apply the Speed effect to the player.
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedDuration, speedAmplifier, false, false));
        }

        // Check if a random number is less than the calculated regeneration chance.
        if (random.nextDouble() < regenerationChance) {
            // Apply the Regeneration effect to the player.
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenerationDuration, regenerationAmplifier, false, false));
        }
    }
}