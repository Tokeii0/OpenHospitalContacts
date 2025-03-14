package dog.ctf.contacts.ui.departmentdetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dog.ctf.contacts.adapter.EmployeeAdapter
import dog.ctf.contacts.databinding.FragmentDepartmentDetailBinding
import dog.ctf.contacts.model.Employee
import dog.ctf.contacts.ui.base.BaseFragment

/**
 * 科室详情Fragment
 * 显示特定科室的所有员工
 */
class DepartmentDetailFragment : BaseFragment() {

    private var _binding: FragmentDepartmentDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DepartmentDetailViewModel
    private lateinit var employeeAdapter: EmployeeAdapter
    
    // 从参数中获取科室名称
    private val departmentName: String by lazy {
        arguments?.getString("departmentName") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepartmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[DepartmentDetailViewModel::class.java]
        
        // 设置标题
        binding.searchView.queryHint = "$departmentName 员工列表"
        
        // 初始化RecyclerView
        setupRecyclerView()
        
        // 设置搜索功能
        setupSearchView()
        
        // 观察数据变化
        observeViewModel()
        
        // 加载科室员工数据
        viewModel.loadEmployeesByDepartment(departmentName)
    }
    
    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter(
            requireContext(), 
            emptyList(),
            onEmployeeClick = { employee ->
                // 处理员工点击事件，拨打电话
                callEmployee(employee)
            },
            onEmployeeLongClick = { _ ->
                // 部门详情页面不需要长按功能
                false
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
        }
    }
    
    /**
     * 拨打员工电话
     */
    private fun callEmployee(employee: Employee) {
        // 优先使用手机号码，如果没有则使用办公电话
        val phoneNumber = if (employee.mobilePhone.isNotEmpty()) {
            employee.mobilePhone
        } else {
            employee.officePhone
        }
        
        // 创建拨号意图
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        
        // 启动拨号界面
        startActivity(dialIntent)
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchEmployeesInDepartment(departmentName, it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchEmployeesInDepartment(departmentName, it) }
                return true
            }
        })
    }
    
    private fun observeViewModel() {
        // 观察员工数据变化
        viewModel.employees.observe(viewLifecycleOwner) { employees ->
            employeeAdapter.updateEmployees(employees)
            updateEmptyViewVisibility(employees.isEmpty())
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        binding.textEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    /**
     * 更新所有文本大小
     * 重写父类方法，同时更新RecyclerView适配器
     */
    override fun updateAllTextSizes() {
        super.updateAllTextSizes()
        employeeAdapter.updateTextSizes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
