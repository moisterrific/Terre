/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NonAsciiCharacters", "ObjectPropertyName")

package org.lanternpowered.terre

import org.lanternpowered.terre.util.Version

/**
 * Represents the protocol version.
 */
sealed class ProtocolVersion {

  /**
   * Represents a vanilla protocol version.
   */
  data class Vanilla(
      val version: Version,
      val protocol: Int
  ) : ProtocolVersion(), Comparable<Vanilla> {

    constructor(version: String, protocol: Int) : this(Version(version), protocol)

    override fun compareTo(other: Vanilla)
        = this.protocol.compareTo(other.protocol)

    override fun equals(other: Any?)
        = other is Vanilla && other.protocol == this.protocol

    override fun hashCode()
        = this.protocol.hashCode()

    companion object {

      /**
       * The 1.3.0.7 vanilla version.
       */
      val `1․3․0․7` = Vanilla("1.3.0.7", 155)

      /**
       * The 1.3.0.8 vanilla version.
       */
      val `1․3․0․8` = Vanilla("1.3.0.8", 156)

      /**
       * The 1.3.5.3 vanilla version.
       */
      val `1․3․5․3` = Vanilla("1.3.5.3", 194)
    }
  }

  /**
   * Represents a tModLoader protocol version.
   *
   * @property version The base version, e.g. 0.11.6.2
   * @property branch The branch, if not the default
   * @property beta The beta build number, if applicable
   */
  data class TModLoader(
      val version: Version,
      val branch: String? = null,
      val beta: Int? = null
  ) : ProtocolVersion(), Comparable<TModLoader> {

    override fun compareTo(other: TModLoader)
        = compareValuesBy(this, other, { it.version }, { it.branch }, { it.beta })
  }
}
