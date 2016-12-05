package me.mrkirby153.utils.nms;

import org.bukkit.plugin.java.JavaPlugin;

public class NMSFactory {

    private static final String NMS_CLASS_FORMAT = "me.mrkirby153.utils.nms.Compat";

    private static final String[] nmsVersions = {
            "1_11_R1"
    };

    private JavaPlugin plugin;

    public NMSFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public NMS getNMS() {
        plugin.getLogger().info("Attempting to load NMS compatibility");
        for (String className : nmsVersions) {
            className = NMS_CLASS_FORMAT + className;
            plugin.getLogger().info(String.format("Attempting to load class %s", className));
            try {
                NMS nms = (NMS) Class.forName(className).newInstance();
                plugin.getLogger().info(String.format("Using compatibility class %s implementing me.mrkirby153.utils.nms.NMS version %s", className, nms.getNMSVersion()));
                nms.enable(plugin);
                return nms;
            } catch (Throwable t) {
                // Ignore
                t.printStackTrace();
            }
        }
        plugin.getLogger().warning("Could not find a compatibility class!");
        plugin.getLogger().warning("\t+ Bukkit version: " + plugin.getServer().getBukkitVersion());
        plugin.getLogger().warning("\t+ Server version: " + plugin.getServer().getVersion());
        return null;
    }
}
