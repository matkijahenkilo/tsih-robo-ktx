package org.matkijahenkilo.tsihRoboKtx.sql

import jakarta.persistence.*

@Entity
@Table(name = "markovAllowedChannels")
data class MarkovAllowedChannel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val markovId: Long? = null,
    val guildId: Long,
    val readingChannelId: Long? = null,
    val writingChannelId: Long? = null,
)
