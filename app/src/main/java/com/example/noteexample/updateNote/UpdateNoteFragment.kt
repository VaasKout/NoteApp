package com.example.noteexample.updateNote

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.utils.adapter.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentUpdateNoteBinding
import com.example.noteexample.insertNote.InsertNoteFragmentDirections
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private val args by navArgs<UpdateNoteFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val camera = Camera(requireActivity())

        val application: Application = requireNotNull(this.activity).application
        val updateViewModelFactory = UpdateNoteViewModelFactory(args.noteId, application)
        val viewModel by lazy {
            ViewModelProvider(this, updateViewModelFactory).get(UpdateNoteViewModel::class.java)
        }

        val binding: FragmentUpdateNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this


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
                } else {
                    Snackbar.make(
                        binding.editButton,
                        R.string.camera_request_failed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        //Launcher for camera itself
        startCamera =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    viewModel.insertCameraPhoto(camera.currentPhotoPath)
                    noteAdapter.notifyDataSetChanged()
                }
            }

        binding.toolbarNoteUpdate.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    viewModel.updateNoteContentList(viewModel.noteContentList)
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    startCamera.launch(
                                        camera.dispatchTakePictureIntent(binding.editButton)
                                    )
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        viewModel.currentNote?.let { note ->
                                            this@UpdateNoteFragment.findNavController()
                                                .navigate(
                                                    UpdateNoteFragmentDirections
                                                        .actionUpdateNoteFragmentToGalleryFragment
                                                            (note.id)
                                                )
                                        }
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
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.getNote()
                if (it != null) {
                    viewModel.noteContentList = it.filter { list -> list.noteId == args.noteId }
                    if (!viewModel.startListInit) {
                        viewModel.noteContentList.forEach { element ->
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
                    withContext(Dispatchers.Main) {
                        noteAdapter.addHeaderAndSubmitList(
                            viewModel.currentNote,
                            viewModel.noteContentList
                        )
                    }
                }
            }
        })

        noteAdapter.noteHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                viewModel.newTitle = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                viewModel.newFirstNote = it.toString()
            }
        }

        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            noteAdapter.currentList[holder.adapterPosition].noteContent?.let {
                if (it.hidden) {
                    holder.binding.photo.visibility = View.GONE
                    holder.binding.restoreButton.visibility = View.VISIBLE
                    holder.binding.deleteCircleIcon.visibility = View.GONE
                    holder.binding.deleteCircle.visibility = View.GONE
                } else {
                    holder.binding.photo.visibility = View.VISIBLE
                    holder.binding.restoreButton.visibility = View.GONE
                    holder.binding.deleteCircleIcon.visibility = View.VISIBLE
                    holder.binding.deleteCircle.visibility = View.VISIBLE
                }
            }

            holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                noteAdapter.currentList[holder.adapterPosition].noteContent?.let {
                    it.note = editable.toString()
                    if (it.photoPath.isEmpty() && it.note.isEmpty()) {
                        viewModel.deleteNoteContent(it)
                    }
                }
            }

            holder.binding.deleteCircle.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].noteContent?.hidden = true
                viewModel.updateNoteContentList(viewModel.noteContentList)
                noteAdapter.notifyItemChanged(holder.adapterPosition)
            }

            holder.binding.restoreButton.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].noteContent?.hidden = false
                viewModel.updateNoteContentList(viewModel.noteContentList)
                noteAdapter.notifyItemChanged(holder.adapterPosition)
            }
        })


        fun checkEmpty() {
            viewModel.updateCurrentNote(viewModel.newTitle, viewModel.newFirstNote)
            if (viewModel.noteContentList.isNotEmpty()) {
                viewModel.updateNoteContentList(viewModel.noteContentList)
            } else if (viewModel.noteContentList.isEmpty() &&
                viewModel.newTitle.isEmpty() &&
                viewModel.newFirstNote.isEmpty()
            ) {
                viewModel.deleteUnused()
            }
            this.findNavController()
                .navigate(
                    UpdateNoteFragmentDirections
                        .actionUpdateNoteFragmentToAllNotesFragment()
                )
            viewModel.onDoneNavigating()
        }

        viewModel.navigateToOneNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {

                if (viewModel.startNoteContentList.size == viewModel.noteContentList.size) {
                    viewModel.noteContentList.forEachIndexed { index, noteContent ->
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
                            viewModel.title != viewModel.newTitle ||
                            viewModel.firstNote != viewModel.newFirstNote
                        ) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Сохранить изменения?")
                                .setNegativeButton("Нет") { _, _ ->
                                    viewModel.deleteNoteContentList(viewModel.noteContentList)
                                    viewModel.insertNoteContentList(viewModel.startNoteContentList)
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

        return binding.root
    }
}