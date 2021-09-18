package dev.foraged.tntgame.task

import dev.foraged.game.Game
import dev.foraged.tntgame.TNTGame
import dev.foraged.tntgame.TNTGamePlugin
import dev.foraged.tntgame.TNTGameState
import dev.foraged.tntgame.player.TNTGamePlayer
import dev.foraged.tntgame.player.TNTGamePlayerState
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.NumberConversions

class TNTGameCampTask extends BukkitRunnable {

    TNTGame game

    TNTGameCampTask(Game game) {
        this.game = game as TNTGame
    }

    @Override
    void run() {
        if (game.gameState != TNTGameState.ACTIVE) return
        game.players().each {
            TNTGamePlayer data = TNTGamePlugin.instance.game.getPlayerData(it.uniqueId)

            if (data.state == TNTGamePlayerState.ALIVE && (data.lastMovement + 2000) <= System.currentTimeMillis()) {
                Block block = findBlockUnderPlayer(it)

                if (block != null && block.type != Material.AIR) {
                    block.type = Material.AIR
                    data.blocksFallen = data.blocksFallen + 1
                }
            }
        }
    }

    Block findBlockUnderPlayer(Player player) {
        Location location = player.location
        World world = location.world

        Block block = world.getBlockAt(NumberConversions.floor(location.x + 0.3), (location.y - 1) as int, NumberConversions.floor(location.z - 0.3))
        if (block.type == Material.AIR) block = world.getBlockAt(NumberConversions.floor(location.x - 0.3), (location.y - 1) as int, NumberConversions.floor(location.z + 0.3))
        if (block.type == Material.AIR) block = world.getBlockAt(NumberConversions.floor(location.x + 0.3), (location.y - 1) as int, NumberConversions.floor(location.z + 0.3))
        if (block.type == Material.AIR) block = world.getBlockAt(NumberConversions.floor(location.x - 0.3), (location.y - 1) as int, NumberConversions.floor(location.z - 0.3))
        return block.type == Material.AIR ? null : block
    }
}
