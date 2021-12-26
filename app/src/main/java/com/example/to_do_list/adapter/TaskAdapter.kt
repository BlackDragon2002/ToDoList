package com.example.to_do_list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.to_do_list.data.Task
import com.example.to_do_list.databinding.ItemTaskBinding

class TaskAdapter(
    private val listener:OnItemClickListener
):ListAdapter<Task,TaskAdapter.taskViewHolder>(DiffCallBack()) {

    inner class taskViewHolder(private val binding: ItemTaskBinding):RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                root.setOnClickListener {
                    val position=adapterPosition
                    if(position!=RecyclerView.NO_POSITION){
                        val task=getItem(position)
                        listener.onItemClick(task)
                    }
                }
                cbCompleted.setOnClickListener {
                    val position=adapterPosition
                    if (position!=RecyclerView.NO_POSITION){
                        val task=getItem(position)
                        listener.onCheckBoxClick(task,cbCompleted.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task){
            binding.apply {
                cbCompleted.isChecked=task.completed
                tvName.text=task.name
                tvName.paint.isStrikeThruText=task.completed
                labelPriority.isVisible=task.important
            }
        }
    }
    class DiffCallBack : DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task)
        =oldItem.id==newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean
        =oldItem==newItem
    }

    interface OnItemClickListener{
        fun onItemClick(task:Task)
        fun onCheckBoxClick(task:Task,isChecked:Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=taskViewHolder(
        ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))

    override fun onBindViewHolder(holder: taskViewHolder, position: Int) {
        val currentItem=getItem(position)
        holder.bind(currentItem)
    }


}