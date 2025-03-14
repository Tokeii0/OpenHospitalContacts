package dog.ctf.contacts.model

/**
 * 员工数据模型类
 * 用于存储医院员工的基本信息
 */
data class Employee(
    val name: String,          // 职工姓名
    val department: String,    // 科室
    val position: String,      // 职务
    val officePhone: String,   // 办公电话
    val mobilePhone: String    // 手机号码
)
