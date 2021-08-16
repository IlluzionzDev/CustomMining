package com.illuzionzstudios.custommining.task;

import com.illuzionzstudios.custommining.controller.MiningController;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;

/**
 * A custom task handling the breaking of blocks. For as long
 * as this is enabled it plays breaking animations for a block
 * over a set amount of time then calls our {@link MiningController#breakBlock(Player, Block)}
 */
@Getter
public class MiningTask implements Runnable {

    /**
     * Player breaking the block. If player is null this task
     * has no player and is code controlled
     */
    @Nullable
    private final Player player;

    /**
     * The block we are currently breaking
     */
    private final Block block;

    /**
     * Ticks task is alive
     */
    private int totalTicks = 0;

    /**
     * Used to handle ticking the block break
     * This counter is how many ticks the block
     * has been breaking. Then we simply check
     * if this reaches the ticks needed for
     * break time, if so, break. Only ticks when task is actually enabled
     */
    private int counter = 0;

    /**
     * Elapsed ticks the task has been disabled
     */
    private int elapsedTicks = 0;

    /**
     * Last damage number used for comparing
     * to avoid resetting progress
     */
    private int lastDamage = 0;

    /**
     * Our task ID to identify the task (bukkit runnable)
     */
    @Setter
    private int taskID;

    /**
     * Internal flag if the break time changed so we can update logic
     */
    private boolean changedBreakTime;

    /**
     * Total time to break the block (in ticks)
     * Can be set when tool etc changes
     */
    private float breakTime;

    /**
     * Percent complete of task
     * Used for scaling. Can be set to a certain percentage
     * and continue mining from there
     */
    private float percent = 0;

    /**
     * If true, the task will tick breaking.
     * If set to false, it will pause breaking
     */
    @Setter
    private boolean enabled = true;

    /**
     * This is the function that runs when this block breaks. By default
     * is {@link MiningController#breakBlock(Player, Block)}
     */
    private final BiConsumer<Player, Block> onBreak;


    /**
     * If no player must implement own onBreak
     */
    public MiningTask(Block block, float breakTime, BiConsumer<Player, Block> onBreak) {
        this(null, block, breakTime, onBreak);
    }

    public MiningTask(@Nullable Player player, Block block, float breakTime) {
        this(player, block, breakTime, MiningController.INSTANCE::breakBlock);
    }

    public MiningTask(@Nullable Player player, Block block, float breakTime, BiConsumer<Player, Block> onBreak) {
        this.player = player;
        this.block = block;
        this.breakTime = breakTime;
        this.onBreak = onBreak;
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
        if (Settings.MINING_SAVE_PROGRESS.getBoolean()) {
            int seconds = elapsedTicks / 20; // Seconds from ticks

            if (seconds >= Settings.MINING_CLEANUP_DELAY.getInt()) {
                MiningController.INSTANCE.cancelBreaking(block);
                return;
            }
        }

        if (!enabled) return;

        // Been enabled for over threshold
        // Urgent cleanup so it doesn't run forever
        // and lag the server
        if (totalSeconds >= Settings.MINING_CLEANUP_THRESHOLD.getInt()) {
            MiningController.INSTANCE.cancelBreaking(block);
            return;
        }

        if (player != null) {
            // If player can't trigger tasks, immediately pause the task
            // After cleanup checks
            if (MiningController.INSTANCE.getDisabled().contains(player.getUniqueId()))
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
            MiningController.INSTANCE.getHandler().sendBlockBreak(block, damage, Settings.MINING_BROADCAST_ANIMATION.getBoolean() || player == null ? new ArrayList<>(Bukkit.getOnlinePlayers()) : Collections.singletonList(player));
        }

        // Update last variable
        lastDamage = damage;
        counter++;

        // Reached break time
        if (getPercent() >= 100f) {
            // Handle breaking the block
            MinecraftScheduler.get().synchronize(() -> {
                this.onBreak.accept(player, block);
            });
            // Disable so stops ticking
            setEnabled(false);
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