package ru.shika.app.common.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pawegio.kandroid.find
import com.pawegio.kandroid.inflateLayout
import ru.shika.app.common.model.BaseListModel
import ru.shika.mamkschedule.R

/**
 * Adapter for group list
 *
 * Created by ashikov on 27/09/2016.
 */
class SimpleListAdapter<in T : BaseListModel> (
    private val activity: Activity
) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items = emptyList<T>()

    fun setItems(items: List<T>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        holder.label.text = item.getLabel()
        holder.info.text  = item.getInfo()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        ItemViewHolder(activity.inflateLayout(R.layout.fragment_list_item))

    override fun getItemCount() = items.size
}

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val info  = view.find<TextView>(R.id.item_list_info)
    val label = view.find<TextView>(R.id.item_list_label)
}
