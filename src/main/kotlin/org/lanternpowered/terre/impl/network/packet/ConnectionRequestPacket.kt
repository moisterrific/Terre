/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.impl.network.ClientVersion
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.impl.util.Version

internal data class ConnectionRequestPacket(val version: ClientVersion) : Packet

private const val vanillaVersionPrefix = "Terraria"

private const val tModLoaderVersionPrefix = "tModLoader"
private val tModLoaderVersionRegex =
    "^$tModLoaderVersionPrefix v([0-9.]*)(?: ([^\\s]*))?(?: Beta ([0-9]*))?\$".toRegex()

internal val ConnectionRequestDecoder = packetDecoderOf { buf ->
  val value = buf.readString()

  val clientVersion = run {
    if (value.startsWith(vanillaVersionPrefix)) {
      ClientVersion.Vanilla(value.substring(vanillaVersionPrefix.length).toInt())
    } else if (value.startsWith(tModLoaderVersionPrefix)) {
      val result = tModLoaderVersionRegex.matchEntire(value)
      if (result != null) {
        val version = Version(result.groupValues[1])
        val branch = result.groups[2]?.value
        val beta = result.groups[3]?.value?.toInt()
        ClientVersion.TModLoader(version, branch, beta)
      } else {
        throw DecoderException("Invalid tModLoader client version: $value")
      }
    } else throw DecoderException("Unsupported client: $value")
  }

  ConnectionRequestPacket(clientVersion)
}

internal val ConnectionRequestEncoder = packetEncoderOf<ConnectionRequestPacket> { buf, packet ->
  val value = when (val version = packet.version) {
    is ClientVersion.Vanilla -> "$vanillaVersionPrefix${version.protocol}"
    is ClientVersion.TModLoader -> {
      val builder = StringBuilder("$tModLoaderVersionPrefix v${version.version}")
      if (version.branch != null)
        builder.append(" ${version.branch}")
      if (version.beta != null)
        builder.append(" Beta ${version.beta}")
      builder.toString()
    }
  }
  buf.writeString(value)
}
