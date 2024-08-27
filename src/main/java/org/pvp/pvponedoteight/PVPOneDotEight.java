package org.pvp.pvponedoteight;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.pvp.pvponedoteight.Utils.*;

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
    private String workingWorld;

    @Override
    public void onEnable() {

        LoadSettings();
        mybukkit = new MyBukkit(this);
        getServer().getPluginManager().registerEvents(this, this);

        if (Utils.checkGreater("1.21.1",Bukkit.getServer().getBukkitVersion()) == -1) {
            getLogger().severe(" You are using a Minecraft Server version with possible data loss and known exploits, get informed and evaluate updating to 1.21.1");
        }

        String version = Bukkit.getServer().getName().toUpperCase();

        if (version.contains("BUKKIT") || version.contains("SPIGOT")) {
            new UpdateCheckerBukkSpig(this, checkOnlineUpdate);
        } else { //Paper based?
            new UpdateChecker(this, checkOnlineUpdate);
        }

        startMetrics();

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
            workingWorld = config.getString("workingWorld", "");
            checkOnlineUpdate = config.getBoolean("checkUpdate", true);
            specialEffects = config.getBoolean("specialEffects", false);
            normalizeHackedItems = config.getBoolean("normalizeHackedItems", false);

            maxCPS = config.getInt("maxCPS", 16);
            noCooldown = config.getBoolean("noCooldown", true);
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
        config.addDefault("noCooldown", true);

        config.options().copyDefaults(true);
        saveConfig();
    }


    private void startMetrics() {
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
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        preparePlayer(player);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        //exiting world
        if (!workingWorld.equals("") && !world.equals(workingWorld)) {
            resetPlayer(player);

        } else if (!workingWorld.equals("") && world.equals(workingWorld)) { //entering world
            preparePlayer(player);
        }
        //else doesnt do anything, like before
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        resetPlayer(player);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityPlaceEvent(@NotNull EntityPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        String myWorld = location.getWorld().getName();

        if (!workingWorld.equals("") && !myWorld.equals(workingWorld)) return;

        if (event.getEntityType() == EntityType.END_CRYSTAL && !(myWorld.endsWith("_nether") || myWorld.endsWith("_end"))) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onSelect(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        //with delay checks current selected item...
        mybukkit.runTaskLater(player, null, null, () -> {
                    filterPlayerInventory(player);
                    //potions
                }
                , 5);
    }


    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String world = player.getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        mybukkit.runTaskLater(player, null, null, () -> {
                    filterPlayerInventory(player);
                }
                , 5);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityHit(EntityDamageByEntityEvent event) {
        String world = event.getDamager().getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            Player attacker = (Player) event.getDamager();

            if (noCooldown) {
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
        String world = player.getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        if (specialEffects) {
            mybukkit.runTaskLater(player, null, null, () -> {
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.getWorld().spawnParticle(Particle.GLOW, player.getLocation(), 10, 0.0, 0.0, 1);
            }, 1);
        }

        if (fastRespawn) fastSpawn(player);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {

        String world = event.getPlayer().getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        if (event.getItem().getType() == Material.CHORUS_FRUIT || event.getItem().getType() == Material.SUSPICIOUS_STEW || event.getItem().getType() == Material.BEETROOT_SOUP) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onRodHit(ProjectileHitEvent event) {
        Entity hookEntity = event.getEntity();
        String world = hookEntity.getWorld().getName();

        if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

        if (event.getEntityType() == EntityType.FISHING_BOBBER) {
            Entity defender = event.getHitEntity();

            if (((FishHook) hookEntity).getShooter() instanceof Player && defender != null) { // && event.getHitEntity() instanceof Player
                defender.setVelocity(calculateKnockBackVelocity(defender.getVelocity(), hookEntity.getVelocity()));
            }
        }
    }


    private void fastSpawn(Player player) {

        mybukkit.runTaskLater(player, null, null, () -> {

            if (!player.isOnline()) return;

            if (specialEffects) player.getWorld().strikeLightningEffect(player.getRespawnLocation());
            player.spigot().respawn();

        }, 40L);
    }


    private void filterPlayerInventory(Player player) {
        ItemStack it1 = player.getInventory().getItemInMainHand();
        ItemStack it2 = player.getInventory().getItemInOffHand();
        String item1 = it1.getType().toString();
        String item2 = it2.getType().toString();

        if ((item1.contains("SWORD") && it1.containsEnchantment(Enchantment.SWEEPING_EDGE))) {

            int level = it1.getEnchantments().get(Enchantment.SWEEPING_EDGE).intValue();

            it1.removeEnchantment(Enchantment.SWEEPING_EDGE);

            Repairable newSword = (Repairable) it1.getItemMeta();

            int repairCost = newSword.getRepairCost();
            //small compensation for previous versions
            if (repairCost > 120) repairCost = (repairCost / 2);

            newSword.setRepairCost((repairCost / 2));
            it1.setItemMeta(newSword);

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) book.getItemMeta();
            bookmeta.addStoredEnchant(Enchantment.SWEEPING_EDGE, level, true);
            book.setItemMeta(bookmeta);

            player.getInventory().addItem(book);
        }

        if (item1.contains("TOTEM")) {
            player.getEnderChest().addItem(it1);
            player.getInventory().remove(it1);
            player.sendMessage("This is PVP 1.8, Totem of Undying is not used");
        }

        if (item2.contains("TOTEM")) {
            player.getEnderChest().addItem(it2);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR, 1));
            player.sendMessage("This is PVP 1.8, Totem of Undying is not used");
        }
    }


    private void preparePlayer(Player player) {

        //make sure they don't bring crap :)
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

        //activation fast CPS
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() == 4) {
            instance.setBaseValue(maxCPS);
        }

        //set shields in off hand and remove totems
        ItemStack it = player.getInventory().getItemInOffHand();

        if (it != null) {
            String myItem = it.getType().toString();

            if ((myItem.contains("SHIELD"))) {
                return;
            }

            if (myItem.contains("TOTEM")) {
                player.getEnderChest().addItem(it);
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR, 1));
            } else {
                player.getInventory().remove(it);
            }

            //put that he/she had on off hand on any free place on normal inventary
            player.getInventory().addItem(it);
        }

        //if player salready has a shield in inv, move it to off hand
        if (player.getInventory().contains(Material.SHIELD)) {
            player.getInventory().remove(Material.SHIELD);
            ItemStack it2 = new ItemStack(Material.SHIELD, 1);
            player.getInventory().setItemInOffHand(it2);
            return;
        }

        //else give new shield on off hand
        ItemStack it2 = new ItemStack(Material.SHIELD, 1);
        player.getInventory().setItemInOffHand(it2);
    }


    private void resetPlayer(Player player) {
        //reset to defaults
        String key;
        AttributeInstance instance;

        key = Attribute.GENERIC_ATTACK_SPEED.name();
        instance = player.getAttribute(Attribute.valueOf(key));

        if (instance != null && instance.getBaseValue() != 4) {
            instance.setBaseValue(4);
        }
    }


    private Vector calculateKnockBackVelocity(Vector currentVelocity, Vector hookVelocity) {

        //adding vectors with compensation ~ mass difference
        currentVelocity.add((hookVelocity.multiply(0.45)));
        currentVelocity.add(new Vector(0, 0.45, 0)); //small knock up

        return currentVelocity;
    }

}
