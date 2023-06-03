package me.nate.givedelay;

import commands.GiveOfflineCommand;
import events.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;


public class GiveDelay extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("giveoffline").setExecutor(new GiveOfflineCommand());
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    @Override
    public void onDisable() {
        System.out.println("Plugin disabilitato!!");
    }
}
