package ru.debaser.projects.tribune.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_postidea.*
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.*
import ru.debaser.projects.tribune.repository.PostIdeaRequest
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.toast
import java.io.IOException

class PostIdeaFragment : Fragment() {

    private var mediaUrl: String = ""

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_postidea, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addImageIv.setOnClickListener {
            val items = resources.getStringArray(R.array.post_item_list)
            val builder = AlertDialog.Builder(requireContext())
            builder.setItems(items) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }
        postIdeaBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (mediaUrl.isNotEmpty() && contentEt.text.isNotEmpty()) {
                    val dialog = LoadingDialog(
                        requireActivity(),
                        R.string.create_new_idea).apply { show() }
                    try {
                        val result =
                            Repository.postIdea(
                                PostIdeaRequest(
                                    content = contentEt.text.toString(),
                                    media = mediaUrl,
                                    link = linkEt.text.toString()
                                )
                            )
                        if (result.isSuccessful) {
                            toast(R.string.success_idea)
                            view.findNavController().navigate(PostIdeaFragmentDirections.actionPostIdeaFragmentToIdeasFragment())
                        } else {
                            toast(R.string.error_occurred)
                        }
                    } catch (e: IOException) {
                        toast(R.string.error_occurred)
                    } finally {
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data.extras?.get("data") as Bitmap
                    uploadImage(bitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedPhotoUri = data.data
                    try {
                        selectedPhotoUri?.let {
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    selectedPhotoUri
                                )
                            } else {
                                val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri)
                                ImageDecoder.decodeBitmap(source)
                            }
                            uploadImage(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        viewLifecycleOwner.lifecycleScope.launch {
            val dialog = LoadingDialog(
                requireContext(),
                R.string.image_uploading).apply { show() }
            try {
                val imageUploadResult =
                    Repository.uploadImage(bitmap)
                if (imageUploadResult.isSuccessful) {
                    loadImage(imageIv, imageUploadResult.body()!!.id)
                    mediaUrl = imageUploadResult.body()!!.id
                } else {
                    toast(R.string.cant_upload_image)
                }
            } catch (e: IOException) {
                toast(R.string.error_occurred)
            } finally {
                dialog.dismiss()
            }
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }
    }

    private fun loadImage(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

    private fun chooseFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent,
            REQUEST_IMAGE_PICK
        )
    }
}