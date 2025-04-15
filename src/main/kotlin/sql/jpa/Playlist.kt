package org.matkija.bot.sql.jpa

import jakarta.persistence.*

@Entity
@Table(name = "playlists")
data class Playlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playlistId: Long? = null,
    val link: String,
    val requester: Long,
    val guildId: Long
)