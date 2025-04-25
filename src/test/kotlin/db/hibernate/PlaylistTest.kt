package db.hibernate

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.junit.jupiter.api.Test
import java.util.function.Consumer

class PlaylistsTest {

    @Test
    fun testAddAndRetrievePlaylist() {
        var sessionFactory: SessionFactory? = null
        val registry = StandardServiceRegistryBuilder().build()
        try {
            sessionFactory =
                MetadataSources(registry)
                    .addAnnotatedClass(Playlist::class.java)
                    .buildMetadata()
                    .buildSessionFactory()
        } catch (e: Exception) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry)
        }

        if (sessionFactory != null) {

            // create a couple of events...
            sessionFactory.inTransaction(Consumer { session: Session ->
                session.persist(Playlist(link = "http://example.com", requester = 1234567890, guildId = 9876543210))
                session.persist(Playlist(link = "http://example2.com", requester = 1234567890, guildId = 9876543212))
            })

            // now lets pull events from the database and list them
            sessionFactory.inTransaction(Consumer { session: Session ->
                session.createQuery("from ${Playlist::class.java.simpleName}", Playlist::class.java).resultList
                    .forEach(Consumer { playlist -> assert(playlist.link == "http://example.com" || playlist.link == "http://example2.com") })

                session.createQuery(
                    "from ${Playlist::class.java.simpleName} p where p.${Playlist::guildId.name} = 9876543210",
                    Playlist::class.java
                ).resultList
                    .forEach(Consumer { playlist -> assert(playlist.link == "http://example.com") })

                session.createQuery(
                    "from ${Playlist::class.java.simpleName} p where p.${Playlist::guildId.name} = 9876543212",
                    Playlist::class.java
                ).resultList
                    .forEach(Consumer { playlist -> assert(playlist.link == "http://example2.com") })
            })
        } else {
            assert(false)
        }
    }
}