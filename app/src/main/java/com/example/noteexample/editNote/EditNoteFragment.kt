package com.example.noteexample.editNote

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
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentEditNoteBinding
import com.example.noteexample.utils.Camera
import com.example.noteexample.utils.DataItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private val args by navArgs<EditNoteFragmentArgs>()

    private val viewModel by lazy {
        val application: Application = requireNotNull(this.activity).application
        val updateViewModelFactory = EditNoteViewModelFactory(args.noteID, application)
        ViewModelProvider(this, updateViewModelFactory).get(EditNoteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val camera = Camera(requireActivity())

        //binding for EditNoteFragment
        val binding: FragmentEditNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note, container, false)
        binding.lifecycleOwner = this

        val noteAdapter = OneNoteEditAdapter()
        binding.editRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }
        viewModel.helper.attachToRecyclerView(binding.editRecycler)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    if (args.noteID > -1) {
                        this.findNavController()
                            .navigate(
                                EditNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment(args.noteID)
                            )
                    } else {
                        viewModel.currentNote?.let {
                            this.findNavController()
                                .navigate(
                                    EditNoteFragmentDirections
                                        .actionEditNoteFragmentToGalleryFragment(it.id)
                                )
                        }
                    }

                } else {
                    Snackbar.make(
                        binding.editRecycler,
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
                    viewModel.itemListInit = false
                    noteAdapter.notifyDataSetChanged()
                }
            }

        binding.toolbarNoteEdit.setNavigationOnClickListener {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }

        binding.toolbarNoteEdit.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    startCamera.launch(
                                        camera.dispatchTakePictureIntent(binding.editRecycler)
                                    )
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        if (args.noteID > -1) {
                                            this.findNavController()
                                                .navigate(
                                                    EditNoteFragmentDirections
                                                        .actionEditNoteFragmentToGalleryFragment(
                                                            args.noteID
                                                        )
                                                )
                                        } else {
                                            viewModel.currentNote?.let { note ->
                                                this.findNavController()
                                                    .navigate(
                                                        EditNoteFragmentDirections
                                                            .actionEditNoteFragmentToGalleryFragment(
                                                                note.id
                                                            )
                                                    )
                                            }
                                        }
                                        viewModel.itemListInit = false
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
                R.id.save_note -> {
                    viewModel.onStartNavigating()
                    true
                }
                else -> false
            }
        }


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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }



        if (args.noteID > -1) {
            viewModel.allNoteContent.observe(viewLifecycleOwner, {
                /**
                 * Here, I have to do new objects for each [EditNoteViewModel.startNoteContentList]
                 * because if it equals to this list, [NoteContent.note] and [NoteContent.photoPath] will
                 * reflect changes in [EditNoteViewModel.startNoteContentList],
                 * it's caused by var fields in [NoteContent]
                 */
                lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.getNote()
                    if (it != null) {
                        viewModel.noteContentList = mutableListOf()
                        viewModel.noteContentList
                            .addAll(it.filter { list -> list.noteId == args.noteID })
                    }
                    if (!viewModel.itemListInit) {
                        viewModel.dataItemList = mutableListOf()
                        viewModel.dataItemList.add(0, DataItem(note = viewModel.currentNote))
                        viewModel.noteContentList.forEach {
                            viewModel.dataItemList.add(DataItem(noteContent = it))
                        }
                        viewModel.itemListInit = true
                    } else {
                        viewModel.dataItemList.forEachIndexed { index, dataItem ->
                            if (index > 0) {
                                dataItem.noteContent = viewModel.noteContentList[index - 1]
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        noteAdapter.submitList(viewModel.dataItemList)
                    }

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
                }
            })

            fun checkEmpty() {
                viewModel.updateCurrentNoteUpdateFr(viewModel.newTitle, viewModel.newFirstNote)
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
                        EditNoteFragmentDirections.actionEditNoteFragmentToAllNotesFragment()
                    )
                viewModel.onDoneNavigating()
            }

            viewModel.navigateBack.observe(viewLifecycleOwner, {
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
                                viewModel.onDoneNavigating()
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

        } else {




            viewModel.allNotes.observe(viewLifecycleOwner, {
                viewModel.lastIndex = it.size - 1
            })


            viewModel.allNoteContent.observe(viewLifecycleOwner, {
                lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.insertNote()
                    if (it != null) {
                        viewModel.noteContentList = mutableListOf()
                        viewModel.noteContentList
                            .addAll(it.filter { list -> list.noteId == viewModel.currentNote?.id })
                            }

                    if (!viewModel.itemListInit) {
                        viewModel.dataItemList = mutableListOf()
                        viewModel.dataItemList.add(0, DataItem(note = viewModel.currentNote))
                        viewModel.noteContentList.forEach {
                            viewModel.dataItemList.add(DataItem(noteContent = it))
                        }
                        viewModel.itemListInit = true
                    } else {
                        viewModel.dataItemList.forEachIndexed { index, dataItem ->
                            if (index > 0) {
                                dataItem.noteContent = viewModel.noteContentList[index - 1]
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        noteAdapter.submitList(viewModel.dataItemList)
                    }
                }
            })

            viewModel.navigateBack.observe(viewLifecycleOwner, {
                if (it == true) {
                    if (viewModel.noteContentList.any { item -> !item.hidden } ||
                        viewModel.noteContentList.any { item -> item.note.isNotEmpty() }) {
                        viewModel.allHidden = false
                    }

                    when {
                        (viewModel.noteContentList.isEmpty() ||
                                viewModel.allHidden) &&
                                viewModel.newTitle.isEmpty() &&
                                viewModel.newFirstNote.isEmpty()
                        -> {
                            this.findNavController().popBackStack()
                            viewModel.deleteUnused()
                            viewModel.onDoneNavigating()
                        }
                        viewModel.backPressed -> {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Сохранить изменения?")
                                .setNegativeButton("Нет") { _, _ ->
                                    viewModel.deleteUnused()
                                    this.findNavController().popBackStack()
                                    viewModel.onDoneNavigating()
                                }
                                .setPositiveButton("Да") { _, _ ->
                                    this.findNavController().popBackStack()
                                    viewModel.onDoneNavigating()
                                }.show()
                            viewModel.onDoneNavigating()
                        }
                        !viewModel.backPressed -> {
                            this.findNavController().popBackStack()
                            viewModel.onDoneNavigating()
                        }
                    }
                }
            })
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        if (args.noteID == -1) {
            viewModel.updateNoteContentList(viewModel.noteContentList)
            viewModel.updateCurrentNoteInsertFr(viewModel.newTitle, viewModel.newFirstNote)
        }
    }
}