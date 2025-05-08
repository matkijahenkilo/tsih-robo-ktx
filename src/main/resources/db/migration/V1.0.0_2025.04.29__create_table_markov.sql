create table markovAllowedChannels (
  markovId integer primary key,
  guildId integer not null,
  readingChannelId integer,
  writingChannelId integer
);