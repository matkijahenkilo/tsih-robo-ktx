package org.matkija.bot.sql.jpa

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.matkija.bot.discordBot.commands.music.RequestedTrackInfo

private val registry = StandardServiceRegistryBuilder().build()

// A SessionFactory is set up once for an application!
private val sessionFactory: SessionFactory =
    MetadataSources(registry)
        .addAnnotatedClasses(
            MarkovAllowedChannel::class.java,
            Playlist::class.java,
            TOCRoom::class.java,
        )
        .buildMetadata()
        .buildSessionFactory()

object PersistenceUtil {

    fun savePlaylistEntries(requestedTrackInfos: List<RequestedTrackInfo>) {
        sessionFactory.inTransaction { session: Session ->
            requestedTrackInfos.forEach { entry ->
                session.persist(
                    Playlist(
                        link = entry.track!!.info.uri,
                        requester = entry.requester!!.idLong,
                        guildId = entry.guild!!.idLong
                    )
                )
            }
        }
    }

    fun getPlaylistsById(guildId: String): List<Playlist> {
        var ret: List<Playlist> = emptyList()
        sessionFactory.inTransaction { session: Session ->
            ret = session.createSelectionQuery(
                "from ${Playlist::class.java.name} p where p.${Playlist::guildId.name} = $guildId",
                Playlist::class.java
            ).resultList
        }
        return ret
    }

    fun deletePlaylistByStringAndId(link: String, guildId: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${Playlist::class.java.name} p where p.${Playlist::link.name} = '$link' and p.${Playlist::guildId.name} = $guildId"
            ).executeUpdate()
        }
    }

    fun deletePlaylistById(guildId: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${Playlist::class.java.name} p where p.${Playlist::guildId.name} = $guildId"
            ).executeUpdate()
        }
    }


    fun saveTsihOClockRoom(tocRooms: TOCRoom) {
        sessionFactory.inTransaction { session: Session ->
            session.persist(tocRooms)
        }
    }

    fun getAllTsihOClockRooms(): List<TOCRoom> {
        var ret: List<TOCRoom> = emptyList()
        sessionFactory.inTransaction { session: Session ->
            ret = session.createSelectionQuery(
                "from ${TOCRoom::class.java.name}",
                TOCRoom::class.java
            ).resultList
        }
        return ret
    }

    fun deleteTsihOClockRoomById(id: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${TOCRoom::class.java.name} t where t.${TOCRoom::roomId.name} = $id"
            ).executeUpdate()
        }
    }

    fun saveMarkovReadingChannel(markovAllowedChannel: MarkovAllowedChannel) {
        sessionFactory.inTransaction { session: Session ->
            session.persist(markovAllowedChannel)
        }
    }

    fun saveMarkovWritingChannel(markovAllowedChannel: MarkovAllowedChannel) {
        sessionFactory.inTransaction { session: Session ->
            session.persist(markovAllowedChannel)
        }
    }

    fun deleteMarkovWritingChannelById(channelId: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${MarkovAllowedChannel::class.java.name} m where m.${MarkovAllowedChannel::writingChannelId.name} = $channelId"
            ).executeUpdate()
        }
    }

    fun deleteMarkovReadingChannelById(channelId: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${MarkovAllowedChannel::class.java.name} m where m.${MarkovAllowedChannel::readingChannelId.name} = $channelId"
            ).executeUpdate()
        }
    }

    fun getAllMarkovInfo(): List<MarkovAllowedChannel> { //unfuck
        var ret: List<MarkovAllowedChannel> = emptyList()
        sessionFactory.inTransaction { session: Session ->
            ret = session.createSelectionQuery(
                "from ${MarkovAllowedChannel::class.java.name}",
                MarkovAllowedChannel::class.java
            ).resultList
        }
        return ret
    }

    fun getMarkovInfoByGuildId(guildId: Long): List<MarkovAllowedChannel> {
        var ret: List<MarkovAllowedChannel> = emptyList()
        sessionFactory.inTransaction { session: Session ->
            ret = session.createSelectionQuery(
                "from ${MarkovAllowedChannel::class.java.name} m where m.${MarkovAllowedChannel::guildId.name} = $guildId",
                MarkovAllowedChannel::class.java
            ).resultList
        }
        return ret
    }
}