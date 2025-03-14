package dog.ctf.contacts.ui.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dog.ctf.contacts.utils.TextSizeHelper

/**
 * 基础Fragment类
 * 所有Fragment都应该继承这个类，以便统一应用字体大小设置
 */
abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 应用全局字体大小设置
        applyGlobalTextSize(view)
    }
    
    /**
     * 递归应用字体大小到所有TextView
     * @param view 视图
     */
    private fun applyGlobalTextSize(view: View) {
        if (view is ViewGroup) {
            // 递归处理所有子视图
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                applyGlobalTextSize(child)
            }
        } else if (view is TextView) {
            // 根据TextView的大小应用不同的文本类型
            when {
                view.textSize >= 24f -> TextSizeHelper.applyTextSize(view, "header")
                view.textSize >= 20f -> TextSizeHelper.applyTextSize(view, "title")
                view.textSize >= 18f -> TextSizeHelper.applyTextSize(view, "subtitle")
                view.textSize >= 16f -> TextSizeHelper.applyTextSize(view, "body")
                else -> TextSizeHelper.applyTextSize(view, "caption")
            }
        }
    }
    
    /**
     * 更新所有文本大小
     * 当字体大小设置改变时调用
     * 子类可以重写此方法来更新RecyclerView适配器等
     */
    open fun updateAllTextSizes() {
        view?.let { applyGlobalTextSize(it) }
    }
}
