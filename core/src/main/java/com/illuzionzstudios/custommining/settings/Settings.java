package com.illuzionzstudios.custommining.settings;

import com.illuzionzstudios.mist.config.ConfigSetting;
import com.illuzionzstudios.mist.config.ConfigSettings;
import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * General settings for the plugin
 */
public class Settings extends PluginSettings {

    /**
     * Main plugin settings
     */
    public static final ConfigSettings MAIN_GROUP = new ConfigSettings("main");

    /**
     * Mining settings
     */
    public static final ConfigSettings MINING_GROUP = new ConfigSettings("mining");

    /**
     * Modifier settings
     */
    public static final ConfigSettings MODIFIERS_GROUP = new ConfigSettings("modifiers");

    public static final ConfigSetting MINING_SAVE_PROGRESS = MINING_GROUP.create("Mining.Save Progress", true,
            "When a player stops mining a block, it will pause the breaking animation at",
            "where it was, and resume when they continue mining it");

    public static final ConfigSetting MINING_CLEANUP_DELAY = MINING_GROUP.create("Mining.Cleanup Delay", 10,
            "How many seconds after saving progress on block to delete progress.",
            "This is to maximise performance and reduce lag, only works if Save Progress is enabled");

    public static final ConfigSetting MINING_CLEANUP_THRESHOLD = MINING_GROUP.create("Mining.Cleanup Threshold", 300,
            "Seconds after a block starts being broken to remove the breaking task.",
            "This is in order to clear any tasks that didn't get enabled and not lag the server.",
            "If you have long break times set this high so it doesn't clear as they're breaking.",
            "Default clear every 5 minutes of old tasks");

    public static final ConfigSetting MINING_BROADCAST_ANIMATION = MINING_GROUP.create("Mining.Broadcast Animation", true,
            "If set to true, all players in the radius when breaking a block",
            "will see the breaking animation");

    public static final ConfigSetting MODIFIER_LIQUID = MODIFIERS_GROUP.create("Modifiers.Liquid", -5.0,
            "Controls the modifier on break speed when the player's head is underwater.",
            "Set to a double for percent increase, so 1.25, for a 25% increase",
            "or 3, for a 200% increase. For a nerf set to \"-\", so -1.25 for a 25% decrease.",
            "Set to 1 to disable.");

    public static final ConfigSetting MODIFIER_AIR = MODIFIERS_GROUP.create("Modifiers.Air", -5.0,
            "Controls the decrease of break speed when feet aren't touching the ground.");

    public static final ConfigSetting UNBREAKABLE_REGIONS = MAIN_GROUP.create("Main.Unbreakable Regions", true,
            "Make it so if World Guard makes a region unbreakable, they appear unbreakable",
            "like bedrock. (Required WorldGuard)");

    public Settings(SpigotPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadSettings() {
        MINING_GROUP.load();
        MODIFIERS_GROUP.load();
        MAIN_GROUP.load();
    }

}
