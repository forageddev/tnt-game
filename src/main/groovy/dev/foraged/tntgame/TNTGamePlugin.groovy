package dev.foraged.tntgame

import dev.foraged.tntgame.listener.TNTGameListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class TNTGamePlugin extends JavaPlugin {

    static TNTGamePlugin instance
    TNTGame game

    @Override
    void onEnable() {
        game = new TNTGame(instance = this)

        Bukkit.pluginManager.registerEvents(new TNTGameListener(), this)
    }
}
