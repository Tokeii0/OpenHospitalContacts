package dog.ctf.contacts.ui.positions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dog.ctf.contacts.R
import dog.ctf.contacts.adapter.PositionAdapter
import dog.ctf.contacts.databinding.FragmentPositionsBinding
import dog.ctf.contacts.model.Position
import dog.ctf.contacts.ui.base.BaseFragment

/**
 * 职位分类Fragment
 * 显示所有职位列表，点击职位可以查看该职位的所有员工
 */
class PositionsFragment : BaseFragment() {

    private var _binding: FragmentPositionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var positionsViewModel: PositionsViewModel
    private lateinit var positionAdapter: PositionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        positionsViewModel = ViewModelProvider(this)[PositionsViewModel::class.java]

        _binding = FragmentPositionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeViewModel()
        positionsViewModel.loadPositions()

        return root
    }

    private fun setupRecyclerView() {
        positionAdapter = PositionAdapter(requireContext(), emptyList()) { position ->
            // 点击职位时的处理
            navigateToPositionEmployees(position)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = positionAdapter
        }
    }
    
    private fun observeViewModel() {
        // 观察职位数据变化
        positionsViewModel.positions.observe(viewLifecycleOwner) { positions ->
            positionAdapter.updatePositions(positions)
            updateEmptyViewVisibility(positions.isEmpty())
        }
        
        // 观察加载状态
        positionsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察错误消息
        positionsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun navigateToPositionEmployees(position: Position) {
        // 使用Navigation组件导航到职位详情页面
        val bundle = Bundle().apply {
            putString("position_name", position.name)
        }
        // 导航到职位详情页面，并传递职位名称
        findNavController().navigate(R.id.action_nav_positions_to_positionDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
