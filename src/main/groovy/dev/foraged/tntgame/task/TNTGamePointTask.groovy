package dev.foraged.tntgame.task

import dev.foraged.game.Game
import dev.foraged.tntgame.TNTGame
import dev.foraged.tntgame.TNTGamePlugin
import dev.foraged.tntgame.TNTGameState
import dev.foraged.tntgame.player.TNTGamePlayer
import dev.foraged.tntgame.player.TNTGamePlayerState
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable

class TNTGamePointTask extends BukkitRunnable {

    TNTGame game

    TNTGamePointTask(Game game) {
        this.game = game as TNTGame
    }

    @Override
    void run() {
        if (game.gameState != TNTGameState.ACTIVE) return
        game.players().each {
            TNTGamePlayer data = TNTGamePlugin.instance.game.getPlayerData(it.uniqueId)

            if (data.state == TNTGamePlayerState.ALIVE) {
                data.coins = data.coins + 30
                it.sendMessage "${ChatColor.GOLD}+30 coins! (Survive Bonus)"
            }
        }
    }
}
