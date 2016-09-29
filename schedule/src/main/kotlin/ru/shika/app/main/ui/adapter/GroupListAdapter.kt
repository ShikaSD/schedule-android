package ru.shika.app.main.ui.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pawegio.kandroid.find
import com.pawegio.kandroid.inflateLayout
import ru.shika.app.annotations.ActivityScope
import ru.shika.app.main.data.model.Group
import ru.shika.mamkschedule.R
import javax.inject.Inject

/**
 * Adapter for group list
 *
 * Created by ashikov on 27/09/2016.
 */
@ActivityScope
class GroupListAdapter @Inject constructor(
    private val activity: Activity
) : RecyclerView.Adapter<GroupViewHolder>() {

    private var items = emptyList<Group>()

    fun setItems(items: List<Group>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.id.text = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        GroupViewHolder(activity.inflateLayout(R.layout.fragment_list_item))

    override fun getItemCount() = items.size
}

class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val id: TextView
    val name: TextView

    init {
        id = view.find(R.id.item_list_id)
        name = view.find(R.id.item_list_main)
    }
}
