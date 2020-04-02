package ru.debaser.projects.tribune

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.debaser.projects.tribune.api.Me
import java.io.IOException

class AuthFragment : Fragment(), CoroutineScope by MainScope() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isAuthenticated()) {
            this.findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
        }
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBtn.setOnClickListener {
            if (!isValid(passwordEt.text.toString())) {
                passwordTil.error = getString(R.string.password_incorrect)
            } else {
                launch {
                    val dialog = LoadingDialog(requireActivity()).apply {
                        setTitle(getString(R.string.authentication))
                        show()
                    }
                    try {
                        val response = Repository.authenticate(
                            loginEt.text.toString(),
                            passwordEt.text.toString()
                        )
                        if (response.isSuccessful) {
                            toast(getString(R.string.success), requireActivity())
                            setUserAuth(response)
                            Repository.createRetrofitWithAuthToken(response.body()!!.token)
                            view.findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
                        } else {
                            toast(getString(R.string.authentication_failed), requireActivity())
                        }
                    } catch (e: IOException) {
                        toast(getString(R.string.error_occured), requireActivity())
                    } finally {
                        dialog.dismiss()
                    }
                }
            }
        }

        noAccountTv.setOnClickListener {
            view.findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToRegFragment())
        }
    }

    override fun onStop() {
        super.onStop()
        cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    private fun isAuthenticated() =
        requireActivity()
            .getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE)
            .getString(AUTHENTICATED_SHARED_TOKEN, "")?.isNotEmpty() ?: false

    private fun setUserAuth(response: Response<Me>) {
        requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
            putLong(AUTHENTICATED_SHARED_ID, response.body()!!.id)
            putString(AUTHENTICATED_SHARED_TOKEN, response.body()!!.token)
            putBoolean(AUTHENTICATED_SHARED_ISHATER, response.body()!!.isHater)
            putBoolean(AUTHENTICATED_SHARED_ISPROMOTER, response.body()!!.isPromoter)
            putBoolean(AUTHENTICATED_SHARED_ISREADER, response.body()!!.isReader)
        }

    }
}