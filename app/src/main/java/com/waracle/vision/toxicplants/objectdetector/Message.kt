package com.waracle.vision.toxicplants.objectdetector

interface Message {
    override fun toString(): String
}

class InfoMessage(val info: String): Message {

    override fun toString(): String = info

    companion object {
        fun String.toInfoMessage(): InfoMessage = InfoMessage(this)
    }
}