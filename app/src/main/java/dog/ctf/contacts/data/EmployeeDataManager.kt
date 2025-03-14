package dog.ctf.contacts.data

import android.content.Context
import android.util.Log
import dog.ctf.contacts.model.Employee
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * 员工数据管理器
 * 负责从JSON文件中读取员工数据并提供查询功能
 */
class EmployeeDataManager(private val context: Context) {
    
    private val TAG = "EmployeeDataManager"
    private var employeeList: MutableList<Employee> = mutableListOf()
    private val localDataFile = File(context.filesDir, "local_employees.json")
    
    init {
        // 初始化时尝试加载本地保存的员工数据
        loadLocalEmployees()
    }
    
    /**
     * 从assets目录下的JSON文件中加载员工数据
     * @param fileName JSON文件名
     * @return 是否加载成功
     */
    fun loadEmployeesFromJson(fileName: String): Boolean {
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val employees = mutableListOf<Employee>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val employee = parseEmployeeFromJson(jsonObject)
                employees.add(employee)
            }
            
            // 合并从assets加载的数据和本地保存的数据
            val localEmployees = employeeList.toList()
            
            // 使用名字作为唯一标识，避免重复
            val existingNames = employees.map { it.name }
            val newLocalEmployees = localEmployees.filter { it.name !in existingNames }
            
            // 合并列表
            employees.addAll(newLocalEmployees)
            
            employeeList = employees
            return true
        } catch (e: IOException) {
            Log.e(TAG, "从assets加载员工数据失败: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 从本地文件加载员工数据
     */
    private fun loadLocalEmployees() {
        if (!localDataFile.exists()) {
            Log.d(TAG, "本地员工数据文件不存在")
            return
        }
        
        try {
            val jsonString = localDataFile.readText()
            val jsonArray = JSONArray(jsonString)
            val employees = mutableListOf<Employee>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val employee = parseEmployeeFromJson(jsonObject)
                employees.add(employee)
            }
            
            Log.d(TAG, "从本地文件加载了 ${employees.size} 个员工")
            employeeList = employees
        } catch (e: Exception) {
            Log.e(TAG, "加载本地员工数据失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 保存员工数据到本地文件
     */
    fun saveEmployeesToLocal() {
        try {
            val jsonArray = JSONArray()
            
            for (employee in employeeList) {
                val jsonObject = JSONObject().apply {
                    put("name", employee.name)
                    put("department", employee.department)
                    put("position", employee.position)
                    put("officePhone", employee.officePhone)
                    put("mobilePhone", employee.mobilePhone)
                }
                jsonArray.put(jsonObject)
            }
            
            // 确保目录存在
            localDataFile.parentFile?.mkdirs()
            
            // 写入文件
            localDataFile.writeText(jsonArray.toString())
            Log.d(TAG, "成功保存 ${employeeList.size} 个员工到本地文件")
        } catch (e: Exception) {
            Log.e(TAG, "保存员工数据到本地文件失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 更新员工列表
     */
    fun updateEmployeeList(newList: List<Employee>) {
        employeeList = newList.toMutableList()
        saveEmployeesToLocal()
    }
    
    /**
     * 从JSONObject解析员工数据
     */
    private fun parseEmployeeFromJson(jsonObject: JSONObject): Employee {
        return Employee(
            name = jsonObject.getString("name"),
            department = jsonObject.getString("department"),
            position = jsonObject.getString("position"),
            officePhone = jsonObject.getString("officePhone"),
            mobilePhone = jsonObject.getString("mobilePhone")
        )
    }
    
    /**
     * 获取所有员工列表
     */
    fun getAllEmployees(): List<Employee> {
        return employeeList
    }
    
    /**
     * 按姓名搜索员工
     */
    fun searchByName(query: String): List<Employee> {
        if (query.isEmpty()) return employeeList
        return employeeList.filter { it.name.contains(query, ignoreCase = true) }
    }
    
    /**
     * 按科室搜索员工
     */
    fun searchByDepartment(query: String): List<Employee> {
        if (query.isEmpty()) return employeeList
        return employeeList.filter { it.department.contains(query, ignoreCase = true) }
    }
    
    /**
     * 按职位搜索员工
     */
    fun searchByPosition(query: String): List<Employee> {
        if (query.isEmpty()) return employeeList
        return employeeList.filter { it.position.contains(query, ignoreCase = true) }
    }
    
    /**
     * 获取所有科室列表
     */
    fun getAllDepartments(): List<String> {
        return employeeList.map { it.department }.distinct().sorted()
    }
    
    /**
     * 获取所有职位列表
     */
    fun getAllPositions(): List<String> {
        return employeeList.map { it.position }.distinct().sorted()
    }
    
    /**
     * 综合搜索（姓名或科室）
     */
    fun search(query: String): List<Employee> {
        if (query.isEmpty()) return employeeList
        return employeeList.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.department.contains(query, ignoreCase = true)
        }
    }
}
