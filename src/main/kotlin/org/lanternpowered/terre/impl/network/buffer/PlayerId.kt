/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.buffer

/**
 * Represents the id of a player.
 */
internal inline class PlayerId(inline val value: Int) {

  companion object {

    /**
     * Represents no player.
     */
    val None = PlayerId(255)
  }
}