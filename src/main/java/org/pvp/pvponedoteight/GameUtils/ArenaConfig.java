package org.pvp.pvponedoteight.GameUtils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.pvp.pvponedoteight.PVPOneDotEight;


public class ArenaConfig {

    private Location iniLocal;
    private Boolean pending = true;
    private Boolean visible = false;
    private boolean editing = false;
    public long lastClick = 0;

    private int x1, y1, z1, largx, largz;
    private String world;
    private boolean pvpOldMode = true;
    private ArmorStand armor;
    private PVPOneDotEight myPvp;

    private Location baseLocation, location2, location3, location4;


    public ArenaConfig(PVPOneDotEight myPvp) {
        this.myPvp = myPvp;
    }


    public boolean SetupArena(Location local, Player player) {

        if (iniLocal == null) {
            iniLocal = local;
            x1 = iniLocal.getBlockX();
            z1 = iniLocal.getBlockZ();
            world = local.getWorld().getName();
            player.sendBlockChange(local, Material.GLOWSTONE.createBlockData());
            player.sendMessage(Component.text("Right click again on the opposite border"));
            return true;
        }

        if (pending || editing) {

            if (editing) {
                Location mostFar;

                double d1 = local.distance(baseLocation);
                double d2 = local.distance(location2);
                double d3 = local.distance(location3);
                double d4 = local.distance(location4);

                d1 = Math.floor(d1 * 100) / 100d;
                d2 = Math.floor(d2 * 100) / 100d;
                d3 = Math.floor(d3 * 100) / 100d;
                d4 = Math.floor(d4 * 100) / 100d;

                // player.sendMessage(" pressed " + local.getBlockX() + ":" + local.getBlockZ());
                //  player.sendMessage("D1: " + d1 + " " +baseLocation.getBlockX() + ":" + baseLocation.getBlockZ() + " D2: " + d2 + " " +location2.getBlockX() + ":" + location2.getBlockZ() + " D3: " + d3 + " " +location3.getBlockX() + ":" + location3.getBlockZ() +" D4: " + d4 + " " +location4.getBlockX() + ":" + location4.getBlockZ() );

                if (d1 > d2) {
                    if (d1 > d3) {
                        if (d1 > d4) {
                            mostFar = baseLocation;
                        } else {
                            mostFar = location4;
                        }
                    } else {
                        if (d3 > d4) {
                            mostFar = location3;
                        } else {
                            mostFar = location4;
                        }
                    }
                } else {
                    if (d2 > d3) {
                        if (d2 > d4) {
                            mostFar = location2;
                        } else {
                            mostFar = location4;
                        }
                    } else {
                        if (d3 > d4) {
                            mostFar = location3;
                        } else {
                            mostFar = location4;
                        }
                    }
                }

                if (mostFar.distance(local) <= 8) return false;

                iniLocal = mostFar;
            } else {
                if (Math.abs(local.getBlockX() - x1) <= 6) return false;
                if (Math.abs(local.getBlockZ() - z1) <= 6) return false;
            }

            if (visible) drawLimits(player, true); //cleans what exists before

            Location endLocal = local;
            int tmpx1, tmpx2, tmpz1, tmpz2;

            tmpx1 = iniLocal.getBlockX();
            tmpz1 = iniLocal.getBlockZ();

            tmpx2 = endLocal.getBlockX();
            tmpz2 = endLocal.getBlockZ();

            if (tmpx1 < tmpx2) {
                x1 = tmpx1;
                largx = tmpx2 - tmpx1;
                y1 = iniLocal.getBlockY();
            } else {
                x1 = tmpx2;
                largx = tmpx1 - tmpx2;
                y1 = endLocal.getBlockY();
            }

            if (tmpz1 < tmpz2) {
                z1 = tmpz1;
                largz = tmpz2 - tmpz1;
            } else {
                z1 = tmpz2;
                largz = tmpz1 - tmpz2;
            }

            baseLocation = new Location(Bukkit.getWorld(world), x1, y1, z1);
            location2 = new Location(Bukkit.getWorld(world), (x1 + largx), y1, z1);
            location3 = new Location(Bukkit.getWorld(world), (x1 + largx), y1, (z1 + largz));
            location4 = new Location(Bukkit.getWorld(world), x1, y1, (z1 + largz));

            //      player.sendMessage(x1 + ":" + z1 + "   " + (x1 + largx) + ":" + z1 + "   " + (x1 + largx) + ":" + (z1 + largz) + "   " + x1 + ":" + (z1 + largz) + "  lx " + largx + "   lz " + largz);

            pending = false;
            editing = true;

            player.sendMessage(Component.text("Left click to save or right click to adjust"));
            drawLimits(player, false);
        }

        return true;
    }


    public void terminateArena() {
        showArenaLabel("", false);
    }


    public Boolean isArenaFinished() {
        return !pending;
    }


    public Boolean isArenaEditing() {
        return editing;
    }


    public void enableArenaEditing(Player player) {
        editing = true;
        drawLimits(player, false);

        showArenaLabel("Editing Arena", false);
    }


    public void endArenaEditing(Player player) {
        if (editing) {
            editing = false;
            drawLimits(player, true);

            showArenaLabel(ChatColor.BLACK + "Arena for oldPVP: " + (pvpOldMode ? ChatColor.GREEN : ChatColor.RED) + pvpOldMode, true);
        }
    }


    public Boolean isVisible() {
        return visible;
    }


    public Boolean isNewPvp() {
        return !pvpOldMode;
    }


    public Boolean isInArena(Location local) {

        int x3 = local.getBlockX();
        int z3 = local.getBlockZ();

        if (x3 >= x1 && x3 <= (x1 + largx)) {
            if (z3 >= z1 && z3 <= (z1 + largz)) {
                return true;
            }
        }

        return false;
    }


    public Boolean isNearArena(Location local) {

        int x3 = local.getBlockX();
        int z3 = local.getBlockZ();

        if (x3 >= (x1 - 30) && x3 <= (x1 + largx + 30)) {
            if (z3 >= (z1 - 30) && z3 <= (z1 + largz + 30)) {
                return true;
            }
        }

        return false;
    }


    public String[] getDimensions() {
        String[] array = new String[7];

        array[0] = "" + x1;
        array[1] = "" + y1;
        array[2] = "" + z1;
        array[3] = "" + largx;
        array[4] = "" + largz;
        array[5] = world;
        array[6] = "" + pvpOldMode;

        return array;
    }


    public void setDimensions(String[] array) {

        x1 = Integer.parseInt(array[0]);
        y1 = Integer.parseInt(array[1]);
        z1 = Integer.parseInt(array[2]);
        largx = Integer.parseInt(array[3]);
        largz = Integer.parseInt(array[4]);
        world = array[5];
        pvpOldMode = Boolean.parseBoolean(array[6]);

        baseLocation = new Location(Bukkit.getWorld(world), x1, y1, z1);
        location2 = new Location(Bukkit.getWorld(world), (x1 + largx), y1, z1);
        location3 = new Location(Bukkit.getWorld(world), (x1 + largx), y1, (z1 + largz));
        location4 = new Location(Bukkit.getWorld(world), x1, y1, (z1 + largz));
        iniLocal = baseLocation.clone();

        pending = false;
        editing = false;
        visible = false;

        showArenaLabel(ChatColor.BLACK + "Arena with oldPVP: " + (pvpOldMode ? ChatColor.GREEN : ChatColor.RED) + pvpOldMode, true);
    }


    private void drawLimits(Player player, boolean hideArena) {

        if (!isArenaFinished()) {
            player.sendMessage("not yet finished!");
            return;
        }

        long counter = System.nanoTime();

        if (!hideArena) {
            if (counter - lastClick < 250000000) {

//                TextComponent textComponent = Component.text("Click me");
//
//                textComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/say hi"));
//                textComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, textComponent));
//
//                player.sendMessage(textComponent);

                pvpOldMode = !pvpOldMode;
            }
            lastClick = counter;
        }

        this.visible = !hideArena;

        int xLimit = largx;
        int zLimit = largz;

        Location loc = baseLocation.clone();
        Location aux;

        // player.sendMessage(y1 + " y1    " + hideArena + " self e desenha terminado " + !pending + "   " + xLimit + "    " + zLimit);

        for (int x = 0; x <= xLimit; x++) {

            for (int z = 0; z <= zLimit; z++) {
                loc.set(x1, y1, z1).add(x, 0, z);
                aux = loc.clone();
                aux.add(0, 1, 0);

                //y self adjustment
                for (int y = 0; y < 7; y++) {
                    //just ok
                    if (loc.getBlock().isSolid() && aux.getBlock().isEmpty()) {
                        break;
                    } else if (loc.getBlock().isSolid() && aux.getBlock().isSolid()) { //up up
                        //    player.sendMessage(x + ":" +z + " sobe "+loc.getBlockY() + " " + aux.getBlockY() + "   " + loc.getBlock() + "   " + aux.getBlock());
                        loc.add(0, 1, 0);
                        aux.add(0, 1, 0);
                    } else if (loc.getBlock().isEmpty() && aux.getBlock().isEmpty()) { //down boy
                        //     player.sendMessage(x + ":" +z + " desc "+loc.getBlockY() + " " + aux.getBlockY() + "   " + loc.getBlock().getType() + "   " + aux.getBlock().getType());
                        loc.add(0, -1, 0);
                        aux.add(0, -1, 0);
                    }
                }

                y1 = loc.getBlockY(); //to save future loops

                if (x == 0 && z == 0 || x == 0 && z == zLimit || z == 0 && x == xLimit || x == xLimit && z == zLimit) {

                    if (hideArena) player.sendBlockChange(loc, loc.getBlock().getBlockData());
                    else if (!hideArena && !pvpOldMode)
                        player.sendBlockChange(loc, Material.SHROOMLIGHT.createBlockData());
                    else player.sendBlockChange(loc, Material.SEA_LANTERN.createBlockData());

                } else if (x == 0 || x == xLimit || z == 0 || z == zLimit) {

                    if (hideArena) player.sendBlockChange(loc, loc.getBlock().getBlockData());
                    else if (!hideArena && !pvpOldMode)
                        player.sendBlockChange(loc, Material.NETHERITE_BLOCK.createBlockData());
                    else player.sendBlockChange(loc, Material.IRON_BLOCK.createBlockData());

                } else {
                    //   player.sendBlockChange(loc, Material.NETHERITE_BLOCK.createBlockData());
                }
            }
        }
        // player.sendMessage("time " + (System.nanoTime() - counter) + "ns for " + (xLimit * zLimit) + " blocks");
    }


    private void showArenaLabel(String message, boolean visible) {

        if (armor == null && visible) {
            Location myLocation = new Location(Bukkit.getWorld(world), Math.abs((x1 + largx / 2)), y1, z1);

            myPvp.mybukkit.runTaskLater(null, myLocation, null, () -> {
                armor = (ArmorStand) myLocation.getWorld().spawnEntity(myLocation, EntityType.ARMOR_STAND);
                armor.setVisible(false);

                String key;
                AttributeInstance instance;

                key = Attribute.GENERIC_SCALE.name();
                instance = armor.getAttribute(Attribute.valueOf(key));

                if (instance != null) {
                    instance.setBaseValue(2);
                }

                armor.customName(Component.text(message));
                armor.setCustomNameVisible(true);
            }, 5);

            return;
        }

        if (visible) {
            armor.customName(Component.text(message));
            armor.setCustomNameVisible(true);
        } else {
            if (armor != null) {
                armor.remove();
                armor = null;
            }
        }
    }


}
