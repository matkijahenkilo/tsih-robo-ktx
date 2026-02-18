package org.matkijahenkilo.tsihRoboKtx.sql

import jakarta.persistence.*

@Entity
@Table(name = "birthdayUsers")
data class BirthdayUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val birthdayUserId: Long? = null,
    val userId: Long,
    // if day or month are set to Int,
    // birthdayUserId will be set as Any in the database instead of Integer
    // what the fuck
    val day: Long,
    val month: Long
)
