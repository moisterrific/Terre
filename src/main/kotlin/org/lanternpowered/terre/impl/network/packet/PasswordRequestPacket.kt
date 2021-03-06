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

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal object PasswordRequestPacket : Packet

internal val PasswordRequestEncoder = packetEncoderOf<PasswordRequestPacket> { _, _ -> }

internal val PasswordRequestDecoder = packetDecoderOf { PasswordRequestPacket }
