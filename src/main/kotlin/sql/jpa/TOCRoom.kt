package org.matkija.bot.sql.jpa

import jakarta.persistence.*

@Entity
@Table(name = "tocrooms")
data class TOCRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tocroomsId: Long? = null,
    val roomId: Long
)