/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.util.toString

internal class UnknownPacket(
    val opcode: Int,
    val content: ByteBuf
) : ForwardingReferenceCounted(content), Packet {

  val length: Int
    get() = this.content.readableBytes()

  override fun toString() = toString {
    "opcode" to opcode
    "contentLength" to length
  }
}