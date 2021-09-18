package dev.foraged.tntgame

import dev.foraged.tntgame.listener.TNTGameListener
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class TNTGamePlugin extends JavaPlugin {

    static TNTGamePlugin instance
    TNTGame game

    @Override
    void onEnable() {
        game = new TNTGame(instance = this)

        register(new TNTGameListener())
    }

    void register(Listener... listeners) {
        for (Listener listener : listeners) server.pluginManager.registerEvents(listener, this)
    }
}
