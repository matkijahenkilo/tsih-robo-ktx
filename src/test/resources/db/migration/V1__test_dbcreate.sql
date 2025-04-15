create table if not exists playlists (
  playlistId integer primary key,
  link string not null,
  requester integer not null,
  guildId integer not null
);

create table if not exists tocrooms (
  roomId integer
);