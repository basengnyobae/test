package com.example.test

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModuleAdapter(
    private val modules: List<Module>,
    private val completedIds: List<String>,
    private val onChecked: (Module, Boolean) -> Unit,
    private val onDelete: (Module) -> Unit // tambahan
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvModuleTitle)
        val checkBox: CheckBox = view.findViewById(R.id.cbComplete)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteModule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.title.text = module.title
        holder.checkBox.isChecked = completedIds.contains(module.id)

        holder.checkBox.setOnClickListener {
            onChecked(module, holder.checkBox.isChecked)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra("videoUrl", module.videoUrl)
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(module)
        }
    }

    override fun getItemCount(): Int = modules.size
}
