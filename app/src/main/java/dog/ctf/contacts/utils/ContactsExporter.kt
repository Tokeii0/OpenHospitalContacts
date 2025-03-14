package dog.ctf.contacts.utils

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import dog.ctf.contacts.model.Employee

/**
 * 联系人导出工具类
 * 负责将员工数据导出到系统通讯录
 */
class ContactsExporter(private val context: Context) {
    
    private val TAG = "ContactsExporter"
    
    /**
     * 将单个员工导出到系统通讯录
     * @param employee 要导出的员工
     * @return 是否导出成功
     */
    fun exportEmployeeToContacts(employee: Employee): Boolean {
        try {
            Log.d(TAG, "开始导出联系人: ${employee.name}")
            
            val operations = ArrayList<ContentProviderOperation>()
            
            // 创建一个新的空联系人
            val contactOp = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
            operations.add(contactOp)
            
            // 添加联系人姓名
            val nameOp = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, employee.name)
                .build()
            operations.add(nameOp)
            
            // 添加公司信息
            val orgOp = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, employee.department)
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, employee.position)
                .build()
            operations.add(orgOp)
            
            // 添加手机号码（如果有）
            if (employee.mobilePhone.isNotEmpty()) {
                val mobileOp = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.mobilePhone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
                operations.add(mobileOp)
            }
            
            // 添加办公电话（如果有）
            if (employee.officePhone.isNotEmpty()) {
                val workOp = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, employee.officePhone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build()
                operations.add(workOp)
            }
            
            // 执行批量操作
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            
            Log.d(TAG, "联系人导出成功: ${employee.name}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "导出联系人失败: ${e.message}", e)
            return false
        }
    }
    
    /**
     * 检查联系人是否已存在于系统通讯录中
     * @param name 联系人姓名
     * @return 是否已存在
     */
    fun isContactExists(name: String): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            "${ContactsContract.Contacts.DISPLAY_NAME} = ?",
            arrayOf(name),
            null
        )
        
        val exists = cursor?.use { it.count > 0 } ?: false
        
        if (exists) {
            Log.d(TAG, "联系人已存在: $name")
        }
        
        return exists
    }
}
