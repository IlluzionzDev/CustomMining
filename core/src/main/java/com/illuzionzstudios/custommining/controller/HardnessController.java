package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.custommining.CustomMining;
import com.illuzionzstudios.mist.controller.PluginController;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Control hardness of blocks, modifiers etc
 * <p>
 * We have to handle things like enchants, physics, potions etc.
 * All will be configurable giving the server owner full control over
 * how breaking works on their server. Also there will be permission
 * based modifiers so certain people can have different modifiers.
 * <p>
 * All information taken from these sources
 * https://minecraft.gamepedia.com/Breaking
 * https://minecraft.gamepedia.com/Mining_Fatigue
 * https://minecraft.gamepedia.com/Haste
 */
public enum HardnessController implements PluginController<CustomMining> {
    INSTANCE;

    @Override
    public void initialize(CustomMining plugin) {

    }

    @Override
    public void stop(CustomMining plugin) {

    }

    /**
     * Master method for determining the final break time of a block.
     * Takes into account all blocks, enchants, potions, regions, and
     * outputs the value we plug into the MiningTask. Other calculations are done
     * in other methods but are linked here for the final result
     * <p>
     * Returning 0 means instantly break and < 0 means unbreakable
     * <p>
     * https://minecraft.fandom.com/wiki/Breaking#Calculation
     *
     * @param block  The block trying to be mined
     * @param player The player mining the block
     * @return Break time in ticks
     */
    public float processFinalBreakTime(Block block, Player player) {
        // Hardness calculations
        float hardness = MiningController.INSTANCE.getHandler().getDefaultBlockHardness(block);

        // This is the percent to decrease the time by
        float speed = 1;

        // Multipliers only if tool helps
        if (block.isPreferredTool(player.getInventory().getItemInMainHand())) {
            // Parse through methods to increase or decrease
            speed = MiningController.INSTANCE.getHandler().getBaseMultiplier(player.getInventory().getItemInMainHand(), block);
            // Only use enchants if can destroy block
            if (MiningController.INSTANCE.getHandler().canDestroyBlock(player.getInventory().getItemInMainHand(), block))
                speed = ModifierController.INSTANCE.getEnchantmentModifiers(speed, player);
        }

        // Modifiers that always apply
        speed = ModifierController.INSTANCE.getPotionModifiers(speed, player);

        // Modifiers for in water (no aqua infinity) and not on ground
        speed = ModifierController.INSTANCE.getWaterGroundModifiers(speed, player);

        // Calculate damage per tick to calculate break
        // (breakSpeed / hardness) * (1 / (doesToolHelp ? 30 : 100))
        float damagePerTick = (speed / hardness) * (1 / (float) (block.isPreferredTool(player.getInventory().getItemInMainHand()) ? 30 : 100));

        // Insta break
        if (damagePerTick > 1)
            return 0;

        // Change to ticks
        return (float) Math.ceil(1.0f / damagePerTick);
    }
}
