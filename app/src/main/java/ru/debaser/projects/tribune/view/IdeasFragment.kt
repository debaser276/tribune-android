package ru.debaser.projects.tribune.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_ideas.*
import kotlinx.coroutines.*
import ru.debaser.projects.tribune.*
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.adapter.onScrolledToFooter
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.*
import java.io.IOException

open class IdeasFragment: Fragment(),
    CoroutineScope by MainScope(),
    IdeaAdapter.OnAvatarClickListener,
    IdeaAdapter.OnLikeClickListener,
    IdeaAdapter.OnDislikeClickListener,
    IdeaAdapter.OnVotesClickListener
{

    lateinit var ideaAdapter: IdeaAdapter
    private lateinit var currentState: State<IdeaModel>
    private lateinit var dialog: LoadingDialog

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ideas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.subtitle = context?.getUsername()
        requestToken()
        if (requireActivity().getIsUserReader()) {
            fab.setOnClickListener {
                view.findNavController()
                    .navigate(IdeasFragmentDirections.actionIdeasFragmentToPostIdeaFragment())
            }
        }
        swipeContainer.setOnRefreshListener {
            currentState.refresh()
        }
    }

    override fun onStart() {
        super.onStart()
        currentState = Empty()
        currentState.refresh()
    }

    private interface State<T> {
        fun refresh() {}
        fun fail(err: String) {}
        fun newData(list: List<T>) {}
        fun release() {}
        fun loadNew() {}
    }

    private inner class Empty:
        State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            showLoadingDialog(true)
            getRecent()
        }
    }

    private inner class EmptyProgress: State<IdeaModel> {
        override fun fail(err: String) {
            currentState = EmptyError()
            showLoadingDialog(false)
            showEmptyError(err)
        }
        override fun newData(list: List<IdeaModel>) {
            showLoadingDialog(false)
            if (list.isEmpty()) {
                toast(R.string.no_idea)
            } else {
                currentState = Data()
                setAdapter(list)
            }
        }
        override fun release() {
            showLoadingDialog(false)
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
        override fun refresh() {
            currentState = Refresh()
            getAfter()
        }
        override fun loadNew() {
            currentState = AddProgress()
            showProgressBar(true)
            getBefore()
        }
    }

    private inner class Refresh: State<IdeaModel> {
        override fun newData(list: List<IdeaModel>) {
            currentState = Data()
            ideaAdapter.list.addAll(list)
        }
        override fun fail(err: String) {
            currentState = Data()
            showLoadingDialog(false)
            showErrorToast(err)
        }
    }

    private inner class AddProgress: State<IdeaModel> {
        override fun newData(list: List<IdeaModel>) {
            if (list.isEmpty()) {
                currentState = AllData()
                toast(R.string.loaded_all_ideas)
                showProgressBar(false)
            } else {
                currentState = Data()
                showProgressBar(false)
                ideaAdapter.list.addAll(list)
            }
        }
        override fun fail(err: String) {
            currentState = Data()
            showProgressBar(false)
            showErrorToast(err)
        }
    }

    private inner class AllData: State<IdeaModel> {
        override fun refresh() {
            currentState = Refresh()
            getAfter()
        }
    }

    private fun getRecent() {
        launch {
            try {
                val result = getRecentFromRepository()
                when {
                    result.isSuccessful -> {
                        currentState.newData(result.body() ?: listOf())
                    }
                    result.code() == 401-> {
                        currentState.release()
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
                currentState.fail(e::class.simpleName ?: "")
            }
        }
    }

    private fun getAfter() {
        launch {
            try {
                val response = getAfterFromRepository(ideaAdapter.list[0].id)
                if (response.isSuccessful) {
                    val newIdeas = response.body()!!
                    currentState.newData(newIdeas)
                    ideaAdapter.notifyItemRangeInserted(0, newIdeas.size)
                } else {
                    currentState.fail(response.code().toString())
                }
            } catch (e: IOException) {
                currentState.fail(e::class.simpleName ?: "")
            } catch (e: IndexOutOfBoundsException) {
                currentState.fail(e::class.simpleName ?: "")
            } finally {
                swipeContainer.isRefreshing = false
            }
        }
    }

    private fun getBefore() {
        launch {
            try {
                val response = getBeforeFromRepository(ideaAdapter.list[ideaAdapter.list.size - 1].id)
                if (response.isSuccessful) {
                    val newIdeas = response.body()!!
                    currentState.newData(newIdeas)
                    ideaAdapter.notifyItemRangeInserted(ideaAdapter.list.size, newIdeas.size)
                } else {
                    currentState.fail(response.code().toString())
                }
            } catch (e: IOException) {
                currentState.fail(e::class.simpleName ?: "")
            }
        }
    }

    open suspend fun getRecentFromRepository() = withContext(Dispatchers.IO) {
        Repository.getRecent()
    }

    open suspend fun getAfterFromRepository(id: Long) = withContext(Dispatchers.IO) {
        Repository.getAfter(id)
    }

    open suspend fun getBeforeFromRepository(id: Long) = withContext(Dispatchers.IO) {
        Repository.getBefore(id)
    }


    open fun setAdapter(list: List<IdeaModel>) {
        with (ideasRecV) {
            layoutManager = LinearLayoutManager(requireActivity())
            ideaAdapter = IdeaAdapter(list.toMutableList()).apply {
                onAvatarClickListener = this@IdeasFragment
                onLikeClickListener = this@IdeasFragment
                onDislikeClickListener = this@IdeasFragment
                onVotesClickListener = this@IdeasFragment
            }
            onScrolledToFooter { currentState.loadNew() }
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

    private fun showErrorToast(err: String) {
        toast("${getString(R.string.error_occured)}: $err")
    }

    private fun showLoadingDialog(show: Boolean) {
        if (show) {
            dialog = LoadingDialog(
                requireActivity(),
                R.string.getting_ideas
            ).apply { show() }
        } else {
            dialog.dismiss()
        }
    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun onAvatarClickListener(ideaModel: IdeaModel) {
        view?.findNavController()?.navigate(
            IdeasFragmentDirections.actionIdeasFragmentToIdeasByAuthorFragment(
                ideaModel.authorId
            )
        )
    }

    override fun onLikeClickListener(idea: IdeaModel, position: Int) {
        if (!isAlreadyVote(idea)) {
            launch {
                idea.likeActionPerforming = true
                try {
                    ideaAdapter.notifyItemChanged(position)
                    val response = Repository.like(idea.id)
                    if (response.isSuccessful) {
                        idea.updateLikes(response.body()!!)
                    }
                } catch (e: IOException) {
                    toast(R.string.error_occured)
                } finally {
                    idea.likeActionPerforming = false
                    ideaAdapter.notifyItemChanged(position)
                }
            }
        } else {
            toast(R.string.vote_once)
        }
    }

    override fun onDislikeClickListener(idea: IdeaModel, position: Int) {
        if (!isAlreadyVote(idea)) {
            launch {
                idea.dislikeActionPerforming = true
                try {
                    ideaAdapter.notifyItemChanged(position)
                    val response = Repository.dislike(idea.id)
                    if (response.isSuccessful) {
                        idea.updateDislikes(response.body()!!)
                    }
                } catch (e: IOException) {
                    toast(R.string.error_occured)
                } finally {
                    idea.dislikeActionPerforming = false
                    ideaAdapter.notifyItemChanged(position)
                }
            }
        } else {
            toast(R.string.vote_once)
        }
    }

    override fun onVotesClickListener(idea: IdeaModel) {
        view?.findNavController()?.navigate(IdeasFragmentDirections.actionIdeasFragmentToVotesFragment(idea.id))
    }

    private fun isAlreadyVote(idea: IdeaModel): Boolean {
        val id = requireActivity().getUserId()
        return idea.likes.contains(id) || idea.dislikes.contains(id)
    }

    private fun requestToken() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(requireActivity())
            if (code == ConnectionResult.SUCCESS) {
                onActivityResult(PLAY_SERVICES_RESOLUTION_REQUEST, Activity.RESULT_OK, null)
            } else if (isUserResolvableError(code)) {
                getErrorDialog(requireActivity(), code, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                toast(R.string.gp_unavailable)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLAY_SERVICES_RESOLUTION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                        launch {
                            Repository.registerPushToken(it.token)
                        }
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}