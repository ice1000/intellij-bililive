package org.ice1000.bililive.danmaku

import charlie.bililivelib.danmaku.datamodel.Danmaku
import charlie.bililivelib.danmaku.datamodel.GiveGiftInfo
import charlie.bililivelib.danmaku.datamodel.WelcomeVipInfo
import charlie.bililivelib.danmaku.event.DanmakuEvent
import charlie.bililivelib.danmaku.event.DanmakuListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager

class IntellijListener(
	private val project: Project?,
	private val show: (String) -> Unit
) : DanmakuListener {
	override fun errorEvent(event: DanmakuEvent) {
		val info = event.param as? Exception ?: return
		info.printStackTrace()
	}

	override fun globalAnnounceEvent(event: DanmakuEvent) {
		show("globalAnnounce: ${event.kind}, ${event.param}")
	}

	override fun startStopEvent(event: DanmakuEvent) {
		show("startStop: ${event.kind}, ${event.param}")
	}

	override fun watcherCountEvent(event: DanmakuEvent) {
		val count = event.param as? Number ?: return
		show("O_o +- (${count.toInt()})")
	}

	override fun welcomeVipEvent(event: DanmakuEvent) {
		val info = event.param as? WelcomeVipInfo ?: return
		if (info.isAdmin) show("==> ${info.username} <==")
		else show("-> ${info.username} <-")
	}

	override fun giveGiftEvent(event: DanmakuEvent) {
		val info = event.param as? GiveGiftInfo ?: return
		val bean = info.content ?: return
		show("@${bean.username} -> ${bean.count} ${bean.giftName} (/$${bean.price})")
	}

	override fun danmakuEvent(event: DanmakuEvent) {
		val info = event.param as? Danmaku ?: return
		showInIde(info.content)
		show("@${info.user.name}: ${info.content}")
	}

	private fun showInIde(text: String) {
		if (text.startsWith(NOTIFY)) {
			val ideFrame = WindowManager.getInstance().getIdeFrame(project) ?: return
			val html = "<html>${text.removePrefix(NOTIFY)}</html>"
			JBPopupFactory.getInstance()
				.createHtmlTextBalloonBuilder(html, MessageType.INFO, null)
				.createBalloon()
				.showInCenterOf(ideFrame.component)
		}
	}

	override fun statusEvent(event: DanmakuEvent) {
		show("status: ${event.kind}, ${event.param}")
	}

	override fun globalGiftEvent(event: DanmakuEvent) {
		show("global gift: ${event.kind}, ${event.param}")
	}
}
