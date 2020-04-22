package com.illuzionzstudios.custommining.task;

import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.core.util.PlayerUtil;
import com.illuzionzstudios.custommining.controller.HardnessController;
import com.illuzionzstudios.custommining.controller.MiningController;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.scheduler.MinecraftScheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;

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
 * A custom task handling the breaking of blocks
 */
@Getter
public class MiningTask implements Runnable {

    /**
     * Used to handle ticking the block break
     * This counter is how many ticks the block
     * has been breaking. Then we simply check
     * if this reaches the ticks needed for
     * break time, if so, break.
     */
    private int counter = 0;

    /**
     * Elapsed ticks the task has been disabled
     */
    private int elapsedTicks = 0;

    /**
     * Ticks task is alive
     */
    private int totalTicks = 0;

    /**
     * Last damage number used for comparing
     * to avoid resetting progress
     */
    private int lastDamage = 0;

    /**
     * Our task ID to identify the task
     */
    @Setter
    private int taskID;

    /**
     * Player breaking the block
     */
    private final Player player;

    /**
     * The block we are currently breaking
     */
    private final Block block;

    /**
     * If the break time changed so we can update logic
     */
    private boolean changedBreakTime;

    /**
     * Total time to break the block in ticks
     * Can be set when tool etc changes
     */
    private float breakTime;

    /**
     * Percent complete of task
     * Used for scaling
     */
    private float percent = 0;

    /**
     * If true, the task will tick breaking.
     * If set to false, it will pause breaking
     */
    @Setter
    private boolean enabled = true;

    public MiningTask(Player player, Block block, float breakTime) {
        this.player = player;
        this.block = block;
        this.breakTime = breakTime;
    }

    /**
     * Called every tick
     * Where we handle breaking and more
     */
    @Override
    public void run() {
        elapsedTicks++;
        totalTicks++;

        int totalSeconds = totalTicks / 20; // Total seconds passed

        // Handle cleanup here
        if (Settings.SAVE_PROGRESS.getBoolean()) {
            int seconds = elapsedTicks / 20; // Seconds from ticks

            if (seconds >= Settings.CLEANUP_DELAY.getInt()) {
                MiningController.INSTANCE.cancelBreaking(block);
                return;
            }
        }

        if (!enabled) return;

        // Been enabled for over threshold
        // Urgent cleanup so it doesn't run forever
        // and lag the server
        if (totalSeconds >= Settings.CLEANUP_THRESHOLD.getInt()) {
            MiningController.INSTANCE.cancelBreaking(block);
            return;
        }

        // If player can't trigger tasks, immediately pause the task
        // After cleanup checks
        if (MiningController.INSTANCE.getDisabled().contains(player.getUniqueId())) {
            return;
        }

        // Reset ticks since it was enabled
        this.elapsedTicks = 0;

        if (changedBreakTime) {
            // Update counters
            setPercent(percent);

            // Reset
            this.changedBreakTime = false;
        }

        // Damage is a value 0 to 9 inclusive representing the 10 different damage textures that can be applied to a block
        int damage = (int) getPercent() / 10;

        // Send the damage animation state once for each increment
        if (damage != (counter == 0 ? -1 : lastDamage)) {
            // Auto gets who to send animation to based on settings
            MiningController.INSTANCE.handler.sendBlockBreak(block, damage, Settings.BROADCAST_ANIMATION.getBoolean() ? PlayerUtil.getPlayers() : Collections.singletonList(player));
        }

        // Update last variable
        lastDamage = damage;
        counter++;

        // Reached break time
        if (getPercent() >= 100f) {
            // Handle breaking the block
            MinecraftScheduler.get().synchronize(() -> {
                MiningController.INSTANCE.breakBlock(player, block);
            });
        }
    }

    /**
     * @param time Change the break time of the block
     */
    public void setBreakTime(float time) {
        // Set changed base on if actually changed
        this.changedBreakTime = this.breakTime != time;

        // Update
        this.breakTime = time;
    }

    /**
     * @return Percent of task completed
     */
    public float getPercent() {
        // Find percent counter is of break time
        float p = (counter / breakTime) * 100;

        // Update local percentage
        if (this.percent != p)
            this.percent = p;
        return p;
    }

    /**
     * Updates counters accordingly
     *
     * @param percent Set the percent completed of task
     */
    public void setPercent(float percent) {
        // Get percentage of break time
        this.counter = (int) (breakTime * (percent / 100));

        // Update local
        getPercent();
    }
}