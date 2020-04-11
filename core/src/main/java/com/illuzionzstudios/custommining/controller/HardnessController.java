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

import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.custommining.CustomMining;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.tools.Tool;

import static org.bukkit.Material.*;

/**
 * Control hardness of blocks, modifiers etc
 * <p>
 * We have to handle things like enchants, physics, potions etc.
 * All will be configurable giving the server owner full control over
 * how breaking works on their server. Also there will be permission
 * based modifiers so certain people can have different modifiers.
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


        return 20f;
    }

    /**
     * This tests if a tool can be used to benefit mining.
     * For instance a pickaxe helps stone, and axe helps wood.
     *
     * @param tool Tool to test
     * @param type The type of material being mined
     * @return If this tool helps or not
     */
    public boolean doesToolHelp(Tool tool, Material type) {
        // Run various checks per tool
        // Currently a lot of legacy materials
        // since it covers most things
        // TODO: Should change and account for older materials
        if (tool == Tool.AXE) {
            // Axe blocks
            return type == COCOA ||
                    type == JACK_O_LANTERN ||
                    type == PUMPKIN ||
                    type == VINE ||
                    type == MELON ||
                    type == BEE_NEST ||
                    type == LEGACY_BANNER ||
                    type == LEGACY_WALL_BANNER ||
                    type == BOOKSHELF ||
                    type == CHEST ||
                    type == BEEHIVE ||
                    type == CRAFTING_TABLE ||
                    type == DAYLIGHT_DETECTOR ||
                    type == LEGACY_FENCE ||
                    type == LEGACY_FENCE_GATE ||
                    type == RED_MUSHROOM_BLOCK ||
                    type == BROWN_MUSHROOM_BLOCK ||
                    type == JUKEBOX ||
                    type == LADDER ||
                    type == NOTE_BLOCK ||
                    type == LEGACY_SIGN ||
                    type == LEGACY_WALL_SIGN ||
                    type == TRAPPED_CHEST ||
                    type == LEGACY_WOOD ||
                    type == LEGACY_WOOD_BUTTON ||
                    type == LEGACY_WOODEN_DOOR ||
                    type == LEGACY_WOOD_DOOR ||
                    type == LEGACY_WOOD_PLATE ||
                    type == LEGACY_WOOD_STEP ||
                    type == LEGACY_WOOD_STAIRS ||
                    type == LEGACY_TRAP_DOOR;
        } else if (tool == Tool.PICKAXE) {
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
                    type == IRON_BLOCK ||
                    type == LAPIS_BLOCK ||
                    type == DIAMOND_BLOCK ||
                    type == EMERALD_BLOCK ||
                    type == GOLD_BLOCK ||
                    type == PISTON ||
                    type == STICKY_PISTON ||
                    type == PISTON_HEAD ||
                    type == SHULKER_BOX ||
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
                    type == LEGACY_CONCRETE ||
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
                    type == IRON_ORE ||
                    type == LAPIS_ORE ||
                    type == DIAMOND_ORE ||
                    type == EMERALD_ORE ||
                    type == GOLD_ORE ||
                    type == REDSTONE_ORE ||
                    type == OBSIDIAN;

        } else if (tool == Tool.SHEARS) {
            return type == LEGACY_LEAVES ||
                    type == LEGACY_LEAVES_2 ||
                    type == COBWEB ||
                    type == LEGACY_WOOL;
        } else if (tool == Tool.SHOVEL) {
            return type == CLAY ||
                    type == COARSE_DIRT ||
                    type == LEGACY_CONCRETE_POWDER ||
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

        if (name.startsWith("WOOD") || name.startsWith("WOODEN")) {
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
        WOOD, STONE, IRON, DIAMOND, GOLD, SHEARS, NONE
    }
}
