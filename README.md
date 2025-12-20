# tsih-robo-ktx

A private [JDA](https://github.com/discord-jda/JDA) bot with multiple functions, made for me and for my friends!

### Introduction

Tsih-robo's primary function is to fix Twitter's horrible embeds on Discord's text chats by sending the post's content,
this can also work on other websites. It works as a music bot and markov chain bot as well.

Tsih is a character owned by Fruitbat Factory, they have nothing to do with this repository and I don't have anything to do with them. I just named the bot after Tsih!

It uses Flyway, Hibernate, SQLite, JDA, JDA-ktx, Lavaplayer (and youtube-source), gallery-dl, yt-dlp and ffmpeg.

This repository is a complete remake of [tsih-robo](https://github.com/matkijahenkilo/tsih-robo) made with Discordia.

## Commands

The list of commands is as it follows:

### SlashInteraction commands

Runs when someone runs the command

- `/avatar`
  - returns the avatar of the user
- `/music`
  - plays music from various websites
  - will also save the server's playlist to disk in case of sudden stop, can be resumed where it stopped

![tsih disgusted](https://raw.githubusercontent.com/matkijahenkilo/matkijahenkilo/refs/heads/main/imgs/2025-05-08_09-34-54.png)

- `/question`
  - answers a yes/no question
- `/toolpost`
  - sends a [they think im tom cruise](https://knowyourmeme.com/memes/they-think-that-im-tom-cruise) meme
    in a similar way of [Toolposting 1019](https://www.facebook.com/profile.php?id=100057113183628) page
- `/tsihoclock`
  - saves or removes the text channel to receive a random art of Tsih
- `/markov`
  - manages bot's permissions on where to read and write [meaningless text](https://en.wikipedia.org/wiki/Markov_chain#Markov_text_generators)
    - `read` ⚠️ **will log a channel's messages** to feed to its vocabulary
    - `write` will use the saved vocabulary to spit out meaningless text
    - `status` shows which channel she is using for what, it's ephemeral

### MessageReceived "passive" commands

Runs every time a message is sent to a text channel where the bot has permission to see/write

- `sauceSender`
  - checks if it's a link that can have its embed "fixed"
- `randomReact`
  - has a small chance to react to a message with a random custom emoji from a random server. It's guaranteed when saying "tsih" or "nora"
- `markov`
  - has a small chance to generate a random text based off a channel where `/markov read` were activated and send to a channel where `/markov write` were activated.
    It's guaranteed when mentioning the bot, and can specify a word to begin with in between double quotes

![tsih trying to english](https://raw.githubusercontent.com/matkijahenkilo/matkijahenkilo/refs/heads/main/imgs/2025-05-08_18-34-54.png)

![you you you you](https://raw.githubusercontent.com/matkijahenkilo/matkijahenkilo/refs/heads/main/imgs/2025-05-31_16-49-26.png)

### Scheduled events

Runs something periodically

- `Random Status`
  - runs every 5 minutes to change the custom status' text
- `Tsih O'Clock`
  - runs every day at 18:00 to send fan arts to saved text channels

## Installation

Before anything, **dedicate a folder to run the bot**, in that folder create the file `data/config.json`
and input the following model, replacing with your info:

```json
[
  {
    "name": "your bot name here",
    "token": "your fancy token here"
  }
]
```

Folder structure should be the following:

```
bot-root-folder/
├─ data/
│  ├─ config.json
├─ tsih-robo-ktx.jar
```

(.jar file is optional in case of Nix users)

It also requires the following CLI programs:

- for `/toolpost`
  - yt-dlp
  - ffmpeg
- for `sauceSender`
  - gallery-dl
    - (and to be correctly configured)

If you're using Nix, you don't have to download them manually.

### Nix

#### Live demo

Run a live demo of the bot inside the dedicated folder by running:

```
nix run github:matkijahenkilo/tsih-robo-ktx
```

#### Nix flake

Add in your flake inputs:

```nix
inputs = {
  tsih-robo-ktx = {
    url = "github:matkijahenkilo/tsih-robo-ktx";
    inputs.nixpkgs.follows = "nixpkgs";
  };
};
```

then import into your environment:

```nix
home.packages = [
  inputs.tsih-robo-ktx.packages.${pkgs.stdenv.hostPlatform.system}.default
];
```

or

```nix
environment.systemPackages = [
  inputs.tsih-robo-ktx.packages.${pkgs.stdenv.hostPlatform.system}.default
];
```

Switch your current system, and you can execute Tsih with `tsih-robo-ktx` inside the dedicated folder.

#### Systemd service

You can start the bot on boot if you want.

Note that the bot's folder is in `/srv/tsih-robo-ktx`. Because another user is taking care of the process, use `chown -R tsih:tsih /srv/tsih-robo-ktx`
after you prepared the folder to run the bot.

Customise the configuration as you'd like

```nix
{
  inputs,
  lib,
  pkgs,
  config,
  ...
}:
let
  tsih-robo-path = "/srv/tsih-robo-ktx";
in
{
  users.users.tsih = {
    description = "Tsih-robo owner (very cute)";
    home = tsih-robo-path;
    createHome = true;
    isSystemUser = true;
    group = config.users.groups.tsih.name;
    shell = "${pkgs.shadow}/bin/nologin";
  };

  users.groups.tsih.gid = config.users.users.tsih.uid;

  systemd.services = {
    tsih-robo-ktx = {
      unitConfig = {
        Description = "Discord bot mais fofa do mundo!";
        Documentation = [
          "https://github.com/matkijahenkilo/tsih-robo-ktx"
        ];
      };

      serviceConfig = {
        User = config.users.users.tsih.name;
        Group = config.users.users.tsih.group;
        WorkingDirectory = tsih-robo-path;
        ExecStart = "${inputs.tsih-robo-ktx.packages.${pkgs.stdenv.hostPlatform.system}.default}/bin/tsih-robo-ktx";
        Restart = "on-failure";
        RestartSec = "5s";
      };

      wantedBy = [
        "multi-user.target"
      ];
    };
  };
}
```

### Manually

Install Java JDK at least on version 21.0.5 and Maven

`git clone` this repository, then `cd` into it and run `mvn package`.

The .jar file will be located at `target/tsih-robo-ktx-VERSION-jar-with-dependencies.jar`.

Preferably rename and move that .jar into your bot folder and run it with `java -jar tsih-robo-ktx.jar`

## Huge thanks

This project wouldn't be possible without:

- People that developed JDA, JDA-ktx, lavaplayer and gallery-dl
- JDA Discord server's brilliant minds for helping me deal with my stupidity
- Asuran95 and rafaelrc7 for telling me the bot was based and that I should rewrite most of the scripts
- Ikuse for making incentivising Tsih art to keep the darkness away
- Riiya for not rebranding Tsih into Tewi in his server
- Lela for supporting this project
- My teacher for presenting me Kotlin
- People that use tsih-robo (and criticised her)
- SaucyBot for being a worthy opponent
- Other people that I forgot to mention
