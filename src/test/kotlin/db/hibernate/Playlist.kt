package db.hibernate

import jakarta.persistence.*

@Entity
@Table(name = "playlists")
data class Playlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val playlistId: Long? = null,
    @Column(name = "link")
    val link: String,
    @Column(name = "requester")
    val requester: Long,
    @Column(name = "guildId")
    val guildId: Long
)