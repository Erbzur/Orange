package lsposed.orange.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import lsposed.orange.R
import lsposed.orange.model.Orientation

class AppListAdapter : ListAdapter<AppListItem, AppListAdapter.ViewHolder>(DiffItemCallback()) {

    var eventListener: EventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let {
            holder.packageName.text = it.packageName
            holder.name.text = it.name
            holder.icon.setImageDrawable(it.icon)
            if (it.orientation != Orientation.UNSPECIFIED) {
                holder.orientation.text = it.orientation.toLabel()
                holder.orientation.isVisible = true
                holder.view.setBackgroundColor(holder.view.context.getColor(R.color.config_app_item_bg))
            } else {
                holder.orientation.text = ""
                holder.orientation.isVisible = false
                holder.view.background = null
            }
            holder.view.setOnClickListener { _ ->
                eventListener?.onAppListItemClick(it)
            }
        }
    }

    class DiffItemCallback : DiffUtil.ItemCallback<AppListItem>() {
        override fun areItemsTheSame(oldItem: AppListItem, newItem: AppListItem) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppListItem, newItem: AppListItem) =
            oldItem == newItem
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val packageName: TextView = view.findViewById(R.id.package_name)
        val name: TextView = view.findViewById(R.id.name)
        val icon: ImageView = view.findViewById(R.id.icon)
        val orientation: TextView = view.findViewById(R.id.orientation)
    }

    interface EventListener {
        fun onAppListItemClick(appListItem: AppListItem)
    }
}