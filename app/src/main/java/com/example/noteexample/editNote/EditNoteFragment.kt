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
import com.example.noteexample.databinding.FragmentEditNoteBinding
import com.example.noteexample.utils.Camera
import com.example.noteexample.utils.NoteWithImagesRecyclerItems
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
                    viewModel.startNote?.note?.let { item ->
                        this.findNavController()
                            .navigate(
                                EditNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment(
                                        item.noteID
                                    )
                            )
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
                                        viewModel.startNote?.note?.let { item ->
                                            this.findNavController()
                                                .navigate(
                                                    EditNoteFragmentDirections
                                                        .actionEditNoteFragmentToGalleryFragment(
                                                            item.noteID
                                                        )
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
                R.id.save_note -> {
                    viewModel.onStartNavigating()
                    true
                }
                else -> false
            }
        }


        noteAdapter.noteHolder.observe(viewLifecycleOwner) { holder ->
            holder.binding.titleEdit.addTextChangedListener {
                viewModel.title = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                viewModel.text = it.toString()
            }
        }

        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->

            if (noteAdapter.currentList[holder.adapterPosition].image?.hidden == true) {
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


            holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                noteAdapter.currentList[holder.adapterPosition].image?.let {
                    it.signature = editable.toString()
                    if (it.photoPath.isEmpty() && it.signature.isEmpty()) {
                        viewModel.deleteImage(it)
                    }
                }
            }

            holder.binding.deleteCircle.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].image?.hidden = true
                viewModel.updateCurrentNote()
                noteAdapter.notifyItemChanged(holder.adapterPosition)
            }

            holder.binding.restoreButton.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].image?.hidden = false
                viewModel.updateCurrentNote()
                noteAdapter.notifyItemChanged(holder.adapterPosition)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }



        viewModel.allNotes.observe(viewLifecycleOwner, {
            viewModel.lastIndex = it.size - 1
        })

        lifecycleScope.launch {
            viewModel.getNote()
            viewModel.currentNoteLiveData.observe(viewLifecycleOwner, {
                viewModel.currentNote = it
                lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.currentNote?.let { note ->
                        if (!viewModel.itemListSame) {
                            viewModel.dataItemList = mutableListOf()
                            viewModel.dataItemList.add(0, NoteWithImagesRecyclerItems(note.note))
                            note.images.forEach { image ->
                                viewModel.dataItemList.add(NoteWithImagesRecyclerItems(image = image))
                            }
                        } else {
                            viewModel.dataItemList.forEachIndexed { index, dataItem ->
                                if (index > 0) {
                                    dataItem.image = note.images[index - 1]
                                }
                            }
                            viewModel.itemListSame = false
                        }

                        withContext(Dispatchers.Main) {
                            noteAdapter.submitList(viewModel.dataItemList)
                        }
                    }
                }
            })
        }

        fun checkEmpty() {
            viewModel.updateCurrentNote()
            viewModel.currentNote?.let {
                if (it.images.isEmpty() &&
                    viewModel.title.isEmpty() &&
                    viewModel.text.isEmpty()
                ) {
                    viewModel.deleteUnused()
                }
            }

            this.findNavController()
                .navigate(
                    EditNoteFragmentDirections.actionEditNoteFragmentToAllNotesFragment()
                )
            viewModel.onDoneNavigating()
        }

        viewModel.navigateBack.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.updateCurrentNote()
                if (args.noteID > -1) {
                    when {
                        viewModel.backPressed -> {
                            if (viewModel.startNote != viewModel.currentNote) {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Сохранить изменения?")
                                    .setNegativeButton("Нет") { _, _ ->
                                        viewModel.deleteNote(viewModel.currentNote)
                                        viewModel.insertNoteWithImages(viewModel.startNote)
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
                } else {
                    viewModel.currentNote?.let { noteWithImages ->
                        if (noteWithImages.images.any { item -> !item.hidden } ||
                            noteWithImages.images.any { item -> item.signature.isNotEmpty() }) {
                            viewModel.allHidden = false
                        }

                        when {
                            (noteWithImages.images.isEmpty() ||
                                    viewModel.allHidden) &&
                                    viewModel.text.isEmpty() &&
                                    viewModel.title.isEmpty()
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
                }
            }
        })
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        if (args.noteID == -1L) {
            viewModel.updateCurrentNote()
        }
    }
}
