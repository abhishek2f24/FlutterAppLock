package com.example.applock.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R
import com.example.applock.models.LockInfo

class LockedAppsAdapter(
    private val context: Context,
    private val onItemClick: (LockInfo) -> Unit
) : ListAdapter<LockInfo, LockedAppsAdapter.LockedAppViewHolder>(LockInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockedAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locked_app, parent, false)
        return LockedAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockedAppViewHolder, position: Int) {
        val lockInfo = getItem(position)
        holder.bind(lockInfo)
    }

    inner class LockedAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.app_name)
        private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        private val unlockIcon: ImageView = itemView.findViewById(R.id.unlock_icon)

        fun bind(lockInfo: LockInfo) {
            appName.text = lockInfo.appName

            // Get app icon
            try {
                val packageManager = context.packageManager
                appIcon.setImageDrawable(packageManager.getApplicationIcon(lockInfo.packageName))
            } catch (e: Exception) {
                appIcon.setImageResource(R.drawable.ic_lock)
            }

            // Set click listener for unlock icon
            unlockIcon.setOnClickListener {
                onItemClick(lockInfo)
            }

            // Set the whole item as clickable
            itemView.setOnClickListener {
                onItemClick(lockInfo)
            }
        }
    }

    private class LockInfoDiffCallback : DiffUtil.ItemCallback<LockInfo>() {
        override fun areItemsTheSame(oldItem: LockInfo, newItem: LockInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: LockInfo, newItem: LockInfo): Boolean {
            return oldItem.appName == newItem.appName
        }
    }
}