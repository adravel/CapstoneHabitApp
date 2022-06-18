package com.example.capstonehabitapp.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentUploadPhotoBinding

private const val CAMERA_PERMISSION_CODE = 1
private const val CAMERA_REQUEST_CODE = 2

class UploadPhotoFragment : Fragment() {

    private var _binding: FragmentUploadPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.upload_photo)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set views appearance when photo has not been taken yet
        binding.askForGradingButton.visibility = View.GONE

        // Set upload photo button onClickListener
        binding.uploadPhotoCardButton.setOnClickListener {
            // Check if app has been granted permission to use camera
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {

                // Open camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                // Ask for permission if app does not have it
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if the permission to use camera has been granted
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Open camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if user has successfully takes a photo using the camera
        if (requestCode == CAMERA_REQUEST_CODE &&
            resultCode == Activity.RESULT_OK
        ) {
            // Get the taken photo data as Bitmap
            val photoImage = data!!.extras!!.get("data") as Bitmap

            // Change views appearance when a photo has been taken
            binding.photoImage.setImageBitmap(photoImage)
            binding.descriptionText.text = getString(R.string.upload_photo_complete_description)
            binding.uploadPhotoCardIcon.visibility = View.GONE
            binding.uploadPhotoCardButton.visibility = View.GONE
            binding.askForGradingButton.visibility = View.VISIBLE

            // Set askForGradingButton onClickListener
            binding.askForGradingButton.setOnClickListener {
                // TODO: Call the method in TaskDetailViewModel to ask for grading
                Log.d("UploadPhoto", "Asking for grading..")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}