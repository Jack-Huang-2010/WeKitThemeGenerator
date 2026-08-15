package com.johnny.wekit.theme.data

object ThemeStrings {

    val ALL_KEYS: List<String> = listOf(
        "home.tabs.wechat_title",
        "home.tabs.contact_title",
        "home.tabs.discovery_title",
        "home.tabs.me_title",
        "chat.input.hint"
    )

    val DEFAULT_STRINGS: Map<String, String> = mapOf(
        "home.tabs.wechat_title" to "微信",
        "home.tabs.contact_title" to "通讯录",
        "home.tabs.discovery_title" to "发现",
        "home.tabs.me_title" to "我",
        "chat.input.hint" to ""
    )

    fun displayName(key: String): String {
        return when (key) {
            "home.tabs.wechat_title" -> "微信标签"
            "home.tabs.contact_title" -> "通讯录标签"
            "home.tabs.discovery_title" -> "发现标签"
            "home.tabs.me_title" -> "我标签"
            "chat.input.hint" -> "聊天输入提示"
            else -> key
        }
    }
}
