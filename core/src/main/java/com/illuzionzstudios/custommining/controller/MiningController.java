package com.illuzionzstudios.custommining.controller;

import com.illuzionzstudios.core.bukkit.controller.BukkitController;
import com.illuzionzstudios.core.scheduler.MinecraftScheduler;
import com.illuzionzstudios.custommining.CustomMining;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

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

    @Override
    public void initialize(CustomMining plugin) {

        // Register our services
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        MinecraftScheduler.get().registerSynchronizationService(this);
    }

    @Override
    public void stop(CustomMining plugin) {

    }
}
