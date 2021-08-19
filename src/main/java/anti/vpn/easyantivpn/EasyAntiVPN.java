package anti.vpn.easyantivpn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

public final class EasyAntiVPN extends JavaPlugin implements Listener {

    public static JsonNode get_api_response(String urlString, String methods) {
        String result = "";
        JsonNode root = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(methods);
            connection.connect(); // URL接続
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String temp;
            while ((temp = in.readLine()) != null) {
                result += temp;
            }
            ObjectMapper mapper = new ObjectMapper();
            root = mapper.readTree(result);
            in.close();
            connection.disconnect();
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        return root;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("EasyAntiVPN enabled");
    }

    @Override
    public void onDisable() {
        getLogger().warning("EasyAntiVPN disabled");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Player join_player = event.getPlayer();
            InetAddress ip = Objects.requireNonNull(join_player.getAddress()).getAddress();
            JsonNode response = get_api_response("http://ip-api.com/json/" + ip + "?fields=proxy", "GET");
            JsonNode proxy_result_json = mapper.readTree(String.valueOf(response));
            boolean proxy_result = proxy_result_json.get("proxy").booleanValue();
            if(proxy_result) {
                join_player.kickPlayer("[EasyAntiVPN]\nVPNが検知されたため参加がブロックされました");
                getLogger().warning(join_player.getName() + "さんからVPNが検知されました");
            } else {
                getLogger().warning(join_player.getName() + "さんからVPNは検出されませんでした");
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }
}