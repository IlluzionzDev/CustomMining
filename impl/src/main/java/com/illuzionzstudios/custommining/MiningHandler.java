package com.illuzionzstudios.custommining;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handle custom methods with packets to simulate
 * the custom mining system. Things that also utilise NMS items
 */
public interface MiningHandler {

    /**
     * Update the damage texture being displayed on a block
     *
     * @param block The block to send breaking to
     * @param damage A param used for which break cycle to display
     * @param players Players to send block break to
     */
    void sendBlockBreak(org.bukkit.block.Block block, int damage, Player... players);

    /**
     * Update the damage texture being displayed on a block
     *
     * @param block The block to send breaking to
     * @param damage A param used for which break cycle to display
     * @param players Players to send block break to
     */
    void sendBlockBreak(org.bukkit.block.Block block, int damage, List<Player> players);

    /**
     * Send client side mining fatigue to the player
     * This stops any breaking animation so we
     * can handle it ourselves
     *
     * @param player Player we are modifying packets for
     */
    void cancelClientBreaking(Player player);

    /**
     * Get the default hardness of a block
     *
     * @param block The block to check
     */
    float getDefaultBlockHardness(org.bukkit.block.Block block);

    /**
     * Get's the base multiplier of an Item (Tool usually) on a block. This is taken
     * from the #getDestroySpeed(ItemStack item, IBlockData block) method in NMS, which gives
     * the base destroy multiplier based on the block and item
     *
     * @param item The item trying to use
     * @param block The data of the block
     * @return The base multiplier
     */
    float getBaseMultiplier(org.bukkit.inventory.ItemStack item, Block block);

    /**
     * Checks if an item can destroy a block (is the appropriate tool for it)
     *
     * @param item The item being used
     * @param block The block checking
     * @return If is the right tool
     */
    boolean canDestroyBlock(org.bukkit.inventory.ItemStack item, Block block);

    /**
     * Play the breaking effect for a block.
     * This includes the breaking particles
     * and the breaking sound.
     *
     * @param block Block to play effects for
     */
    void playBreakEffect(org.bukkit.block.Block block);

    /**
     * Used for a custom id based off block
     */
    default int getBlockEntityId(org.bukkit.block.Block block){
        return ((block.getX() & 0xFFF) << 20 | (block.getZ() & 0xFFF) << 8) | (block.getY() & 0xFF);
    }

}
