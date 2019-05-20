package ru.touchin.spizdev.models

data class SendStampBody(
    val batteryLevel: Int,
    val gpsPosition: GpsPosition?,
    val phoneId: String,
    val wiFiScans: List<WiFiScan>
)
