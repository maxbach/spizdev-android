package ru.touchin.spizdev.models

data class SendStampBody(
    override val batteryLevel: Int,
    override val gpsPosition: GpsPosition?,
    val phoneId: String,
    val wiFiScans: List<WiFiScan>
) : Stamp(batteryLevel, gpsPosition)
