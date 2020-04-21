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
     * This counter tracks how many
     * elapsedTicks the block has been mined
     * for in total. We use to calculate
     * damage numbers for the animation.
     */
    private int counter = 0;

    /**
     * Ticks passed since not enabled
     */
    private int elapsedTicks = 0;

    /**
     * Total ticks task is alive
     */
    private int totalTicks = 0;

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
     * The stored percent of task
     * completed.
     */
    private float percent = 0;

    /**
     * Stored to track last damage
     * animation
     */
    private int lastDamageStep = 0;

    /**
     * This is the required damage required to break
     * the block. This is based off damage by the calculation
     * hardness * 30.
     */
    public float requiredDamage;

    /**
     * This keeps track of the total damage done
     * to the block. Used to track if we've broken the block.
     * If this exceeds {@link #requiredDamage}, it breaks the block.
     */
    public float damageDone;

    /**
     * This is the current amount of damage
     * being done per tick. This is updated every time
     * damage is different so if we change tool etc.
     */
    public float damagePerTick;

    /**
     * The stored hardness of the block. This in sense
     * is the break time, but used for damage calculation.
     */
    public float hardness;

    /**
     * If true, the task will tick breaking.
     * If set to false, it will pause breaking
     */
    @Setter
    private boolean enabled = true;

    public MiningTask(Player player, Block block, float hardness, float breakTime) {
        this.player = player;
        this.block = block;
        this.hardness = hardness;
        // Set formula for required total damage
        this.requiredDamage = hardness * 30;

        // Formula to calculate damage to be done
        // from the amount of total ticks
        // needed
        this.damagePerTick = (int) (requiredDamage / breakTime);

        Logger.debug("Break Time: " + requiredDamage / damagePerTick);
    }

    /**
     * @param damagePerTick Has to be passed as double to distinguish
     */
    public MiningTask(Player player, Block block, float hardness, double damagePerTick) {
        this.player = player;
        this.block = block;
        this.hardness = hardness;
        // Set formula for required total damage
        this.requiredDamage = hardness * 30;
        this.damagePerTick = (float) damagePerTick;

        Logger.debug("Break Time: " + requiredDamage / damagePerTick);
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
            int seconds = elapsedTicks / 20; // Seconds from elapsedTicks

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

        // Reset elapsedTicks since it was enabled
        this.elapsedTicks = 0;

        if (changedBreakTime) {
            // Update counters
            setPercent(percent);

            // Reset
            this.changedBreakTime = false;
        }

        // Deal damage to block
        damageDone += damagePerTick;

        // Damage is a value 0 to 9 inclusive representing the 10 different damage textures that can be applied to a block
        // Do calculation for break time based off damage
        int damage = (int) getPercent() / 10;

        // Send the damage animation state once for each increment
        // Check damage is not the same for previous tick
        if (damage != (counter == 0 ? -1 : lastDamageStep)) {
            // Auto gets who to send animation to based on settings
            MiningController.INSTANCE.handler.sendBlockBreak(block, damage, Settings.BROADCAST_ANIMATION.getBoolean() ? PlayerUtil.getPlayers() : Collections.singletonList(player));
        }

        // Update old
        lastDamageStep = damage;
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
        // New calculated damage per tick
        int damagePerTick = (int) (requiredDamage / time);

        // Set changed base on if actually changed
        this.changedBreakTime = this.damagePerTick != damagePerTick;

        // Update
        // Formula to calculate damage to be done
        // from the amount of total ticks
        // needed
        this.damagePerTick = damagePerTick;
    }

    /**
     * @param damage Change damage done per tick
     */
    public void setDamagePerTick(float damage) {
        this.changedBreakTime = this.damagePerTick != damage;
        this.damagePerTick = damage;
    }

    /**
     * @return Percent of task completed
     */
    public float getPercent() {
        // Find percent counter is of breaktime
        float p = (damageDone / requiredDamage) * 100;

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
        // Get percentage of breaktime
        this.damageDone = (int) (requiredDamage * (percent / 100));

        // Update local
        getPercent();
    }
}
