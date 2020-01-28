package com.illuzionzstudios.custommining.task;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
    private final int breakTime;

    /**
     * If true, the task will tick breaking.
     * If set to false, it will pause breaking
     */
    @Setter
    private boolean enabled = true;

    public MiningTask(Player player, Block block, int breakTime) {
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
    }
}
