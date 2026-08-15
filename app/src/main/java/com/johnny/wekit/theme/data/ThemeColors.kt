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
}
