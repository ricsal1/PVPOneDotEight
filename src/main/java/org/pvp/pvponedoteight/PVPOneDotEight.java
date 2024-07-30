package org.pvp.pvponedoteight;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.pvp.pvponedoteight.Utils.Metrics;
import org.pvp.pvponedoteight.Utils.MyBukkit;
import org.pvp.pvponedoteight.Utils.UpdateChecker;
import org.pvp.pvponedoteight.Utils.UpdateCheckerBukkSpig;

import java.io.File;
import java.util.Map;


public final class PVPOneDotEight extends JavaPlugin implements Listener {

    public final String VERSION = getDescription().getVersion();
    public boolean isInit = true;

    public MyBukkit mybukkit;
    private int maxCPS;
    private boolean checkOnlineUpdate;
    private boolean specialEffects;
    private boolean noCooldown;
    private boolean fastRespawn;
    private boolean normalizeHackedItems;

    @Override
    public void onEnable() {

        LoadSettings();
        mybukkit = new MyBukkit(this);
        getServer().getPluginManager().registerEvents(this, this);

        String version = Bukkit.getServer().getName().toUpperCase();

        if (version.contains("BUKKIT") || version.contains("SPIGOT")) {
            new UpdateCheckerBukkSpig(this, checkOnlineUpdate);
        } else { //Paper based?
            new UpdateChecker(this, checkOnlineUpdate);
        }

        try {
            Metrics metrics = new Metrics(this, 22590);

            metrics.addCustomChart(new Metrics.SimplePie("check_online", () -> {
                if (checkOnlineUpdate) return "true";
                return "false";
            }));

            metrics.addCustomChart(new Metrics.SimplePie("no_cooldown", () -> {
                if (noCooldown) return "true";
                return "false";
            }));

            metrics.addCustomChart(new Metrics.SimplePie("fast_respawn", () -> {
                if (fastRespawn) return "true";
                return "false";
            }));

            metrics.addCustomChart(new Metrics.SimplePie("max_cps", () -> {
                return String.valueOf(maxCPS);
            }));

            metrics.addCustomChart(new Metrics.SimplePie("special_effects", () -> {
                if (specialEffects) return "true";
                return "false";
            }));

            metrics.addCustomChart(new Metrics.SimplePie("normalize_Hacked_Items", () -> {
                if (normalizeHackedItems) return "true";
                return "false";
            }));

        } catch (Exception e) {
            getLogger().info(ChatColor.RED + " Failed to register into Bstats");
        }

        getLogger().info(VERSION + " enabled on " + version + "!");
        isInit = false;
    }

    @Override
    public void onDisable() {
        getLogger().info(VERSION + " disabled!");
    }


    public void LoadSettings() {
        FileConfiguration config = getConfig();
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            setupConfig();
        }

        try {
            checkOnlineUpdate = config.getBoolean("checkUpdate", true);
            specialEffects = config.getBoolean("specialEffects", false);
            normalizeHackedItems = config.getBoolean("normalizeHackedItems", false);

            maxCPS = config.getInt("maxCPS", 16);
            noCooldown = config.getBoolean("noCooldown", false);
            fastRespawn = config.getBoolean("fastRespawn", false);
        } catch (Exception e) {
            maxCPS = 16;
        }
    }


    private void setupConfig() {
        FileConfiguration config = getConfig();
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) dataFolder.mkdir();

        config.options().header("==== PVPOneDotEight Configs ====");
        config.addDefault("checkUpdate", true);
        config.addDefault("specialEffects", false);
        config.addDefault("normalizeHackedItems", false);

        config.options().copyDefaults(true);
        saveConfig();
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() == 4) {
            instance.setBaseValue(maxCPS);
        }

        if (normalizeHackedItems) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getEnchantments() != null) {

                    for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                        int level = entry.getValue();

                        if (level >= 6) {
                            item.removeEnchantment(entry.getKey());
                            break;
                        }
                    }
                }
            }
        }
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

        //with delay checks current selected item...
        mybukkit.runTaskLater(player, null, null, () -> {
                    ItemStack it = player.getInventory().getItemInMainHand();
                    String weapon = it.getType().toString();

                    if ((weapon.contains("SWORD") && it.containsEnchantment(Enchantment.SWEEPING_EDGE))) {

                        int level = it.getEnchantments().get(Enchantment.SWEEPING_EDGE).intValue();

                        it.removeEnchantment(Enchantment.SWEEPING_EDGE);

                        Repairable newSword = (Repairable) it.getItemMeta();

                        int repairCost = newSword.getRepairCost();
                        //small compensation for previous versions
                        if (repairCost > 120) repairCost = (repairCost / 2);

                        newSword.setRepairCost((repairCost / 2));
                        it.setItemMeta(newSword);

                        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
                        EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) book.getItemMeta();
                        bookmeta.addStoredEnchant(Enchantment.SWEEPING_EDGE, level, true);
                        book.setItemMeta(bookmeta);

                        player.getInventory().addItem(book);
                    }
                }
                , 5);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityHit(EntityDamageByEntityEvent event) {

        if (noCooldown) {

            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

                Player attacker = (Player) event.getDamager();
                float cooldown = attacker.getAttackCooldown();

                if (cooldown < 1.0) {
                    event.setDamage(event.getDamage() * (1 / cooldown));
                }

            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (specialEffects) {
            mybukkit.runTaskLater(player, null, null, () -> {
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.getWorld().spawnParticle(Particle.GLOW, player.getLocation(), 10, 0.0, 0.0, 1);
            }, 1);
        }

        if (fastRespawn) fastSpawn(player);
    }


    private void fastSpawn(Player player) {

        mybukkit.runTaskLater(player, null, null, () -> {

            if (!player.isOnline()) return;

            if (specialEffects) player.getWorld().strikeLightningEffect(player.getRespawnLocation());
            player.spigot().respawn();

        }, 40L);
    }


}
