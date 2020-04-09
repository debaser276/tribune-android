package ru.debaser.projects.tribune.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_reg.*
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

class RegFragment : Fragment(), CoroutineScope by MainScope() {
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
            val password = passwordEt.text.toString()
            val passwordRepeated = passwordConfirmEt.text.toString()
            if (password != passwordRepeated) {
                passwordEt.error = getString(R.string.passwords_not_match)
            } else if (!isValid(passwordEt.text.toString())) {
                passwordTil.error = getString(R.string.password_incorrect)
            } else {
                launch {
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
                            view.findNavController().navigate(RegFragmentDirections.actionRegFragmentToIdeasFragment())
                        } else {
                            toast(R.string.registration_failed, requireActivity())
                        }
                    } catch (e: IOException) {
                        toast(R.string.error_occured, requireActivity())
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
            putString(AUTHENTICATED_SHARED_TOKEN, response.body()!!.token)
            putBoolean(AUTHENTICATED_SHARED_ISHATER, response.body()!!.isHater)
            putBoolean(AUTHENTICATED_SHARED_ISPROMOTER, response.body()!!.isPromoter)
            putBoolean(AUTHENTICATED_SHARED_ISREADER, response.body()!!.isReader)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}