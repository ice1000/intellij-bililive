package org.ice1000.bililive.danmaku

import charlie.bililivelib.danmaku.DanmakuReceiver
import com.intellij.icons.AllIcons
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BiliLiveToolWindowFactory : ToolWindowFactory, DumbAware, Disposable {
	@Volatile
	@get:Synchronized
	@set:Synchronized
	private var receiver: DanmakuReceiver? = null

	override fun isApplicable(project: Project) = !project.isDisposed

	private val document = EditorFactory.getInstance().createDocument("")
	private lateinit var editor: Editor
	private var roomId = DEFAULT_ID

	override fun dispose() {
		if (::editor.isInitialized) EditorFactory.getInstance().releaseEditor(editor)
		receiver?.disconnect()
		receiver = null
	}

	override fun createToolWindowContent(project: Project, window: ToolWindow) {
		val contentFactory = ContentFactory.SERVICE.getInstance()
		val panel = DanmakuWindow()
		editor = EditorFactory.getInstance().createViewer(document, project)
		panel.updateRoom.addActionListener {
			receiver?.disconnect()
			roomId = panel.roomId.text?.filter { it.isDigit() }?.toIntOrNull() ?: return@addActionListener
			receiver = listen(roomId).apply { initWith(project, panel, document) }
		}
		panel.setContent(editor.component)
		panel.roomId.text = roomId.toString()

		(receiver ?: listen(roomId).also { receiver = it }).apply {
			initWith(project, panel, document)
		}
		val toolWindowPanel = SimpleToolWindowPanel(false, false)
		toolWindowPanel.setContent(panel.component)
		val title = DanmakuBundle.message("danmaku.window.title")
		val group = DefaultActionGroup(
			object : DumbAwareAction(DanmakuBundle.message("danmaku.window.action.clear-all"), null, AllIcons.Actions.GC) {
				override fun actionPerformed(event: AnActionEvent) {
					WriteCommandAction.runWriteCommandAction(project) {
						document.deleteString(0, document.textLength)
					}
				}
			},
			object : DumbAwareAction(DanmakuBundle.message("danmaku.window.action.open-in-browser"), null, AllIcons.Ide.External_link_arrow) {
				override fun actionPerformed(event: AnActionEvent) {
					BrowserLauncher.instance.open("https://live.bilibili.com/$roomId")
				}
			})
		val toolbar = ActionManager.getInstance().createActionToolbar(title, group, true)
		toolWindowPanel.toolbar = toolbar.component
		window.title = title
		window.contentManager.addContent(contentFactory.createContent(toolWindowPanel, title, false))
		Disposer.register(window.disposable, this)
	}

	private fun DanmakuReceiver.initWith(
		project: Project,
		danmakuWindow: DanmakuWindow,
		document: Document
	) {
		WriteCommandAction.runWriteCommandAction(project) {
			document.deleteString(0, document.textLength)
		}
		addDanmakuListener(IntellijListener(project, danmakuWindow, document))
		connect()
	}
}
