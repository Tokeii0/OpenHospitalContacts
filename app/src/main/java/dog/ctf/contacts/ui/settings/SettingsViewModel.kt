package dog.ctf.contacts.ui.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel

/**
 * 设置ViewModel
 * 负责管理应用设置的读取和保存
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_FONT_SIZE = "font_size"
        
        // 默认值
        private const val DEFAULT_THEME_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        private const val DEFAULT_FONT_SIZE = "medium"
    }
    
    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 获取主题模式
     */
    fun getThemeMode(): Int {
        return sharedPreferences.getInt(KEY_THEME_MODE, DEFAULT_THEME_MODE)
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(mode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply()
    }
    
    /**
     * 获取字体大小
     */
    fun getFontSize(): String {
        return sharedPreferences.getString(KEY_FONT_SIZE, DEFAULT_FONT_SIZE) ?: DEFAULT_FONT_SIZE
    }
    
    /**
     * 设置字体大小
     */
    fun setFontSize(size: String) {
        sharedPreferences.edit().putString(KEY_FONT_SIZE, size).apply()
    }
}
