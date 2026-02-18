package org.matkijahenkilo.tsihRoboKtx.utils

private val colors = listOf(
    0xA7F3E1,
    0xF00707,
    0xF07807,
    0xF5D50E,
    0x9FEF0B,
    0x2AEF0B,
    0x0BEF80,
    0x0BEFD3,
    0x0A97E8,
    0x0A2CE8,
    0x610AE8,
    0xC60AE8,
    0x970AE8,
    0xE80AD0,
    0xF20D8A,
    0xF20D4B,
    0xEB0808,
    0x030303,
    0xFFFFFF,
    0xff80fd,
    0xfffd80,
    0x80fdff,
    0x80fffd,
    0xfdff80,
    0xfd80ff
)

fun getRandomColor(): Int = colors.random()