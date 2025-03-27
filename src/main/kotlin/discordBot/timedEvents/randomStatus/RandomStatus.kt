package org.matkija.bot.discordBot.timedEvents.randomStatus

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import org.matkija.bot.discordBot.abstracts.TimedEvent

class RandomStatus(private val jda: JDA): TimedEvent() {

    override val task: Runnable = Runnable {
        jda.presence.activity = Activity.customStatus(statusList.random())
    }

    private val statusList: List<String> = listOf(
        "なのら！",
        "にゅん～",
        "ルア語！",
        "にゅん~？",
        "月語。。。",
        "💙ｔ＋ｎ💜",
        "君が好きのら！",
        "ぴっぎゃーもぅ！",
        "もう歯磨きした？",
        "んにゅーん。。。",
        "私の月は青のら～！",
        "( ,,`･ω･´)ﾝﾝﾝ。。。月",
        "抱きしめるじゃないのら！",
        "んー、やーな感じなのら。",
        "おやすみ、おーーやすみ！",
        "わ、だらしねぇ大人なのら！",
        "ねねむちゃんは巧みなのら！",
        "1日に5回シコをするわすれないなのら！",
        "政府は僕の陰茎の後ろにあり、俺はあなたがそれを隠すのを手伝ってくれる必要があります。",

        "Nyuuun~?",
        "FF5858F0",
        "FFFF67FF",
        "ohoooooo.....",
        "Oh brother...",
        "H-Henno nora...",
        "[Made in Nanako]!",
        "Remember to be patient!",
        "WooooOOOOoooOOOooooooo...~",
        "I am watching your links nora 👁〰👁",
        "Did you brush your teeth today nora?",
        "Maybe I am pettanko after all... good.",
        "I wonder... What is Nanako studying today?",
        "It's a long way down, please use the stairs!",
        "Nooo! Don't turn me into a marketable plushie nora!",
        "Remember! Tsih O'Clock happens everyday at 9PM! (GMT)",
        "T-There's something behind you... Oh! It's Nanako-Chan!",
    )
}