package com.illuzionzstudios.custommining.controller;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.cryptomorin.xseries.XSound;
import com.illuzionzstudios.custommining.*;
import com.illuzionzstudios.custommining.settings.Settings;
import com.illuzionzstudios.custommining.task.MiningTask;
import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.compatibility.ServerVersion;
import com.illuzionzstudios.mist.controller.PluginController;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.scheduler.rate.Async;
import com.illuzionzstudios.mist.scheduler.rate.Rate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

/**
 * This is the core class that handles the full mining system.
 * From here it will branch out to other classes to handle other sub
 * features. But here is the super of all mining
 */
public enum MiningController implements PluginController<CustomMining>, Listener {
    INSTANCE;

    /**
     * Our custom handler to handle NMS packets
     * between versions
     */
    @Getter
    private MiningHandler handler;

    /**
     * Scheduler for block breaking
     */
    private BukkitScheduler scheduler;

    /**
     * Here we handle all instances of players breaking blocks
     * Tasks will be run async and only do sync tasks like
     * breaking blocks when needed to avoid as much lag
     * as possible
     * <p>
     * Transient because we don't want to save, it's all cached
     */
    private transient Map<UUID, ArrayList<MiningTask>> miningTasks;

    /**
     * List of player's who currently can't tick
     * {@link MiningTask}'s. Used if the player triggered one
     * accidentally or can't mine that block.
     */
    @Getter
    private List<UUID> disabled;

    @Override
    public void initialize(CustomMining plugin) {
        // Setup handler
        if (ServerVersion.equals(ServerVersion.V.v1_12)) {
            this.handler = new MiningHandler_1_12_R1();
        } else if (ServerVersion.equals(ServerVersion.V.v1_13)) {
            this.handler = new MiningHandler_1_13_R2();
        } else if (ServerVersion.equals(ServerVersion.V.v1_14)) {
            this.handler = new MiningHandler_1_14_R1();
        } else if (ServerVersion.equals(ServerVersion.V.v1_15)) {
            this.handler = new MiningHandler_1_15_R1();
        } else if (ServerVersion.equals(ServerVersion.V.v1_17)) {
            this.handler = new MiningHandler_1_17_R1();
        }

        // If NMS not handled, not available on server
        if (this.handler == null) {
            Logger.severe("Not supported on your server version " + ServerVersion.getServerVersion());
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        this.miningTasks = new HashMap<>();
        this.disabled = new ArrayList<>();

        // Register our services
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        MinecraftScheduler.get().registerSynchronizationService(this);

        registerProtocols();
        scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void stop(CustomMining plugin) {
        miningTasks.clear();
    }

    /**
     * Handle cleanup so no memory leaks
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        miningTasks.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Update players with mining fatigue
     */
    @Async(rate = Rate.FASTEST)
    public void giveEffect() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handler.cancelClientBreaking(player);
        }
    }

    /**
     * This is the entry point for handling
     * custom break times. We will handle setting
     * tasks etc here
     *
     * @param event Interact event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Insta break when in creative so skip entirely
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // Make sure it doesn't give null when block is air
        if (event.getAction() == Action.LEFT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }

        // Start our code if they are going to break the block
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Cancel as we will handle ourselves
            event.setCancelled(true);

            // About to create new task make sure
            // to remove disabled
            this.disabled.remove(player.getUniqueId());

            // Our block that was clicked
            Block block = event.getClickedBlock();

            // Here handle the break time based on conditions

            // Here break time is 0, so we just insta break
            // Also check default block hardness because can't change
            // break time of default insta breaks
            if ((HardnessController.INSTANCE.processFinalBreakTime(block, player) >= 0 &&
                    HardnessController.INSTANCE.processFinalBreakTime(block, player) <= 0.05) ||
                    handler.getDefaultBlockHardness(block) == 0.0) {
                breakBlock(player, block);
                return;
            }

            MiningTask previous = null;

            // Detect if the block was being mined, so resume it
            // only if set in settings
            if (Settings.MINING_SAVE_PROGRESS.getBoolean()) {
                previous = resumeBreaking(player, block);
            }

            // If there is no previously targeted block or if the currently targeted block isn't the same
            // as the previous then destroying the new block
            if (previous == null || !previous.getBlock().getLocation().equals(block.getLocation())) {
                // The player has switched targets so the previous block is no longer being destroyed
                if (previous != null) {
                    previous.setEnabled(false);
                }

                // Create new instance of block breaking runnable
                MiningTask task = new MiningTask(player, block, HardnessController.INSTANCE.processFinalBreakTime(block, player));

                // Do task async, will handle minecraft things sync
                int taskID = scheduler.scheduleAsyncRepeatingTask(CustomMining.getInstance(), task, 0L, 1L);
                task.setTaskID(taskID);

                // Here we add the breaking to the tasks
                ArrayList<MiningTask> list = new ArrayList<>();

                if (miningTasks.containsKey(player.getUniqueId())) {
                    list = miningTasks.get(player.getUniqueId());
                }

                list.add(task);
                miningTasks.put(player.getUniqueId(), list);
            }

        }
    }

    /**
     * Pause the breaking animation of a block
     * for a player breaking it. Can be resumed with
     * {@link MiningController#resumeBreaking}
     *
     * @param player The player breaking the block
     * @param block  The block that was being broken
     */
    public void pauseBreaking(Player player, Block block) {
        // If they don't save progress just delete the task
        if (!Settings.MINING_SAVE_PROGRESS.getBoolean()) {
            cancelBreaking(block);
            return;
        }

        if (!miningTasks.containsKey(player.getUniqueId())) return;
        miningTasks.get(player.getUniqueId()).forEach(task -> {
            // If task doesn't exist
            if (task == null) {
                // Abort all tasks
                miningTasks.remove(player.getUniqueId());
                return;
            }

            if (task.getBlock().getLocation().equals(block.getLocation())) {
                task.setEnabled(false);
            }
        });
    }

    /**
     * Resume breaking of a block that was
     * being mined by the player
     *
     * @param player The player that was mining the block
     * @param block  The block that was being broken
     */
    public MiningTask resumeBreaking(Player player, Block block) {
        if (miningTasks.containsKey(player.getUniqueId())) {
            for (MiningTask task : miningTasks.get(player.getUniqueId())) {
                if (task.getBlock().getLocation().equals(block.getLocation())) {
                    // Make sure to update break time
                    // if tool switched etc
                    task.setBreakTime(HardnessController.INSTANCE.processFinalBreakTime(block, player));
                    task.setEnabled(true);
                    return task;
                }
            }
        }

        return null;
    }

    /**
     * Cancel the breaking of a block and
     * remove the task. Cancels breaking for all
     * players since we don't want it to be glitched
     *
     * @param block Block to stop the task for
     */
    public void cancelBreaking(Block block) {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (miningTasks.containsKey(player.getUniqueId())) {
                    ArrayList<MiningTask> list = miningTasks.get(player.getUniqueId());

                    // Only need the first as we can't have two tasks at the same location
                    Optional<MiningTask> taskStream = list.stream().filter(miningTask -> miningTask.getBlock().getLocation().equals(block.getLocation())).findFirst();

                    // If no task simply return because we don't need to do anything else
                    if (taskStream.isEmpty()) return;

                    // Finally assign found task to our instanced task
                    MiningTask task = taskStream.get();

                    // Cancel mining task
                    scheduler.cancelTask(task.getTaskID());
                    // Remove task from player's running tasks
                    list.remove(task);
                    // Send final animation
                    handler.sendBlockBreak(block, 10, Settings.MINING_BROADCAST_ANIMATION.getBoolean() ? new ArrayList<>(Bukkit.getOnlinePlayers()) : Collections.singletonList(player));
                    if (list.isEmpty()) {
                        // No more mining tasks so remove from map
                        miningTasks.remove(player.getUniqueId());
                    } else {
                        // Otherwise just update map with one less task
                        miningTasks.put(player.getUniqueId(), list);
                    }
                }
            }
        } catch (Exception ignored) {
            // Sometimes random error with accessing list
        }
    }

    /**
     * Our method to break blocks, here we can handle
     * particles etc
     *
     * @param player Player that broke the block
     * @param block  The block that was broken
     */
    public void breakBlock(Player player, Block block) {
        // Make sure to cancel tasks when breaking any block
        cancelBreaking(block);

        // Block break effect
        handler.playBreakEffect(block);

        ItemStack hand = player.getInventory().getItemInMainHand();
        // Use durability on item (account for unbreaking)
        int durabilityLevel = hand.getEnchantmentLevel(Enchantment.DURABILITY);
        if (Math.random() <= (1 / (float) (durabilityLevel + 1))) {
            // Has durability
            if (hand.getType().getMaxDurability() > 0) {
                int newDurability = player.getInventory().getItemInMainHand().getDurability();
                player.getInventory().getItemInMainHand().setDurability((short) (newDurability + 1));
                Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent(player, player.getInventory().getItemInMainHand(), 1));
            }
        }

        // Force call block break event
        BlockBreakEvent blockBreak = new BlockBreakEvent(block, player);
        blockBreak.setExpToDrop(handler.getDefaultBlockExp(block, player.getInventory().getItemInMainHand(), false));
        Bukkit.getPluginManager().callEvent(blockBreak);
        // Spawn exp
        handler.getDefaultBlockExp(block, player.getInventory().getItemInMainHand(), true);

        // Drops based on item used
        block.breakNaturally(player.getInventory().getItemInMainHand());
    }

    /**
     * Check for durability change from item and break it
     */
    @EventHandler
    public void onDurabilityChange(PlayerItemDamageEvent event) {
        if (event.getItem().getDurability() - event.getDamage() >= event.getItem().getType().getMaxDurability() - 1) {
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            XSound.ENTITY_ITEM_BREAK.play(event.getPlayer());
        }
    }

    /**
     * Registers protocols through ProtocolLib to cancel block breaking while looking away
     */
    public void registerProtocols() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(CustomMining.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().getValues().get(0);
                if (digType.equals(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK) ||
                        digType.equals(EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK)) {

                    // Loop through abandoned blocks and make sure to stop them all
                    for (BlockPosition position : packet.getBlockPositionModifier().getValues()) {
                        Block createdBlock = event.getPlayer().getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());

                        pauseBreaking(event.getPlayer(), createdBlock);
                    }

                    // Disable ticking tasks
                    getDisabled().add(event.getPlayer().getUniqueId());
                }
            }
        });
    }
}
