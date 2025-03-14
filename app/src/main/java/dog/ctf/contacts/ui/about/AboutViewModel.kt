package dog.ctf.contacts.ui.about

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 关于页面ViewModel
 * 负责管理应用版本信息
 */
class AboutViewModel : ViewModel() {

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    /**
     * 加载应用版本信息
     */
    fun loadAppVersion(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _appVersion.value = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            _appVersion.value = "未知"
        }
    }
}
