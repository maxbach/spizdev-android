package ru.touchin.spizdev.models

data class WiFiScan(
    val name: String,
    val macAddress: String,
    val frequencyInMhz: Int,
    val levelInDb: Int
)
