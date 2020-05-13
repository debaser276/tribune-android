package ru.debaser.projects.tribune.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_auth.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import ru.debaser.projects.tribune.*
import ru.debaser.projects.tribune.utils.*
import ru.debaser.projects.tribune.viewmodel.AuthViewModel
import androidx.lifecycle.observe

class AuthFragment : Fragment() {
    private val authViewModel: AuthViewModel by inject()
    private val dialog: LoadingDialog by inject { parametersOf(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (authViewModel.isAuthenticated()) {
            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
        }
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBtn.setOnClickListener {
            authViewModel.authenticate(
                loginEt.text.toString(),
                passwordEt.text.toString()
            )
        }

        noAccountTv.setOnClickListener {
            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToRegFragment())
        }

        passwordEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordTil.error = null
            }
        })

        with (authViewModel) {
            passwordErrorEvent.observe(viewLifecycleOwner) {
                passwordTil.error = getString(R.string.password_incorrect)
            }
            showLoadingDialogEvent.observe(viewLifecycleOwner) {
                showLoadingDialog(it)
            }
            moveToIdeasFragmentEvent.observe(viewLifecycleOwner) {
                findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToIdeasFragment())
            }
            showToastEvent.observe(viewLifecycleOwner) {
                toast(it)
            }
        }
    }

    private fun showLoadingDialog(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
}