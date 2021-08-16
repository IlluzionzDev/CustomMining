package com.illuzionzstudios.custommining;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MiningHandler_1_12_R1 implements MiningHandler {

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
        net.minecraft.server.v1_12_R1.Block nmsBlock = CraftMagicNumbers.getBlock(block.getType());

        // Pass some values, it only returns the hardness so idk what these args are for
        return nmsBlock.a(nmsBlock.getBlockData(), (World) block.getWorld(), new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public int getDefaultBlockExp(org.bukkit.block.Block block, ItemStack item, boolean spawnEntity) {
        return 0;
    }

    @Override
    public float getBaseMultiplier(ItemStack item, org.bukkit.block.Block block) {
        return 0;
    }

    @Override
    public boolean canDestroyBlock(ItemStack item, org.bukkit.block.Block block) {
        return false;
    }

    @Override
    public void playBreakEffect(org.bukkit.block.Block block) {

    }
}
