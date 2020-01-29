package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.core.compatibility.ServerVersion;
import com.illuzionzstudios.core.scheduler.MinecraftScheduler;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.custommining.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

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

        // Register our services
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        MinecraftScheduler.get().registerSynchronizationService(this);
    }

    @Override
    public void stop(CustomMining plugin) {

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.getPlayer().sendMessage("Hardness: " + handler.getDefaultBlockHardness(event.getClickedBlock()));
    }
}
