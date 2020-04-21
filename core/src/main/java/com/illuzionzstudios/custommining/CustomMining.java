package com.illuzionzstudios.custommining;

import com.illuzionzstudios.config.Config;
import com.illuzionzstudios.core.plugin.IlluzionzPlugin;
import com.illuzionzstudios.custommining.controller.HardnessController;
import com.illuzionzstudios.custommining.controller.MiningController;
import com.illuzionzstudios.custommining.controller.ModifierController;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.scheduler.bukkit.BukkitScheduler;

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

public final class CustomMining extends IlluzionzPlugin {

    private static CustomMining INSTANCE;

    public static CustomMining getInstance() {
        return INSTANCE;
    }

    public void onPluginLoad() {
        INSTANCE = this;
    }

    public void onPluginEnable() {
        // Load all settings and language
        Settings.loadSettings();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        new BukkitScheduler(this).initialize();

        // Load controllers
        ModifierController.INSTANCE.initialize(this);
        HardnessController.INSTANCE.initialize(this);
        MiningController.INSTANCE.initialize(this);

        // Metrics
        int pluginId = 7248;
        Metrics metrics = new Metrics(this, pluginId);
    }

    public void onPluginDisable() {
        ModifierController.INSTANCE.stop(this);
        HardnessController.INSTANCE.stop(this);
        MiningController.INSTANCE.stop(this);
    }

    public void onConfigReload() {
        // Load all settings and language
        Settings.loadSettings();
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
    }

    public List<Config> getExtraConfig() {
        return null;
    }

    @Override
    public String getPluginName() {
        return "CustomMining";
    }

    @Override
    public String getPluginVersion() {
        return "1.0";
    }
}
