# tsih-robo-ktx

A private [JDA](https://github.com/discord-jda/JDA) bot with multiple functions, made for me and for my friends!

### Introduction

Tsih-robo's primary function is to fix Twitter's horrible embeds on Discord's text chats by sending the post's content,
this can also work on other websites. It works as a music bot as well.

This repository is a complete remake of [tsih-robo](https://github.com/matkijahenkilo/tsih-robo) made with Discordia.

It uses Flyway, Hibernate, SQLite, JDA, JDA-ktx, Lavaplayer (and youtube-source), gallery-dl, yt-dlp and ffmpeg.

## Commands

The list of commands is as it follows:

### SlashInteraction commands

Runs when someone runs the command

- `/avatar`
  - returns the avatar of the user
- `/music`
  - plays music from various websites
  - will also save the server's playlist to disk in case of sudden stop, can be resumed where it stopped
- `/question`
  - answers a yes/no question
- `/toolpost`
  - sends a [they think im tom cruise](https://knowyourmeme.com/memes/they-think-that-im-tom-cruise) meme
    in a similar way of [Toolposting 1019](https://www.facebook.com/profile.php?id=100057113183628) page
- `/tsihoclock`
  - saves or removes the text channel to receive a random art of Tsih

### MessageReceived commands

Runs every time a message is sent to a text channel where the bot has permission to see

- `sauceSender`
  - checks if it's a link that can have its embed "fixed"

### Scheduled events

Runs something periodically

- `Random Status`
  - runs every 5 minutes to change the custom status' text
- `Tsih O'Clock`
  - runs every day at 18:00 to send fan arts to saved text channels

## Installation

It requires the following CLI programs:

- for `/toolpost`
  - yt-dlp
  - ffmpeg
- for `sauceSender`
  - gallery-dl
    - (and to be correctly configured)

the rest is soon™️

## Huge thanks

This project wouldn't be possible without:

- People that developed JDA, JDA-ktx, lavaplayer and gallery-dl
- JDA Discord server's brilliant minds for helping me deal with my stupidity
- Asuran95 and rafaelrc7 for telling me the bot was based and that I should rewrite most of the scripts
- Ikuse for making incentivising Tsih art to keep the darkness away
- Riiya for not rebranding Tsih into Tewi on his server
- My teacher for presenting me Kotlin
- People that use tsih-robo (and criticised her)
- SaucyBot for being a worthy opponent
- Other people that I forgot to mention
