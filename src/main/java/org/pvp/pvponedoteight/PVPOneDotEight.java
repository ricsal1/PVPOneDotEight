package org.pvp.pvponedoteight;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class PVPOneDotEight extends JavaPlugin implements Listener {

    public final String VERSION = getDescription().getVersion();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        try {
            new Metrics(this, 22590);
        } catch (Exception e) {
            getLogger().info(ChatColor.RED + " Failed to register into Bstats");
        }

        getLogger().info(VERSION + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(VERSION + " disabled!");
    }



    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() == 4) {
            instance.setBaseValue(16);
        }

//        for (int i = 0; i < player.getInventory().getSize(); i++) {
//            ItemStack item = player.getInventory().getItem(i);
//            if (item != null && item.getEnchantments() != null) {
//
//                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
//                    int level = entry.getValue();
//
//                    if (level >= 6) {
//                        item.removeEnchantment(entry.getKey());
//                        break;
//                    }
//                }
//            }
//        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() != 4) {
            instance.setBaseValue(4);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityPlaceEvent(@NotNull EntityPlaceEvent event) {

        Location location = event.getBlock().getLocation();
        String myWorld = location.getWorld().getName();

        if (event.getEntityType() == EntityType.END_CRYSTAL && !(myWorld.endsWith("_nether") || myWorld.endsWith("_end"))) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onSelect(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack it = player.getInventory().getItemInMainHand();
        String weapon = it.getType().toString();

        //checks previous unselected, so next time it unselects de sword...
        if ((weapon.contains("SWORD") && it.containsEnchantment(Enchantment.SWEEPING_EDGE))) {
            it.removeEnchantment(Enchantment.SWEEPING_EDGE);
        }
    }

}
