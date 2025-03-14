package dog.ctf.contacts.ui.departments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dog.ctf.contacts.R
import dog.ctf.contacts.adapter.DepartmentAdapter
import dog.ctf.contacts.databinding.FragmentDepartmentsBinding
import dog.ctf.contacts.model.Department
import dog.ctf.contacts.ui.base.BaseFragment

/**
 * 科室分类Fragment
 * 显示所有科室列表，点击科室可以查看该科室的所有员工
 */
class DepartmentsFragment : BaseFragment() {

    private var _binding: FragmentDepartmentsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var departmentsViewModel: DepartmentsViewModel
    private lateinit var departmentAdapter: DepartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        departmentsViewModel = ViewModelProvider(this)[DepartmentsViewModel::class.java]

        _binding = FragmentDepartmentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeViewModel()
        departmentsViewModel.loadDepartments()

        return root
    }
    
    private fun setupRecyclerView() {
        departmentAdapter = DepartmentAdapter(
            requireContext(),
            emptyList()
        ) { department ->
            // 点击科室时的处理
            navigateToDepartmentEmployees(department)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = departmentAdapter
        }
    }
    
    private fun observeViewModel() {
        // 观察科室数据变化
        departmentsViewModel.departments.observe(viewLifecycleOwner) { departments ->
            departmentAdapter.updateDepartments(departments)
            updateEmptyViewVisibility(departments.isEmpty())
        }
        
        // 观察加载状态
        departmentsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察错误消息
        departmentsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
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
    
    /**
     * 导航到科室员工列表页面
     */
    private fun navigateToDepartmentEmployees(department: Department) {
        val bundle = Bundle().apply {
            putString("departmentName", department.name)
        }
        findNavController().navigate(R.id.action_nav_departments_to_departmentDetailFragment, bundle)
    }

    /**
     * 更新所有文本大小
     * 重写父类方法，同时更新RecyclerView适配器
     */
    override fun updateAllTextSizes() {
        super.updateAllTextSizes()
        departmentAdapter.updateTextSizes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
