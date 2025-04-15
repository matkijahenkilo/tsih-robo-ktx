create table playlists (
  playlistId integer primary key,
  link string not null,
  requester integer not null,
  guildId integer not null
);

create table tocrooms (
  tocroomsId integer primary key,
  roomId integer not null
);