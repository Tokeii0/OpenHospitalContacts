package dog.ctf.contacts.style

/**
 * 应用主题样式管理类
 * 替代原来的style.py文件，提供主题和字体大小的配置
 */
object ThemeStyle {
    // 主题定义
    val themes = mapOf(
        "light" to mapOf(
            "name" to "浅色主题",
            "primary" to "#2196F3",  // 主色调 - 医院蓝
            "primary_dark" to "#1976D2",  // 深色调
            "accent" to "#FF5722",  // 强调色
            "background" to "#FFFFFF",  // 背景色
            "card_background" to "#F5F5F5",  // 卡片背景色
            "text_primary" to "#212121",  // 主文本色
            "text_secondary" to "#757575",  // 次要文本色
            "divider" to "#BDBDBD"  // 分隔线颜色
        ),
        "dark" to mapOf(
            "name" to "深色主题",
            "primary" to "#2196F3",  // 主色调 - 医院蓝
            "primary_dark" to "#1976D2",  // 深色调
            "accent" to "#FF5722",  // 强调色
            "background" to "#121212",  // 背景色
            "card_background" to "#1E1E1E",  // 卡片背景色
            "text_primary" to "#FFFFFF",  // 主文本色
            "text_secondary" to "#B0B0B0",  // 次要文本色
            "divider" to "#424242"  // 分隔线颜色
        )
    )

    // 字体大小配置
    var textSizes = mutableMapOf(
        "header" to "24sp",
        "title" to "20sp",
        "subtitle" to "18sp",
        "body" to "16sp",
        "caption" to "14sp"
    )
    
    // 字体大小配置 - 小号
    private val smallTextSizes = mapOf(
        "header" to "20sp",
        "title" to "18sp",
        "subtitle" to "16sp",
        "body" to "14sp",
        "caption" to "12sp"
    )
    
    // 字体大小配置 - 中号（默认）
    private val mediumTextSizes = mapOf(
        "header" to "24sp",
        "title" to "20sp",
        "subtitle" to "18sp",
        "body" to "16sp",
        "caption" to "14sp"
    )
    
    // 字体大小配置 - 大号
    private val largeTextSizes = mapOf(
        "header" to "28sp",
        "title" to "24sp",
        "subtitle" to "20sp",
        "body" to "18sp",
        "caption" to "16sp"
    )
    
    // 字体大小配置 - 特大号
    private val extraLargeTextSizes = mapOf(
        "header" to "32sp",
        "title" to "28sp",
        "subtitle" to "24sp",
        "body" to "20sp",
        "caption" to "18sp"
    )
    
    // 当前使用的颜色
    var colors = themes["light"] ?: error("默认主题不存在")

    // 间距
    val spacing = mapOf(
        "small" to "8dp",
        "medium" to "16dp",
        "large" to "24dp"
    )

    // 列表项样式
    val listItem = mapOf(
        "height" to "72dp",
        "padding" to "16dp"
    )

    // 搜索框样式
    val searchBar = mapOf(
        "height" to "48dp",
        "corner_radius" to "24dp",
        "elevation" to "2dp"
    )

    /**
     * 应用主题设置
     * @param themeName 主题名称，可选值：light, dark
     * @return 当前主题的颜色配置
     */
    fun applyTheme(themeName: String): Map<String, String> {
        themes[themeName]?.let {
            colors = it
        }
        return colors
    }

    /**
     * 应用字体大小设置
     * @param fontSize 字体大小类别：small, medium, large, extra_large
     */
    fun applyFontSize(fontSize: String) {
        textSizes.clear()
        
        val newSizes = when (fontSize) {
            "small" -> smallTextSizes
            "large" -> largeTextSizes
            "extra_large" -> extraLargeTextSizes
            else -> mediumTextSizes
        }
        
        textSizes.putAll(newSizes)
    }
    
    /**
     * 获取指定类型的文本大小
     * @param textType 文本类型
     * @return 文本大小（带sp单位）
     */
    fun getTextSize(textType: String): String {
        return textSizes[textType] ?: "16sp" // 默认返回16sp
    }
}
