package dog.ctf.contacts.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import dog.ctf.contacts.MainActivity
import dog.ctf.contacts.R
import dog.ctf.contacts.databinding.FragmentSettingsBinding
import dog.ctf.contacts.ui.base.BaseFragment
import dog.ctf.contacts.utils.TextSizeHelper

/**
 * 设置Fragment
 * 提供主题切换和字体大小设置功能
 */
class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        
        // 加载当前设置
        loadCurrentSettings()
        
        // 设置保存按钮点击事件
        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
        }
        
        // 设置字体大小选项的即时预览效果
        setupFontSizePreview()
    }
    
    /**
     * 设置字体大小选项的即时预览效果
     */
    private fun setupFontSizePreview() {
        // 小号字体
        binding.radioFontSmall.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyFontSizePreview("small")
            }
        }
        
        // 中号字体
        binding.radioFontMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyFontSizePreview("medium")
            }
        }
        
        // 大号字体
        binding.radioFontLarge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyFontSizePreview("large")
            }
        }
        
        // 特大字体
        binding.radioFontExtraLarge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyFontSizePreview("extra_large")
            }
        }
    }
    
    /**
     * 应用字体大小预览
     */
    private fun applyFontSizePreview(fontSize: String) {
        // 应用字体大小到当前页面的文本元素
        TextSizeHelper.applyGlobalTextSize(requireContext(), fontSize)
        
        // 更新当前页面的文本大小
        updateTextSizes()
        
        // 更新所有Fragment的文本大小
        updateAllFragmentsTextSize()
    }
    
    /**
     * 更新所有Fragment的文本大小
     */
    private fun updateAllFragmentsTextSize() {
        // 获取MainActivity实例
        val activity = requireActivity()
        if (activity is MainActivity) {
            // 调用MainActivity的方法更新所有文本大小
            activity.updateAllTextSizes()
        }
    }
    
    /**
     * 更新当前页面的文本大小
     */
    private fun updateTextSizes() {
        // 更新标题文本大小
        TextSizeHelper.applyTextSize(binding.textSettingsTitle, "header")
        
        // 更新卡片中的标题文本大小
        val themeTitle = view?.findViewById<TextView>(R.id.text_theme_title)
        val fontSizeTitle = view?.findViewById<TextView>(R.id.text_font_size_title)
            
        themeTitle?.let {
            TextSizeHelper.applyTextSize(it, "subtitle")
        }
        
        fontSizeTitle?.let {
            TextSizeHelper.applyTextSize(it, "subtitle")
        }
        
        // 更新按钮文本大小
        binding.buttonSaveSettings.textSize = TextSizeHelper.getTextSize("body")
        
        // 更新单选按钮文本大小
        binding.radioThemeLight.textSize = TextSizeHelper.getTextSize("body")
        binding.radioThemeDark.textSize = TextSizeHelper.getTextSize("body")
        binding.radioThemeSystem.textSize = TextSizeHelper.getTextSize("body")
        
        binding.radioFontSmall.textSize = TextSizeHelper.getTextSize("body")
        binding.radioFontMedium.textSize = TextSizeHelper.getTextSize("body")
        binding.radioFontLarge.textSize = TextSizeHelper.getTextSize("body")
        binding.radioFontExtraLarge.textSize = TextSizeHelper.getTextSize("body")
    }
    
    /**
     * 加载当前设置
     */
    private fun loadCurrentSettings() {
        // 加载主题设置
        val themeMode = viewModel.getThemeMode()
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioThemeDark.isChecked = true
            else -> binding.radioThemeSystem.isChecked = true
        }
        
        // 加载字体大小设置
        val fontSize = viewModel.getFontSize()
        when (fontSize) {
            "small" -> binding.radioFontSmall.isChecked = true
            "large" -> binding.radioFontLarge.isChecked = true
            "extra_large" -> binding.radioFontExtraLarge.isChecked = true
            else -> binding.radioFontMedium.isChecked = true
        }
        
        // 更新当前页面的文本大小
        updateTextSizes()
    }
    
    /**
     * 保存设置
     */
    private fun saveSettings() {
        // 保存主题设置
        val themeMode = when {
            binding.radioThemeLight.isChecked -> AppCompatDelegate.MODE_NIGHT_NO
            binding.radioThemeDark.isChecked -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        viewModel.setThemeMode(themeMode)
        
        // 保存字体大小设置
        val fontSize = when {
            binding.radioFontSmall.isChecked -> "small"
            binding.radioFontLarge.isChecked -> "large"
            binding.radioFontExtraLarge.isChecked -> "extra_large"
            else -> "medium"
        }
        viewModel.setFontSize(fontSize)
        
        // 应用设置
        applySettings(themeMode, fontSize)
        
        // 显示保存成功提示
        Toast.makeText(requireContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        
        // 重新创建活动以应用所有设置
        requireActivity().recreate()
    }
    
    /**
     * 应用设置
     */
    private fun applySettings(themeMode: Int, fontSize: String) {
        // 应用主题
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // 应用字体大小
        TextSizeHelper.applyGlobalTextSize(requireContext(), fontSize)
        
        // 更新所有Fragment的文本大小
        updateAllFragmentsTextSize()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
