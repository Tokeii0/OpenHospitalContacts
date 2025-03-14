package dog.ctf.contacts.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import dog.ctf.contacts.model.Employee

/**
 * 通讯录导入工具类
 * 用于从设备通讯录读取联系人信息并转换为应用内的Employee对象
 */
class ContactsImporter(private val context: Context) {

    private val TAG = "ContactsImporter"

    /**
     * 从设备通讯录导入联系人
     * @return 导入的员工列表
     */
    fun importContacts(): List<Employee> {
        val employees = mutableListOf<Employee>()
        val contentResolver: ContentResolver = context.contentResolver
        var cursor: Cursor? = null
        
        try {
            Log.d(TAG, "开始导入联系人")
            
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
            
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    try {
                        // 安全获取列索引
                        val idIndex = getColumnIndexSafely(cursor, ContactsContract.Contacts._ID)
                        val nameIndex = getColumnIndexSafely(cursor, ContactsContract.Contacts.DISPLAY_NAME)
                        val hasPhoneIndex = getColumnIndexSafely(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        
                        // 检查索引是否有效
                        if (idIndex < 0 || nameIndex < 0 || hasPhoneIndex < 0) {
                            Log.e(TAG, "无效的列索引: idIndex=$idIndex, nameIndex=$nameIndex, hasPhoneIndex=$hasPhoneIndex")
                            continue
                        }
                        
                        // 安全获取值
                        val id = getStringSafely(cursor, idIndex)
                        val name = getStringSafely(cursor, nameIndex) ?: "未知姓名"
                        val hasPhoneStr = getStringSafely(cursor, hasPhoneIndex) ?: "0"
                        
                        // 检查联系人是否有电话号码
                        val hasPhone = try {
                            hasPhoneStr.toInt() > 0
                        } catch (e: Exception) {
                            Log.e(TAG, "转换hasPhone值失败: $hasPhoneStr, ${e.message}")
                            false
                        }
                        
                        if (hasPhone && id != null) {
                            // 查询电话号码
                            val employee = getPhoneNumbers(contentResolver, id, name)
                            if (employee != null) {
                                employees.add(employee)
                                Log.d(TAG, "添加联系人: ${employee.name}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "处理联系人时出错: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "导入联系人过程中发生异常: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                Log.e(TAG, "关闭cursor时出错: ${e.message}")
            }
        }
        
        Log.d(TAG, "导入完成，共导入 ${employees.size} 个联系人")
        return employees
    }
    
    /**
     * 获取联系人的电话号码并创建Employee对象
     */
    private fun getPhoneNumbers(contentResolver: ContentResolver, contactId: String, name: String): Employee? {
        var phoneCursor: Cursor? = null
        
        try {
            // 查询该联系人的所有电话号码
            phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )
            
            if (phoneCursor == null) {
                Log.e(TAG, "查询电话号码时cursor为null")
                return null
            }
            
            var mobilePhone = ""
            var officePhone = ""
            
            while (phoneCursor.moveToNext()) {
                try {
                    val phoneNumberIndex = getColumnIndexSafely(phoneCursor, ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneTypeIndex = getColumnIndexSafely(phoneCursor, ContactsContract.CommonDataKinds.Phone.TYPE)
                    
                    // 检查索引是否有效
                    if (phoneNumberIndex < 0 || phoneTypeIndex < 0) {
                        continue
                    }
                    
                    val phoneNumber = getStringSafely(phoneCursor, phoneNumberIndex) ?: ""
                    if (phoneNumber.isBlank()) {
                        continue
                    }
                    
                    val phoneType = try {
                        phoneCursor.getInt(phoneTypeIndex)
                    } catch (e: Exception) {
                        Log.e(TAG, "读取电话类型出错: ${e.message}")
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                    }
                    
                    // 根据电话类型分类
                    when (phoneType) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> {
                            mobilePhone = phoneNumber
                        }
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> {
                            officePhone = phoneNumber
                        }
                        // 如果没有特定类型，则优先设置手机号，其次设置办公电话
                        else -> {
                            if (mobilePhone.isEmpty()) {
                                mobilePhone = phoneNumber
                            } else if (officePhone.isEmpty()) {
                                officePhone = phoneNumber
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "处理电话号码时出错: ${e.message}")
                }
            }
            
            // 至少有一个电话号码才添加
            if (mobilePhone.isNotEmpty() || officePhone.isNotEmpty()) {
                // 创建员工对象
                return Employee(
                    name = name,
                    department = "导入联系人", // 默认部门
                    position = "未知", // 默认职位
                    officePhone = officePhone,
                    mobilePhone = mobilePhone
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取电话号码时出错: ${e.message}")
        } finally {
            try {
                phoneCursor?.close()
            } catch (e: Exception) {
                Log.e(TAG, "关闭phoneCursor时出错: ${e.message}")
            }
        }
        
        return null
    }
    
    /**
     * 安全获取列索引，避免抛出IllegalArgumentException
     */
    private fun getColumnIndexSafely(cursor: Cursor, columnName: String): Int {
        return try {
            cursor.getColumnIndex(columnName)
        } catch (e: Exception) {
            Log.e(TAG, "获取列索引出错: $columnName, ${e.message}")
            -1
        }
    }
    
    /**
     * 安全获取字符串值，避免抛出异常
     */
    private fun getStringSafely(cursor: Cursor, index: Int): String? {
        return try {
            cursor.getString(index)
        } catch (e: Exception) {
            Log.e(TAG, "获取字符串值出错: index=$index, ${e.message}")
            null
        }
    }
}
