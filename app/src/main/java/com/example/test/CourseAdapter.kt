package com.example.test

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CourseAdapter(private val courseList: MutableList<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val instructor: TextView = itemView.findViewById(R.id.tvInstructor)
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.title.text = course.title
        holder.instructor.text = course.instructor
        Glide.with(holder.itemView).load(course.thumbnailUrl).into(holder.thumbnail)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailCourseActivity::class.java)
            intent.putExtra("title", course.title)
            intent.putExtra("instructor", course.instructor)
            intent.putExtra("thumbnailUrl", course.thumbnailUrl)
            intent.putExtra("id", course.id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = courseList.size

    fun updateList(newList: List<Course>) {
        courseList.clear()
        courseList.addAll(newList)
        notifyDataSetChanged()
    }
}
