package com.illuzionzstudios.custommining;

import com.illuzionzstudios.custommining.controller.HardnessController;
import com.illuzionzstudios.custommining.controller.MiningController;
import com.illuzionzstudios.custommining.controller.ModifierController;
import com.illuzionzstudios.custommining.settings.MiningLocale;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomMining extends SpigotPlugin {

    /**
     * Singleton instance of our {@link SpigotPlugin}
     */
    private static volatile CustomMining INSTANCE;

    /**
     * Return our instance of the {@link SpigotPlugin}
     * <p>
     * Should be overridden in your own {@link SpigotPlugin} class
     * as a way to implement your own methods per plugin
     *
     * @return This instance of the plugin
     */
    public static CustomMining getInstance() {
        // Assign if null
        if (INSTANCE == null) {
            INSTANCE = JavaPlugin.getPlugin(CustomMining.class);

            Objects.requireNonNull(INSTANCE, "Cannot create instance of plugin. Did you reload?");
        }

        return INSTANCE;
    }

    public void onPluginLoad() {
    }

    @Override
    public void onPluginPreEnable() {

    }

    public void onPluginEnable() {
        // Load controllers
        ModifierController.INSTANCE.initialize(this);
        HardnessController.INSTANCE.initialize(this);
        MiningController.INSTANCE.initialize(this);

        // Metrics
        int pluginId = 7248;
        new Metrics(this, pluginId);
    }

    public void onPluginDisable() {
        ModifierController.INSTANCE.stop(this);
        HardnessController.INSTANCE.stop(this);
        MiningController.INSTANCE.stop(this);
    }

    @Override
    public void onPluginPreReload() {

    }

    @Override
    public void onPluginReload() {

    }

    @Override
    public void onReloadablesStart() {

    }

    @Override
    public PluginSettings getPluginSettings() {
        return new Settings(this);
    }

    @Override
    public PluginLocale getPluginLocale() {
        return new MiningLocale(this);
    }

    @Override
    public int getPluginId() {
        return 0;
    }
}
