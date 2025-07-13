package org.pvp.pvponedoteight.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pvp.pvponedoteight.GameUtils.MyPlayer;
import org.pvp.pvponedoteight.PVPOneDotEight;

import java.util.UUID;


public class PVPPlaceholderHandler extends PlaceholderExpansion {

    private PVPOneDotEight plugin;

    public PVPPlaceholderHandler(PVPOneDotEight plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "PVPOneDotEight";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }


    public String onPlaceholderRequest(Player player, @NotNull String params) {

        String returnString = "N/S";

        MyPlayer myPlayer = null;
        if (player != null) plugin.getPlayersHash().get(player.getUniqueId());

        try {
            switch (params.toLowerCase()) {

                case "players_in_arena" -> returnString = String.valueOf(playersInInArena());
                case "player_in_arena" -> returnString = isInArena(player);
                case "player_kill_count" -> returnString = myPlayer == null ? "N/A" : String.valueOf(myPlayer.getKills());
                case "player_death_count" -> returnString= myPlayer == null ? "N/A" : String.valueOf(myPlayer.getDeaths());

            }

        } catch (Exception e) {
            returnString = "Exception, plz report";
        }

        return returnString;
    }

    private String isInArena(Player player) {
        MyPlayer myPlayer = plugin.getPlayersHash().get(player.getUniqueId());

        if (myPlayer == null) return "0";

        if (myPlayer.isInPVPArena()) {
            return "1";
        }

        return "0";
    }

    private int playersInInArena() {
        int counter = 0;

        for (UUID entry : plugin.getPlayersHash().keySet()) {
            if (plugin.getPlayersHash().get(entry).isInPVPArena()) {
                counter++;
            }
        }

        return counter;
    }

}
