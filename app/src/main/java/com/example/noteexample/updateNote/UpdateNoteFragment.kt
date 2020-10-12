package com.example.noteexample.updateNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.FragmentUpdateNoteBinding

class UpdateNoteFragment : Fragment() {

    private val args by navArgs<UpdateNoteFragmentArgs>()

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding : FragmentUpdateNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)


         //
         val application = requireNotNull(this.activity).application
         val updateViewModelFactory = UpdateNoteViewModelFactory(args.noteId, application)
         val viewModel =
             ViewModelProvider(this, updateViewModelFactory)
                 .get(UpdateNoteViewModel::class.java)

         binding.viewModel = viewModel

         viewModel.currentNote.observe(viewLifecycleOwner, {
             binding.titleEditTextUpdate.setText(it.title)
             binding.noteEditTextUpdate.setText(it.note)
         })

         viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
             if (it == true){
                 val title = binding.titleEditTextUpdate.text.toString()
                 val noteText = binding.noteEditTextUpdate.text.toString()
                 if (title.isNotEmpty() || noteText.isNotEmpty()){
                 val note = Note(id = args.noteId, title = title, note = noteText)
                     viewModel.onUpdate(note)
                 }

                 this.findNavController()
                     .navigate(UpdateNoteFragmentDirections.actionUpdateNoteFragmentToNoteFragment())
                 viewModel.onStopNavigating()
             }
         })

         binding.lifecycleOwner = this
        return binding.root
    }
}