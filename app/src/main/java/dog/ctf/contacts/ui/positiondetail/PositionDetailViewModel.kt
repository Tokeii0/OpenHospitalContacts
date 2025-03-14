package dog.ctf.contacts.ui.positiondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dog.ctf.contacts.data.EmployeeDataManager
import dog.ctf.contacts.model.Employee

/**
 * 职位详情ViewModel
 * 负责管理特定职位的员工数据和搜索状态
 */
class PositionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val employeeDataManager = EmployeeDataManager(application)
    
    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * 加载特定职位的员工数据
     */
    fun loadEmployeesByPosition(positionName: String) {
        _isLoading.value = true
        try {
            val success = employeeDataManager.loadEmployeesFromJson("employees.json")
            if (success) {
                _employees.value = employeeDataManager.searchByPosition(positionName)
                _errorMessage.value = null
            } else {
                _errorMessage.value = "加载员工数据失败"
            }
        } catch (e: Exception) {
            _errorMessage.value = "发生错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 在特定职位内搜索员工
     */
    fun searchEmployeesInPosition(positionName: String, query: String) {
        _isLoading.value = true
        try {
            val positionEmployees = employeeDataManager.searchByPosition(positionName)
            if (query.isEmpty()) {
                _employees.value = positionEmployees
            } else {
                _employees.value = positionEmployees.filter { 
                    it.name.contains(query, ignoreCase = true) 
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "搜索错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
