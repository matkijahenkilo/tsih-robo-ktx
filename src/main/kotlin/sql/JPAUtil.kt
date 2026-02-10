package org.matkija.bot.sql

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.matkija.bot.discordBot.commands.music.RequestedTrackInfo

//TODO(switch to Exposed https://www.jetbrains.com/exposed/ ( ; ω ; ))

private val registry = StandardServiceRegistryBuilder().build()

// A SessionFactory is set up once for an application!
private val sessionFactory: SessionFactory =
    MetadataSources(registry)
        .addAnnotatedClasses(
            BirthdayChannel::class.java,
            BirthdayUser::class.java,
            BirthdayUserChannelSubscription::class.java,
            CustomChance::class.java,
            MarkovAllowedChannel::class.java,
            Playlist::class.java,
            TOCChannel::class.java,
        )
        .buildMetadata()
        .buildSessionFactory()

object JPAUtil {

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


    fun saveTsihOClockRoom(tocRooms: TOCChannel) {
        sessionFactory.inTransaction { session: Session ->
            session.persist(tocRooms)
        }
    }

    fun getAllTsihOClockRooms(): List<TOCChannel> {
        var ret: List<TOCChannel> = emptyList()
        sessionFactory.inTransaction { session: Session ->
            ret = session.createSelectionQuery(
                "from ${TOCChannel::class.java.name}",
                TOCChannel::class.java
            ).resultList
        }
        return ret
    }

    fun deleteTsihOClockRoomById(id: Long) {
        sessionFactory.inTransaction { session: Session ->
            session.createMutationQuery(
                "delete from ${TOCChannel::class.java.name} t where t.${TOCChannel::channelId.name} = $id"
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

    fun saveOrUpdateCustomChance(e: CustomChance) {
        sessionFactory.inTransaction { session: Session ->
            if (e.chanceId == null) {
                session.persist(e)
            } else {
                val chanceId = session.createSelectionQuery(
                    "from ${e::class.java.name} m where m.${e::guildId.name} = ${e.guildId}",
                    e::class.java
                ).resultList[0].chanceId
                if (session.find(e::class.java, chanceId) == null) {
                    session.persist(e)
                } else {
                    session.merge(e)
                }
            }
        }
    }

    fun getCustomChanceEntity(guildId: Long): CustomChance? {
        var ret: CustomChance? = null
        sessionFactory.inTransaction { session: Session ->
            val resultList = session.createSelectionQuery(
                "from ${CustomChance::class.java.name} c where c.${CustomChance::guildId.name} = $guildId",
                CustomChance::class.java
            ).resultList
            ret = if (resultList.isNotEmpty())
                resultList[0]
            else
                null

        }
        return ret
    }

    fun getTodayBirthdays(currentDay: Long, currentMonth: Long): List<BirthdayUserChannelSubscription>? {
        var birthdays: List<BirthdayUserChannelSubscription>? = null
        sessionFactory.inTransaction { session ->
            birthdays = session.createSelectionQuery(
                "from BirthdayUserChannelSubscription sub where sub.birthdayUserId.day = $currentDay and sub.birthdayUserId.month = $currentMonth",
                BirthdayUserChannelSubscription::class.java
            ).resultList
        }
        return birthdays
    }

    fun getBirthdayUserSubscriptionFromGuildIdAndUserId(userId: Long, guildId: Long): BirthdayUserChannelSubscription? {
        var sub: BirthdayUserChannelSubscription? = null
        sessionFactory.inTransaction { session ->
            val count = session.createNativeQuery(
                "SELECT count(*) FROM birthdayUserChannelSubscriptions",
                Long::class.javaObjectType
            )
                .singleResult ?: 0L

            if (count == 0L) {
                sub = null
                return@inTransaction
            }

            sub = session.createSelectionQuery(
                """
                from ${BirthdayUserChannelSubscription::class.java.name} sub
                where
                sub.birthdayChannelId.guildId = $guildId
                and
                sub.birthdayUserId.userId = $userId
            """,
                BirthdayUserChannelSubscription::class.java
            ).singleResultOrNull
        }
        return sub
    }

    fun getBirthdayUserSubscriptionsFromGuildId(guildId: Long): List<BirthdayUserChannelSubscription>? {
        var result: List<BirthdayUserChannelSubscription>? = null
        sessionFactory.inTransaction { session ->
            result = session.createSelectionQuery(
                """
                    from ${BirthdayUserChannelSubscription::class.java.name} sub
                    where
                    sub.birthdayChannelId.guildId = $guildId
                    order by sub.birthdayUserId.month asc, sub.birthdayUserId.day asc
                """,
                BirthdayUserChannelSubscription::class.java
            ).resultList
        }
        return result
    }


    fun getBirthdayNotificationChannelFromGuildId(guildId: Long): BirthdayChannel? {
        var result: BirthdayChannel? = null
        sessionFactory.inTransaction { session ->
            result = session.createSelectionQuery(
                "from ${BirthdayChannel::class.java.name} bc where bc.${BirthdayChannel::guildId.name} = $guildId",
                BirthdayChannel::class.java
            ).singleResultOrNull
        }
        return result
    }

    fun setOrReplaceBirthdayNotificationChannel(birthdayChannel: BirthdayChannel) {
        sessionFactory.inTransaction { session ->
            val birthdayChannelObjFromExistingGuild = session.createSelectionQuery(
                """
                    from ${BirthdayChannel::class.java.name} m
                    where m.${BirthdayChannel::guildId.name} = ${birthdayChannel.guildId}
                """,
                BirthdayChannel::class.java
            ).singleResultOrNull

            if (birthdayChannelObjFromExistingGuild == null) {
                session.persist(birthdayChannel)
            } else {
                println(
                    session.merge(
                        BirthdayChannel(
                            birthdayChannelId = birthdayChannelObjFromExistingGuild.birthdayChannelId,
                            channelId = birthdayChannel.channelId,
                            guildId = birthdayChannelObjFromExistingGuild.guildId
                        )
                    )
                )
            }
        }
    }

    fun saveBirthdayUser(birthdayUser: BirthdayUser, guildId: Long) {
        sessionFactory.inTransaction { session ->
            // fetch the birthdayChannel object to associate the user with the server
            val birthdayChannelFromDB = session.createSelectionQuery(
                "from ${BirthdayChannel::class.java.name} bc where bc.${BirthdayChannel::guildId.name} = $guildId",
                BirthdayChannel::class.java
            ).singleResult

            println(birthdayChannelFromDB)
            val managedUser = session.merge(birthdayUser)
            println(managedUser)

            val sub = BirthdayUserChannelSubscription(
                birthdayChannelId = birthdayChannelFromDB,
                birthdayUserId = managedUser
            )
            println(sub)
            session.persist(sub)
        }
    }

    fun deleteBirthdayUser(userId: Long, guildId: Long) {
        sessionFactory.inTransaction { session ->
            session.createMutationQuery(
                "delete from ${BirthdayUserChannelSubscription::class.java.name} sub where sub.birthdayUserId.userId = $userId and sub.birthdayChannelId.guildId = $guildId"
            ).executeUpdate()

            // clear the user from db only if they have 0 subscriptions left
            session.createMutationQuery(
                """
                    delete from ${BirthdayUser::class.java.name} u
                    where u.userId = :userId 
                    and not exists (
                        select 1 from BirthdayUserChannelSubscription sub 
                        where sub.birthdayUserId = u
                    )
                """
            )
                .setParameter("userId", userId)
                .executeUpdate()
        }
    }
}