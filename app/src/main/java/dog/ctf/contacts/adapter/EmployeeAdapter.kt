package dog.ctf.contacts.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dog.ctf.contacts.R
import dog.ctf.contacts.model.Employee
import dog.ctf.contacts.utils.TextSizeHelper

/**
 * 员工列表适配器
 * 用于在RecyclerView中显示员工信息
 */
class EmployeeAdapter(
    private val context: Context,
    private var employees: List<Employee>,
    private val onEmployeeClick: (Employee) -> Unit,
    private val onEmployeeLongClick: (Employee) -> Boolean
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    // 选中的员工列表
    private val selectedEmployees = mutableSetOf<Employee>()
    
    /**
     * 更新员工列表数据
     */
    fun updateEmployees(newEmployees: List<Employee>) {
        this.employees = newEmployees
        selectedEmployees.clear()
        notifyDataSetChanged()
    }
    
    /**
     * 获取当前员工列表
     */
    fun getEmployees(): List<Employee> {
        return employees
    }
    
    /**
     * 切换选择状态
     */
    fun toggleSelection(employee: Employee) {
        if (selectedEmployees.contains(employee)) {
            selectedEmployees.remove(employee)
        } else {
            selectedEmployees.add(employee)
        }
        notifyDataSetChanged()
    }
    
    /**
     * 全选
     */
    fun selectAll() {
        selectedEmployees.clear()
        selectedEmployees.addAll(employees)
        notifyDataSetChanged()
    }
    
    /**
     * 清除选择
     */
    fun clearSelection() {
        selectedEmployees.clear()
        notifyDataSetChanged()
    }
    
    /**
     * 获取选中的员工列表
     */
    fun getSelectedEmployees(): List<Employee> {
        return selectedEmployees.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]
        holder.bind(employee, selectedEmployees.contains(employee))
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onEmployeeClick(employee)
        }
    }

    override fun getItemCount(): Int = employees.size
    
    /**
     * 更新所有文本大小
     */
    fun updateTextSizes() {
        notifyDataSetChanged()
    }

    /**
     * 员工信息ViewHolder
     */
    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
        private val departmentTextView: TextView = itemView.findViewById(R.id.tv_department)
        private val positionTextView: TextView = itemView.findViewById(R.id.tv_position)
        private val officePhoneTextView: TextView = itemView.findViewById(R.id.tv_office_phone)
        private val mobilePhoneTextView: TextView = itemView.findViewById(R.id.tv_mobile_phone)

        fun bind(employee: Employee, isSelected: Boolean) {
            // 设置文本内容
            nameTextView.text = employee.name
            departmentTextView.text = employee.department
            positionTextView.text = employee.position
            officePhoneTextView.text = "办公电话: ${employee.officePhone}"
            mobilePhoneTextView.text = "手机: ${employee.mobilePhone}"
            
            // 应用字体大小
            TextSizeHelper.applyTextSize(nameTextView, "title")
            TextSizeHelper.applyTextSize(departmentTextView, "subtitle")
            TextSizeHelper.applyTextSize(positionTextView, "caption")
            TextSizeHelper.applyTextSize(officePhoneTextView, "body")
            TextSizeHelper.applyTextSize(mobilePhoneTextView, "body")
            
            // 设置选中状态
            if (isSelected) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selected_item_background))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            }
        }
    }
}
