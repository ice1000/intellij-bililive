package org.ice1000.bililive.danmaku

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object DanmakuBundle {
	@NonNls
	private const val BUNDLE = "org.ice1000.bililive.danmaku.bilibili-bundle"
	private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

	@JvmStatic
	fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
		AbstractBundle.message(bundle, key, *params)
}
