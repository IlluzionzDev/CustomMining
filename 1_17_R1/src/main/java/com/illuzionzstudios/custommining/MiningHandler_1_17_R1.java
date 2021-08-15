package com.illuzionzstudios.custommining;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class MiningHandler_1_17_R1 implements MiningHandler {

    /**
     * Break sound of block
     */
    private static Field breakSound;

    /**
     * Minecraft key
     */
    private static Field minecraftKey;

    static {
        try {
            breakSound = SoundEffectType.class.getDeclaredField("aA");
            breakSound.setAccessible(true);

            minecraftKey = SoundEffect.class.getDeclaredField("b");
            minecraftKey.setAccessible(true);


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendBlockBreak(org.bukkit.block.Block block, int damage, Player... players) {
        PacketPlayOutBlockBreakAnimation breakBlockPacket = new PacketPlayOutBlockBreakAnimation(getBlockEntityId(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().b.sendPacket(breakBlockPacket);
        }
    }

    @Override
    public void sendBlockBreak(org.bukkit.block.Block block, int damage, List<Player> players) {
        PacketPlayOutBlockBreakAnimation breakBlockPacket = new PacketPlayOutBlockBreakAnimation(getBlockEntityId(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().b.sendPacket(breakBlockPacket);
        }
    }

    @Override
    public void cancelClientBreaking(Player player) {
        PacketPlayOutEntityEffect eff = new PacketPlayOutEntityEffect(player.getEntityId(), new MobEffect(MobEffectList.fromId(4), 255, Integer.MAX_VALUE, true, false));
        ((CraftPlayer) player).getHandle().b.sendPacket(eff);
    }

    @Override
    public float getDefaultBlockHardness(org.bukkit.block.Block block) {
        System.out.println("Hardness: " + CraftMagicNumbers.getBlock(block.getType()).getDurability());
        return CraftMagicNumbers.getBlock(block.getType()).getDurability();
    }

    @Override
    public float getBaseMultiplier(ItemStack item, Block block) {
        return CraftMagicNumbers.getItem(item.getType()).getDestroySpeed(CraftMagicNumbers.getItem(item.getType()).createItemStack(), CraftMagicNumbers.getBlock(block.getType()).getBlockData());
    }

    @Override
    public boolean canDestroyBlock(ItemStack item, Block block) {
        return CraftMagicNumbers.getItem(item.getType()).canDestroySpecialBlock(CraftMagicNumbers.getBlock(block.getType()).getBlockData());
    }

    @Override
    public void playBreakEffect(org.bukkit.block.Block block) {
        block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.5, 0.5),
                new Random().nextInt(20) + 10,
                0.5, 0.5, 0.5, block.getBlockData());

        try {
            IBlockData nmsBlock = ((CraftBlock) block).getNMS();
            SoundEffectType soundEffectType = nmsBlock.getStepSound();

            Field breakSound = SoundEffectType.class.getDeclaredField("aA");
            breakSound.setAccessible(true);
            SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);
            MinecraftKey minecraftKey = nmsSound.a();
            String soundName = minecraftKey.getKey();
            soundName = soundName.toUpperCase().replaceAll("\\.", "_");

            block.getWorld().playSound(block.getLocation(), Sound.valueOf(soundName), 1, 0.5f);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
