package org.ice1000.bililive.danmaku

import charlie.bililivelib.danmaku.DanmakuReceiver
import charlie.bililivelib.danmaku.dispatch.*
import charlie.bililivelib.room.Room

fun main() {
	listen(937724).apply {
		addDanmakuListener(IntellijListener(null, ::println))
		connect()
	}
}

const val NOTIFY = "!!"

fun listen(id: Int): DanmakuReceiver {
	val receiver = DanmakuReceiver(Room.getRealRoomID(id))
	receiver.dispatchManager.apply {
		registerDispatcher(WelcomeVipDispatcher())
		registerDispatcher(DanmakuDispatcher())
		registerDispatcher(GlobalAnnounceDispatcher())
		registerDispatcher(GlobalGiftDispatcher())
		registerDispatcher(GiveGiftDispatcher())
		registerDispatcher(StartStopDispatcher())
	}
	return receiver
}
