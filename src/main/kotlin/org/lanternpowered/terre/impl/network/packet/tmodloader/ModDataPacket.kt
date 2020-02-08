/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.tmodloader

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

data class ModDataPacket(val data: ByteBuf) : Packet, ForwardingReferenceCounted(data)

internal val ModDataEncoder = packetEncoderOf<ModDataPacket> { buf, packet ->
  val data = packet.data
  buf.writeBytes(data, data.readerIndex(), data.readableBytes())
}

internal val ModDataDecoder = packetDecoderOf { buf ->
  val data = buf.readBytes(buf.readableBytes())
  ModDataPacket(data)
}
