package org.pvp.pvponedoteight.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.pvp.pvponedoteight.PVPOneDotEight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckerBukkSpig {

    private final PVPOneDotEight main;
    private final String projectName;
    private boolean isInformed = false;
    private boolean starting = true;

    public UpdateCheckerBukkSpig(PVPOneDotEight main, boolean loop) {
        this.main = main;
        projectName = main.getDescription().getName();

        if (loop) {
            main.mybukkit.runTaskTimer(null, null, null, () -> {
                run();
            }, 5, (120 * 60 * 20));
        } else {
            run();
        }
    }

    public void run() {
        try {
            StringBuilder page = makeAsyncGetRequest("https://cld.pt/dl/download/51c19f75-8900-49f2-8e1b-a92256bf2d4a/bukkit.txt?download=true/");

            if (page != null && page.length() > 10) {
                String pagina = page.toString();
                int pointer = pagina.indexOf("project-file-name-container-" + projectName);
                pagina = pagina.substring(pointer); //smaller data

                String tmp = pagina.substring(pagina.indexOf("https://cdn.modrinth.com/"));
                String version = tmp.substring(tmp.indexOf("data-name=\"") + 11).split("\"")[0];
                String url = tmp.split("\"")[0];

                promptUpdate(version, url);
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.RED + "Connection exception: " + e.getMessage());
        }
    }

    private StringBuilder makeAsyncGetRequest(String url) {
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    response.append(line);
                }
            }
        } catch (Exception ex) {
        }
        return response;
    }

    private void promptUpdate(String serverVersion, String Url) {

        if (serverVersion == null) {
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.RED + "Unknown error checking version");
            return;
        }

        String tmpServerVersion = null;
        if (serverVersion.split(" v").length > 1) tmpServerVersion = serverVersion.split(" v")[1];
        if (tmpServerVersion == null) tmpServerVersion = serverVersion.split(" ")[1];
        serverVersion = tmpServerVersion;

        String currentVersion = main.getDescription().getVersion();
        int versionStatus = Utils.checkGreater(serverVersion, currentVersion);

        if (versionStatus == -1) {
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.GREEN + "NEW VERSION: " + serverVersion +
                    " available at: " + Url);

            if (!isInformed) {
                for (Player myPlayer : main.getServer().getOnlinePlayers()) {
                    if (myPlayer.isOp()) {
                        myPlayer.sendMessage("[" + projectName + "] " + ChatColor.GREEN + " NEW VERSION: " + serverVersion +
                                " available at: " + Url);
                        isInformed = true;
                    }
                }
            }

        } else if (versionStatus == 0) {

            if (!starting) return;
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.DARK_GREEN + "You have the latest released version");

        } else if (versionStatus == 1) {

            if (!starting) return;
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.YELLOW + "Congrats, you are testing a new version!");

        } else {
            if (!starting) return;
            Bukkit.getConsoleSender().sendMessage("[" + projectName + "] " + ChatColor.RED + "Unknown error checking version (" + versionStatus + ")" + serverVersion + "   " + currentVersion);
        }

    }

}