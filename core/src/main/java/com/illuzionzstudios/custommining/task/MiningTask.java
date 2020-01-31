package com.illuzionzstudios.custommining.task;

import com.illuzionzstudios.core.scheduler.MinecraftScheduler;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.core.util.PlayerUtil;
import com.illuzionzstudios.custommining.controller.MiningController;
import com.illuzionzstudios.custommining.settings.Settings;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.Arrays;

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
     */
    private int counter = 0;

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
     * Total time to break the block in ticks
     */
    private final float breakTime;

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
        if (!enabled) return;

        // Damage is a value 0 to 9 inclusive representing the 10 different damage textures that can be applied to a block
        int damage = (int) (counter / (float) breakTime * 10);

        // Send the damage animation state once for each increment
        if (damage != (counter == 0 ? -1 : (int) ((counter - 1) / (float) breakTime * 10))) {
            Logger.debug("Sent animation");
            // Auto gets who to send animation to based on settings
            MiningController.INSTANCE.handler.sendBlockBreak(block, damage, Settings.BROADCAST_ANIMATION.getBoolean() ? PlayerUtil.getPlayers() : Arrays.asList(player));
        }

        counter++;

        // Reached break time
        if (counter == breakTime) {
            // Handle breaking the block
            MinecraftScheduler.get().synchronize(() -> {
                MiningController.INSTANCE.breakBlock(player, block);
            });
        }
    }
}
