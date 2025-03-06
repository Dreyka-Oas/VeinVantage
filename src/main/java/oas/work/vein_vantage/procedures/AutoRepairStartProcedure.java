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

@EventBusSubscriber
public class AutoRepairStartProcedure {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getLevel(), event.getPlayer());
    }

    public static void execute(LevelAccessor world, Entity entity) {
        execute(null, world, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
        if (entity == null)
            return;

        ItemStack mainHandItem = (entity instanceof LivingEntity _livEnt) ? _livEnt.getMainHandItem() : ItemStack.EMPTY;
        int enchantmentLevel = mainHandItem.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:auto_repair"))));

        if (enchantmentLevel > 0 && Math.random() < 0.7 && mainHandItem.isDamaged()) {
            double baseRepairPercentage = 0.02;
            double repairPercentagePerLevel = 0.01;
            double maxRepairPercentage = 0.10;

            double totalRepairPercentage = baseRepairPercentage + (enchantmentLevel - 1) * repairPercentagePerLevel;
            totalRepairPercentage = Math.min(totalRepairPercentage, maxRepairPercentage);

            int currentDamage = mainHandItem.getDamageValue();
            int damageToRepair = (int) (currentDamage * totalRepairPercentage);

            int newDamage = Math.max(0, currentDamage - damageToRepair);

            mainHandItem.setDamageValue(newDamage);
        }
    }
}