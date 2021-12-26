package com.example.to_do_list.ui.task

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.to_do_list.R
import com.example.to_do_list.adapter.TaskAdapter
import com.example.to_do_list.data.SortOrder
import com.example.to_do_list.data.Task
import com.example.to_do_list.databinding.FragmentTasksBinding
import com.example.to_do_list.utils.exhaustive
import com.example.to_do_list.utils.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment: Fragment(R.layout.fragment_tasks),TaskAdapter.OnItemClickListener {

    private val viewModel: TaskViewModel by viewModels()

    private lateinit var binding: FragmentTasksBinding

    private lateinit var  searchView:SearchView

    private lateinit var taskAdapter: TaskAdapter

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentTasksBinding.bind(view)

        taskAdapter= TaskAdapter(this)

        setUpRecyclerView()

        viewModel.tasks.observe(viewLifecycleOwner){
            Log.d("Listing","$it")
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect {event->
                when(event){
                    is TaskViewModel.TaskEvent.ShowUndoDeleteTaskMessage->{
                        Snackbar.make(
                            requireView(),
                            "Task Deleted",
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction("UNDO"){
                            viewModel.onUndoClicked(event.task)
                        }.show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action=
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(null,"New Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action=
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(event.task,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(),event.message,Snackbar.LENGTH_SHORT).show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action=
                            TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
        binding.apply {
            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request"){_,bundle->
            val result=bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        setHasOptionsMenu(true)

    }

    private fun setUpRecyclerView(){
        taskAdapter= TaskAdapter(this)
        binding.rvTasks.apply {
            adapter=taskAdapter
            layoutManager=LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task=taskAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onTaskSwiped(task)
            }
        }).attachToRecyclerView(rv_tasks)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery=viewModel.searchQuery.value
        if(pendingQuery!=null&&pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value=it
        }
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hideCompletedTask).isChecked=
                viewModel.preferencesFlow.first().hideCompleted
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_sortByName->{
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sortByDate->{
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hideCompletedTask->{
                item.isChecked=!item.isChecked
                viewModel.onHideCompleted(item.isChecked)
                true
            }
            R.id.action_deleteCompletedTask->{
                viewModel.onDeleteAllCompletedTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task,isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}