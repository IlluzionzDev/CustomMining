package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.core.compatibility.ServerVersion;
import com.illuzionzstudios.core.scheduler.MinecraftScheduler;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.core.util.PlayerUtil;
import com.illuzionzstudios.custommining.*;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.custommining.task.MiningTask;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

/**
 * This is the core class that handles the full mining system.
 * From here it will branch out to other classes to handle other sub
 * features. But here is the super of all mining
 */
public enum MiningController implements BukkitController<CustomMining>, Listener {
    INSTANCE;

    /**
     * Our custom handler to handle NMS packets
     * between versions
     */
    public MiningHandler handler;

    /**
     * Here we handle all instances of players breaking blocks
     * Tasks will be run async and only do sync tasks like
     * breaking blocks when needed to avoid as much lag
     * as possible
     */
    private Map<UUID, ArrayList<MiningTask>> miningTasks;

    @Override
    public void initialize(CustomMining plugin) {

        // Setup handler
        if (ServerVersion.isServerVersion(ServerVersion.V1_12)) {
            this.handler = new MiningHandler_1_12_R1();
        } else if (ServerVersion.isServerVersion(ServerVersion.V1_13)) {
            this.handler = new MiningHandler_1_13_R2();
        } else if (ServerVersion.isServerVersion(ServerVersion.V1_14)) {
            this.handler = new MiningHandler_1_14_R1();
        } else if (ServerVersion.isServerVersion(ServerVersion.V1_15)) {
            this.handler = new MiningHandler_1_15_R1();
        }

        // If NMS not handled, not available on server
        if (this.handler == null) {
            Logger.severe("Not supported on your server version " + ServerVersion.getServerVersionString());
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        this.miningTasks = new HashMap<>();

        // Register our services
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        MinecraftScheduler.get().registerSynchronizationService(this);
    }

    @Override
    public void stop(CustomMining plugin) {
        miningTasks.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.getPlayer().sendMessage("Hardness: " + handler.getDefaultBlockHardness(event.getClickedBlock()));
    }

    /**
     * Pause the breaking animation of a block
     * for a player breaking it. Can be resumed with
     * {@link MiningController#resumeBreaking}
     *
     * @param player The player breaking the block
     * @param block  The block that was being broken
     */
    public void pauseBreaking(Player player, Block block) {
        if (!miningTasks.containsKey(player.getUniqueId())) return;
        miningTasks.get(player.getUniqueId()).forEach(task -> {
            if (task.getBlock().equals(block)) {
                task.setEnabled(false);
            }
        });
    }

    /**
     * Resume breaking of a block that was
     * being mined by the player
     *
     * @param player The player that was mining the block
     * @param block  The block that was being broken
     */
    public void resumeBreaking(Player player, Block block) {
        if (!miningTasks.containsKey(player.getUniqueId())) return;

        miningTasks.get(player.getUniqueId()).forEach(task -> {
            if (task.getBlock().equals(block)) {
                task.setEnabled(true);
            }
        });
    }

    /**
     * Cancel the breaking of a block and
     * remove the task. Cancels breaking for all
     * players since we don't want it to be glitched
     *
     * @param block Block to stop the task for
     */
    public void cancelBreaking(Block block) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (miningTasks.containsKey(player.getUniqueId())) {
                ArrayList<MiningTask> list = miningTasks.get(player.getUniqueId());

                MiningTask task = null;

                // See which tasks have the block
                // TODO: Probably optimize this
                for (MiningTask miningTask : list) {
                    if (miningTask.getBlock().equals(block)) {
                        task = miningTask;
                    } else {
                        return;
                    }
                }

                list.remove(task);
                if (list.isEmpty()) {
                    miningTasks.remove(player.getUniqueId());
                    handler.sendBlockBreak(block, 10, Settings.BROADCAST_ANIMATION.getBoolean() ? PlayerUtil.getPlayers() : Arrays.asList(player));
                } else {
                    miningTasks.put(player.getUniqueId(), list);
                }
            }
        }
    }

    /**
     * Our method to break blocks, here we can handle
     * particles etc
     *
     * @param player Player that broke the block
     * @param block  The block that was broken
     */
    public void breakBlock(Player player, Block block) {
        // Make sure to cancel tasks when breaking any block
        cancelBreaking(block);

        // Actually break the block
        block.breakNaturally();
        block.

        // Force call block break event
        BlockBreakEvent blockBreak = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(blockBreak);
    }
}
