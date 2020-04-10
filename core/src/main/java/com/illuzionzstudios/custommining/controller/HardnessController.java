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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Control hardness of blocks, modifiers etc
 *
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
     *
     * Returning 0 means instantly break and < 0 means unbreakable
     *
     * @param block The block trying to be mined
     * @param player The player mining the block
     * @return Break time in ticks
     */
    public float processFinalBreakTime(Block block, Player player) {


        return 20f;
    }
}
