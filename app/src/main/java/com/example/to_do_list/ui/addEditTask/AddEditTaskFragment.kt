package com.example.to_do_list.ui.addEditTask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.to_do_list.R
import com.example.to_do_list.databinding.FragmentAddEditTaskBinding
import com.example.to_do_list.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment:Fragment( R.layout.fragment_add_edit_task) {

    private lateinit var binding: FragmentAddEditTaskBinding

    private val viewModel: AddEditTaskViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            etTaskName.setText(viewModel.taskName)
            cbImportant.isChecked=viewModel.taskImportance
            cbImportant.jumpDrawablesToCurrentState()
            tvDateCreated.isVisible=viewModel.task != null
            tvDateCreated.text="Created ${viewModel.task?.createdDateFormatted}"

            etTaskName.addTextChangedListener {
                viewModel.taskName=it.toString()
            }

            cbImportant.setOnCheckedChangeListener{_,isChecked ->
                viewModel.taskImportance=isChecked
            }
            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event->
                when(event){
                    is AddEditTaskViewModel.AddEditEvent.NavigateBackWithResult -> {
                        binding.etTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditEvent.ShowInvalidMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }
    }

}