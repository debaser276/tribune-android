package ru.debaser.projects.tribune.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_reg.*
import kotlinx.android.synthetic.main.fragment_reg.loginEt
import kotlinx.android.synthetic.main.fragment_reg.loginTil
import kotlinx.android.synthetic.main.fragment_reg.passwordEt
import kotlinx.android.synthetic.main.fragment_reg.passwordTil
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.utils.*
import ru.debaser.projects.tribune.viewmodel.RegViewModel
import androidx.lifecycle.observe

class RegFragment : Fragment() {

    private val regViewModel: RegViewModel by inject()
    private val dialog: LoadingDialog by inject { parametersOf(requireActivity()) }

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
            findNavController().navigate(RegFragmentDirections.actionRegFragmentToAuthFragment())
        }
        regBtn.setOnClickListener {
            hideKeyboard()
            regViewModel.register(
                loginEt.text.toString(),
                passwordEt.text.toString(),
                passwordConfirmEt.text.toString()
            )
        }
        passwordConfirmEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordTil.error = null
            }
        })
        with (regViewModel) {
            passwordErrorEvent.observe(viewLifecycleOwner) {
                passwordTil.error = getString(it)
            }
            showLoadingDialogEvent.observe(viewLifecycleOwner) {
                with (dialog) {
                    if (it != null) {
                        setTitle(it)
                        show()
                    } else dismiss()
                }
            }
            addingAvatarEvent.observe(viewLifecycleOwner) {
                addAvatarView()
            }
            showToastEvent.observe(viewLifecycleOwner) {
                toast(it)
            }
            moveToIdeasFragmentEvent.observe(viewLifecycleOwner) {
                findNavController().navigate(RegFragmentDirections.actionRegFragmentToIdeasFragment())
            }
            avatarId.observe(viewLifecycleOwner) {
                loadImage(avatarIv, it)
            }
        }
    }

    private fun addAvatarView() {
        loginTil.visibility = View.GONE
        passwordTil.visibility = View.GONE
        passwordConfirmTil.visibility = View.GONE
        regBtn.visibility = View.GONE
        haveAccountTv.visibility = View.GONE
        avatarRl.visibility = View.VISIBLE
        addAvatarBtn.visibility = View.VISIBLE
        addAvatarBtn.setOnClickListener {
            regViewModel.addAvatar()
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
                    regViewModel.uploadImage(bitmap)
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
                            regViewModel.uploadImage(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }
}