package ru.debaser.projects.tribune.view

import android.app.Activity
import android.content.Context
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
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_reg.*
import kotlinx.android.synthetic.main.fragment_reg.loginEt
import kotlinx.android.synthetic.main.fragment_reg.loginTil
import kotlinx.android.synthetic.main.fragment_reg.passwordEt
import kotlinx.android.synthetic.main.fragment_reg.passwordTil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.repository.Me
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.*
import java.io.IOException

class RegFragment : Fragment() {

    private var avatarId: String = ""

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        haveAccountTv.setOnClickListener {
            view.findNavController().navigate(RegFragmentDirections.actionRegFragmentToAuthFragment())
        }
        regBtn.setOnClickListener {
            hideKeyboard()
            val password = passwordEt.text.toString()
            val passwordRepeated = passwordConfirmEt.text.toString()
            if (password != passwordRepeated) {
                passwordEt.error = getString(R.string.passwords_not_match)
            } else if (!isValid(passwordEt.text.toString())) {
                passwordTil.error = getString(R.string.password_incorrect)
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    val dialog = LoadingDialog(
                        requireActivity(),
                        R.string.registration).apply { show() }
                    try {
                        val response = Repository.register(
                            loginEt.text.toString(),
                            passwordEt.text.toString()
                        )
                        if (response.isSuccessful) {
                            setUserAuth(response)
                            Repository.createRetrofitWithAuthToken(response.body()!!.token)
                            addAvatar()
                        } else {
                            toast(R.string.registration_failed)
                        }
                    } catch (e: IOException) {
                        toast(R.string.error_occured)
                    } finally {
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun setUserAuth(response: Response<Me>) {
        requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
            putLong(AUTHENTICATED_SHARED_ID, response.body()!!.id)
            putString(AUTHENTICATED_SHARED_USERNAME, response.body()!!.username)
            putString(AUTHENTICATED_SHARED_TOKEN, response.body()!!.token)
            putBoolean(AUTHENTICATED_SHARED_ISHATER, response.body()!!.isHater)
            putBoolean(AUTHENTICATED_SHARED_ISPROMOTER, response.body()!!.isPromoter)
            putBoolean(AUTHENTICATED_SHARED_ISREADER, response.body()!!.isReader)
        }
    }

    private fun addAvatar() {
        loginTil.visibility = View.GONE
        passwordTil.visibility = View.GONE
        passwordConfirmTil.visibility = View.GONE
        regBtn.visibility = View.GONE
        haveAccountTv.visibility = View.GONE
        avatarRl.visibility = View.VISIBLE
        addAvatarBtn.visibility = View.VISIBLE
        addAvatarBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val dialog = LoadingDialog(
                    requireContext(),
                    R.string.set_avatar).apply { show() }
                try {
                    if (avatarId.isNotEmpty()) Repository.addAvatar(avatarId)
                    view?.findNavController()?.navigate(RegFragmentDirections.actionRegFragmentToIdeasFragment())
                } catch (e: IOException) {
                    toast(R.string.error_occured)
                } finally {
                    dialog.dismiss()
                }
            }
        }
        avatarIv.setOnClickListener {
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
                    loadImage(avatarIv, imageUploadResult.body()!!.id)
                    avatarId = imageUploadResult.body()!!.id
                } else {
                    toast(R.string.cant_upload_image)
                }
            } catch (e: IOException) {
                toast(R.string.error_occured)
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