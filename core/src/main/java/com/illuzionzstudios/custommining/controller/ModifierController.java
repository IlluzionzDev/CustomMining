/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.custommining.CustomMining;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Controller for modifier on blocks. This could be enchants,
 * potions effects, and custom ones from loaded configs.
 *
 * All will be passed to master break time method to handle the final
 * break time there.
 */
public enum ModifierController implements BukkitController<CustomMining>  {
    INSTANCE;

    @Override
    public void initialize(CustomMining customMining) {

    }

    @Override
    public void stop(CustomMining customMining) {

    }

    /**
     * Get modifiers for item enchantments
     *
     * @param modifier The base modifier already
     * @param block The blocking being mined
     * @param player The player mining the block
     * @return Modifiers from enchant if tool helps
     */
    public float getEnchantmentModifiers(float modifier, Block block, Player player) {
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
     * @param block The blocking being mined
     * @param player The player mining the block
     * @return Modifiers from potion effects
     */
    public float getPotionModifiers(float modifier, Block block, Player player) {


        // Potions to check
        PotionEffect haste = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);

        // Haste check
        if (haste != null) {
            int level = haste.getAmplifier();

            // Increase by formula
            float increaseBy = ((float) 20 * level) / 100;

            // Minus the percent of that to reduce
            modifier += modifier * increaseBy;

            Logger.debug("Haste Level: " + level);
            Logger.debug("IncreaseBy: " + increaseBy);
        }

        // Fatigue check
        if (fatigue != null) {
            int level = fatigue.getAmplifier();

            // Increase by formula
            // only does at each certain level
            float decreaseBy = 0.99919f;

            switch (level) {
                case 1:
                    decreaseBy = 0.7f;
                case 2:
                    decreaseBy = 0.91f;
                case 3:
                    decreaseBy = 0.9973f;
            }

            // Get that percent of modifier, and take it from it
            float take = modifier * decreaseBy;
            modifier -= take;
        }

        return modifier;
    }
}
