package ru.touchin.spizdev.models

import ru.touchin.spizdev.models.enums.PhoneOs

data class Phone(
    val id: String,
    val model: String,
    val os: PhoneOs,
    val osVersion: String
)
