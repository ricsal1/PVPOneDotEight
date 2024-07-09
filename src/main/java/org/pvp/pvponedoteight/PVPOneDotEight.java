package org.pvp.pvponedoteight;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PVPOneDotEight extends JavaPlugin implements Listener {

    public final String VERSION = getDescription().getVersion();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        try {
            new Metrics(this, 22590);
        }
        catch (Exception e){
            getLogger().info(ChatColor.RED + " Failed to register into Bstats");
        }

        getLogger().info(VERSION + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(VERSION + " disabled!");
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() == 4) {
            instance.setBaseValue(16);
        }
    }

    @EventHandler
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

}
