package org.pvp.pvponedoteight.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pvp.pvponedoteight.PVPOneDotEight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final PVPOneDotEight main;
    private final String projectName;
    private String features = "";
    private boolean isInformed = false;
    private boolean starting = true;

    public UpdateChecker(PVPOneDotEight main, boolean loop) {
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
                features = tmp.substring(tmp.indexOf("features=\"") + 10).split("\"")[0];

                promptUpdate(version, url);
            }
        } catch (Exception e) {
            Bukkit.getLogger().info("[" + projectName + "] Connection exception: " + e.getMessage());

            Bukkit.getConsoleSender().sendMessage(net.kyori.adventure.text.Component
                    .text("[" + projectName + "] Connection exception: " + e.getMessage())
                    .color(net.kyori.adventure.text.format.NamedTextColor.RED)
            );
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
        TextComponent component;

        if (serverVersion == null) {
            component = Component.text(" Unknown error checking version");

            Bukkit.getConsoleSender().sendMessage(Component
                    .text("[" + projectName + "]")
                    .color(NamedTextColor.RED)
                    .append(component)
            );

            return;
        }

        String tmpServerVersion = null;
        if (serverVersion.split(" v").length > 1) tmpServerVersion = serverVersion.split(" v")[1];
        if (tmpServerVersion == null) tmpServerVersion = serverVersion.split(" ")[1];
        serverVersion = tmpServerVersion;

        String currentVersion = main.getDescription().getVersion();
        int versionStatus = Utils.checkGreater(serverVersion, currentVersion);
        NamedTextColor color = NamedTextColor.GRAY;

        if (versionStatus == -1) {

            if (features.length() > 1) {
                features = "\nFeaturing: " + features + "\n";
            } else {
                features = "";
            }

            component = Component.text("[" + projectName + "] " + " NEW VERSION: " + serverVersion + features +
                    "Available at: " + Url, NamedTextColor.GREEN);

            if (!isInformed) {
                for (Player myPlayer : main.getServer().getOnlinePlayers()) {
                    if (myPlayer.isOp()) {
                        myPlayer.sendMessage(component);
                        isInformed = true;
                    }
                }
            }

            component = Component.text(" NEW VERSION AVAILABLE: " + serverVersion +
                    " available at: " + Url, NamedTextColor.GREEN);

        } else if (versionStatus == 0) {

            if (!starting) return;
            component = Component.text(" You have the latest released version!", NamedTextColor.GREEN);

        } else if (versionStatus == 1) {

            if (!starting) return;
            component = Component.text(" Congrats, you are testing a new version!", NamedTextColor.YELLOW);

        } else {

            if (!starting) return;
            component = Component.text(" Unknown error checking version (" + versionStatus + ")" + serverVersion + "   " + currentVersion, NamedTextColor.RED);
        }

        Bukkit.getConsoleSender().sendMessage(Component
                .text("[" + projectName + "]")
                .color(color)
                .append(component)
        );

        starting=false;
    }

}