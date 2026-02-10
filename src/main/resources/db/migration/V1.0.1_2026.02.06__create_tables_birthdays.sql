create table birthdayChannels (
    birthdayChannelId integer primary key,
    channelId integer not null,
    guildId integer not null
);

create table birthdayUsers (
    birthdayUserId integer primary key,
    userId integer not null,
    day integer not null,
    month integer not null
);

create table birthdayUserChannelSubscriptions (
    subscriptionId integer primary key,
    birthdayChannelId integer not null,
    birthdayUserId integer not null,

    constraint fkChannel
        foreign key (birthdayChannelId)
        references birthdayChannels(birthdayChannelId)
        on delete cascade,

    constraint fkUser
        foreign key (birthdayUserId)
        references birthdayUsers(birthdayUserId)
        on delete cascade
);