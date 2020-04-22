package com.illuzionzstudios.custommining.controller;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

import com.illuzionzstudios.compatibility.CompatibleMaterial;
import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.custommining.CustomMining;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.illuzionzstudios.compatibility.CompatibleMaterial.*;

/**
 * Control hardness of blocks, modifiers etc
 * <p>
 * We have to handle things like enchants, physics, potions etc.
 * All will be configurable giving the server owner full control over
 * how breaking works on their server. Also there will be permission
 * based modifiers so certain people can have different modifiers.
 *
 * All information taken from these sources
 * https://minecraft.gamepedia.com/Breaking
 * https://minecraft.gamepedia.com/Mining_Fatigue
 * https://minecraft.gamepedia.com/Haste
 */
public enum HardnessController implements BukkitController<CustomMining> {
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
     *
     * @param block  The block trying to be mined
     * @param player The player mining the block
     * @return Break time in ticks
     */
    public float processFinalBreakTime(Block block, Player player) {
        // Hardness calculations
        float hardness = MiningController.INSTANCE.handler.getDefaultBlockHardness(block);

        // Base time in seconds based on if tool helps
        float baseTime = doesToolHelp(getHeldTool(player), block.getType(), player) ?
                (float) (hardness * 1.5) :
                hardness * 5;

        // The modifier to apply
        float modifier = 1;

        // Multipliers only if tool helps
        if (doesToolMultiply(getHeldTool(player), block.getType(), player)) {
            modifier = getBaseMultiplier(getTier(player), block.getType());
            modifier = ModifierController.INSTANCE.getEnchantmentModifiers(modifier, block, player);
        }

        // Modifiers that always apply
        modifier = ModifierController.INSTANCE.getPotionModifiers(modifier, block, player);

        // Check insta breaking
        // Vanilla minecraft method to see if insta breaks
        if (modifier > hardness * 30) return 0f;

        // Apply modifiers
        baseTime /= modifier;

        // Round to nearest 0.05 like minecraft breaking time
        baseTime = (float) (Math.round(baseTime * 20.0) / 20.0);

        Logger.debug("Time: " + baseTime);
        Logger.debug("Modifier: " + modifier);

        // Change to ticks
        return baseTime * 20;
    }

    /**
     * Get the breaking multiplier for a tool tier
     * (This is also known as the damage dealt pet tick)
     *
     * @param tier Tier to get multiplier for
     * @return Multiplier as float
     */
    public float getBaseMultiplier(ToolTier tier, Material mat) {
        // Get compatibility from material
        // Now checks for legacy materials
        CompatibleMaterial type = CompatibleMaterial.getBlockMaterial(mat);

        switch (tier) {
            case NONE:
                return 1;
            case WOOD:
                return 2;
            case STONE:
                return 4;
            case IRON:
                return 6;
            case DIAMOND:
                return 8;
            case GOLD:
                return 12;
            case SHEARS:
                // Extra material checks
                if (type == OAK_LEAVES ||
                        type == SPRUCE_LEAVES ||
                        type == BIRCH_LEAVES ||
                        type == JUNGLE_LEAVES ||
                        type == ACACIA_LEAVES ||
                        type == DARK_OAK_LEAVES ||
                        type == COBWEB) return 15;

                if (type == WHITE_WOOL ||
                        type == ORANGE_WOOL ||
                        type == PURPLE_WOOL ||
                        type == MAGENTA_WOOL ||
                        type == LIGHT_BLUE_WOOL ||
                        type == YELLOW_WOOL ||
                        type == LIME_WOOL ||
                        type == PINK_WOOL ||
                        type == GRAY_WOOL ||
                        type == LIGHT_GRAY_WOOL ||
                        type == CYAN_WOOL ||
                        type == BLUE_WOOL ||
                        type == BROWN_WOOL ||
                        type == GREEN_WOOL ||
                        type == RED_WOOL ||
                        type == BLACK_WOOL) return 5;
                return 1.5f;
            case SWORD:
                if (type == COBWEB) return 15;
                return 1.5f;
        }

        return 1;
    }

    /**
     * This tests if a tool can be used to benefit mining.
     * For instance a pickaxe helps stone, and axe helps wood.
     *
     * @param tool Tool to test
     * @param mat  The type of material being mined
     * @return If this tool helps or not
     */
    public boolean doesToolHelp(Tool tool, Material mat, Player player) {
        // Tier of tool
        ToolTier tier = getTier(player);

        // Set in a previous check to cut
        // down on repeating code
        boolean doesHelp = false;

        // Get compatibility from material
        // Now checks for legacy materials
        CompatibleMaterial type = CompatibleMaterial.getBlockMaterial(mat);

        // Make sure type exists
        if (type == null) {
            return false;
        }

        // Things that can be mined by hand
        // Returned by default if no other tool
        boolean master =  type == CLAY ||
                type == COARSE_DIRT ||
                type == WHITE_CONCRETE_POWDER ||
                type == ORANGE_CONCRETE_POWDER ||
                type == PURPLE_CONCRETE_POWDER ||
                type == MAGENTA_CONCRETE_POWDER ||
                type == LIGHT_BLUE_CONCRETE_POWDER ||
                type == YELLOW_CONCRETE_POWDER ||
                type == LIME_CONCRETE_POWDER ||
                type == PINK_CONCRETE_POWDER ||
                type == GRAY_CONCRETE_POWDER ||
                type == LIGHT_GRAY_CONCRETE_POWDER ||
                type == CYAN_CONCRETE_POWDER ||
                type == BLUE_CONCRETE_POWDER ||
                type == BROWN_CONCRETE_POWDER ||
                type == GREEN_CONCRETE_POWDER ||
                type == RED_CONCRETE_POWDER ||
                type == BLACK_CONCRETE_POWDER ||
                type == DIRT ||
                type == FARMLAND ||
                type == GRASS_BLOCK ||
                type == GRAVEL ||
                type == MYCELIUM ||
                type == PODZOL ||
                type == RED_SAND ||
                type == SAND ||
                type == SOUL_SAND ||
                type == SNOW_BLOCK ||
                type == SNOW ||

                type == OAK_LEAVES ||
                type == SPRUCE_LEAVES ||
                type == BIRCH_LEAVES ||
                type == JUNGLE_LEAVES ||
                type == ACACIA_LEAVES ||
                type == DARK_OAK_LEAVES ||

                type == COCOA ||
                type == JACK_O_LANTERN ||
                type == PUMPKIN ||
                type == VINE ||
                type == MELON ||
                type == BEE_NEST ||

                type == WHITE_BED ||
                type == ORANGE_BED ||
                type == PURPLE_BED ||
                type == MAGENTA_BED ||
                type == LIGHT_BLUE_BED ||
                type == YELLOW_BED ||
                type == LIME_BED ||
                type == PINK_BED ||
                type == GRAY_BED ||
                type == LIGHT_GRAY_BED ||
                type == CYAN_BED ||
                type == BLUE_BED ||
                type == BROWN_BED ||
                type == GREEN_BED ||
                type == RED_BED ||
                type == BLACK_BED ||

                type == WHITE_CARPET ||
                type == ORANGE_CARPET ||
                type == PURPLE_CARPET ||
                type == MAGENTA_CARPET ||
                type == LIGHT_BLUE_CARPET ||
                type == YELLOW_CARPET ||
                type == LIME_CARPET ||
                type == PINK_CARPET ||
                type == GRAY_CARPET ||
                type == LIGHT_GRAY_CARPET ||
                type == CYAN_CARPET ||
                type == BLUE_CARPET ||
                type == BROWN_CARPET ||
                type == GREEN_CARPET ||
                type == RED_CARPET ||
                type == BLACK_CARPET ||

                type == SPONGE ||
                type == WET_SPONGE ||
                type == CAKE ||
                type == DRAGON_EGG ||
                type == BEACON ||
                type == LEVER ||
                type == PISTON ||
                type == STICKY_PISTON ||
                type == CACTUS ||
                type == PLAYER_HEAD ||
                type == CREEPER_HEAD ||
                type == ZOMBIE_HEAD ||
                type == PLAYER_WALL_HEAD ||
                type == CREEPER_WALL_HEAD ||
                type == ZOMBIE_WALL_HEAD ||
                type == GLOWSTONE ||
                type == REDSTONE_LAMP ||
                type == SEA_LANTERN ||
                type == GLASS ||
                type == GLASS_PANE ||

                type == WHITE_STAINED_GLASS ||
                type == ORANGE_STAINED_GLASS ||
                type == PURPLE_STAINED_GLASS ||
                type == MAGENTA_STAINED_GLASS ||
                type == LIGHT_BLUE_STAINED_GLASS ||
                type == YELLOW_STAINED_GLASS ||
                type == LIME_STAINED_GLASS ||
                type == PINK_STAINED_GLASS ||
                type == GRAY_STAINED_GLASS ||
                type == LIGHT_GRAY_STAINED_GLASS ||
                type == CYAN_STAINED_GLASS ||
                type == BLUE_STAINED_GLASS ||
                type == BROWN_STAINED_GLASS ||
                type == GREEN_STAINED_GLASS ||
                type == RED_STAINED_GLASS ||
                type == BLACK_STAINED_GLASS ||

                type == WHITE_STAINED_GLASS_PANE ||
                type == ORANGE_STAINED_GLASS_PANE ||
                type == PURPLE_STAINED_GLASS_PANE ||
                type == MAGENTA_STAINED_GLASS_PANE ||
                type == LIGHT_BLUE_STAINED_GLASS_PANE ||
                type == YELLOW_STAINED_GLASS_PANE ||
                type == LIME_STAINED_GLASS_PANE ||
                type == PINK_STAINED_GLASS_PANE ||
                type == GRAY_STAINED_GLASS_PANE ||
                type == LIGHT_GRAY_STAINED_GLASS_PANE ||
                type == CYAN_STAINED_GLASS_PANE ||
                type == BLUE_STAINED_GLASS_PANE ||
                type == BROWN_STAINED_GLASS_PANE ||
                type == GREEN_STAINED_GLASS_PANE ||
                type == RED_STAINED_GLASS_PANE ||
                type == BLACK_STAINED_GLASS_PANE ||

                type == WHITE_BANNER ||
                type == ORANGE_BANNER ||
                type == PURPLE_BANNER ||
                type == MAGENTA_BANNER ||
                type == LIGHT_BLUE_BANNER ||
                type == YELLOW_BANNER ||
                type == LIME_BANNER ||
                type == PINK_BANNER ||
                type == GRAY_BANNER ||
                type == LIGHT_GRAY_BANNER ||
                type == CYAN_BANNER ||
                type == BLUE_BANNER ||
                type == BROWN_BANNER ||
                type == GREEN_BANNER ||
                type == RED_BANNER ||
                type == BLACK_BANNER ||

                type == WHITE_WALL_BANNER ||
                type == ORANGE_WALL_BANNER ||
                type == PURPLE_WALL_BANNER ||
                type == MAGENTA_WALL_BANNER ||
                type == LIGHT_BLUE_WALL_BANNER ||
                type == YELLOW_WALL_BANNER ||
                type == LIME_WALL_BANNER ||
                type == PINK_WALL_BANNER ||
                type == GRAY_WALL_BANNER ||
                type == LIGHT_GRAY_WALL_BANNER ||
                type == CYAN_WALL_BANNER ||
                type == BLUE_WALL_BANNER ||
                type == BROWN_WALL_BANNER ||
                type == GREEN_WALL_BANNER ||
                type == RED_WALL_BANNER ||
                type == BLACK_WALL_BANNER ||

                type == BOOKSHELF ||
                type == CHEST ||
                type == BEEHIVE ||
                type == CRAFTING_TABLE ||
                type == DAYLIGHT_DETECTOR ||
                type == OAK_FENCE ||
                type == SPRUCE_FENCE ||
                type == BIRCH_FENCE ||
                type == JUNGLE_FENCE ||
                type == ACACIA_FENCE ||
                type == DARK_OAK_FENCE ||
                type == OAK_FENCE_GATE ||
                type == SPRUCE_FENCE_GATE ||
                type == BIRCH_FENCE_GATE ||
                type == JUNGLE_FENCE_GATE ||
                type == ACACIA_FENCE_GATE ||
                type == DARK_OAK_FENCE_GATE ||
                type == RED_MUSHROOM_BLOCK ||
                type == BROWN_MUSHROOM_BLOCK ||
                type == JUKEBOX ||
                type == LADDER ||
                type == NOTE_BLOCK ||
                type == OAK_SIGN ||
                type == SPRUCE_SIGN ||
                type == BIRCH_SIGN ||
                type == JUNGLE_SIGN ||
                type == ACACIA_SIGN ||
                type == DARK_OAK_SIGN ||
                type == OAK_WALL_SIGN ||
                type == SPRUCE_WALL_SIGN ||
                type == BIRCH_WALL_SIGN ||
                type == JUNGLE_WALL_SIGN ||
                type == ACACIA_WALL_SIGN ||
                type == DARK_OAK_WALL_SIGN ||
                type == TRAPPED_CHEST ||

                type == OAK_WOOD ||
                type == SPRUCE_WOOD ||
                type == BIRCH_WOOD ||
                type == JUNGLE_WOOD ||
                type == ACACIA_WOOD ||
                type == DARK_OAK_WOOD ||

                type == OAK_LOG ||
                type == SPRUCE_LOG ||
                type == BIRCH_LOG ||
                type == JUNGLE_LOG ||
                type == ACACIA_LOG ||
                type == DARK_OAK_LOG ||

                type == OAK_PLANKS ||
                type == SPRUCE_PLANKS ||
                type == BIRCH_PLANKS ||
                type == JUNGLE_PLANKS ||
                type == ACACIA_PLANKS ||
                type == DARK_OAK_PLANKS ||

                type == OAK_BUTTON ||
                type == SPRUCE_BUTTON ||
                type == BIRCH_BUTTON ||
                type == JUNGLE_BUTTON ||
                type == ACACIA_BUTTON ||
                type == DARK_OAK_BUTTON ||

                type == OAK_DOOR ||
                type == SPRUCE_DOOR ||
                type == BIRCH_DOOR ||
                type == JUNGLE_DOOR ||
                type == ACACIA_DOOR ||
                type == DARK_OAK_DOOR ||

                type == OAK_PRESSURE_PLATE ||
                type == SPRUCE_PRESSURE_PLATE ||
                type == BIRCH_PRESSURE_PLATE ||
                type == JUNGLE_PRESSURE_PLATE ||
                type == ACACIA_PRESSURE_PLATE ||
                type == DARK_OAK_PRESSURE_PLATE ||

                type == OAK_SLAB ||
                type == SPRUCE_SLAB ||
                type == BIRCH_SLAB ||
                type == JUNGLE_SLAB ||
                type == ACACIA_SLAB ||
                type == DARK_OAK_SLAB ||

                type == OAK_STAIRS ||
                type == SPRUCE_STAIRS ||
                type == BIRCH_STAIRS ||
                type == JUNGLE_STAIRS ||
                type == ACACIA_STAIRS ||
                type == DARK_OAK_STAIRS ||

                type == OAK_TRAPDOOR ||
                type == SPRUCE_TRAPDOOR ||
                type == BIRCH_TRAPDOOR ||
                type == JUNGLE_TRAPDOOR ||
                type == ACACIA_TRAPDOOR ||
                type == DARK_OAK_TRAPDOOR ||

                type == STRIPPED_OAK_WOOD ||
                type == STRIPPED_OAK_LOG ||
                type == STRIPPED_SPRUCE_WOOD ||
                type == STRIPPED_SPRUCE_LOG ||
                type == STRIPPED_BIRCH_WOOD ||
                type == STRIPPED_BIRCH_LOG ||
                type == STRIPPED_JUNGLE_WOOD ||
                type == STRIPPED_JUNGLE_LOG ||
                type == STRIPPED_ACACIA_WOOD ||
                type == STRIPPED_ACACIA_LOG ||
                type == STRIPPED_DARK_OAK_WOOD ||
                type == STRIPPED_DARK_OAK_LOG;

        // Run various checks per tool
        // Currently a lot of legacy materials
        // since it covers most things
        //
        // Also can mine all wood things with hand
        if (tool == Tool.AXE) {
            // Axe blocks
            return type == COCOA ||
                    type == JACK_O_LANTERN ||
                    type == PUMPKIN ||
                    type == VINE ||
                    type == MELON ||
                    type == BEE_NEST ||

                    type == WHITE_BANNER ||
                    type == ORANGE_BANNER ||
                    type == PURPLE_BANNER ||
                    type == MAGENTA_BANNER ||
                    type == LIGHT_BLUE_BANNER ||
                    type == YELLOW_BANNER ||
                    type == LIME_BANNER ||
                    type == PINK_BANNER ||
                    type == GRAY_BANNER ||
                    type == LIGHT_GRAY_BANNER ||
                    type == CYAN_BANNER ||
                    type == BLUE_BANNER ||
                    type == BROWN_BANNER ||
                    type == GREEN_BANNER ||
                    type == RED_BANNER ||
                    type == BLACK_BANNER ||

                    type == WHITE_WALL_BANNER ||
                    type == ORANGE_WALL_BANNER ||
                    type == PURPLE_WALL_BANNER ||
                    type == MAGENTA_WALL_BANNER ||
                    type == LIGHT_BLUE_WALL_BANNER ||
                    type == YELLOW_WALL_BANNER ||
                    type == LIME_WALL_BANNER ||
                    type == PINK_WALL_BANNER ||
                    type == GRAY_WALL_BANNER ||
                    type == LIGHT_GRAY_WALL_BANNER ||
                    type == CYAN_WALL_BANNER ||
                    type == BLUE_WALL_BANNER ||
                    type == BROWN_WALL_BANNER ||
                    type == GREEN_WALL_BANNER ||
                    type == RED_WALL_BANNER ||
                    type == BLACK_WALL_BANNER ||

                    type == BOOKSHELF ||
                    type == CHEST ||
                    type == BEEHIVE ||
                    type == CRAFTING_TABLE ||
                    type == DAYLIGHT_DETECTOR ||
                    type == OAK_FENCE ||
                    type == SPRUCE_FENCE ||
                    type == BIRCH_FENCE ||
                    type == JUNGLE_FENCE ||
                    type == ACACIA_FENCE ||
                    type == DARK_OAK_FENCE ||
                    type == OAK_FENCE_GATE ||
                    type == SPRUCE_FENCE_GATE ||
                    type == BIRCH_FENCE_GATE ||
                    type == JUNGLE_FENCE_GATE ||
                    type == ACACIA_FENCE_GATE ||
                    type == DARK_OAK_FENCE_GATE ||
                    type == RED_MUSHROOM_BLOCK ||
                    type == BROWN_MUSHROOM_BLOCK ||
                    type == JUKEBOX ||
                    type == LADDER ||
                    type == NOTE_BLOCK ||
                    type == OAK_SIGN ||
                    type == SPRUCE_SIGN ||
                    type == BIRCH_SIGN ||
                    type == JUNGLE_SIGN ||
                    type == ACACIA_SIGN ||
                    type == DARK_OAK_SIGN ||
                    type == OAK_WALL_SIGN ||
                    type == SPRUCE_WALL_SIGN ||
                    type == BIRCH_WALL_SIGN ||
                    type == JUNGLE_WALL_SIGN ||
                    type == ACACIA_WALL_SIGN ||
                    type == DARK_OAK_WALL_SIGN ||
                    type == TRAPPED_CHEST ||

                    type == OAK_WOOD ||
                    type == SPRUCE_WOOD ||
                    type == BIRCH_WOOD ||
                    type == JUNGLE_WOOD ||
                    type == ACACIA_WOOD ||
                    type == DARK_OAK_WOOD ||

                    type == OAK_LOG ||
                    type == SPRUCE_LOG ||
                    type == BIRCH_LOG ||
                    type == JUNGLE_LOG ||
                    type == ACACIA_LOG ||
                    type == DARK_OAK_LOG ||

                    type == OAK_PLANKS ||
                    type == SPRUCE_PLANKS ||
                    type == BIRCH_PLANKS ||
                    type == JUNGLE_PLANKS ||
                    type == ACACIA_PLANKS ||
                    type == DARK_OAK_PLANKS ||

                    type == OAK_BUTTON ||
                    type == SPRUCE_BUTTON ||
                    type == BIRCH_BUTTON ||
                    type == JUNGLE_BUTTON ||
                    type == ACACIA_BUTTON ||
                    type == DARK_OAK_BUTTON ||

                    type == OAK_DOOR ||
                    type == SPRUCE_DOOR ||
                    type == BIRCH_DOOR ||
                    type == JUNGLE_DOOR ||
                    type == ACACIA_DOOR ||
                    type == DARK_OAK_DOOR ||

                    type == OAK_PRESSURE_PLATE ||
                    type == SPRUCE_PRESSURE_PLATE ||
                    type == BIRCH_PRESSURE_PLATE ||
                    type == JUNGLE_PRESSURE_PLATE ||
                    type == ACACIA_PRESSURE_PLATE ||
                    type == DARK_OAK_PRESSURE_PLATE ||

                    type == OAK_SLAB ||
                    type == SPRUCE_SLAB ||
                    type == BIRCH_SLAB ||
                    type == JUNGLE_SLAB ||
                    type == ACACIA_SLAB ||
                    type == DARK_OAK_SLAB ||

                    type == OAK_STAIRS ||
                    type == SPRUCE_STAIRS ||
                    type == BIRCH_STAIRS ||
                    type == JUNGLE_STAIRS ||
                    type == ACACIA_STAIRS ||
                    type == DARK_OAK_STAIRS ||

                    type == OAK_TRAPDOOR ||
                    type == SPRUCE_TRAPDOOR ||
                    type == BIRCH_TRAPDOOR ||
                    type == JUNGLE_TRAPDOOR ||
                    type == ACACIA_TRAPDOOR ||
                    type == DARK_OAK_TRAPDOOR ||

                    type == STRIPPED_OAK_WOOD ||
                    type == STRIPPED_OAK_LOG ||
                    type == STRIPPED_SPRUCE_WOOD ||
                    type == STRIPPED_SPRUCE_LOG ||
                    type == STRIPPED_BIRCH_WOOD ||
                    type == STRIPPED_BIRCH_LOG ||
                    type == STRIPPED_JUNGLE_WOOD ||
                    type == STRIPPED_JUNGLE_LOG ||
                    type == STRIPPED_ACACIA_WOOD ||
                    type == STRIPPED_ACACIA_LOG ||
                    type == STRIPPED_DARK_OAK_WOOD ||
                    type == STRIPPED_DARK_OAK_LOG || master;
        } else if (tool == Tool.PICKAXE) {
            // Checks for each tier
            // include all blocks for that tier
            if (tier.atLeast(ToolTier.WOOD)) {
                doesHelp = type == ICE ||
                        type == PACKED_ICE ||
                        type == BLUE_ICE ||
                        type == FROSTED_ICE ||

                        type == ANVIL ||
                        type == REDSTONE_BLOCK ||
                        type == BREWING_STAND ||
                        type == CAULDRON ||
                        type == IRON_BARS ||
                        type == IRON_DOOR ||
                        type == IRON_TRAPDOOR ||
                        type == HOPPER ||
                        type == HEAVY_WEIGHTED_PRESSURE_PLATE ||
                        type == LIGHT_WEIGHTED_PRESSURE_PLATE ||

                        type == PISTON ||
                        type == STICKY_PISTON ||
                        type == PISTON_HEAD ||

                        type == SHULKER_BOX ||
                        type == WHITE_SHULKER_BOX ||
                        type == ORANGE_SHULKER_BOX ||
                        type == PURPLE_SHULKER_BOX ||
                        type == MAGENTA_SHULKER_BOX ||
                        type == LIGHT_BLUE_SHULKER_BOX ||
                        type == YELLOW_SHULKER_BOX ||
                        type == LIME_SHULKER_BOX ||
                        type == PINK_SHULKER_BOX ||
                        type == GRAY_SHULKER_BOX ||
                        type == LIGHT_GRAY_SHULKER_BOX ||
                        type == CYAN_SHULKER_BOX ||
                        type == BLUE_SHULKER_BOX ||
                        type == BROWN_SHULKER_BOX ||
                        type == GREEN_SHULKER_BOX ||
                        type == RED_SHULKER_BOX ||
                        type == BLACK_SHULKER_BOX ||

                        type == ACTIVATOR_RAIL ||
                        type == DETECTOR_RAIL ||
                        type == POWERED_RAIL ||
                        type == RAIL ||

                        type == ANDESITE ||
                        type == COAL_BLOCK ||
                        type == QUARTZ_BLOCK ||
                        type == QUARTZ_PILLAR ||
                        type == QUARTZ_SLAB ||
                        type == BRICKS ||
                        type == BRICK_SLAB ||
                        type == BRICK_STAIRS ||
                        type == COAL_ORE ||
                        type == COBBLESTONE ||
                        type == COBBLESTONE_SLAB ||
                        type == COBBLESTONE_STAIRS ||
                        type == COBBLESTONE_WALL ||
                        type == MOSSY_COBBLESTONE ||
                        type == MOSSY_COBBLESTONE_SLAB ||
                        type == MOSSY_COBBLESTONE_STAIRS ||
                        type == MOSSY_COBBLESTONE_WALL ||

                        type == WHITE_CONCRETE ||
                        type == ORANGE_CONCRETE ||
                        type == PURPLE_CONCRETE ||
                        type == MAGENTA_CONCRETE ||
                        type == LIGHT_BLUE_CONCRETE ||
                        type == YELLOW_CONCRETE ||
                        type == LIME_CONCRETE ||
                        type == PINK_CONCRETE ||
                        type == GRAY_CONCRETE ||
                        type == LIGHT_GRAY_CONCRETE ||
                        type == CYAN_CONCRETE ||
                        type == BLUE_CONCRETE ||
                        type == BROWN_CONCRETE ||
                        type == GREEN_CONCRETE ||
                        type == RED_CONCRETE ||
                        type == BLACK_CONCRETE ||

                        type == WHITE_GLAZED_TERRACOTTA ||
                        type == ORANGE_GLAZED_TERRACOTTA ||
                        type == PURPLE_GLAZED_TERRACOTTA ||
                        type == MAGENTA_GLAZED_TERRACOTTA ||
                        type == LIGHT_BLUE_GLAZED_TERRACOTTA ||
                        type == YELLOW_GLAZED_TERRACOTTA ||
                        type == LIME_GLAZED_TERRACOTTA ||
                        type == PINK_GLAZED_TERRACOTTA ||
                        type == GRAY_GLAZED_TERRACOTTA ||
                        type == LIGHT_GRAY_GLAZED_TERRACOTTA ||
                        type == CYAN_GLAZED_TERRACOTTA ||
                        type == BLUE_GLAZED_TERRACOTTA ||
                        type == BROWN_GLAZED_TERRACOTTA ||
                        type == GREEN_GLAZED_TERRACOTTA ||
                        type == RED_GLAZED_TERRACOTTA ||
                        type == BLACK_GLAZED_TERRACOTTA ||

                        type == DARK_PRISMARINE ||
                        type == DARK_PRISMARINE_SLAB ||
                        type == DARK_PRISMARINE_STAIRS ||
                        type == DIORITE ||
                        type == DIORITE_SLAB ||
                        type == DIORITE_STAIRS ||
                        type == DIORITE_WALL ||
                        type == DISPENSER ||
                        type == DROPPER ||
                        type == ENCHANTING_TABLE ||
                        type == END_STONE ||
                        type == END_STONE_BRICKS ||
                        type == END_STONE_BRICK_SLAB ||
                        type == END_STONE_BRICK_STAIRS ||
                        type == END_STONE_BRICK_WALL ||
                        type == ENDER_CHEST ||
                        type == FURNACE ||
                        type == GRANITE ||
                        type == GRANITE_SLAB ||
                        type == GRANITE_STAIRS ||
                        type == GRANITE_WALL ||
                        type == NETHER_BRICKS ||
                        type == NETHER_BRICK_SLAB ||
                        type == NETHER_BRICK_STAIRS ||
                        type == NETHER_BRICK_FENCE ||
                        type == NETHER_QUARTZ_ORE ||
                        type == NETHERRACK ||
                        type == POLISHED_ANDESITE ||
                        type == POLISHED_ANDESITE_SLAB ||
                        type == POLISHED_ANDESITE_STAIRS ||
                        type == PRISMARINE ||
                        type == PRISMARINE_SLAB ||
                        type == PRISMARINE_STAIRS ||
                        type == PRISMARINE_BRICKS ||
                        type == PRISMARINE_BRICK_SLAB ||
                        type == PRISMARINE_BRICK_STAIRS ||
                        type == POLISHED_DIORITE ||
                        type == POLISHED_DIORITE_SLAB ||
                        type == POLISHED_DIORITE_STAIRS ||
                        type == POLISHED_GRANITE ||
                        type == POLISHED_GRANITE_SLAB ||
                        type == POLISHED_GRANITE_STAIRS ||
                        type == RED_SANDSTONE ||
                        type == RED_SANDSTONE_SLAB ||
                        type == RED_SANDSTONE_STAIRS ||
                        type == RED_SANDSTONE_WALL ||
                        type == SANDSTONE ||
                        type == SANDSTONE_STAIRS ||
                        type == SANDSTONE_SLAB ||
                        type == SANDSTONE_WALL ||
                        type == SPAWNER ||
                        type == SMOOTH_STONE ||
                        type == SMOOTH_STONE_SLAB ||
                        type == STONE ||
                        type == STONE_SLAB ||
                        type == STONE_STAIRS ||
                        type == STONE_BRICKS ||
                        type == STONE_BRICK_SLAB ||
                        type == STONE_BRICK_STAIRS ||
                        type == STONE_BRICK_WALL ||
                        type == STONE_BUTTON ||
                        type == STONE_PRESSURE_PLATE ||
                        type == TERRACOTTA;
            }
            if (tier.atLeast(ToolTier.STONE)) {
                doesHelp = type == IRON_BLOCK ||
                        type == LAPIS_BLOCK ||

                        type == IRON_ORE ||
                        type == LAPIS_ORE || doesHelp;
            }
            if (tier.atLeast(ToolTier.IRON)) {
                doesHelp = type == DIAMOND_BLOCK ||
                        type == EMERALD_BLOCK ||
                        type == GOLD_BLOCK ||

                        type == DIAMOND_ORE ||
                        type == EMERALD_ORE ||
                        type == GOLD_ORE ||
                        type == REDSTONE_ORE || doesHelp;
            }
            if (tier.atLeast(ToolTier.DIAMOND)) {
                doesHelp = type == OBSIDIAN || doesHelp;
            }

            // Finally return if does now help
            return doesHelp || master;
        } else if (tool == Tool.SHEARS) {
            return type == OAK_LEAVES ||
                    type == SPRUCE_LEAVES ||
                    type == BIRCH_LEAVES ||
                    type == JUNGLE_LEAVES ||
                    type == ACACIA_LEAVES ||
                    type == DARK_OAK_LEAVES ||

                    type == COBWEB ||

                    type == WHITE_WOOL ||
                    type == ORANGE_WOOL ||
                    type == PURPLE_WOOL ||
                    type == MAGENTA_WOOL ||
                    type == LIGHT_BLUE_WOOL ||
                    type == YELLOW_WOOL ||
                    type == LIME_WOOL ||
                    type == PINK_WOOL ||
                    type == GRAY_WOOL ||
                    type == LIGHT_GRAY_WOOL ||
                    type == CYAN_WOOL ||
                    type == BLUE_WOOL ||
                    type == BROWN_WOOL ||
                    type == GREEN_WOOL ||
                    type == RED_WOOL ||
                    type == BLACK_WOOL || master;
            // All things mined with shovel can be mined
            // with their hand
        } else if (tool == Tool.SHOVEL) {
            return type == CLAY ||
                    type == COARSE_DIRT ||
                    type == WHITE_CONCRETE_POWDER ||
                    type == ORANGE_CONCRETE_POWDER ||
                    type == PURPLE_CONCRETE_POWDER ||
                    type == MAGENTA_CONCRETE_POWDER ||
                    type == LIGHT_BLUE_CONCRETE_POWDER ||
                    type == YELLOW_CONCRETE_POWDER ||
                    type == LIME_CONCRETE_POWDER ||
                    type == PINK_CONCRETE_POWDER ||
                    type == GRAY_CONCRETE_POWDER ||
                    type == LIGHT_GRAY_CONCRETE_POWDER ||
                    type == CYAN_CONCRETE_POWDER ||
                    type == BLUE_CONCRETE_POWDER ||
                    type == BROWN_CONCRETE_POWDER ||
                    type == GREEN_CONCRETE_POWDER ||
                    type == RED_CONCRETE_POWDER ||
                    type == BLACK_CONCRETE_POWDER ||
                    type == DIRT ||
                    type == FARMLAND ||
                    type == GRASS_BLOCK ||
                    type == GRAVEL ||
                    type == MYCELIUM ||
                    type == PODZOL ||
                    type == RED_SAND ||
                    type == SAND ||
                    type == SOUL_SAND ||
                    type == SNOW_BLOCK ||
                    type == SNOW || master;
        } else if (tool == Tool.SWORD) {
            return type == COBWEB ||
                    type == BAMBOO ||
                    type == BAMBOO_SAPLING || master;
        }

        return master;
    }

    /**
     * This tests if a tool can be used to benefit mining.
     * For instance a pickaxe helps stone, and axe helps wood.
     * But in this case if it will cause multipliers for each
     * tier
     *
     * @param tool Tool to test
     * @param mat  The type of material being mined
     * @return If this tool helps or not
     */
    public boolean doesToolMultiply(Tool tool, Material mat, Player player) {
        // Tier of tool
        ToolTier tier = getTier(player);

        // No tool so doesn't help
        if (tier == ToolTier.NONE) return false;

        // Get compatibility from material
        // Now checks for legacy materials
        CompatibleMaterial type = CompatibleMaterial.getBlockMaterial(mat);

        // Make sure type exists
        if (type == null) {
            return false;
        }

        // Run various checks per tool
        // Currently a lot of legacy materials
        // since it covers most things
        //
        // Simply
        if (tool == Tool.AXE) {
            // Axe blocks
            return type == COCOA ||
                    type == JACK_O_LANTERN ||
                    type == PUMPKIN ||
                    type == VINE ||
                    type == MELON ||
                    type == BEE_NEST ||

                    type == WHITE_BANNER ||
                    type == ORANGE_BANNER ||
                    type == PURPLE_BANNER ||
                    type == MAGENTA_BANNER ||
                    type == LIGHT_BLUE_BANNER ||
                    type == YELLOW_BANNER ||
                    type == LIME_BANNER ||
                    type == PINK_BANNER ||
                    type == GRAY_BANNER ||
                    type == LIGHT_GRAY_BANNER ||
                    type == CYAN_BANNER ||
                    type == BLUE_BANNER ||
                    type == BROWN_BANNER ||
                    type == GREEN_BANNER ||
                    type == RED_BANNER ||
                    type == BLACK_BANNER ||

                    type == WHITE_WALL_BANNER ||
                    type == ORANGE_WALL_BANNER ||
                    type == PURPLE_WALL_BANNER ||
                    type == MAGENTA_WALL_BANNER ||
                    type == LIGHT_BLUE_WALL_BANNER ||
                    type == YELLOW_WALL_BANNER ||
                    type == LIME_WALL_BANNER ||
                    type == PINK_WALL_BANNER ||
                    type == GRAY_WALL_BANNER ||
                    type == LIGHT_GRAY_WALL_BANNER ||
                    type == CYAN_WALL_BANNER ||
                    type == BLUE_WALL_BANNER ||
                    type == BROWN_WALL_BANNER ||
                    type == GREEN_WALL_BANNER ||
                    type == RED_WALL_BANNER ||
                    type == BLACK_WALL_BANNER ||

                    type == BOOKSHELF ||
                    type == CHEST ||
                    type == BEEHIVE ||
                    type == CRAFTING_TABLE ||
                    type == DAYLIGHT_DETECTOR ||
                    type == OAK_FENCE ||
                    type == SPRUCE_FENCE ||
                    type == BIRCH_FENCE ||
                    type == JUNGLE_FENCE ||
                    type == ACACIA_FENCE ||
                    type == DARK_OAK_FENCE ||
                    type == OAK_FENCE_GATE ||
                    type == SPRUCE_FENCE_GATE ||
                    type == BIRCH_FENCE_GATE ||
                    type == JUNGLE_FENCE_GATE ||
                    type == ACACIA_FENCE_GATE ||
                    type == DARK_OAK_FENCE_GATE ||
                    type == RED_MUSHROOM_BLOCK ||
                    type == BROWN_MUSHROOM_BLOCK ||
                    type == JUKEBOX ||
                    type == LADDER ||
                    type == NOTE_BLOCK ||
                    type == OAK_SIGN ||
                    type == SPRUCE_SIGN ||
                    type == BIRCH_SIGN ||
                    type == JUNGLE_SIGN ||
                    type == ACACIA_SIGN ||
                    type == DARK_OAK_SIGN ||
                    type == OAK_WALL_SIGN ||
                    type == SPRUCE_WALL_SIGN ||
                    type == BIRCH_WALL_SIGN ||
                    type == JUNGLE_WALL_SIGN ||
                    type == ACACIA_WALL_SIGN ||
                    type == DARK_OAK_WALL_SIGN ||
                    type == TRAPPED_CHEST ||

                    type == OAK_WOOD ||
                    type == SPRUCE_WOOD ||
                    type == BIRCH_WOOD ||
                    type == JUNGLE_WOOD ||
                    type == ACACIA_WOOD ||
                    type == DARK_OAK_WOOD ||

                    type == OAK_LOG ||
                    type == SPRUCE_LOG ||
                    type == BIRCH_LOG ||
                    type == JUNGLE_LOG ||
                    type == ACACIA_LOG ||
                    type == DARK_OAK_LOG ||

                    type == OAK_PLANKS ||
                    type == SPRUCE_PLANKS ||
                    type == BIRCH_PLANKS ||
                    type == JUNGLE_PLANKS ||
                    type == ACACIA_PLANKS ||
                    type == DARK_OAK_PLANKS ||

                    type == OAK_BUTTON ||
                    type == SPRUCE_BUTTON ||
                    type == BIRCH_BUTTON ||
                    type == JUNGLE_BUTTON ||
                    type == ACACIA_BUTTON ||
                    type == DARK_OAK_BUTTON ||

                    type == OAK_DOOR ||
                    type == SPRUCE_DOOR ||
                    type == BIRCH_DOOR ||
                    type == JUNGLE_DOOR ||
                    type == ACACIA_DOOR ||
                    type == DARK_OAK_DOOR ||

                    type == OAK_PRESSURE_PLATE ||
                    type == SPRUCE_PRESSURE_PLATE ||
                    type == BIRCH_PRESSURE_PLATE ||
                    type == JUNGLE_PRESSURE_PLATE ||
                    type == ACACIA_PRESSURE_PLATE ||
                    type == DARK_OAK_PRESSURE_PLATE ||

                    type == OAK_SLAB ||
                    type == SPRUCE_SLAB ||
                    type == BIRCH_SLAB ||
                    type == JUNGLE_SLAB ||
                    type == ACACIA_SLAB ||
                    type == DARK_OAK_SLAB ||

                    type == OAK_STAIRS ||
                    type == SPRUCE_STAIRS ||
                    type == BIRCH_STAIRS ||
                    type == JUNGLE_STAIRS ||
                    type == ACACIA_STAIRS ||
                    type == DARK_OAK_STAIRS ||

                    type == OAK_TRAPDOOR ||
                    type == SPRUCE_TRAPDOOR ||
                    type == BIRCH_TRAPDOOR ||
                    type == JUNGLE_TRAPDOOR ||
                    type == ACACIA_TRAPDOOR ||
                    type == DARK_OAK_TRAPDOOR ||

                    type == STRIPPED_OAK_WOOD ||
                    type == STRIPPED_OAK_LOG ||
                    type == STRIPPED_SPRUCE_WOOD ||
                    type == STRIPPED_SPRUCE_LOG ||
                    type == STRIPPED_BIRCH_WOOD ||
                    type == STRIPPED_BIRCH_LOG ||
                    type == STRIPPED_JUNGLE_WOOD ||
                    type == STRIPPED_JUNGLE_LOG ||
                    type == STRIPPED_ACACIA_WOOD ||
                    type == STRIPPED_ACACIA_LOG ||
                    type == STRIPPED_DARK_OAK_WOOD ||
                    type == STRIPPED_DARK_OAK_LOG;
        } else if (tool == Tool.PICKAXE) {
            // Checks for each tier
            // include all blocks for that tier
            return type == ICE ||
                    type == PACKED_ICE ||
                    type == BLUE_ICE ||
                    type == FROSTED_ICE ||

                    type == ANVIL ||
                    type == REDSTONE_BLOCK ||
                    type == BREWING_STAND ||
                    type == CAULDRON ||
                    type == IRON_BARS ||
                    type == IRON_DOOR ||
                    type == IRON_TRAPDOOR ||
                    type == HOPPER ||
                    type == HEAVY_WEIGHTED_PRESSURE_PLATE ||
                    type == LIGHT_WEIGHTED_PRESSURE_PLATE ||

                    type == PISTON ||
                    type == STICKY_PISTON ||
                    type == PISTON_HEAD ||

                    type == SHULKER_BOX ||
                    type == WHITE_SHULKER_BOX ||
                    type == ORANGE_SHULKER_BOX ||
                    type == PURPLE_SHULKER_BOX ||
                    type == MAGENTA_SHULKER_BOX ||
                    type == LIGHT_BLUE_SHULKER_BOX ||
                    type == YELLOW_SHULKER_BOX ||
                    type == LIME_SHULKER_BOX ||
                    type == PINK_SHULKER_BOX ||
                    type == GRAY_SHULKER_BOX ||
                    type == LIGHT_GRAY_SHULKER_BOX ||
                    type == CYAN_SHULKER_BOX ||
                    type == BLUE_SHULKER_BOX ||
                    type == BROWN_SHULKER_BOX ||
                    type == GREEN_SHULKER_BOX ||
                    type == RED_SHULKER_BOX ||
                    type == BLACK_SHULKER_BOX ||

                    type == ACTIVATOR_RAIL ||
                    type == DETECTOR_RAIL ||
                    type == POWERED_RAIL ||
                    type == RAIL ||

                    type == ANDESITE ||
                    type == COAL_BLOCK ||
                    type == QUARTZ_BLOCK ||
                    type == QUARTZ_PILLAR ||
                    type == QUARTZ_SLAB ||
                    type == BRICKS ||
                    type == BRICK_SLAB ||
                    type == BRICK_STAIRS ||
                    type == COAL_ORE ||
                    type == COBBLESTONE ||
                    type == COBBLESTONE_SLAB ||
                    type == COBBLESTONE_STAIRS ||
                    type == COBBLESTONE_WALL ||
                    type == MOSSY_COBBLESTONE ||
                    type == MOSSY_COBBLESTONE_SLAB ||
                    type == MOSSY_COBBLESTONE_STAIRS ||
                    type == MOSSY_COBBLESTONE_WALL ||

                    type == WHITE_CONCRETE ||
                    type == ORANGE_CONCRETE ||
                    type == PURPLE_CONCRETE ||
                    type == MAGENTA_CONCRETE ||
                    type == LIGHT_BLUE_CONCRETE ||
                    type == YELLOW_CONCRETE ||
                    type == LIME_CONCRETE ||
                    type == PINK_CONCRETE ||
                    type == GRAY_CONCRETE ||
                    type == LIGHT_GRAY_CONCRETE ||
                    type == CYAN_CONCRETE ||
                    type == BLUE_CONCRETE ||
                    type == BROWN_CONCRETE ||
                    type == GREEN_CONCRETE ||
                    type == RED_CONCRETE ||
                    type == BLACK_CONCRETE ||

                    type == WHITE_GLAZED_TERRACOTTA ||
                    type == ORANGE_GLAZED_TERRACOTTA ||
                    type == PURPLE_GLAZED_TERRACOTTA ||
                    type == MAGENTA_GLAZED_TERRACOTTA ||
                    type == LIGHT_BLUE_GLAZED_TERRACOTTA ||
                    type == YELLOW_GLAZED_TERRACOTTA ||
                    type == LIME_GLAZED_TERRACOTTA ||
                    type == PINK_GLAZED_TERRACOTTA ||
                    type == GRAY_GLAZED_TERRACOTTA ||
                    type == LIGHT_GRAY_GLAZED_TERRACOTTA ||
                    type == CYAN_GLAZED_TERRACOTTA ||
                    type == BLUE_GLAZED_TERRACOTTA ||
                    type == BROWN_GLAZED_TERRACOTTA ||
                    type == GREEN_GLAZED_TERRACOTTA ||
                    type == RED_GLAZED_TERRACOTTA ||
                    type == BLACK_GLAZED_TERRACOTTA ||

                    type == DARK_PRISMARINE ||
                    type == DARK_PRISMARINE_SLAB ||
                    type == DARK_PRISMARINE_STAIRS ||
                    type == DIORITE ||
                    type == DIORITE_SLAB ||
                    type == DIORITE_STAIRS ||
                    type == DIORITE_WALL ||
                    type == DISPENSER ||
                    type == DROPPER ||
                    type == ENCHANTING_TABLE ||
                    type == END_STONE ||
                    type == END_STONE_BRICKS ||
                    type == END_STONE_BRICK_SLAB ||
                    type == END_STONE_BRICK_STAIRS ||
                    type == END_STONE_BRICK_WALL ||
                    type == ENDER_CHEST ||
                    type == FURNACE ||
                    type == GRANITE ||
                    type == GRANITE_SLAB ||
                    type == GRANITE_STAIRS ||
                    type == GRANITE_WALL ||
                    type == NETHER_BRICKS ||
                    type == NETHER_BRICK_SLAB ||
                    type == NETHER_BRICK_STAIRS ||
                    type == NETHER_BRICK_FENCE ||
                    type == NETHER_QUARTZ_ORE ||
                    type == NETHERRACK ||
                    type == POLISHED_ANDESITE ||
                    type == POLISHED_ANDESITE_SLAB ||
                    type == POLISHED_ANDESITE_STAIRS ||
                    type == PRISMARINE ||
                    type == PRISMARINE_SLAB ||
                    type == PRISMARINE_STAIRS ||
                    type == PRISMARINE_BRICKS ||
                    type == PRISMARINE_BRICK_SLAB ||
                    type == PRISMARINE_BRICK_STAIRS ||
                    type == POLISHED_DIORITE ||
                    type == POLISHED_DIORITE_SLAB ||
                    type == POLISHED_DIORITE_STAIRS ||
                    type == POLISHED_GRANITE ||
                    type == POLISHED_GRANITE_SLAB ||
                    type == POLISHED_GRANITE_STAIRS ||
                    type == RED_SANDSTONE ||
                    type == RED_SANDSTONE_SLAB ||
                    type == RED_SANDSTONE_STAIRS ||
                    type == RED_SANDSTONE_WALL ||
                    type == SANDSTONE ||
                    type == SANDSTONE_STAIRS ||
                    type == SANDSTONE_SLAB ||
                    type == SANDSTONE_WALL ||
                    type == SPAWNER ||
                    type == SMOOTH_STONE ||
                    type == SMOOTH_STONE_SLAB ||
                    type == STONE ||
                    type == STONE_SLAB ||
                    type == STONE_STAIRS ||
                    type == STONE_BRICKS ||
                    type == STONE_BRICK_SLAB ||
                    type == STONE_BRICK_STAIRS ||
                    type == STONE_BRICK_WALL ||
                    type == STONE_BUTTON ||
                    type == STONE_PRESSURE_PLATE ||
                    type == TERRACOTTA ||

                    type == IRON_BLOCK ||
                    type == LAPIS_BLOCK ||

                    type == IRON_ORE ||
                    type == LAPIS_ORE ||
                    type == DIAMOND_BLOCK ||
                    type == EMERALD_BLOCK ||
                    type == GOLD_BLOCK ||

                    type == DIAMOND_ORE ||
                    type == EMERALD_ORE ||
                    type == GOLD_ORE ||
                    type == REDSTONE_ORE ||

                    type == OBSIDIAN;
        } else if (tool == Tool.SHEARS) {
            return type == OAK_LEAVES ||
                    type == SPRUCE_LEAVES ||
                    type == BIRCH_LEAVES ||
                    type == JUNGLE_LEAVES ||
                    type == ACACIA_LEAVES ||
                    type == DARK_OAK_LEAVES ||

                    type == COBWEB ||

                    type == WHITE_WOOL ||
                    type == ORANGE_WOOL ||
                    type == PURPLE_WOOL ||
                    type == MAGENTA_WOOL ||
                    type == LIGHT_BLUE_WOOL ||
                    type == YELLOW_WOOL ||
                    type == LIME_WOOL ||
                    type == PINK_WOOL ||
                    type == GRAY_WOOL ||
                    type == LIGHT_GRAY_WOOL ||
                    type == CYAN_WOOL ||
                    type == BLUE_WOOL ||
                    type == BROWN_WOOL ||
                    type == GREEN_WOOL ||
                    type == RED_WOOL ||
                    type == BLACK_WOOL;
        } else if (tool == Tool.SHOVEL) {
            return type == CLAY ||
                    type == COARSE_DIRT ||
                    type == WHITE_CONCRETE_POWDER ||
                    type == ORANGE_CONCRETE_POWDER ||
                    type == PURPLE_CONCRETE_POWDER ||
                    type == MAGENTA_CONCRETE_POWDER ||
                    type == LIGHT_BLUE_CONCRETE_POWDER ||
                    type == YELLOW_CONCRETE_POWDER ||
                    type == LIME_CONCRETE_POWDER ||
                    type == PINK_CONCRETE_POWDER ||
                    type == GRAY_CONCRETE_POWDER ||
                    type == LIGHT_GRAY_CONCRETE_POWDER ||
                    type == CYAN_CONCRETE_POWDER ||
                    type == BLUE_CONCRETE_POWDER ||
                    type == BROWN_CONCRETE_POWDER ||
                    type == GREEN_CONCRETE_POWDER ||
                    type == RED_CONCRETE_POWDER ||
                    type == BLACK_CONCRETE_POWDER ||
                    type == DIRT ||
                    type == FARMLAND ||
                    type == GRASS_BLOCK ||
                    type == GRAVEL ||
                    type == MYCELIUM ||
                    type == PODZOL ||
                    type == RED_SAND ||
                    type == SAND ||
                    type == SOUL_SAND ||
                    type == SNOW_BLOCK ||
                    type == SNOW;
        } else if (tool == Tool.SWORD) {
            return type == COBWEB ||
                    type == BAMBOO ||
                    type == BAMBOO_SAPLING;
        }

        return false;
    }

    /**
     * Get the held tool the player is currently holding.
     *
     * @param player The player to check
     * @return The {@link Tool} being held
     */
    public Tool getHeldTool(Player player) {
        // Run a series of checks to detect, legacy because backwards compatability
        ItemStack heldItem = player.getInventory().getItemInHand();

        // Make sure item isn't null
        if (heldItem == null) return Tool.HAND;

        // Get type of held item
        String name = heldItem.getType().name().toUpperCase();

        // Now if checks
        if (name.endsWith("PICKAXE")) {
            return Tool.PICKAXE;
        } else if (name.endsWith("AXE")) {
            return Tool.AXE;
        } else if (name.endsWith("SHOVEL")) {
            return Tool.SHOVEL;
        } else if (name.endsWith("SWORD")) {
            return Tool.SWORD;
        } else if (name.endsWith("SHEARS")) {
            return Tool.SHEARS;
        }

        // Default to hand if no tool
        return Tool.HAND;
    }

    /**
     * Get's {@link ToolTier} for tool. Only takes
     * player as we need item. Returns {@link ToolTier#NONE}
     * if no actual tool.
     *
     * @param player Player to check
     * @return {@link ToolTier}
     */
    public ToolTier getTier(Player player) {
        // Run a series of checks to detect, legacy because backwards compatability
        ItemStack heldItem = player.getInventory().getItemInHand();

        // Make sure item isn't null
        if (heldItem == null) return ToolTier.NONE;

        // Get type of held item
        String name = heldItem.getType().name().toUpperCase();

        // Sword check first since this can have
        // WOOD or DIAMOND before it
        if (name.endsWith("SWORD")) {
            return ToolTier.SWORD;
        } else if (name.startsWith("WOOD") || name.startsWith("WOODEN")) {
            return ToolTier.WOOD;
        } else if (name.startsWith("STONE")) {
            return ToolTier.STONE;
        } else if (name.startsWith("IRON")) {
            return ToolTier.IRON;
        } else if (name.startsWith("DIAMOND")) {
            return ToolTier.DIAMOND;
        } else if (name.startsWith("GOLD")) {
            return ToolTier.GOLD;
        } else if (name.startsWith("SHEARS")) {
            return ToolTier.SHEARS;
        }

        // Default no tier
        return ToolTier.NONE;
    }

    /**
     * The tool being used to mine blocks
     */
    public enum Tool {
        PICKAXE, AXE, SHOVEL, SWORD, SHEARS, HAND
    }

    /**
     * Tier of a tool for mining,
     * etc WOODEN, DIAMOND, SHEARS etc
     */
    public enum ToolTier {
        WOOD, GOLD, STONE, IRON, DIAMOND, SHEARS, SWORD, NONE;

        /**
         * @param tier Check if this tier is at least
         *             another tier
         */
        public boolean atLeast(ToolTier tier) {
            return this.ordinal() >= tier.ordinal();
        }
    }
}
