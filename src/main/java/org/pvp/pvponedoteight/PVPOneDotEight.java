package org.pvp.pvponedoteight;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.pvp.pvponedoteight.GameUtils.ArenaConfig;
import org.pvp.pvponedoteight.GameUtils.MyPlayer;
import org.pvp.pvponedoteight.Utils.Metrics;
import org.pvp.pvponedoteight.Utils.MyBukkit;
import org.pvp.pvponedoteight.Utils.PVPPlaceholderHandler;
import org.pvp.pvponedoteight.Utils.Utils;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;


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
    private boolean autoSetShield;
    private boolean controlEntitiesInArena;
    private boolean protectArena;
    private String workingWorld;
    private LinkedList<ArenaConfig> myArenas = new LinkedList();
    private Hashtable<UUID, MyPlayer> playersHash = new Hashtable();

    @Override
    public void onEnable() {

        mybukkit = new MyBukkit(this);
        LoadSettings();
        getServer().getPluginManager().registerEvents(this, this);

        if (Utils.checkGreater("1.21.1", Bukkit.getServer().getBukkitVersion()) == -1) {
            getLogger().severe(" You are using a Minecraft Server version with possible data loss and known exploits, get informed and evaluate updating to 1.21.1");
        }

        String version = Bukkit.getServer().getName().toUpperCase();

        mybukkit.UpdateChecker(true);

        startMetrics();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new PVPPlaceholderHandler(this).register(); //
        } else {
            getLogger().info("Could not find PlaceholderAPI!");
        }

        getLogger().info("Version " + VERSION + " enabled on " + version + "!");
        isInit = false;
    }

    @Override
    public void onDisable() {

        ArenaConfig arena;

        for (int k = 0; k < myArenas.size(); k++) {
            arena = myArenas.get(k);

            arena.terminateArena();
        }

        getLogger().info(VERSION + " disabled!");
    }


    public void LoadSettings() {
        FileConfiguration config = getConfig();

        setupConfig();

        try {
            workingWorld = config.getString("workingWorld", "");
            checkOnlineUpdate = config.getBoolean("checkUpdate", true);
            specialEffects = config.getBoolean("specialEffects", false);
            normalizeHackedItems = config.getBoolean("normalizeHackedItems", false);
            noCooldown = config.getBoolean("noCooldown", true);
            autoSetShield = config.getBoolean("autoSetShield", true);
            controlEntitiesInArena = config.getBoolean("controlEntitiesInArena", false);
            protectArena = config.getBoolean("protectArena", false);

            maxCPS = config.getInt("maxCPS", 16);
            fastRespawn = config.getBoolean("fastRespawn", false);
        } catch (Exception e) {
            maxCPS = 16;
        }

        try {
            ArenaConfig arena;

            for (int k = 0; k < 100; k++) {

                String[] array = new String[7];
                array[0] = config.getString("Arenas.Arena" + k + ".a");

                if (array[0] == null) break;

                array[1] = config.getString("Arenas.Arena" + k + ".b");
                array[2] = config.getString("Arenas.Arena" + k + ".c");
                array[3] = config.getString("Arenas.Arena" + k + ".d");
                array[4] = config.getString("Arenas.Arena" + k + ".e");
                array[5] = config.getString("Arenas.Arena" + k + ".f");
                array[6] = config.getString("Arenas.Arena" + k + ".g");

                arena = new ArenaConfig(this);
                arena.setDimensions(array);

                myArenas.add(arena);
            }
        } catch (Exception e) {
            System.out.println("Arenas. excep  " + e.getMessage());
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
        config.addDefault("autoSetShield", true);
        config.addDefault("controlEntitiesInArena", false);
        config.addDefault("protectArena", false);

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
        boolean newPVP = false;

        if (myArenas.size() == 0) {
            String world = player.getWorld().getName();
            if (!workingWorld.equals("") && !world.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(player.getUniqueId());

            if (myPlayer == null) {
                myPlayer = new MyPlayer();
                playersHash.put(player.getUniqueId(), myPlayer);
            }

            ArenaConfig arena = isInArena(player.getLocation());

            if (arena == null) {
                return;
            } else {
                myPlayer.UpdateArena(arena);
                newPVP = arena.isNewPvpMode();
            }
        }

        preparePlayer(player, newPVP);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        //exiting world
        if (myArenas.size() == 0) {
            String world = player.getWorld().getName();

            if (!workingWorld.equals("") && !world.equals(workingWorld)) {
                resetPlayer(player);

            } else if (!workingWorld.equals("") && world.equals(workingWorld)) { //entering world
                preparePlayer(player, false);
            }
        } else {
            MyPlayer myPlayer = playersHash.get(player.getUniqueId());

            ArenaConfig arena = isInArena(player.getLocation());

            if (arena == null) {
                myPlayer.UpdateArena(null);
                resetPlayer(player);
            } else {
                myPlayer.UpdateArena(arena);
                preparePlayer(player, arena.isNewPvpMode());
            }
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

        if (myArenas.size() == 0) {
            if (!workingWorld.equals("") && !myWorld.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(event.getPlayer().getUniqueId());

            if (myPlayer == null) return;

            //only for 1.8
            if (!myPlayer.isInPVPArena() || myPlayer.isNewPVPArena()) {
                return;
            }
        }

        if (event.getEntityType() == EntityType.END_CRYSTAL && !(myWorld.endsWith("_nether") || myWorld.endsWith("_end"))) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onSelect(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (myArenas.size() == 0) {
            String world = player.getWorld().getName();
            if (!workingWorld.equals("") && !world.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(player.getUniqueId());

            if (myPlayer == null) return;

            //for 1.8 only
            if (!myPlayer.isInPVPArena() || myPlayer.isNewPVPArena()) {
                return;
            }
        }
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

        if (myArenas.size() == 0) {
            String world = player.getWorld().getName();
            if (!workingWorld.equals("") && !world.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(player.getUniqueId());

            if (myPlayer == null) return;

            if (!myPlayer.isInPVPArena() || myPlayer.isNewPVPArena()) {
                return;
            }
        }

        mybukkit.runTaskLater(player, null, null, () -> {
                    filterPlayerInventory(player);
                }
                , 5);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityHit(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            MyPlayer myPlayer = null;

            if (myArenas.size() == 0) {
                String world = attacker.getWorld().getName();
                if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

            } else {
                myPlayer = playersHash.get(attacker.getUniqueId());

                if (myPlayer == null) return;

                //because of cooldown on 1.8
                if (myPlayer.isInPVPArena() && myPlayer.isNewPVPArena()) {
                    return;
                }
            }

            if (myPlayer == null) return;

            //victim
            if (event.getEntity() instanceof Player) {

                if (!myPlayer.isInPVPArena()) {
                    event.setCancelled(true);
                    return;
                }

                if (!myPlayer.isPlayingPVP()) {
                    event.setCancelled(true);
                    return;
                }

                if (myArenas.size() > 0) {
                    MyPlayer myPlayer2 = playersHash.get(event.getEntity().getUniqueId());

                    if (!myPlayer2.isPlayingPVP()) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (noCooldown) {
                    float cooldown = attacker.getAttackCooldown();

                    if (cooldown < 1.0) {
                        event.setDamage(event.getDamage() * (1 / cooldown));
                    }
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Entity myEntity = event.getEntity();
        if (!(myEntity instanceof Player)) return;

        Player victim = (Player) myEntity;

        if (myArenas.size() == 0) {
            String world = victim.getWorld().getName();
            if (!workingWorld.equals("") && !world.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(victim.getUniqueId());

            if (myPlayer == null) return;

            if (!myPlayer.isInPVPArena()) {
                return;
            }

            myPlayer.setDeaths();

            Entity killer = victim.getKiller();

            if (killer instanceof Player) {
                MyPlayer myPlayer2 = playersHash.get(killer.getUniqueId());

                if (!myPlayer2.isInPVPArena()) {
                    event.setCancelled(true);
                    return;
                }

                if (myPlayer2 != null) myPlayer2.setKills();
            }
        }

        if (specialEffects) {
            mybukkit.runTaskLater(victim, null, null, () -> {
                victim.getWorld().strikeLightningEffect(victim.getLocation());
                victim.getWorld().spawnParticle(Particle.GLOW, victim.getLocation(), 10, 0.0, 0.0, 1);
            }, 1);
        }

        if (fastRespawn) fastSpawn(victim);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {

        if (myArenas.size() == 0) {
            String world = event.getPlayer().getWorld().getName();
            if (!workingWorld.equals("") && !world.equals(workingWorld)) return;
        } else {
            MyPlayer myPlayer = playersHash.get(event.getPlayer().getUniqueId());

            if (myPlayer == null) return;

            //for 1.8 only
            if (!myPlayer.isInPVPArena() || myPlayer.isNewPVPArena()) {
                return;
            }
        }

        if (event.getItem().getType() == Material.CHORUS_FRUIT || event.getItem().getType() == Material.SUSPICIOUS_STEW || event.getItem().getType() == Material.BEETROOT_SOUP) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onRodHit(ProjectileHitEvent event) {

        if (event.getEntityType() == EntityType.FISHING_BOBBER) {
            Entity hookEntity = event.getEntity();
            ProjectileSource fisher = ((FishHook) hookEntity).getShooter();

            if (fisher instanceof Player) {
                Player player = (Player) fisher;

                if (myArenas.size() == 0) {
                    String world = hookEntity.getWorld().getName();
                    if (!workingWorld.equals("") && !world.equals(workingWorld)) return;

                } else {
                    MyPlayer myPlayer = playersHash.get(player.getUniqueId());

                    if (myPlayer == null) return;

                    //for 1.8
                    if (!myPlayer.isInPVPArena() || myPlayer.isNewPVPArena()) {
                        return;
                    }
                }

                Entity defender = event.getHitEntity();

                if (defender != null) { // && event.getHitEntity() instanceof Player
                    defender.setVelocity(calculateKnockBackVelocity(defender.getVelocity(), hookEntity.getVelocity()));
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(@NotNull PlayerMoveEvent event) {

        //not Arenas mode
        if (myArenas.size() == 0) return;

        Player player = event.getPlayer();
        MyPlayer myPlayer = playersHash.get(player.getUniqueId());

        if (myPlayer == null || !myPlayer.isPlayingPVP() || !player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        ArenaConfig arena = isInArena(player.getLocation());

        if (arena == null && myPlayer.isInPVPArena()) {
            player.sendMessage("Exiting Arena!");
            myPlayer.UpdateArena(null);
            resetPlayer(player);
        } else {
            boolean newArena = myPlayer.UpdateArena(arena);

            if (newArena) {
                player.sendMessage("In Arena with oldPVP: " + (!arena.isNewPvpMode() ? ChatColor.GREEN : ChatColor.RED) + !arena.isNewPvpMode());
                preparePlayer(player, arena.isNewPvpMode());
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawnEvent(@NotNull EntitySpawnEvent event) {

        if (!controlEntitiesInArena) return;

        if (myArenas.size() == 0) return;

        Entity ent = event.getEntity();

        if (ent instanceof LivingEntity) {
            ArenaConfig arena = isInArena(ent.getLocation());
            if (arena != null) event.setCancelled(true);
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void onProcessBlockBreakEvent(BlockBreakEvent e) {

        if (!protectArena) return;

        Player player = e.getPlayer();
        ArenaConfig arena = isInArena(player.getLocation());

        if (arena != null) e.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {

        if (!protectArena) return;

        Player player = e.getPlayer();
        ArenaConfig arena = isInArena(player.getLocation());

        if (arena != null) e.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onArenaClick(PlayerInteractEvent event) {

        Action a = event.getAction();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if (block == null || item == null) return;

        Player player = event.getPlayer();
        Location loc = block.getLocation();
        ArenaConfig arena = null;

        if (!player.isOp()) return;

        if (a.equals(Action.RIGHT_CLICK_BLOCK) && item.getType().equals(Material.BONE)) {

            for (int k = 0; k < myArenas.size(); k++) {
                arena = myArenas.get(k);

                if (arena.isInArena(loc) && arena.isArenaEditing() && (System.currentTimeMillis() - arena.lastClick < 5000)) {

                    FileConfiguration config = getConfig();

                    for (int delete = 0; delete < myArenas.size(); delete++) {
                        config.set("Arenas.Arena" + delete + ".a", null);
                        config.set("Arenas.Arena" + delete + ".b", null);
                        config.set("Arenas.Arena" + delete + ".c", null);
                        config.set("Arenas.Arena" + delete + ".d", null);
                        config.set("Arenas.Arena" + delete + ".e", null);
                        config.set("Arenas.Arena" + delete + ".f", null);
                        config.set("Arenas.Arena" + delete + ".g", null);
                    }

                    myArenas.remove(arena);
                    arena.endArenaEditing(player, true);
                    arena = null;

                    for (int update = 0; update < myArenas.size(); update++) {
                        arena = myArenas.get(update);

                        String[] list = arena.getDimensions();

                        config.set("Arenas.Arena" + update + ".a", list[0]);
                        config.set("Arenas.Arena" + update + ".b", list[1]);
                        config.set("Arenas.Arena" + update + ".c", list[2]);
                        config.set("Arenas.Arena" + update + ".d", list[3]);
                        config.set("Arenas.Arena" + update + ".e", list[4]);
                        config.set("Arenas.Arena" + update + ".f", list[5]);
                        config.set("Arenas.Arena" + update + ".g", list[6]);
                    }

                    config.options().copyDefaults(true);
                    saveConfig();

                    player.sendMessage("Arena deleted!");

                    return;
                } else if (arena.isInArena(loc)) {
                    arena.enableArenaEditing(player);
                    arena.lastClick = System.currentTimeMillis();
                    player.sendMessage("Click again to delete (in less then 5 secs)");
                }
            }

        } else if (a.equals(Action.LEFT_CLICK_BLOCK) && item.getType().equals(Material.ARROW)) {

            for (int k = 0; k < myArenas.size(); k++) {
                arena = myArenas.get(k);

                if (arena.isInArena(loc) && arena.isArenaEditing()) {
                    arena.endArenaEditing(player, false);
                    event.setCancelled(true);

                    //  player.sendMessage("End arena editing");

                    FileConfiguration config = getConfig();

                    String[] list = arena.getDimensions();

                    config.set("Arenas.Arena" + k + ".a", list[0]);
                    config.set("Arenas.Arena" + k + ".b", list[1]);
                    config.set("Arenas.Arena" + k + ".c", list[2]);
                    config.set("Arenas.Arena" + k + ".d", list[3]);
                    config.set("Arenas.Arena" + k + ".e", list[4]);
                    config.set("Arenas.Arena" + k + ".f", list[5]);
                    config.set("Arenas.Arena" + k + ".g", list[6]);

                    config.options().copyDefaults(true);
                    saveConfig();
                }
            }

        } else if (a.equals(Action.RIGHT_CLICK_BLOCK) && item.getType().equals(Material.ARROW)) {

            for (int k = 0; k < myArenas.size(); k++) {
                arena = myArenas.get(k);

                if (arena.isNearArena(loc)) {

                    if (arena.isArenaFinished() && !arena.isVisible()) {
                        //     player.sendMessage("enable arena editing");
                        arena.enableArenaEditing(player);
                    } else {
                        //   player.sendMessage("in arena editing");
                        boolean ok = arena.SetupArena(loc, player);

                        if (!ok) player.sendMessage("Invalid finished position, too small");
                    }

                    return;
                } else {
                    player.sendMessage("You are too far from start position");
                }
            }

            if (arena != null && arena.isArenaEditing()) return;

            if (arena == null || arena.isArenaFinished() && !arena.isNearArena(loc)) {
                //  player.sendMessage("cria " + (arena == null));
                arena = new ArenaConfig(this);
                arena.SetupArena(loc, player);
                myArenas.add(arena);
                return;
            }

        }

    }


    private ArenaConfig isInArena(Location loc) {

        ArenaConfig arena;

        for (int k = 0; k < myArenas.size(); k++) {
            arena = myArenas.get(k);

            if (arena.isInArena(loc)) {
                return arena;
            }
        }

        return null;
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


    private void preparePlayer(Player player, boolean newPVP) {

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

        if (newPVP) return;

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

        if (autoSetShield) {
            //if player already has a shield in inv, move it to off hand
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


    public Hashtable<UUID, MyPlayer> getPlayersHash() {
        return playersHash;
    }
}
