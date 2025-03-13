package oas.work.vein_vantage.procedures;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

/**
 * This class implements the auto-repair enchantment logic.
 * It listens for block break events and has a chance to repair the player's held item if it has the "auto_repair" enchantment.
 */
@EventBusSubscriber
public class AutoRepairStartProcedure {

    /**
     * Subscribed to the `BlockEvent.BreakEvent`, this method is called whenever a block is broken by a player.
     * It initiates the auto-repair procedure when a block is broken.
     * @param event The BlockBreakEvent provided by Forge.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getLevel(), event.getPlayer());
    }

    /**
     * First `execute` method overload. This method is designed to be called directly from other parts of the mod if needed.
     * @param world The world in which the block is broken.
     * @param entity The entity that broke the block (ideally a player).
     */
    public static void execute(LevelAccessor world, Entity entity) {
        execute(null, world, entity);
    }

    /**
     * Second and main `execute` method overload. This method contains the core logic for the auto-repair enchantment.
     * It checks for the enchantment level, calculates the repair amount based on the level, and applies the repair to the item.
     * @param event The event that triggered this procedure (can be null if called directly).
     * @param world The world in which the block is broken.
     * @param entity The entity that broke the block (must be a player).
     */
    private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
        // Check if the entity is null, if so, return to avoid NullPointerExceptions.
        if (entity == null)
            return;

        // Get the ItemStack in the main hand of the entity (if it's a LivingEntity).
        ItemStack mainHandItem = (entity instanceof LivingEntity _livEnt) ? _livEnt.getMainHandItem() : ItemStack.EMPTY;
        // Retrieve the level of the "auto_repair" enchantment from the main hand item.
        int enchantmentLevel = mainHandItem.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:auto_repair"))));

        // Check if the enchantment level is greater than 0, if a random number is less than 0.7 (70% chance), and if the item is currently damaged.
        if (enchantmentLevel > 0 && Math.random() < 0.7 && mainHandItem.isDamaged()) {
            // Define base repair percentage and repair percentage increase per enchantment level.
            double baseRepairPercentage = 0.02;
            double repairPercentagePerLevel = 0.01;
            // Define a maximum repair percentage to prevent overpowered repair.
            double maxRepairPercentage = 0.10;

            // Calculate the total repair percentage based on the enchantment level, starting with the base percentage and adding the per-level increase.
            double totalRepairPercentage = baseRepairPercentage + (enchantmentLevel - 1) * repairPercentagePerLevel;
            // Cap the total repair percentage to the maximum defined percentage.
            totalRepairPercentage = Math.min(totalRepairPercentage, maxRepairPercentage);

            // Get the current damage value of the item.
            int currentDamage = mainHandItem.getDamageValue();
            // Calculate the amount of damage to repair based on the total repair percentage.
            int damageToRepair = (int) (currentDamage * totalRepairPercentage);

            // Calculate the new damage value after repair, ensuring it's not less than 0.
            int newDamage = Math.max(0, currentDamage - damageToRepair);

            // Set the new damage value to the item, effectively repairing it.
            mainHandItem.setDamageValue(newDamage);
        }
    }
}