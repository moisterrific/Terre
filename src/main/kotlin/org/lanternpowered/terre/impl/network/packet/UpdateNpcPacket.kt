/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.math.Vec2f
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.LeftOrRight
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.NpcType
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.UpOrDown
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class UpdateNpcPacket(
    val npcId: NpcId,
    val npcType: NpcType,
    val position: Vec2f,
    val velocity: Vec2f,
    val target: PlayerId?,
    val ai: NpcAI,
    val direction: LeftOrRight,
    val directionY: UpOrDown,
    val spriteDirection: LeftOrRight,
    val life: Int?,
    val releaseOwner: PlayerId?
) : Packet

internal data class NpcAI(
    val ai1: Float,
    val ai2: Float,
    val ai3: Float,
    val ai4: Float
)

internal val UpdateNpcEncoder = UpdateNpcEncoder(Int.MAX_VALUE)

internal fun UpdateNpcEncoder(protocol: Int) = packetEncoderOf<UpdateNpcPacket> { buf, packet ->
  buf.writeNpcId(packet.npcId)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  val target = packet.target?.to(this.isMobile)?.value ?: -1
  if (protocol == 155) {
    buf.writeByte(target)
  } else {
    buf.writeShortLE(target)
  }
  var flags = 0
  if (packet.direction.isRight)
    flags += 0x1
  if (packet.directionY.isUp)
    flags += 0x2
  if (packet.spriteDirection.isRight)
    flags += 0x40
  val ai = packet.ai
  if (ai.ai1 != 0f)
    flags += 0x4
  if (ai.ai2 != 0f)
    flags += 0x8
  if (ai.ai3 != 0f)
    flags += 0x10
  if (ai.ai4 != 0f)
    flags += 0x20
  val life = packet.life
  if (life == null)
    flags += 0x80
  buf.writeByte(flags)
  if (ai.ai1 != 0f)
    buf.writeFloatLE(ai.ai1)
  if (ai.ai2 != 0f)
    buf.writeFloatLE(ai.ai2)
  if (ai.ai3 != 0f)
    buf.writeFloatLE(ai.ai3)
  if (ai.ai4 != 0f)
    buf.writeFloatLE(ai.ai4)
  buf.writeShortLE(packet.npcType.value)
  if (life != null) {
    buf.writeByte(when {
      life <= Byte.MAX_VALUE -> Byte.SIZE_BYTES
      life <= Short.MAX_VALUE -> Short.SIZE_BYTES
      else -> Int.SIZE_BYTES
    })
    when {
      life <= Byte.MAX_VALUE -> buf.writeByte(life)
      life <= Short.MAX_VALUE -> buf.writeShortLE(life)
      else -> buf.writeIntLE(life)
    }
  }
  val releaseOwner = packet.releaseOwner
  if (releaseOwner != null)
    buf.writePlayerId(releaseOwner.to(this.isMobile))
}

internal val UpdateNpcDecoder = UpdateNpcDecoder(Int.MAX_VALUE)

internal fun UpdateNpcDecoder(protocol: Int) = packetDecoderOf { buf ->
  val npcId = buf.readNpcId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val targetId = if (protocol == 155) buf.readUnsignedByte().toInt() else buf.readUnsignedShortLE()
  val target = if (targetId == -1) null else PlayerId(targetId).from(this.isMobile)
  val flags = buf.readByte().toInt()
  val direction = LeftOrRight((flags and 0x1) != 0)
  val directionY = UpOrDown((flags and 0x2) != 0)
  val spriteDirection = LeftOrRight((flags and 0x40) != 0)
  val ai1 = if ((flags and 0x4) != 0) buf.readFloatLE() else 0f
  val ai2 = if ((flags and 0x8) != 0) buf.readFloatLE() else 0f
  val ai3 = if ((flags and 0x10) != 0) buf.readFloatLE() else 0f
  val ai4 = if ((flags and 0x20) != 0) buf.readFloatLE() else 0f
  val ai = NpcAI(ai1, ai2, ai3, ai4)
  val npcType = NpcType(buf.readShortLE().toInt())
  val life = if ((flags and 0x80) == 0) {
    when (buf.readByte().toInt()) {
      2 -> buf.readShortLE().toInt()
      4 -> buf.readIntLE()
      else -> buf.readByte().toInt()
    }
  } else null
  val releaseOwner = if (buf.readableBytes() > 0) buf.readPlayerId().from(this.isMobile) else null
  UpdateNpcPacket(npcId, npcType, position, velocity, target, ai,
      direction, directionY, spriteDirection, life, releaseOwner)
}
