package dog.ctf.contacts

import android.app.Application
import dog.ctf.contacts.utils.ThemeManager

/**
 * 应用程序类
 * 用于在应用启动时进行全局初始化
 */
class ContactsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化主题和字体大小
        ThemeManager.initTheme(this)
    }
}
