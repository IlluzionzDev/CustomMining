package com.illuzionzstudios.custommining.settings;

import com.illuzionzstudios.config.Config;
import com.illuzionzstudios.config.ConfigSetting;
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

    public static final ConfigSetting SAVE_PROGRESS = new ConfigSetting(config, "Mining.Save Progress", true,
            "When a player stops mining a block, it will pause the breaking animation at",
            "where it was, and resume when they continue mining it");

    public static final ConfigSetting CLEANUP_DELAY = new ConfigSetting(config, "Mining.Cleanup Delay", 10,
            "How many seconds after saving progress on block to delete progress.",
            "This is to maximise performance and reduce lag, only works if Save Progress is enabled");

    public static final ConfigSetting CLEANUP_THRESHOLD = new ConfigSetting(config, "Mining.Cleanup Threshold", 300,
            "Seconds after a block starts being broken to remove the breaking task.",
            "This is in order to clear any tasks that didn't get enabled and not lag the server.",
            "If you have long break times set this high so it doesn't clear as they're breaking.",
            "Default clear every 5 minutes of old tasks");

    public static final ConfigSetting BROADCAST_ANIMATION = new ConfigSetting(config, "Mining.Broadcast Animation", true,
            "If set to true, all players in the radius when breaking a block",
            "will see the breaking animation");

    public static final ConfigSetting LIQUID_MOD = new ConfigSetting(config, "Modifiers.Liquid", -5.0,
            "Controls the modifier on break speed when the player's head is underwater.",
            "Set to a double for percent increase, so 1.25, for a 25% increase",
            "or 3, for a 200% increase. For a nerf set to \"-\", so -1.25 for a 25% decrease.",
            "Set to 1 to disable.");

    public static final ConfigSetting AIR_MOD = new ConfigSetting(config, "Modifiers.Air", -5.0,
            "Controls the decrease of break speed when feet aren't touching the ground.");

    public static final ConfigSetting UNBREAKBLE_REGIONS = new ConfigSetting(config, "Main.Unbreakble Regions", true,
            "Make it so if worldguard makes a region unbreakable, they appear unbreakble",
            "like bedrock. (Required WorldGuard)");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The language file to use for the plugin",
            "More language files (if available) can be found in the plugins locale folder.");

    /**
     * Setup the configuration
     */
    public static void loadSettings() {
        config.load();
        config.setAutoremove(true);

        config.saveChanges();
    }

}
