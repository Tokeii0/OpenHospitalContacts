package dog.ctf.contacts.ui.positions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dog.ctf.contacts.data.EmployeeDataManager
import dog.ctf.contacts.model.Position

/**
 * 职位分类ViewModel
 * 负责管理职位数据和状态
 */
class PositionsViewModel(application: Application) : AndroidViewModel(application) {

    private val employeeDataManager = EmployeeDataManager(application)
    
    private val _positions = MutableLiveData<List<Position>>()
    val positions: LiveData<List<Position>> = _positions
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * 加载职位数据
     */
    fun loadPositions() {
        _isLoading.value = true
        try {
            val success = employeeDataManager.loadEmployeesFromJson("employees.json")
            if (success) {
                // 从员工数据中提取所有职位
                val allEmployees = employeeDataManager.getAllEmployees()
                val positionMap = mutableMapOf<String, Int>()
                
                // 统计每个职位的员工数量
                allEmployees.forEach { employee ->
                    val count = positionMap.getOrDefault(employee.position, 0)
                    positionMap[employee.position] = count + 1
                }
                
                // 转换为Position对象列表
                val positionList = positionMap.map { (name, count) ->
                    Position(name, count)
                }.sortedBy { it.name }
                
                _positions.value = positionList
                _errorMessage.value = null
            } else {
                _errorMessage.value = "加载职位数据失败"
            }
        } catch (e: Exception) {
            _errorMessage.value = "发生错误: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 获取指定职位的所有员工
     */
    fun getEmployeesByPosition(positionName: String) = 
        employeeDataManager.searchByPosition(positionName)
}
