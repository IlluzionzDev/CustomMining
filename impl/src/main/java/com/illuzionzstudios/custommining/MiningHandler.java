package com.illuzionzstudios.custommining;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

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
 * Handle custom methods with packets to simulate
 * the custom mining system
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
     * Play the breaking effect for a block.
     * This includes the breaking particles
     * and the breaking sound.
     *
     * @param block Block to play effects for
     */
    void playBreakEffect(org.bukkit.block.Block block);

}
