package ru.touchin.spizdev.models

abstract class Stamp(
    open val batteryLevel: Int,
    open val gpsPosition: GpsPosition?
)
