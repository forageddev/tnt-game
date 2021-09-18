package dev.foraged.tntgame.player

import dev.foraged.game.player.GamePlayer
import org.jetbrains.annotations.NotNull

class TNTGamePlayer extends GamePlayer implements Comparable<TNTGamePlayer> {

    TNTGamePlayerState state
    int doubleJumps, blocksFallen
    long lastMovement, timeOfDeath

    TNTGamePlayer(UUID id) {
        super(id)
        doubleJumps = 6
        state = TNTGamePlayerState.ALIVE
    }

    @Override
    int compareTo(@NotNull TNTGamePlayer o) {
        return o.timeOfDeath - timeOfDeath
    }
}
