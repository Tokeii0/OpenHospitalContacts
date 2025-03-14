package dog.ctf.contacts.utils

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import dog.ctf.contacts.style.ThemeStyle

/**
 * 文本大小辅助工具类
 * 用于根据设置动态调整应用中的文本大小
 */
object TextSizeHelper {

    /**
     * 应用文本大小到TextView
     * @param textView 目标TextView
     * @param textType 文本类型，如"header", "title", "subtitle", "body", "caption"
     */
    fun applyTextSize(textView: TextView, textType: String) {
        val size = ThemeStyle.textSizes[textType] ?: return
        // 移除"sp"后转换为浮点数
        val sizeValue = size.replace("sp", "").toFloatOrNull() ?: return
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeValue)
    }
    
    /**
     * 获取指定类型的文本大小（以sp为单位）
     * @param textType 文本类型
     * @return 文本大小（浮点数，单位sp）
     */
    fun getTextSize(textType: String): Float {
        val size = ThemeStyle.textSizes[textType] ?: return 14f // 默认14sp
        return size.replace("sp", "").toFloatOrNull() ?: 14f
    }
    
    /**
     * 应用全局文本大小设置到所有TextView
     * 这个方法需要在Activity或Fragment中调用，遍历所有TextView并应用大小
     */
    fun applyGlobalTextSize(context: Context, fontSizeCategory: String) {
        // 保存字体大小设置
        val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("font_size", fontSizeCategory).apply()
        
        // 应用字体大小设置
        ThemeStyle.applyFontSize(fontSizeCategory)
    }
}
