package ru.debaser.projects.tribune

import android.content.Context
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_ideas.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import java.io.IOException

class IdeasFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var ideaAdapter: IdeaAdapter
    private var currentState: State = Empty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ideas, container, false)
        currentState.refresh(view)
        return view
    }

    private interface State {
        fun refresh(view: View) {}
    }

    private inner class Empty: State {
        override fun refresh(view: View) {
            launch {
                val dialog = LoadingDialog(requireActivity()).apply {
                    setTitle(R.string.getting_ideas)
                    show()
                }
                try {
                    val result = Repository.getRecentIdeas()
                    when {
                        result.isSuccessful -> {
                            with(container) {
                                layoutManager = LinearLayoutManager(requireActivity())
                                ideaAdapter = IdeaAdapter(result.body() ?: mutableListOf())
                                adapter = ideaAdapter
                            }
                        }
                        result.code() == 401 -> {
                            toast(getString(R.string.unauthorised), requireActivity())
                            requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
                                clear()
                                apply()
                            }
                            view.findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
                        }
                        else -> {
                            toast(getString(R.string.error_occured), requireActivity())
                        }
                    }
                } catch(e: IOException) {
                    toast(getString(R.string.error_occured), requireContext())
                } finally {
                    dialog.dismiss()
                }
            }
        }
    }

    private inner class EmptyProgress: State

}