package ru.debaser.projects.tribune

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AuthFragment : Fragment(), CoroutineScope by MainScope() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_auth, container, false)
        return view
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

    private fun isAuthenticated() =
        requireActivity()
            .getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE)
            .getString(AUTHENTICATED_SHARED_KEY, "")?.isNotEmpty() ?: false
}