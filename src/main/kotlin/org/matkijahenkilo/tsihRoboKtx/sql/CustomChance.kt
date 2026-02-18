package org.matkijahenkilo.tsihRoboKtx.sql

import jakarta.persistence.*

@Entity
@Table(name = "customChances")
data class CustomChance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val chanceId: Long? = null,
    val guildId: Long,
    var eventRandomReactChance: Float? = null,
    var eventMarkovTextChance: Float? = null
)