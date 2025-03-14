package dog.ctf.contacts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dog.ctf.contacts.R
import dog.ctf.contacts.model.Department
import dog.ctf.contacts.utils.TextSizeHelper

/**
 * 科室列表适配器
 * 用于在RecyclerView中显示科室信息
 */
class DepartmentAdapter(
    private val context: Context,
    private var departments: List<Department>,
    private val onDepartmentClick: (Department) -> Unit
) : RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder>() {

    /**
     * 更新科室列表数据
     */
    fun updateDepartments(newDepartments: List<Department>) {
        this.departments = newDepartments
        notifyDataSetChanged()
    }
    
    /**
     * 更新所有项的字体大小
     */
    fun updateTextSizes() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_department, parent, false)
        return DepartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        val department = departments[position]
        holder.bind(department)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onDepartmentClick(department)
        }
    }

    override fun getItemCount(): Int = departments.size

    /**
     * 科室信息ViewHolder
     */
    class DepartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_department_name)
        private val countTextView: TextView = itemView.findViewById(R.id.tv_employee_count)

        fun bind(department: Department) {
            nameTextView.text = department.name
            countTextView.text = "${department.employeeCount}人"
            
            // 应用字体大小
            applyTextSizes()
        }
        
        /**
         * 应用字体大小
         */
        private fun applyTextSizes() {
            TextSizeHelper.applyTextSize(nameTextView, "title")
            TextSizeHelper.applyTextSize(countTextView, "body")
        }
    }
}
