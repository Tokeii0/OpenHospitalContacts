package dog.ctf.contacts.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dog.ctf.contacts.R
import dog.ctf.contacts.adapter.EmployeeAdapter
import dog.ctf.contacts.databinding.FragmentHomeBinding
import dog.ctf.contacts.model.Employee
import dog.ctf.contacts.ui.base.BaseFragment
import dog.ctf.contacts.utils.ContactsExporter

class HomeFragment : BaseFragment() {

    private val TAG = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: EmployeeAdapter
    private lateinit var contactsExporter: ContactsExporter
    
    private var actionMode: ActionMode? = null
    private var selectedEmployees = mutableListOf<Employee>()
    
    // 写入联系人权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限获取成功，导出选中的联系人
            exportSelectedContacts()
        } else {
            // 权限被拒绝
            Toast.makeText(requireContext(), "需要写入通讯录权限才能导出联系人", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        contactsExporter = ContactsExporter(requireContext())

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()
        
        // 启用选项菜单
        setHasOptionsMenu(true)
        
        // 加载员工数据
        homeViewModel.loadEmployees()

        return root
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_contacts -> {
                // 启动多选模式
                startSelectionMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = EmployeeAdapter(
            requireContext(),
            emptyList(),
            onEmployeeClick = { employee ->
                // 如果在选择模式下，则切换选择状态
                if (actionMode != null) {
                    toggleSelection(employee)
                } else {
                    // 否则拨打电话
                    callEmployee(employee)
                }
            },
            onEmployeeLongClick = { employee ->
                // 长按启动选择模式
                if (actionMode == null) {
                    startSelectionMode()
                    toggleSelection(employee)
                    true
                } else {
                    false
                }
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
        }
    }
    
    /**
     * 设置浮动按钮
     */
    private fun setupFab() {
        binding.fabExport.setOnClickListener {
            // 启动多选模式
            startSelectionMode()
        }
    }
    
    /**
     * 启动选择模式
     */
    private fun startSelectionMode() {
        actionMode = (activity as? androidx.appcompat.app.AppCompatActivity)?.startSupportActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.selection_menu, menu)
                binding.fabExport.hide()
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_export -> {
                        // 检查是否有选中的联系人
                        if (selectedEmployees.isEmpty()) {
                            Toast.makeText(requireContext(), "请先选择要导出的联系人", Toast.LENGTH_SHORT).show()
                            return true
                        }
                        
                        // 检查权限并导出
                        checkWriteContactsPermissionAndExport()
                        true
                    }
                    R.id.action_select_all -> {
                        // 全选
                        selectAllEmployees()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                // 退出选择模式
                actionMode = null
                selectedEmployees.clear()
                adapter.clearSelection()
                binding.fabExport.show()
            }
        })
    }
    
    /**
     * 切换选择状态
     */
    private fun toggleSelection(employee: Employee) {
        if (selectedEmployees.contains(employee)) {
            selectedEmployees.remove(employee)
        } else {
            selectedEmployees.add(employee)
        }
        
        adapter.toggleSelection(employee)
        updateSelectionTitle()
    }
    
    /**
     * 全选
     */
    private fun selectAllEmployees() {
        val allEmployees = adapter.getEmployees()
        selectedEmployees.clear()
        selectedEmployees.addAll(allEmployees)
        adapter.selectAll()
        updateSelectionTitle()
    }
    
    /**
     * 更新选择模式标题
     */
    private fun updateSelectionTitle() {
        actionMode?.title = "已选择 ${selectedEmployees.size} 项"
    }
    
    /**
     * 检查写入通讯录权限并导出
     */
    private fun checkWriteContactsPermissionAndExport() {
        when {
            // 已有权限
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                exportSelectedContacts()
            }
            // 应该显示权限说明
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("需要权限")
                    .setMessage("导出联系人需要写入通讯录权限，请授予此权限。")
                    .setPositiveButton("确定") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
            // 直接请求权限
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
            }
        }
    }
    
    /**
     * 导出选中的联系人
     */
    private fun exportSelectedContacts() {
        if (selectedEmployees.isEmpty()) {
            Toast.makeText(requireContext(), "请先选择要导出的联系人", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示确认对话框
        AlertDialog.Builder(requireContext())
            .setTitle("导出联系人")
            .setMessage("确定要将选中的 ${selectedEmployees.size} 个联系人导出到系统通讯录吗？")
            .setPositiveButton("确定") { _, _ ->
                // 在后台线程中导出联系人
                Thread {
                    var successCount = 0
                    var failCount = 0
                    
                    for (employee in selectedEmployees) {
                        // 检查联系人是否已存在
                        if (!contactsExporter.isContactExists(employee.name)) {
                            // 导出联系人
                            val success = contactsExporter.exportEmployeeToContacts(employee)
                            if (success) {
                                successCount++
                            } else {
                                failCount++
                            }
                        } else {
                            // 联系人已存在
                            failCount++
                        }
                    }
                    
                    // 在主线程中显示结果
                    activity?.runOnUiThread {
                        // 关闭选择模式
                        actionMode?.finish()
                        
                        // 显示结果
                        val message = "导出完成：成功 $successCount 个，失败 $failCount 个"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
            .setNegativeButton("取消", null)
            .show()
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
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                homeViewModel.searchEmployees(newText ?: "")
                return true
            }
        })
    }
    
    private fun observeViewModel() {
        homeViewModel.employees.observe(viewLifecycleOwner) { employees ->
            adapter.updateEmployees(employees)
            updateEmptyViewVisibility(employees.isEmpty())
        }
        
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 更新空视图的可见性
     */
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
        adapter.updateTextSizes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}