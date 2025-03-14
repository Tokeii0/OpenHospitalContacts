package dog.ctf.contacts.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dog.ctf.contacts.data.EmployeeDataManager
import dog.ctf.contacts.model.Employee
import dog.ctf.contacts.utils.ContactsImporter

/**
 * 主页ViewModel
 * 负责管理员工数据和搜索状态
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "HomeViewModel"
    private val employeeDataManager = EmployeeDataManager(application)
    private val contactsImporter = ContactsImporter(application)
    
    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * 获取当前员工列表
     */
    fun getCurrentEmployees(): List<Employee> {
        return employeeDataManager.getAllEmployees()
    }
    
    /**
     * 更新员工列表
     */
    fun updateEmployeeList(newList: List<Employee>) {
        // 使用数据管理器更新列表并保存到本地
        employeeDataManager.updateEmployeeList(newList)
        
        // 更新LiveData
        _employees.postValue(newList)
        
        Log.d(TAG, "员工列表已更新，共 ${newList.size} 个员工")
    }
    
    /**
     * 加载员工数据
     */
    fun loadEmployees() {
        _isLoading.value = true
        try {
            val success = employeeDataManager.loadEmployeesFromJson("employees.json")
            if (success) {
                _employees.value = employeeDataManager.getAllEmployees()
                _errorMessage.value = null
            } else {
                _errorMessage.value = "加载员工数据失败"
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载员工数据时出错: ${e.message}")
            _errorMessage.value = "发生错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 搜索员工
     */
    fun searchEmployees(query: String) {
        _isLoading.value = true
        try {
            val results = if (query.isEmpty()) {
                employeeDataManager.getAllEmployees()
            } else {
                employeeDataManager.search(query)
            }
            _employees.value = results
        } catch (e: Exception) {
            Log.e(TAG, "搜索员工时出错: ${e.message}")
            _errorMessage.value = "搜索失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 导入本地通讯录联系人
     * @return 导入的联系人数量
     */
    fun importLocalContacts(): Int {
        Log.d(TAG, "开始导入本地联系人")
        try {
            // 导入联系人前设置加载状态
            _isLoading.postValue(true)
            
            // 导入联系人
            val importedContacts = contactsImporter.importContacts()
            Log.d(TAG, "从通讯录导入了 ${importedContacts.size} 个联系人")
            
            if (importedContacts.isNotEmpty()) {
                // 获取当前员工列表
                val currentEmployees = employeeDataManager.getAllEmployees().toMutableList()
                Log.d(TAG, "当前已有 ${currentEmployees.size} 个员工")
                
                // 添加导入的联系人（避免重复）
                val existingNames = currentEmployees.map { it.name }
                val newContacts = importedContacts.filter { it.name !in existingNames }
                Log.d(TAG, "新增加的非重复联系人: ${newContacts.size} 个")
                
                // 合并列表
                currentEmployees.addAll(newContacts)
                
                // 更新员工列表并保存到本地
                updateEmployeeList(currentEmployees)
                
                // 重置错误消息
                _errorMessage.postValue(null)
                
                // 导入完成后设置加载状态
                _isLoading.postValue(false)
                
                return newContacts.size
            }
            
            // 导入完成后设置加载状态
            _isLoading.postValue(false)
            return 0
        } catch (e: Exception) {
            Log.e(TAG, "导入联系人失败: ${e.message}", e)
            _errorMessage.postValue("导入联系人失败: ${e.message}")
            _isLoading.postValue(false)
            return 0
        }
    }
}