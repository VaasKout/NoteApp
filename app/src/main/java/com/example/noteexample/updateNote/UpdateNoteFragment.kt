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
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentUpdateNoteBinding
import com.example.noteexample.gallery.GalleryFragment
import com.example.noteexample.insertNote.InsertNoteFragmentDirections
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UpdateNoteFragment : Fragment() {

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val args by navArgs<UpdateNoteFragmentArgs>()

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding : FragmentUpdateNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)

         val application = requireNotNull(this.activity).application
         val updateViewModelFactory = UpdateNoteViewModelFactory(args.noteId, application)
         val viewModel =
             ViewModelProvider(this, updateViewModelFactory)
                 .get(UpdateNoteViewModel::class.java)



         viewModel.currentNote.observe(viewLifecycleOwner, {
             binding.titleEditUpdate.setText(it.title)
         })

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
             when(it.itemId){
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
                                                     InsertNoteFragmentDirections
                                                         .actionEditNoteFragmentToGalleryFragment
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
                 } else -> false
             }
         }




//         viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
//             if (it == true){
//                 val title = binding.titleEditUpdate.text.toString()
//                 val noteText = binding.noteEditTextUpdate.text.toString()
//                 if (title.isNotEmpty() || noteText.isNotEmpty()){
//                 val note = Note(id = args.noteId, title = title, note = noteText)
//                     viewModel.onUpdate(note)
//                 }
//
//                 this.findNavController()
//                     .navigate(UpdateNoteFragmentDirections
//                         .actionUpdateNoteFragmentToOneNoteFragment(args.noteId))
//                 viewModel.onStopNavigating()
//             }
//         })

         binding.viewModel = viewModel
         binding.lifecycleOwner = this
        return binding.root
    }
}