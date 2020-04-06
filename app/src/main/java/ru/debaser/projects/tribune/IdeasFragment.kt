package ru.debaser.projects.tribune

import android.content.Context
import android.os.Bundle
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
import ru.debaser.projects.tribune.model.IdeaModel
import java.io.IOException

class IdeasFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var ideaAdapter: IdeaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ideas, container, false)
        return view
    }



    override fun onStart() {
        super.onStart()
        launch {
            val dialog = LoadingDialog(requireActivity()).apply {
                setTitle(R.string.getting_ideas)
                show()
            }
            try {
                val result = Repository.getRecentIdeas()
                when {
                    result.isSuccessful -> {
                        with(recyclerView) {
                            if (result.body()?.size == 0) {
                                showNoIdeas()
                            }
                            else {
                                layoutManager = LinearLayoutManager(requireActivity())
                                ideaAdapter = IdeaAdapter(result.body() as MutableList<IdeaModel>)
                                adapter = ideaAdapter
                            }
                        }
                    }
                    result.code() == 401 -> {
                        requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
                            clear()
                            apply()
                        }
                        view?.findNavController()?.navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
                    }
                    else -> {
                        showEmptyError(result.code().toString())
                    }
                }
            } catch(e: IOException) {
                showEmptyError(e::class.simpleName)
            } finally {
                dialog.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            view.findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToPostIdeaFragment())
        }
    }

    private fun showEmptyError(error: String?) {
        errorRv.visibility = View.VISIBLE
        errorTv.text = "${getString(R.string.error_occured)}: $error"
        errorBtn.setOnClickListener {
            errorRv.visibility = View.GONE
        }
    }

    private fun showNoIdeas() {
        noIdeasTv.visibility = View.VISIBLE
    }
}