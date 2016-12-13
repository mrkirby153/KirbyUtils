package me.mrkirby153.kcutils.event;

import me.mrkirby153.kcutils.Module;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateEventHandler extends Module<JavaPlugin> implements Runnable {

    private int ticks = 0;

    public UpdateEventHandler(JavaPlugin plugin) {
        super("Update Event", "1.0", plugin);
    }

    @Override
    public void run() {
        // Prevent some sort of overflow if it's running too long. That could be bad
        if(++ticks >= Integer.MAX_VALUE){
            ticks = 1;
        }
        for(UpdateType t : UpdateType.values()){
            if(t.getUpdateTime() % ticks == 0){
                getPlugin().getServer().getPluginManager().callEvent(new UpdateEvent(t));
            }
        }
    }

    @Override
    protected void init() {
        scheduleRepeating(this, 0L, 1L);
    }
}
