package org.ice1000.bililive.danmaku

import charlie.bililivelib.danmaku.datamodel.Danmaku
import charlie.bililivelib.danmaku.datamodel.GiveGiftInfo
import charlie.bililivelib.danmaku.datamodel.StartStopInfo
import charlie.bililivelib.danmaku.datamodel.WelcomeVipInfo
import charlie.bililivelib.danmaku.event.DanmakuEvent
import charlie.bililivelib.danmaku.event.DanmakuListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import org.intellij.lang.annotations.Language

class IntellijListener(
	private val project: Project?,
	private val watcher: (Long) -> Unit,
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
		val info = event.param as? StartStopInfo ?: return
		if (info.isLiving) show("---v^-v^-v^---v^-v^-v^---")
		else show("-------------------------")
	}

	override fun watcherCountEvent(event: DanmakuEvent) {
		val count = event.param as? Number ?: return
		watcher(count.toLong())
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
			@Language("HTML")
			val html = "<html><h1>${text.removePrefix(NOTIFY)}</h1></html>"
			showPopup(html, MessageType.INFO)
		} else if (text.startsWith(NOTIFY_ERROR)) {
			@Language("HTML")
			val html = "<html><h1>${text.removePrefix(NOTIFY_ERROR)}</h1></html>"
			showPopup(html, MessageType.ERROR)
		}
	}

	private fun showPopup(html: String, type: MessageType) {
		val ideFrame = WindowManager.getInstance().getIdeFrame(project) ?: return
		JBPopupFactory.getInstance()
			.createHtmlTextBalloonBuilder(html, type, null)
			.createBalloon()
			.showInCenterOf(ideFrame.component)
	}

	override fun statusEvent(event: DanmakuEvent) {
		show("status: ${event.kind}, ${event.param}")
	}

	override fun globalGiftEvent(event: DanmakuEvent) {
		show("global gift: ${event.kind}, ${event.param}")
	}
}
