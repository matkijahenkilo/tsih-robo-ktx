package org.matkijahenkilo.tsihRoboKtx.sql

import jakarta.persistence.*

@Entity
@Table(name = "birthdayChannels")
data class BirthdayChannel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val birthdayChannelId: Long? = null,
    var channelId: Long,
    val guildId: Long
)
