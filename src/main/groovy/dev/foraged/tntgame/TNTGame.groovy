package dev.foraged.tntgame

import dev.foraged.game.Game
import dev.foraged.game.SpectatableGame
import dev.foraged.game.arena.impl.UnlimitedArena
import dev.foraged.game.board.GameBoardAdapter
import dev.foraged.game.task.GameTask
import dev.foraged.game.util.CC
import dev.foraged.game.util.TimeUtil
import dev.foraged.tntgame.listener.TNTGameListener
import dev.foraged.tntgame.player.TNTGamePlayer
import dev.foraged.tntgame.player.TNTGamePlayerState
import dev.foraged.tntgame.task.TNTGameCampTask
import dev.foraged.tntgame.task.TNTGamePointTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

import java.text.SimpleDateFormat
import java.util.stream.Collectors

class TNTGame extends Game<TNTGamePlayer, UnlimitedArena> implements SpectatableGame, GameBoardAdapter {

    TNTGameState gameState
    var _startTime = 30

    TNTGame(TNTGamePlugin plugin) {
        super(plugin, "TNT Run", "Avoid the falling blocks and try to\nsurvive longer than everyone else!")
        arena = new UnlimitedArena()
        arena.spawnPoints = new Location(Bukkit.getWorlds()[0], 100, 5, 100)
        gameState = TNTGameState.WAITING
    }

    @Override
    void ready() {
        gameState = TNTGameState.STARTING
        new BukkitRunnable() {
            @Override
            void run() {
                if (gameState == TNTGameState.ACTIVE) {
                    cancel()
                    return
                }

                if (players.size() < 2 && gameState == TNTGameState.STARTING) {
                    _startTime = 60
                    gameState = TNTGameState.WAITING
                    cancel()
                    return
                }

                _startTime--
                if (_startTime == 0) {
                    start()
                    cancel()
                } else if (_startTime % 5 == 0 || _startTime <= 5) {
                    var color
                    if (_startTime > 10) color = "&e"
                    else if (_startTime > 5) color = "&6"
                    else color = "&c"
                    broadcast("&eThe game starts in ${color}${_startTime}&e second${_startTime == 1 ? "" : "s"}!")
                    players().each {it.playSound(it.location, Sound.BLOCK_COMPARATOR_CLICK, 1f, 1f)}
                }
            }
        }.runTaskTimer(plugin, 20L, 20L)
    }

    @Override
    void start() {
        super.start()
        broadcast("&b&lThe game has started!")
        new GameTask(plugin, new TNTGameCampTask(instance)).delay(5L).repeating()
        new GameTask(plugin, new TNTGamePointTask(instance)).delay(300L).repeating()
        gameState = TNTGameState.ACTIVE
        players().each {it.allowFlight = true}
    }

    @Override
    void stop() {
        super.stop()
        gameState = TNTGameState.ENDING
        new GameTask(plugin, () -> {
            players().each {
                TNTGamePlayer data = getPlayerData(it)
                buildWrapper("Reward Summary", "  &7You earned\n      &f▪ &b${data.coins} Party Games Coins\n      &f▪ &30 Twoot Experience\n&f").toList().forEach(s -> it.sendMessage(CC.translate(s)))
            }
        }).delay(60L).then(() -> Bukkit.shutdown()).delay(240L).complete()
    }

    @Override
    void join(Player player) {
        players.put(player.uniqueId, new TNTGamePlayer(player.uniqueId))
        super.join(player)

        if (players.size() == 2) ready()
    }

    @Override
    void startSpectating(Player player) {
        getPlayerData(player).timeOfDeath = System.currentTimeMillis()
        getPlayerData(player).state = TNTGamePlayerState.SPECTATING
        gameItemManager.getItemBundle("spectator").apply(player)
        player.allowFlight = true
        player.flying = true
        player.teleport(arena.spawnPoints[0])
    }

    @Override
    boolean isSpectating(Player player) {
        return getPlayerData(player).state == TNTGamePlayerState.SPECTATING
    }

    @Override
    Collection<? extends Player> alivePlayers() {
        return players().stream().filter(p -> getPlayerData(p).state == TNTGamePlayerState.ALIVE).collect(Collectors.toList())
    }

    @Override
    String getTitle(Player player) {
        return "&e&lTNT RUN"
    }

    @Override
    List<String> getLines(Player player) {
        TNTGamePlayer data = getPlayerData(player)
        var required = 2 - players.size()

        switch (gameState) {
            case TNTGameState.WAITING: {
                return [
                        arenaInfo,
                        "",
                        "&fMap: &b${arena.name}",
                        "&fPlayers: &b${players.size()}/${Bukkit.maxPlayers}",
                        "",
                        "&fWaiting for &b${required}&f more",
                        "&fplayer${required == 1 ? "" : "s"} to join",
                        "",
                        "&fGame: &b${name}",
                        "",
                        footer
                ]
            }
            case TNTGameState.STARTING: {
                return [
                        arenaInfo,
                        "",
                        "&fMap: &b${arena.name}",
                        "&fPlayers: &b${players.size()}/${Bukkit.maxPlayers}",
                        "",
                        "&fStarting in &b${new SimpleDateFormat("mm:ss").format(new Date(_startTime * 1000))}&f to",
                        "&fallow time for",
                        "&fadditional players",
                        "",
                        "&fGame: &b${name}",
                        "",
                        footer
                ]
            }
            case TNTGameState.ACTIVE:
            case TNTGameState.ENDING: {
                return [
                        "&7Duration: ${TimeUtil.formatTime(System.currentTimeMillis() - started)}",
                        "",
                        "&fDouble Jump: &b${data.doubleJumps}&7/6",
                        "",
                        "&fPlayers Alive: &b${players.values().stream().filter(it -> it.state == TNTGamePlayerState.ALIVE).count()}",
                        "",
                        "&fCoins Earned: &b${data.coins}",
                        "",
                        arenaInfo,
                        footer
                ]
            }
        }
    }
}
