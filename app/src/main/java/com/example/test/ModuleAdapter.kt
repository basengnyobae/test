package com.example.test

import android.util.Log
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModuleAdapter(
    private val moduleList: List<Module>,
    private val completedModules: List<String>,
    private val userRole: String,
    private val onChecked: (Module, Boolean) -> Unit,
    private val onDelete: (Module) -> Unit,
    private val onItemClick: ((Module) -> Unit)? = null
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvModuleTitle)
        val duration: TextView = itemView.findViewById(R.id.tvModuleDuration)
        val checkBox: CheckBox = itemView.findViewById(R.id.cbModuleCompleted)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteModule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = moduleList[position]
        Log.d("DEBUG", "MENGGAMBAR MODULE: ${module.title}")
        holder.title.text = module.title
        holder.duration.text = "${module.duration} menit"
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = completedModules.contains(module.id)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onChecked(module, isChecked)
        }

        if (userRole == "admin") {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDelete(module)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(module)


        }
    }

    override fun getItemCount(): Int = moduleList.size
}