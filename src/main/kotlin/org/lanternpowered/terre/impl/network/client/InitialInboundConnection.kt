/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.client

import org.lanternpowered.terre.InboundConnection
import org.lanternpowered.terre.util.ToStringHelper
import java.net.SocketAddress

class InitialInboundConnection(
    override val remoteAddress: SocketAddress
) : InboundConnection {

  override fun toString() = ToStringHelper()
      .add("remoteAddress", this.remoteAddress)
      .toString()
}
