package com.johnny.wekit.theme.data

object ThemeColors {

    val ALL_KEYS: List<String> = listOf(
        "chat.actionbar.title_text",
        "chat.actionbar.unread_badge_text",
        "chat.input.send_text",
        "chat.input.bottom_text",
        "settings.actionbar.title_text",
        "home.actionbar.title_text",
        "home.tabs.selected_text",
        "home.tabs.unselected_text",
        "home.tabs.unread_badge_text",
        "home.tabs.unread_red_tip",
        "home.item.primary_text",
        "home.item.secondary_text",
        "home.conversation_item.primary_text",
        "home.conversation_item.secondary_text",
        "home.conversation_item.unread_badge_text",
        "home.conversation_item.red_tip",
        "home.taskbar.mask",
        "chat.long_press_menu.item_text",
        "chat.red_packet.open_text",
        "chat.input.hint_text",
        "chat.input.text",
        "chat.history_tongue.text",
        "chat.link_text",
        "chat.nickname_text",
        "chat.tips_text",
        "chat.plus_panel.icon_text",
        "chat.text_bubble.left_text",
        "chat.text_bubble.right_text",
        "chat.file_bubble.left_text",
        "chat.file_bubble.right_text",
        "chat.red_packet_bubble.left_text",
        "chat.red_packet_bubble.right_text",
        "chat.transfer_bubble.left_text",
        "chat.transfer_bubble.right_text",
        "chat.transfer_bubble.left_received_text",
        "chat.transfer_bubble.right_received_text",
        "settings.switch_thumb"
    )

    val DEFAULT_COLORS: Map<String, String> = ALL_KEYS.associateWith { "000000" }

    /** Group keys by sub-category (first two segments) */
    fun groupBySubCategory(keys: List<String> = ALL_KEYS): Map<String, List<String>> {
        return keys.groupBy { key ->
            val parts = key.split(".")
            if (parts.size >= 2) "${parts[0]}.${parts[1]}" else parts[0]
        }
    }

    /** Group label for display */
    fun groupLabel(groupKey: String): String {
        return when (groupKey) {
            "chat.actionbar" -> "聊天 - 操作栏"
            "chat.input" -> "聊天 - 输入框"
            "chat.long_press_menu" -> "聊天 - 长按菜单"
            "chat.red_packet" -> "聊天 - 红包"
            "chat.history_tongue" -> "聊天 - 历史记录"
            "chat.text_bubble" -> "聊天 - 文字气泡"
            "chat.file_bubble" -> "聊天 - 文件气泡"
            "chat.red_packet_bubble" -> "聊天 - 红包气泡"
            "chat.transfer_bubble" -> "聊天 - 转账气泡"
            "chat.link_text" -> "聊天 - 链接"
            "chat.nickname_text" -> "聊天 - 昵称"
            "chat.tips_text" -> "聊天 - 提示"
            "chat.plus_panel" -> "聊天 - 加号面板"
            "home.actionbar" -> "首页 - 操作栏"
            "home.tabs" -> "首页 - 标签栏"
            "home.item" -> "首页 - 列表项"
            "home.conversation_item" -> "首页 - 会话项"
            "home.taskbar" -> "首页 - 任务栏"
            "settings.actionbar" -> "设置 - 操作栏"
            "settings" -> "设置"
            else -> groupKey
        }
    }
}
