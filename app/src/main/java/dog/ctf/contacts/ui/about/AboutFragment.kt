package dog.ctf.contacts.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dog.ctf.contacts.databinding.FragmentAboutBinding
import dog.ctf.contacts.ui.base.BaseFragment

/**
 * 关于页面Fragment
 * 显示应用的基本信息和版本
 */
class AboutFragment : BaseFragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var aboutViewModel: AboutViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        aboutViewModel = ViewModelProvider(this)[AboutViewModel::class.java]

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 观察版本信息
        aboutViewModel.appVersion.observe(viewLifecycleOwner) { version ->
            binding.textVersion.text = "版本: $version"
        }

        // 加载版本信息
        aboutViewModel.loadAppVersion(requireContext())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
