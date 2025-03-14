package dog.ctf.contacts.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import dog.ctf.contacts.style.ThemeStyle

/**
 * 主题管理工具类
 * 负责应用主题和字体大小设置
 */
object ThemeManager {

    /**
     * 应用主题设置
     * @param themeMode 主题模式
     */
    fun applyTheme(themeMode: Int) {
        // 设置深色/浅色主题
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // 根据当前主题模式更新ThemeStyle中的颜色配置
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> ThemeStyle.applyTheme("dark")
            AppCompatDelegate.MODE_NIGHT_NO -> ThemeStyle.applyTheme("light")
            else -> {
                // 根据系统设置选择主题
                val currentNightMode = AppCompatDelegate.getDefaultNightMode()
                if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    ThemeStyle.applyTheme("dark")
                } else {
                    ThemeStyle.applyTheme("light")
                }
            }
        }
    }
    
    /**
     * 应用字体大小设置
     * @param context 上下文
     * @param fontSize 字体大小
     */
    fun applyFontSize(context: Context, fontSize: String) {
        // 应用字体大小设置到ThemeStyle
        ThemeStyle.applyFontSize(fontSize)
        
        // 保存设置到SharedPreferences
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("font_size", fontSize).apply()
    }
    
    /**
     * 判断当前是否为深色模式
     * @param context 上下文
     * @return 是否为深色模式
     */
    fun isDarkMode(context: Context): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // 根据系统设置判断
                val nightModeFlags = context.resources.configuration.uiMode and 
                        Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    /**
     * 初始化应用主题和字体大小
     * @param context 上下文
     */
    fun initTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        
        // 应用主题
        val themeMode = sharedPreferences.getInt(
            "theme_mode", 
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        applyTheme(themeMode)
        
        // 应用字体大小
        val fontSize = sharedPreferences.getString("font_size", "medium") ?: "medium"
        applyFontSize(context, fontSize)
    }
}
