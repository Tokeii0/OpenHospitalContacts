package dog.ctf.contacts

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import dog.ctf.contacts.databinding.ActivityMainBinding
import dog.ctf.contacts.model.Employee
import dog.ctf.contacts.ui.home.HomeViewModel
import dog.ctf.contacts.utils.TextSizeHelper
import dog.ctf.contacts.utils.ThemeManager

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel
    
    // 联系人权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限获取成功，导入联系人
            importContactsDirectly()
        } else {
            // 权限被拒绝
            Toast.makeText(this, "需要通讯录权限才能导入联系人", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在设置内容视图之前初始化主题
        ThemeManager.initTheme(this)
        
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // 移除浮动按钮的点击事件，因为我们的通讯录应用不需要它
        binding.appBarMain.fab.hide()
        
        // 初始化ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        // 配置顶级导航目的地
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_departments, R.id.nav_positions, 
                R.id.nav_settings, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // 设置导航菜单项点击事件
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_import_contacts -> {
                    // 处理导入通讯录点击事件
                    Log.d(TAG, "导入联系人菜单项被点击")
                    checkContactsPermissionAndImport()
                    // 关闭抽屉
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    // 其他菜单项使用默认导航处理
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
        
        // 应用全局字体大小设置
        applyGlobalTextSize(binding.root)
        applyGlobalTextSize(binding.navView)
    }
    
    /**
     * 检查通讯录权限并导入
     */
    private fun checkContactsPermissionAndImport() {
        Log.d(TAG, "检查通讯录权限")
        try {
            when {
                // 已有权限
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "已有通讯录权限，开始导入")
                    importContactsDirectly()
                }
                // 应该显示权限说明
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                    Log.d(TAG, "需要显示权限说明")
                    Toast.makeText(
                        this,
                        "需要通讯录权限才能导入联系人",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
                // 直接请求权限
                else -> {
                    Log.d(TAG, "直接请求权限")
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查权限时发生错误: ${e.message}")
            Toast.makeText(this, "检查权限时发生错误: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 导入联系人 - 直接在主活动中实现
     */
    private fun importContactsDirectly() {
        // 显示加载提示
        Toast.makeText(this, "正在导入联系人...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "开始在主活动中直接导入联系人")
        
        // 在后台线程中导入联系人
        Thread {
            try {
                val importedContacts = readLocalContacts()
                Log.d(TAG, "读取到 ${importedContacts.size} 个联系人")
                
                // 测试：强制添加一个测试联系人
                val testContacts = mutableListOf<Employee>()
                testContacts.add(
                    Employee(
                        name = "测试联系人-${System.currentTimeMillis()}",
                        department = "测试部门",
                        position = "测试职位",
                        officePhone = "010-12345678",
                        mobilePhone = "13800138000"
                    )
                )
                Log.d(TAG, "添加了1个测试联系人")
                
                // 合并真实联系人和测试联系人
                val allContacts = importedContacts + testContacts
                
                val addedCount = if (allContacts.isNotEmpty()) {
                    // 获取当前员工列表
                    val currentEmployees = homeViewModel.getCurrentEmployees().toMutableList()
                    Log.d(TAG, "当前已有 ${currentEmployees.size} 个员工")
                    
                    // 添加导入的联系人（避免重复）
                    val existingNames = currentEmployees.map { it.name }
                    val newContacts = allContacts.filter { it.name !in existingNames }
                    Log.d(TAG, "过滤后的新联系人数量: ${newContacts.size}")
                    
                    if (newContacts.isNotEmpty()) {
                        // 添加新联系人
                        currentEmployees.addAll(newContacts)
                        
                        // 更新员工列表
                        homeViewModel.updateEmployeeList(currentEmployees)
                        newContacts.size
                    } else {
                        0
                    }
                } else {
                    0
                }
                
                // 切换回主线程更新UI
                runOnUiThread {
                    // 显示导入结果
                    val message = if (addedCount > 0) {
                        "成功导入 $addedCount 个联系人"
                    } else {
                        "没有新的联系人可导入"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "导入联系人失败: ${e.message}")
                e.printStackTrace()
                
                // 切换回主线程显示错误信息
                runOnUiThread {
                    Toast.makeText(this, "导入联系人失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    
    /**
     * 读取本地通讯录联系人
     */
    private fun readLocalContacts(): List<Employee> {
        val employees = mutableListOf<Employee>()
        val contentResolver: ContentResolver = contentResolver
        var cursor: Cursor? = null
        
        try {
            Log.d(TAG, "开始读取本地通讯录")
            
            // 查询联系人数据
            cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            
            if (cursor == null) {
                Log.e(TAG, "查询联系人时cursor为null")
                return employees
            }
            
            Log.d(TAG, "查询到 ${cursor.count} 个联系人")
            
            cursor.let { 
                if (it.count > 0) {
                    while (it.moveToNext()) {
                        try {
                            // 安全获取列索引
                            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                            
                            Log.d(TAG, "列索引: idIndex=$idIndex, nameIndex=$nameIndex, hasPhoneIndex=$hasPhoneIndex")
                            
                            if (idIndex < 0 || nameIndex < 0 || hasPhoneIndex < 0) {
                                Log.e(TAG, "无效的列索引")
                                continue
                            }
                            
                            val id = it.getString(idIndex)
                            val name = it.getString(nameIndex) ?: "未知姓名"
                            val hasPhone = it.getString(hasPhoneIndex)
                            
                            Log.d(TAG, "联系人: id=$id, name=$name, hasPhone=$hasPhone")
                            
                            // 确保联系人有电话号码
                            if (hasPhone.toInt() > 0) {
                                val phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id),
                                    null
                                )
                                
                                if (phoneCursor == null) {
                                    Log.e(TAG, "查询电话号码时cursor为null")
                                    continue
                                }
                                
                                Log.d(TAG, "联系人 $name 有 ${phoneCursor.count} 个电话号码")
                                
                                var mobilePhone = ""
                                var officePhone = ""
                                
                                phoneCursor.use { pCursor ->
                                    while (pCursor.moveToNext()) {
                                        try {
                                            val phoneNumberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                            val phoneTypeIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                            
                                            if (phoneNumberIndex < 0 || phoneTypeIndex < 0) {
                                                Log.e(TAG, "无效的电话号码列索引")
                                                continue
                                            }
                                            
                                            val phoneNumber = pCursor.getString(phoneNumberIndex) ?: ""
                                            if (phoneNumber.isBlank()) {
                                                Log.e(TAG, "空的电话号码")
                                                continue
                                            }
                                            
                                            val phoneType = pCursor.getInt(phoneTypeIndex)
                                            
                                            Log.d(TAG, "电话号码: number=$phoneNumber, type=$phoneType")
                                            
                                            when (phoneType) {
                                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> {
                                                    mobilePhone = phoneNumber
                                                    Log.d(TAG, "设置手机号: $phoneNumber")
                                                }
                                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> {
                                                    officePhone = phoneNumber
                                                    Log.d(TAG, "设置办公电话: $phoneNumber")
                                                }
                                                else -> {
                                                    if (mobilePhone.isEmpty()) {
                                                        mobilePhone = phoneNumber
                                                        Log.d(TAG, "设置其他类型为手机号: $phoneNumber")
                                                    } else if (officePhone.isEmpty()) {
                                                        officePhone = phoneNumber
                                                        Log.d(TAG, "设置其他类型为办公电话: $phoneNumber")
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "处理电话号码时出错: ${e.message}")
                                        }
                                    }
                                }
                                
                                // 至少有一个电话号码才添加
                                if (mobilePhone.isNotEmpty() || officePhone.isNotEmpty()) {
                                    val employee = Employee(
                                        name = name,
                                        department = "导入联系人",
                                        position = "未知",
                                        officePhone = officePhone,
                                        mobilePhone = mobilePhone
                                    )
                                    employees.add(employee)
                                    Log.d(TAG, "添加联系人: $name, 手机: $mobilePhone, 办公电话: $officePhone")
                                } else {
                                    Log.e(TAG, "联系人 $name 没有有效的电话号码")
                                }
                            } else {
                                Log.d(TAG, "联系人 $name 没有电话号码")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "处理联系人时出错: ${e.message}")
                        }
                    }
                } else {
                    Log.d(TAG, "通讯录中没有联系人")
                }
            }
            
            Log.d(TAG, "成功读取 ${employees.size} 个联系人")
        } catch (e: Exception) {
            Log.e(TAG, "读取通讯录时出错: ${e.message}", e)
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                Log.e(TAG, "关闭cursor时出错: ${e.message}")
            }
        }
        
        return employees
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 加载菜单
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    /**
     * 递归应用字体大小到所有TextView
     * @param view 视图
     */
    private fun applyGlobalTextSize(view: View) {
        if (view is ViewGroup) {
            // 递归处理所有子视图
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                applyGlobalTextSize(child)
            }
        } else if (view is TextView) {
            // 根据TextView的大小应用不同的文本类型
            when {
                view.textSize >= 24f -> TextSizeHelper.applyTextSize(view, "header")
                view.textSize >= 20f -> TextSizeHelper.applyTextSize(view, "title")
                view.textSize >= 18f -> TextSizeHelper.applyTextSize(view, "subtitle")
                view.textSize >= 16f -> TextSizeHelper.applyTextSize(view, "body")
                else -> TextSizeHelper.applyTextSize(view, "caption")
            }
        }
    }
    
    /**
     * 更新所有文本大小
     * 当字体大小设置改变时调用
     */
    fun updateAllTextSizes() {
        applyGlobalTextSize(binding.root)
        applyGlobalTextSize(binding.navView)
    }
}