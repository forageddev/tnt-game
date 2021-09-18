package dev.foraged.tntgame

import org.bukkit.plugin.java.JavaPlugin

class TNTGamePlugin extends JavaPlugin {

    static TNTGamePlugin instance
    TNTGame game

    @Override
    void onEnable() {
        game = new TNTGame(instance = this)
    }
}
