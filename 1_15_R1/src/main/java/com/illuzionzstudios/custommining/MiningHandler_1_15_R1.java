package com.illuzionzstudios.custommining;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
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

public class MiningHandler_1_15_R1 implements MiningHandler {

    @Override
    public void sendBlockBreak(org.bukkit.block.Block block, int damage, Player... players) {
        // Generate unique id based off location
        String idString = String.valueOf(block.getX()) + block.getY() + block.getZ();
        int id = Integer.parseInt(idString);
        PacketPlayOutBlockBreakAnimation breakBlockPacket = new PacketPlayOutBlockBreakAnimation(id, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(breakBlockPacket);
        }
    }

    @Override
    public void sendBlockBreak(org.bukkit.block.Block block, int damage, List<Player> players) {
        // Generate unique id based off location
        String idString = String.valueOf(Math.abs(block.getX())) + Math.abs(block.getY()) + Math.abs(block.getZ());
        int id = Integer.parseInt(idString);
        PacketPlayOutBlockBreakAnimation breakBlockPacket = new PacketPlayOutBlockBreakAnimation(id, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(breakBlockPacket);
        }
    }

    @Override
    public void cancelClientBreaking(Player player) {
        PacketPlayOutEntityEffect eff = new PacketPlayOutEntityEffect(player.getEntityId(), new MobEffect(MobEffectList.fromId(4), 255, Integer.MAX_VALUE, true, true));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(eff);
    }

    @Override
    public float getDefaultBlockHardness(org.bukkit.block.Block block) {
        return CraftMagicNumbers.getBlock(block.getType()).strength;
    }

    @Override
    public void playBreakEffect(org.bukkit.block.Block block) {

    }
}
