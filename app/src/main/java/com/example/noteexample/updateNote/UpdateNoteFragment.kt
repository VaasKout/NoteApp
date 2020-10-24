package com.example.noteexample.updateNote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.Note
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


        viewModel.currentNote.observe(viewLifecycleOwner, {
            viewModel.note = it
            viewModel.titleText = it.title
        })

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            if (it != null){
                val list = it.filter { list -> list.noteId == args.noteId }
                Log.e("photoList", list.toString())
                /**
                 * Here, I have to do new objects for each [UpdateNoteViewModel.startNoteContentList]
                 * because if it equals to this list, [NoteContent.note] and [NoteContent.photoPath] will
                 * reflect changes in [UpdateNoteViewModel.startNoteContentList],
                 * it's caused by var fields in [NoteContent]
                 */
                if (viewModel.startNoteContentList.isEmpty()){
                    list.forEach { element ->
                        val noteContent = NoteContent(
                            id = element.id,
                            noteId = element.noteId,
                            note = element.note,
                            photoPath = element.photoPath,
                        )
                        viewModel.startNoteContentList.add(noteContent)
                    }
                    Log.e("note", viewModel.startNoteContentList[0].note)
                }
                Log.e("currentNote", "${viewModel.currentNote.value?.id}")
                noteAdapter.addHeaderAndSubmitList(viewModel.note, list)
            }
        })

//        val noteContentList = mutableListOf<NoteContent>()
//        noteAdapter.holder.observe(viewLifecycleOwner, {adapter ->
//            val item = noteAdapter.currentList[adapter.adapterPosition]
//            noteContentList.add(item)
//            adapter.binding.noteEditTextFirst.addTextChangedListener {
//                item.note = it.toString()
//                noteContentList[adapter.adapterPosition].note = item.note
//                Log.e("noteInNav", viewModel.startNoteContentList[0].note)
//            }
//        })

        //TODO Add firstNote in viewModel


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

//        viewModel.navigateToOneNoteFragment.observe(viewLifecycleOwner, {
//            if (it == true) {
//                val title = binding.titleUpdate.text.toString()
//                if (viewModel.startNoteContentList.size == noteContentList.size){
//                    noteContentList.forEachIndexed { index, noteContent ->
//                        Log.e("currentListNote", noteContent.note)
//                        Log.e("startListNote", viewModel.startNoteContentList[index].note)
//                        if (noteContent != viewModel.startNoteContentList[index]){
//                            viewModel.textChanged = true
//                        }
//                    }
//                } else {
//                    viewModel.sizeChanged = true
//                }
//
//                when {
//                    viewModel.backPressed -> {
//                        if (viewModel.sizeChanged ||
//                            viewModel.textChanged ||
//                            viewModel.titleText != binding.titleUpdate.text.toString()){
//
//                            MaterialAlertDialogBuilder(requireContext())
//                                .setMessage("Сохранить изменения?")
//                                .setNegativeButton("Нет") { _, _ ->
//                                    this.findNavController().popBackStack()
//                                    viewModel.onDoneNavigating()
//                                }
//                                .setPositiveButton("Да") { _, _ ->
//                                    if (noteContentList.isNotEmpty()){
//                                        viewModel.updateNoteContent(noteContentList)
//                                        viewModel.updateCurrentNote(binding.titleUpdate.text.toString())
//                                    } else if (noteContentList.isEmpty() && title.isEmpty()){
//                                        viewModel.deleteUnused()
//                                    }
//                                    this.findNavController().popBackStack()
//                                    viewModel.onDoneNavigating()
//                                }.show()
//                        } else {
//                            this.findNavController().popBackStack()
//                            viewModel.onDoneNavigating()
//                        }
//                    }
//                    !viewModel.backPressed -> {
//                        viewModel.updateNoteContent(noteContentList)
//                        viewModel.updateCurrentNote(binding.titleUpdate.text.toString())
//                        this.findNavController().popBackStack()
//                        viewModel.onDoneNavigating()
//                    }
//                }
//            }
//        })

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            viewModel.backPressed = true
//            viewModel.onStartNavigating()
//        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}