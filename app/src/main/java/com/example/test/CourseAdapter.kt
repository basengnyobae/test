package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CourseAdapter(
    private val _courseList: MutableList<Course>,
    private val onClick: ((Course) -> Unit)? = null
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val instructor: TextView = itemView.findViewById(R.id.tvInstructor)
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val price: TextView = itemView.findViewById(R.id.tvCoursePrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = _courseList[position]

        holder.title.text = course.title
        holder.instructor.text = course.instructor
        Glide.with(holder.itemView).load(course.thumbnailUrl).into(holder.thumbnail)

        if (course.price == 0L) {
            holder.price.text = "Gratis"
        } else {
            holder.price.text = "Rp ${course.price}"
        }


        holder.itemView.setOnClickListener {
            onClick?.invoke(course)
        }
    }

    override fun getItemCount(): Int = _courseList.size

    fun updateList(newList: List<Course>) {
        _courseList.clear()
        _courseList.addAll(newList)
        notifyDataSetChanged()
    }
}