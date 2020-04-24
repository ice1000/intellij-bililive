package org.ice1000.bililive.danmaku

import charlie.bililivelib.danmaku.DanmakuReceiver
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
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
			val int = panel.roomId.text?.filter { it.isDigit() }?.toIntOrNull() ?: return@addActionListener
			receiver = listen(int).apply { initWith(project, document) }
		}
		panel.setContent(editor.component)

		(receiver ?: listen(937724).also { receiver = it }).apply {
			initWith(project, document)
		}
		val title = DanmakuBundle.message("danmaku.window.title")
		window.title = title
		window.contentManager.addContent(contentFactory.createContent(panel.component, title, false))
		Disposer.register(window.disposable, this)
	}

	private fun DanmakuReceiver.initWith(project: Project, document: Document) {
		WriteCommandAction.runWriteCommandAction(project) {
			document.deleteString(0, document.textLength)
		}
		addDanmakuListener(IntellijListener(project) { msg ->
			WriteCommandAction.runWriteCommandAction(project) {
				document.insertString(document.textLength, msg + "\n")
			}
		})
		connect()
	}
}
