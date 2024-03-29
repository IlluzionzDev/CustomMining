package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.custommining.CustomMining;
import com.illuzionzstudios.mist.controller.PluginController;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Controller for modifier on blocks. This could be enchants,
 * potions effects, and custom ones from loaded configs.
 * <p>
 * All will be passed to master break time method to handle the final
 * break time there.
 */
public enum ModifierController implements PluginController<CustomMining> {

    INSTANCE;

    @Override
    public void initialize(CustomMining customMining) {
    }

    @Override
    public void stop(CustomMining customMining) {
    }

    /**
     * Get modifiers for item enchantments. Will already be checked if is best tool
     *
     * @param modifier The base modifier already
     * @param player   The player mining the block
     * @return Modifiers from enchant if tool helps
     */
    public float getEnchantmentModifiers(float modifier, Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        // Level of efficiency for modifier checks
        int efficiencyLevel = heldItem.getEnchantmentLevel(Enchantment.DIG_SPEED);

        // If no level, no modifier
        if (efficiencyLevel <= 0) return modifier;

        // Formula for vanilla minecraft
        float efficiencyMultiplier = (float) ((Math.pow(efficiencyLevel, 2)) + 1);

        // Add modifiers as decimal percent
        return modifier + efficiencyMultiplier;
    }

    /**
     * Get modifiers for potions effects on the player
     *
     * @param modifier The base modifier
     * @param player   The player mining the block
     * @return Modifiers from potion effects
     */
    public float getPotionModifiers(float modifier, Player player) {
        // Potions to check
        PotionEffect haste = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);

        // Haste check
        if (haste != null) {
            int level = haste.getAmplifier();

            // Formula
            modifier *= 1 + (0.2 * level);
        }

        // Fatigue check
        if (fatigue != null) {
            int level = fatigue.getAmplifier();

            switch (level) {
                case 1:
                    modifier *= 0.3;
                case 2:
                    modifier *= 0.09;
                case 3:
                    modifier *= 0.0027;
                default:
                    modifier *= 0.00081;
            }
        }

        return modifier;
    }

    /**
     * Get modifiers for if the player is in water and if they're
     * not on the ground.
     *
     * @param modifier The base modifier
     * @param player   The player mining the block
     * @return Modifiers from positioning
     */
    public float getWaterGroundModifiers(float modifier, Player player) {
        // If in water and no aqua infinity
        if (player.isInWater() && !armourContainsEnchant(player, Enchantment.WATER_WORKER))
            modifier /= 5;

        // If isn't on ground
        if (!player.isOnGround())
            modifier /= 5;

        return modifier;
    }

    /**
     * Checks if the player's armour contains an enchantment
     *
     * @param player      The player to check
     * @param enchantment The enchantment to check
     * @return If any armour contains this enchant
     */
    public boolean armourContainsEnchant(Player player, Enchantment enchantment) {
        EntityEquipment equipment = player.getEquipment();

        boolean helmetContains = equipment.getHelmet() != null && equipment.getHelmet().containsEnchantment(enchantment);
        boolean chestplateContains = equipment.getChestplate() != null && equipment.getHelmet().containsEnchantment(enchantment);
        boolean leggingsContains = equipment.getLeggings() != null && equipment.getHelmet().containsEnchantment(enchantment);
        boolean bootsContains = equipment.getHelmet() != null && equipment.getHelmet().containsEnchantment(enchantment);

        return helmetContains || chestplateContains || leggingsContains || bootsContains;
    }
}
