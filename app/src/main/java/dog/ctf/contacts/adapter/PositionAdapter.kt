package dog.ctf.contacts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dog.ctf.contacts.R
import dog.ctf.contacts.model.Position

/**
 * 职位列表适配器
 * 用于在RecyclerView中显示职位信息
 */
class PositionAdapter(
    private val context: Context,
    private var positions: List<Position>,
    private val onPositionClick: (Position) -> Unit
) : RecyclerView.Adapter<PositionAdapter.PositionViewHolder>() {

    /**
     * 更新职位列表数据
     */
    fun updatePositions(newPositions: List<Position>) {
        this.positions = newPositions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_position, parent, false)
        return PositionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PositionViewHolder, position: Int) {
        val positionItem = positions[position]
        holder.bind(positionItem)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onPositionClick(positionItem)
        }
    }

    override fun getItemCount(): Int = positions.size

    /**
     * 职位信息ViewHolder
     */
    class PositionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_position_name)
        private val countTextView: TextView = itemView.findViewById(R.id.tv_employee_count)

        fun bind(position: Position) {
            nameTextView.text = position.name
            countTextView.text = "${position.employeeCount}人"
        }
    }
}
