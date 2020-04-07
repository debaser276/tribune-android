package ru.debaser.projects.tribune

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import retrofit2.HttpException
import retrofit2.http.HTTP
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.model.IdeaModel
import java.io.IOException

class IdeasFragment: Fragment(), CoroutineScope by MainScope() {

    private lateinit var ideaAdapter: IdeaAdapter
    private var currentState: State<IdeaModel> = Empty()
    private lateinit var dialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ideas, container, false)
    }

    override fun onStart() {
        super.onStart()
        currentState = Empty()
        currentState.refresh()
    }

    private interface State<T> {
        fun refresh() {}
        fun fail(err: String?) {}
        fun newData(list: List<T>) {}
    }

    private inner class Empty: State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            showLoadingDialog(true)
            getRecent()
        }
    }

    private inner class EmptyProgress: State<IdeaModel> {
        override fun fail(err: String?) {
            currentState = EmptyError()
            showLoadingDialog(false)
            showEmptyError(err)
        }

        override fun newData(list: List<IdeaModel>) {
            showLoadingDialog(false)
            if (list.isEmpty()) {
                showNoIdeas()
            } else {
                currentState = Data()
                setAdapter(list)
            }
        }
    }

    private inner class EmptyError: State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            showLoadingDialog(true)
            getRecent()
        }
    }

    private inner class Data: State<IdeaModel> {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            view.findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToPostIdeaFragment())
        }
    }

    private fun getRecent() {
        launch {
            try {
                val result = Repository.getRecentIdeas()
                when {
                    result.isSuccessful -> {
                        currentState.newData(result.body() ?: listOf())
                    }
                    result.code() == 401-> {
                        requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
                            clear()
                            apply()
                        }
                        view?.findNavController()?.navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
                    }
                    else -> {
                        currentState.fail(result.code().toString())
                    }
                }
            } catch(e: IOException) {
                currentState.fail(e::class.simpleName)
            } finally {
                dialog.dismiss()
            }
        }
    }

    private fun setAdapter(list: List<IdeaModel>) {
        with (recyclerView) {
            layoutManager = LinearLayoutManager(requireActivity())
            ideaAdapter = IdeaAdapter(list)
            adapter = ideaAdapter
        }
    }

    private fun showEmptyError(error: String?) {
        errorRv.visibility = View.VISIBLE
        errorTv.text = "${getString(R.string.error_occured)}: $error"
        errorBtn.setOnClickListener {
            currentState.refresh()
            errorRv.visibility = View.GONE
        }
    }

    private fun showNoIdeas() {
        toast(R.string.no_idea, requireContext())
    }

    private fun showLoadingDialog(show: Boolean) {
        if (show) {
            dialog = LoadingDialog(requireActivity()).apply {
                setTitle(R.string.getting_ideas)
                show()
            }
        } else {
            dialog.dismiss()
        }
    }
}