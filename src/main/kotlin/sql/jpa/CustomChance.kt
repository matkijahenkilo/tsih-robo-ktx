package org.matkija.bot.sql.jpa

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

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