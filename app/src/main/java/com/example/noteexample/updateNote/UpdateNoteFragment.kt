package com.example.noteexample.updateNote

import android.Manifest
import android.app.Application
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
import com.example.noteexample.utils.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentUpdateNoteBinding
import com.example.noteexample.insertNote.InsertNoteFragmentDirections
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UpdateNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val args by navArgs<UpdateNoteFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentUpdateNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)
        val application: Application = requireNotNull(this.activity).application
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
                                    viewModel.insertPhoto(camera.currentPhotoPath)
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

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            /**
             * Here, I have to do new objects for each [UpdateNoteViewModel.startNoteContentList]
             * because if it equals to this list, [NoteContent.note] and [NoteContent.photoPath] will
             * reflect changes in [UpdateNoteViewModel.startNoteContentList],
             * it's caused by var fields in [NoteContent]
             */
            val list = it.filter { list -> list.noteId == args.noteId }
            if (!viewModel.startListInit) {
                list.forEach { element ->
                    val noteContent = NoteContent(
                        id = element.id,
                        noteId = element.noteId,
                        note = element.note,
                        photoPath = element.photoPath,
                    )
                    viewModel.startNoteContentList.add(noteContent)
                }
                viewModel.startListInit = true
            }
            if (it != null) {
                Log.e("currentNote", "${viewModel.currentNote?.id}")
                Log.e("photoList", list.toString())
                noteAdapter.addHeaderAndSubmitList(viewModel.currentNote, list)
            }
        })

        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            val item = noteAdapter.currentList[holder.adapterPosition].noteContent

            item?.let {
                viewModel.noteContentList.add(it)
                holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                    it.note = editable.toString()
                    viewModel.noteContentList[holder.adapterPosition - 1].note = item.note
                    if (it.note.isEmpty() && it.photoPath.isEmpty()){
                        viewModel.deleteNoteContent(it)
                    }
                    Log.e("noteInNav", viewModel.startNoteContentList[0].note)
                }
            }
            holder.binding.deleteCircle.setOnClickListener {
                item?.let { current ->
                    current.photoPath = ""
                    if (current.note.isEmpty()){
                        viewModel.deleteNoteContent(current)
                    }
                    noteAdapter.notifyDataSetChanged()
                }
            }
        })

        var newTitle = ""
        var newFirstNote = ""
        noteAdapter.noteHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                newTitle = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                newFirstNote = it.toString()
            }
        }

        fun checkEmpty(){
            viewModel.updateCurrentNote(newTitle, newFirstNote)
            if (viewModel.noteContentList.isNotEmpty()) {
                viewModel.updateNoteContent(viewModel.noteContentList)
            } else if (viewModel.noteContentList.isEmpty() &&
                newTitle.isEmpty() &&
                newFirstNote.isEmpty()
            ) {
                viewModel.deleteUnused()
            }
            this.findNavController()
                .navigate(UpdateNoteFragmentDirections
                    .actionUpdateNoteFragmentToAllNotesFragment())
            viewModel.onDoneNavigating()
        }

        viewModel.navigateToOneNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                Log.e("startNoteContentList", "${viewModel.startNoteContentList.size}")
                Log.e("noteContentList", "${viewModel.noteContentList.size}")
                if (viewModel.startNoteContentList.size == viewModel.noteContentList.size) {
                    viewModel.noteContentList.forEachIndexed { index, noteContent ->
                        Log.e("currentListNote", noteContent.note)
                        Log.e("startListNote", viewModel.startNoteContentList[index].note)
                        if (noteContent != viewModel.startNoteContentList[index]) {
                            viewModel.textChanged = true
                        }
                    }
                } else {
                    viewModel.sizeChanged = true
                }

                when {
                    viewModel.backPressed -> {
                        if (viewModel.sizeChanged ||
                            viewModel.textChanged ||
                            viewModel.titleText != newTitle ||
                            viewModel.firstNote != newFirstNote
                        ) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Сохранить изменения?")
                                .setNegativeButton("Нет") { _, _ ->
                                    viewModel.deleteNoteContentList(viewModel.noteContentList)
                                    viewModel.insertNoteContent(viewModel.startNoteContentList)
                                    this.findNavController().popBackStack()
                                    viewModel.onDoneNavigating()
                                }
                                .setPositiveButton("Да") { _, _ ->
                                    checkEmpty()
                                }.show()
                        } else {
                            this.findNavController().popBackStack()
                            viewModel.onDoneNavigating()
                        }
                    }
                    !viewModel.backPressed -> {
                        checkEmpty()
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