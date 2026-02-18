package org.matkijahenkilo.tsihRoboKtx.sql

import jakarta.persistence.*

@Entity
@Table(name = "tocChannels")
data class TOCChannel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tocChannelId: Long? = null,
    val channelId: Long
)