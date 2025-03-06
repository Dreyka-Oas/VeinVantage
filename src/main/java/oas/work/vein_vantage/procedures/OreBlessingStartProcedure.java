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

@EventBusSubscriber
public class OreBlessingStartProcedure {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event.getLevel(), event.getPos(), event.getState(), event.getPlayer());
    }

    public static void execute(LevelAccessor world, BlockPos pos, BlockState blockstate, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return;

        int enchantmentLevel = ((livingEntity instanceof LivingEntity _livEnt) ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:ore_blessing"))));

        if (enchantmentLevel == 0) return;

        String blockName = blockstate.getBlock().getDescriptionId();
        if (!blockName.toLowerCase().contains("ore")) return;

        double baseChance = 0.3;
        double chancePerLevel = 0.1;
        double hasteChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);
        double speedChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);
        double regenerationChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);

        int baseHasteDuration = 100;
        int baseSpeedDuration = 100;
        int baseRegenerationDuration = 100;

        int hasteDuration = baseHasteDuration + (enchantmentLevel * 20);
        int hasteAmplifier = 0;

        int speedDuration = baseSpeedDuration + (enchantmentLevel * 20);
        int speedAmplifier = enchantmentLevel - 1;

        int regenerationDuration = baseRegenerationDuration + (enchantmentLevel * 20);
        int regenerationAmplifier = enchantmentLevel - 1;

        Random random = new Random();

        if (random.nextDouble() < hasteChance) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, hasteDuration, hasteAmplifier, false, false));
        }

        if (random.nextDouble() < speedChance) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedDuration, speedAmplifier, false, false));
        }

        if (random.nextDouble() < regenerationChance) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenerationDuration, regenerationAmplifier, false, false));
        }
    }
}