package com.example.applock.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R
import com.example.applock.models.AppInfo

class AppListAdapter(private val onAppLockToggle: (AppInfo, Boolean) -> Unit) :
    ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = getItem(position)
        holder.bind(appInfo)
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        private val appName: TextView = itemView.findViewById(R.id.app_name)
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private val lockSwitch: Switch = itemView.findViewById(R.id.lock_switch)

        fun bind(appInfo: AppInfo) {
            appIcon.setImageDrawable(appInfo.appIcon)
            appName.text = appInfo.appName
            lockSwitch.isChecked = appInfo.isLocked

            lockSwitch.setOnCheckedChangeListener { _, isChecked ->
                appInfo.isLocked = isChecked
                onAppLockToggle(appInfo, isChecked)
            }

            // Set the whole item as clickable
            itemView.setOnClickListener {
                lockSwitch.toggle()
            }
        }
    }

    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.isLocked == newItem.isLocked &&
                    oldItem.appName == newItem.appName
        }
    }
}