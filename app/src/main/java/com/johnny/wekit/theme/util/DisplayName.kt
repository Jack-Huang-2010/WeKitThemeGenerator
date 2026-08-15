package com.johnny.wekit.theme.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.johnny.wekit.theme.data.ImageSlot

/**
 * 资源显示名工具 — 全部走 strings.xml 查表，找不到 fallback 原始英文。
 *
 * 设计原则：
 * 1. 底层数据（ImageSlot.path、颜色 KEY、字符串 KEY）始终保持原始英文
 * 2. UI 显示层通过 `getIdentifier()` 查找 strings.xml 翻译
 * 3. 翻译 key 规则：
 *    - 图片槽位：slot_<path>  （path 的 / 和 .png 替换为 _）
 *    - 颜色 KEY：slot_color_<key>（key 的 . 替换为 _）
 *    - 字符串 KEY：slot_str_<key>（key 的 . 替换为 _）
 *    - 分类：category_<category>
 *    - 颜色分组：group_<group>
 * 4. 翻译失败 → 直接返回原始英文（永不崩溃、永不空白）
 * 5. 增加新资源时，只需要在 strings.xml 加翻译，不需要改 Kotlin 代码
 */
object DisplayName {

    // ============ 图片槽位 ============

    /**
     * 获取图片槽位的中文显示名。
     * @param path 原始英文路径，如 "home/background.png"
     * @return 翻译后的中文，找不到则返回原始 path
     */
    fun imageSlotName(context: Context, path: String): String {
        val resId = lookupString(context, "slot_" + deriveImageKey(path))
        return if (resId != 0) context.getString(resId) else path
    }

    @Composable
    fun rememberImageSlotName(path: String): String {
        val context = LocalContext.current
        return remember(path) { imageSlotName(context, path) }
    }

    /**
     * 获取 Slot 的分类名（splash/home/chat/plus/settings → 中文）
     */
    fun categoryName(context: Context, category: String): String {
        val resId = lookupString(context, "category_$category")
        return if (resId != 0) context.getString(resId) else category
    }

    @Composable
    fun rememberCategoryName(category: String): String {
        val context = LocalContext.current
        return remember(category) { categoryName(context, category) }
    }

    // ============ 颜色 KEY ============

    /**
     * 获取颜色 KEY 的中文显示名
     * @param key 原始英文 key，如 "home.tabs.selected_text"
     */
    fun colorKeyName(context: Context, key: String): String {
        val resId = lookupString(context, "slot_color_" + key.replace(".", "_"))
        return if (resId != 0) context.getString(resId) else key
    }

    @Composable
    fun rememberColorKeyName(key: String): String {
        val context = LocalContext.current
        return remember(key) { colorKeyName(context, key) }
    }

    /**
     * 颜色分组的中文名（groupKey 形如 "chat.input"）
     */
    fun colorGroupName(context: Context, groupKey: String): String {
        val resId = lookupString(context, "group_" + groupKey.replace(".", "_"))
        return if (resId != 0) context.getString(resId) else groupKey
    }

    @Composable
    fun rememberColorGroupName(groupKey: String): String {
        val context = LocalContext.current
        return remember(groupKey) { colorGroupName(context, groupKey) }
    }

    // ============ 字符串 KEY ============

    /**
     * 获取字符串 KEY 的中文显示名
     */
    fun stringKeyName(context: Context, key: String): String {
        val resId = lookupString(context, "slot_str_" + key.replace(".", "_"))
        return if (resId != 0) context.getString(resId) else key
    }

    @Composable
    fun rememberStringKeyName(key: String): String {
        val context = LocalContext.current
        return remember(key) { stringKeyName(context, key) }
    }

    // ============ ImageSlot helper ============

    /**
     * 一次性获取 Slot 的中文名 + 分类中文名
     */
    fun imageSlotDisplay(context: Context, slot: ImageSlot): String = imageSlotName(context, slot.path)

    @Composable
    fun rememberImageSlotDisplay(slot: ImageSlot): String {
        val context = LocalContext.current
        return remember(slot.path) { imageSlotName(context, slot.path) }
    }

    // ============ 内部工具 ============

    /**
     * 把图片路径转换为字符串资源 KEY
     * "home/background.png" → "home_background"
     * "chat/actionbar/back.png" → "chat_actionbar_back"
     */
    private fun deriveImageKey(path: String): String {
        return path
            .replace("/", "_")
            .replace(".png", "")
            .replace(".jpg", "")
            .replace(".webp", "")
            .replace(".PNG", "")
            .replace(".JPG", "")
            .replace(".WEBP", "")
    }

    /**
     * 在 strings.xml 中查找资源，找不到返回 0
     */
    private fun lookupString(context: Context, key: String): Int {
        return try {
            context.resources.getIdentifier(key, "string", context.packageName)
        } catch (e: Throwable) {
            // 任何异常都返回 0（fallback）
            0
        }
    }
}
