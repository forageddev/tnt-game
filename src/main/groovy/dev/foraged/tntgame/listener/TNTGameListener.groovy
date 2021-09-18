package dev.foraged.tntgame.listener

import dev.foraged.game.task.GameTask
import dev.foraged.game.util.CC
import dev.foraged.tntgame.TNTGame
import dev.foraged.tntgame.TNTGamePlugin
import dev.foraged.tntgame.TNTGameState
import dev.foraged.tntgame.player.TNTGamePlayer
import dev.foraged.tntgame.player.TNTGamePlayerState
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class TNTGameListener implements Listener {

    TNTGame game = TNTGamePlugin.instance.game

    @EventHandler
    void onJoin(PlayerJoinEvent e) {
        Player player = e.player

        e.joinMessage = null
        if (game.gameState == TNTGameState.WAITING || game.gameState == TNTGameState.STARTING) {
            game.join(player)
        } else {
            e.player.kickPlayer(CC.translate("&cGame already started go awsay loasser")) //TODO: CHANGE THIS
        }
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e) {
        Player player = e.player

        e.quitMessage = null
        game.leave(player)
    }

    @EventHandler
    void onDamage(EntityDamageEvent e) {
        e.cancelled = true
    }

    @EventHandler
    void onHungerChange(FoodLevelChangeEvent e) {
        e.cancelled = true
    }

    @EventHandler
    void onFlightToggle(PlayerToggleFlightEvent e) {
        if (game.gameState != TNTGameState.ACTIVE) return

        Player player = e.player
        if (player.gameMode == GameMode.CREATIVE) return

        TNTGamePlayer data = game.getPlayerData(player)
        if (data != null && data.state == TNTGamePlayerState.ALIVE) {
            e.cancelled = true
            player.allowFlight = false
            data.doubleJumps = data.doubleJumps - 1
            player.playSound(player.location, Sound.ENTITY_BAT_TAKEOFF, 1f, 1f)
            player.velocity = (player.location.direction * 0.65).setY(0.65)

            if (data.doubleJumps != 0) new GameTask(game.plugin, () -> player.allowFlight = true).delay(20L).complete()
        }
    }

    @EventHandler
    void onMove(PlayerMoveEvent e) {
        if (game.gameState != TNTGameState.ACTIVE) return

        Player player = e.player
        Location from = e.from, to = e.to
        if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
            Block relative = to.block.getRelative(BlockFace.DOWN)
            TNTGamePlayer data = game.getPlayerData(player.uniqueId)

            if (data != null && data.state == TNTGamePlayerState.ALIVE) {
                if (from.blockY < 0 || player.inLava || player.inWater) {
                    game.startSpectating(player)
                    player.sendMessage(CC.translate("&cYou died! &eYou can now spectate the game!"))
                    game.broadcast("&7${player.displayName} &cdied!")
                    game.broadcast("&e${game.alivePlayers().size()} players remaining.")
                    if (game.alivePlayers().size() == 1) {
                        game.getPlayerData(game.alivePlayers().first()).coins(250, "Win Bonus")
                        game.stop()
                    }
                    return
                }

                data.lastMovement = System.currentTimeMillis()
                if (relative.type != Material.AIR) {
                    new GameTask(game.plugin, () -> {
                            data.blocksFallen = data.blocksFallen + 1
                            relative.type = Material.AIR
                    }).delay(10L).complete()
                }
            }
        }
    }
}
