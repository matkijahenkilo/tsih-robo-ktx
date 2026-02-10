package org.matkija.bot.sql

import jakarta.persistence.*

@Entity
@Table(name = "birthdayUserChannelSubscriptions")
data class BirthdayUserChannelSubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val subscriptionId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "birthdayChannelId")
    val birthdayChannelId: BirthdayChannel,

    @ManyToOne
    @JoinColumn(name = "birthdayUserId")
    val birthdayUserId: BirthdayUser
)