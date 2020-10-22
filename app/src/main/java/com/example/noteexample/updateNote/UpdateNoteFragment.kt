package com.example.noteexample.updateNote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentUpdateNoteBinding
import com.example.noteexample.insertNote.InsertNoteFragmentDirections
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UpdateNoteFragment : Fragment() {

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val args by navArgs<UpdateNoteFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentUpdateNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)

        val application = requireNotNull(this.activity).application
        val updateViewModelFactory = UpdateNoteViewModelFactory(args.noteId, application)
        val viewModel =
            ViewModelProvider(this, updateViewModelFactory)
                .get(UpdateNoteViewModel::class.java)

        /**
         * Initialize [OneNoteEditAdapter] for [FragmentUpdateNoteBinding.updateRecycler]
         */
        val noteAdapter = OneNoteEditAdapter()
        binding.updateRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    this.findNavController()
                        .navigate(
                            InsertNoteFragmentDirections
                                .actionEditNoteFragmentToGalleryFragment
                                    (args.noteId)
                        )
                    Log.e("requestId", "${args.noteId}")
                }
            }

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            if (it != null){
                val list = it.filter { list -> list.noteId == args.noteId }
                Log.e("photoList", list.toString())
                    //TODO Figure out the problem
                if (!viewModel.listInit){
                    viewModel.startNoteContentList = list
                    Log.e("note", viewModel.startNoteContentList[0].note)
                    viewModel.listInit = true
                }
                noteAdapter.submitList(list)
            }
        })

        val noteContentList = mutableListOf<NoteContent>()
        noteAdapter.holder.observe(viewLifecycleOwner, {adapter ->
            val item = noteAdapter.currentList[adapter.adapterPosition]
            noteContentList.add(item)
            adapter.binding.noteEditTextFirst.addTextChangedListener {
                item.note = it.toString()
                noteContentList[adapter.adapterPosition].note = item.note
                Log.e("noteInNav", viewModel.startNoteContentList[0].note)
            }
        })

        //TODO Rewrite in BindingAdapter
        viewModel.currentNote.observe(viewLifecycleOwner, {
            binding.titleUpdate.setText(it.title)
        })

        binding.toolbarNoteUpdate.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    val camera = Camera(requireActivity())
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    camera.dispatchTakePictureIntent(binding.editButton)
//                                     GlideApp.with(this)
//                                         .load(camera.currentPhotoPath)
//                                         .into(binding.image)
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        this.findNavController()
                                            .navigate(
                                                UpdateNoteFragmentDirections
                                                    .actionUpdateNoteFragmentToGalleryFragment
                                                        (args.noteId)
                                            )
                                        Log.e("permittionAccessID", "$args")
                                    } else {
                                        requestPermissionLauncher.launch(
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    }
                                }
                            }
                        }.show()

                    true
                }
                else -> false
            }
        }

        viewModel.navigateToOneNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {

                val title = binding.titleUpdate.text.toString()
                if (viewModel.startNoteContentList.size == noteContentList.size){
                    noteContentList.forEachIndexed { index, noteContent ->
                        Log.e("currentListNote", noteContent.note)
                        Log.e("startListNote", viewModel.startNoteContentList[index].note)
                        if (noteContent.note != viewModel.startNoteContentList[index].note){
                            viewModel.textChanged = true
                        }
                    }
                } else{
                    viewModel.sizeChanged = true
                }

                when {
                    viewModel.backPressed -> {
                        if (viewModel.sizeChanged || viewModel.textChanged){
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Сохранить изменения?")
                                .setNegativeButton("Нет") { _, _ ->
                                    this.findNavController().popBackStack()
                                    viewModel.onDoneNavigating()
                                }
                                .setPositiveButton("Да") { _, _ ->
                                    if (noteContentList.isNotEmpty()){
                                        viewModel.updateNoteContent(noteContentList)
                                        viewModel.updateCurrentNote(binding.titleUpdate.text.toString())
                                    } else if (noteContentList.isEmpty() && title.isEmpty()){
                                        viewModel.deleteUnused()
                                    }
                                    this.findNavController().popBackStack()
                                    viewModel.onDoneNavigating()
                                }.show()
                        } else {
                            this.findNavController().popBackStack()
                            viewModel.onDoneNavigating()
                        }
                    }
                    !viewModel.backPressed -> {
                        viewModel.updateNoteContent(noteContentList)
                        viewModel.updateCurrentNote(binding.titleUpdate.text.toString())
                        this.findNavController().popBackStack()
                        viewModel.onDoneNavigating()
                    }
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}