/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.lanternpowered.terre.MessageSender
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.impl.network.VersionedProtocol
import org.lanternpowered.terre.text.Text

internal class ServerImpl(
    override val info: ServerInfo,
    override var allowAutoJoin: Boolean = false,
    val versionedProtocol: VersionedProtocol? = null
) : Server {

  var unregistered = false
    private set

  val mutablePlayers = MutablePlayerCollection.concurrentOf()

  /**
   * The last server version that was noticed by connecting clients. Is
   * used to speed up connection when multiple versions are possible.
   */
  @Volatile var lastKnownVersion: ProtocolVersion? = null

  override val players
    get() = this.mutablePlayers.toImmutable()

  override fun unregister() {
    this.unregistered = true
    ProxyImpl.servers.unregister(this)
  }

  override fun evacuate() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun sendMessage(message: String) {
    this.mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    this.mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    this.mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun sendMessageAs(message: String, sender: MessageSender) {
    this.mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }
}
