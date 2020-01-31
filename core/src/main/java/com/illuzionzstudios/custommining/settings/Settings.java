package com.illuzionzstudios.custommining.settings;

import com.illuzionzstudios.core.config.Config;
import com.illuzionzstudios.core.config.ConfigSetting;
import com.illuzionzstudios.custommining.CustomMining;

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
 * General settings for the plugin
 */
public class Settings {

    static final Config config = CustomMining.getInstance().getCoreConfig();

    public static final ConfigSetting SAVE_PROGRESS = new ConfigSetting(config, "Main.Save Progress", true,
            "When a player stops mining a block, it will pause the breaking animation at",
            "where it was, and resume when they continue mining it");

    public static final ConfigSetting CLEANUP_DELAY = new ConfigSetting(config, "Main.Cleanup Delay", 10,
            "How many seconds after saving progress on block to delete progress.",
            "This is to maximise performance and reduce lag, only works if Save Progress is enabled");

    public static final ConfigSetting BROADCAST_ANIMATION = new ConfigSetting(config, "Main.Broadcast Animation", true,
            "If set to true, all players in the radius when breaking a block",
            "will see the breaking animation");

    public static final ConfigSetting UNBREAKBLE_REGIONS = new ConfigSetting(config, "Main.Unbreakble Regions", true,
            "Make it so if worldguard makes a region unbreakable, they appear unbreakble",
            "like bedrock. (Required WorldGuard)");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The language file to use for the plugin",
            "More language files (if available) can be found in the plugins locale folder.");

    public static final ConfigSetting AUTOSAVE = new ConfigSetting(config, "System.Autosave Interval", 60,
            "Seconds between autosaves and loading the updated config");


    /**
     * Setup the configuration
     */
    public static void loadSettings() {
        config.load();
        config.setAutoremove(true).setAutosave(true).setAutosaveInterval(AUTOSAVE.getInt());

        config.saveChanges();
    }

}
