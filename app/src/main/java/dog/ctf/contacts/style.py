"""
xxxxx医院通讯录App样式定义文件
包含应用的颜色、字体、尺寸等主题相关定义
"""

# 主题定义
themes = {
    "light": {
        "name": "浅色主题",
        "primary": "#2196F3",  # 主色调 - 医院蓝
        "primary_dark": "#1976D2",  # 深色调
        "accent": "#FF5722",  # 强调色
        "background": "#FFFFFF",  # 背景色
        "card_background": "#F5F5F5",  # 卡片背景色
        "text_primary": "#212121",  # 主文本色
        "text_secondary": "#757575",  # 次要文本色
        "divider": "#BDBDBD",  # 分隔线颜色
    },
    "dark": {
        "name": "深色主题",
        "primary": "#2196F3",  # 主色调 - 医院蓝
        "primary_dark": "#1976D2",  # 深色调
        "accent": "#FF5722",  # 强调色
        "background": "#121212",  # 背景色
        "card_background": "#1E1E1E",  # 卡片背景色
        "text_primary": "#FFFFFF",  # 主文本色
        "text_secondary": "#B0B0B0",  # 次要文本色
        "divider": "#424242",  # 分隔线颜色
    },
}

# 颜色定义 (默认浅色主题)
colors = themes["light"]

# 字体大小配置
font_sizes = {
    "small": {
        "header": "18sp",
        "title": "16sp",
        "subtitle": "14sp",
        "body": "12sp",
        "caption": "10sp",
    },
    "medium": {
        "header": "20sp",
        "title": "18sp",
        "subtitle": "16sp",
        "body": "14sp",
        "caption": "12sp",
    },
    "large": {
        "header": "22sp",
        "title": "20sp",
        "subtitle": "18sp",
        "body": "16sp",
        "caption": "14sp",
    },
    "extra_large": {
        "header": "24sp",
        "title": "22sp",
        "subtitle": "20sp",
        "body": "18sp",
        "caption": "16sp",
    },
}

# 默认字体大小
text_sizes = font_sizes["medium"]

# 间距
spacing = {
    "small": "8dp",
    "medium": "16dp",
    "large": "24dp",
}

# 列表项样式
list_item = {
    "height": "72dp",
    "padding": "16dp",
}

# 搜索框样式
search_bar = {
    "height": "48dp",
    "corner_radius": "24dp",
    "elevation": "2dp",
}

# 设置主题和字体大小的函数
def apply_theme(theme_name):
    """
    应用主题设置
    :param theme_name: 主题名称，可选值：light, dark
    """
    global colors
    if theme_name in themes:
        colors = themes[theme_name]
    return colors

def apply_font_size(size_name):
    """
    应用字体大小设置
    :param size_name: 字体大小名称，可选值：small, medium, large, extra_large
    """
    global text_sizes
    if size_name in font_sizes:
        text_sizes = font_sizes[size_name]
    return text_sizes
