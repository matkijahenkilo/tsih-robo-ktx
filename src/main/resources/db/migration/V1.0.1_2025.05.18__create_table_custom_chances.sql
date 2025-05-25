create table customChances (
  chanceId integer primary key,
  guildId integer not null,
  eventRandomReactChance real,
  eventMarkovTextChance real
);