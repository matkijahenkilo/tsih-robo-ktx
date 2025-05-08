alter table tocrooms rename to tocChannels;
alter table tocChannels rename column tocroomsId to tocChannelId;
alter table tocChannels rename column roomId to channelId;