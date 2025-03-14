package dog.ctf.contacts.ui.departments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dog.ctf.contacts.data.EmployeeDataManager
import dog.ctf.contacts.model.Department

/**
 * 科室分类ViewModel
 * 负责管理科室数据和状态
 */
class DepartmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val employeeDataManager = EmployeeDataManager(application)
    
    private val _departments = MutableLiveData<List<Department>>()
    val departments: LiveData<List<Department>> = _departments
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * 加载科室数据
     */
    fun loadDepartments() {
        _isLoading.value = true
        try {
            val success = employeeDataManager.loadEmployeesFromJson("employees.json")
            if (success) {
                // 从员工数据中提取所有科室
                val allEmployees = employeeDataManager.getAllEmployees()
                val departmentMap = mutableMapOf<String, Int>()
                
                // 统计每个科室的员工数量
                allEmployees.forEach { employee ->
                    val count = departmentMap.getOrDefault(employee.department, 0)
                    departmentMap[employee.department] = count + 1
                }
                
                // 转换为Department对象列表
                val departmentList = departmentMap.map { (name, count) ->
                    Department(name, count)
                }.sortedBy { it.name }
                
                _departments.value = departmentList
                _errorMessage.value = null
            } else {
                _errorMessage.value = "加载科室数据失败"
            }
        } catch (e: Exception) {
            _errorMessage.value = "发生错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 获取指定科室的所有员工
     */
    fun getEmployeesByDepartment(departmentName: String) = 
        employeeDataManager.searchByDepartment(departmentName)
}
