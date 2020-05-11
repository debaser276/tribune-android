package ru.debaser.projects.tribune.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import retrofit2.Response
import ru.debaser.projects.tribune.*
import ru.debaser.projects.tribune.repository.Me
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.*
import java.io.IOException

class AuthFragment : Fragment() {
    private val sharedPref: SharedPreferences by inject(named(API_SHARED_FILE))
    private val repository: Repository by inject()
    private val dialog: LoadingDialog by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isAuthenticated()) {
            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
        }
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBtn.setOnClickListener {
            if (!isValid(passwordEt.text.toString())) {
                passwordTil.error = getString(R.string.password_incorrect)
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    dialog.apply {
                        setTitle(R.string.authentication)
                        show()
                    }
                    try {
                        val response =
                            repository.authenticate(
                                loginEt.text.toString(),
                                passwordEt.text.toString()
                            )
                        if (response.isSuccessful) {
                            setUserAuth(response)
                            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
                        } else {
                            toast(R.string.authentication_failed)
                        }
                    } catch (e: IOException) {
                        toast(R.string.error_occurred)
                    } finally {
                        dialog.dismiss()
                    }
                }
            }
        }

        noAccountTv.setOnClickListener {
            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToRegFragment())
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun isAuthenticated() =
        sharedPref.getString(AUTHENTICATED_SHARED_TOKEN, "")?.isNotEmpty() ?: false

    private fun setUserAuth(response: Response<Me>) {
        sharedPref.edit {
            putLong(AUTHENTICATED_SHARED_ID, response.body()!!.id)
            putString(AUTHENTICATED_SHARED_USERNAME, response.body()!!.username)
            putString(AUTHENTICATED_SHARED_TOKEN, response.body()!!.token)
            putBoolean(AUTHENTICATED_SHARED_ISHATER, response.body()!!.isHater)
            putBoolean(AUTHENTICATED_SHARED_ISPROMOTER, response.body()!!.isPromoter)
            putBoolean(AUTHENTICATED_SHARED_ISREADER, response.body()!!.isReader)
        }
    }
}