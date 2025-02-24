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
import net.minecraft.world.entity.LivingEntity;
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

        // Récupérer le niveau de l'enchantement "ore_blessing"
        int enchantmentLevel = ((livingEntity instanceof LivingEntity _livEnt) ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                .getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("vein_vantage:ore_blessing"))));

        if (enchantmentLevel == 0) return;

        // Vérifier si le bloc cassé est un minerai (en utilisant une simple vérification du nom du bloc)
        String blockName = blockstate.getBlock().getDescriptionId(); // Récupérer l'ID de description du bloc
        if (!blockName.toLowerCase().contains("ore")) return;

        // Configuration des chances d'application des effets
        double baseChance = 0.3;       // Chance de base (30%)
        double chancePerLevel = 0.1;    // Augmentation de chance par niveau (10%)
        double hasteChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);  // Haste
        double speedChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);  // Speed
        double regenerationChance = Math.min(1.0, baseChance + enchantmentLevel * chancePerLevel);   // Regeneration

        // Durée et puissance des effets (La puissance augmente en fonction du niveau de l'enchantement)
        int baseHasteDuration = 100; //Durée de base de Haste
        int baseSpeedDuration = 100; //Durée de base de Speed
        int baseRegenerationDuration = 100; //Durée de base de Régénération

        int hasteDuration = baseHasteDuration + (enchantmentLevel * 20); // Durée de l'effet Haste augmente avec le niveau
        int hasteAmplifier = 0; // Haste de base

        int speedDuration = baseSpeedDuration + (enchantmentLevel * 20); // Durée de l'effet Speed augmente avec le niveau
        int speedAmplifier = enchantmentLevel - 1; // Speed et Regen augmentent avec le niveau

        int regenerationDuration = baseRegenerationDuration + (enchantmentLevel * 20); // Durée de l'effet Régénération augmente avec le niveau
        int regenerationAmplifier = enchantmentLevel - 1;

        // Appliquer les effets avec probabilité
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