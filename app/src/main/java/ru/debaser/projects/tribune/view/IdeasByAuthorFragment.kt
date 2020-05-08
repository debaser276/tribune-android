package ru.debaser.projects.tribune.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_ideas.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.adapter.onScrolledToFooter
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.utils.API_SHARED_FILE
import ru.debaser.projects.tribune.utils.getUserId
import ru.debaser.projects.tribune.utils.toast
import ru.debaser.projects.tribune.viewmodel.IdeasByAuthorViewModel

class IdeasByAuthorFragment : Fragment(),
    IdeaAdapter.OnLikeClickListener,
    IdeaAdapter.OnDislikeClickListener,
    IdeaAdapter.OnVotesClickListener,
    IdeaAdapter.OnLinkClickListener
{
    private val ideasViewModel: IdeasByAuthorViewModel by viewModel {
        parametersOf(ideaAdapter, IdeasByAuthorFragmentArgs.fromBundle(arguments!!).authorId)
    }
    private val ideaAdapter: IdeaAdapter = IdeaAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_ideas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val dialog = LoadingDialog(
            requireActivity(),
            R.string.getting_ideas
        )

        fab.hide()

        with (ideasRecV) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = ideaAdapter.apply {
                onLikeClickListener = this@IdeasByAuthorFragment
                onDislikeClickListener = this@IdeasByAuthorFragment
                onVotesClickListener = this@IdeasByAuthorFragment
                onLinkClickListener = this@IdeasByAuthorFragment
            }
            onScrolledToFooter { ideasViewModel.loadMore() }
        }

        swipeContainer.setOnRefreshListener {
            ideasViewModel.refresh()
        }

        with (ideasViewModel) {
            showLoadingDialogEvent.observe(viewLifecycleOwner) {
                showLoadingDialog(dialog, it)
            }
            noAuthEvent.observe(viewLifecycleOwner) {
                if (it) {
                    clearCredentialsAndDeletePushToken()
                    ideasViewModel.noAuthEventDone()
                }
            }
            showEmptyErrorEvent.observe(viewLifecycleOwner) {
                if (it) showEmptyError()
            }
            cancelRefreshingEvent.observe(viewLifecycleOwner) {
                if (it) {
                    swipeContainer.isRefreshing = !it
                    cancelRefreshingEventDone()
                }
            }
            showToastEvent.observe(viewLifecycleOwner) {
                toast(it)
            }
            showProgressBarEvent.observe(viewLifecycleOwner) {
                progressBar.isVisible = it
            }
            changeIdeasEvent.observe(viewLifecycleOwner) {
                if (it) {
                    ideaAdapter.submit(ideas)
                    changeIdeasEventDone()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.logout -> {
                clearCredentialsAndDeletePushToken()
                view?.findNavController()?.navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun clearCredentialsAndDeletePushToken() {
        requireActivity().getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE).edit {
            clear()
            apply()
        }
    }

    private fun showLoadingDialog(dialog: LoadingDialog, show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }

    private fun showEmptyError() {
        errorRv.visibility = View.VISIBLE
        errorTv.setText(R.string.error_occurred)
        errorBtn.setOnClickListener {
            ideasViewModel.refresh()
            errorRv.visibility = View.GONE
        }
    }

    override fun onLikeClickListener(idea: IdeaModel, position: Int) {
        if (!isAlreadyVote(idea)) {
            ideasViewModel.likeClick(idea, position)
        } else {
            toast(R.string.vote_once)
        }
    }

    private fun isAlreadyVote(idea: IdeaModel): Boolean {
        val id = requireActivity().getUserId()
        return idea.likes.contains(id) || idea.dislikes.contains(id)
    }

    override fun onDislikeClickListener(idea: IdeaModel, position: Int) {
        if (!isAlreadyVote(idea)) {
            ideasViewModel.dislikeClick(idea, position)
        } else {
            toast(R.string.vote_once)
        }
    }

    override fun onVotesClickListener(idea: IdeaModel) {
        view?.findNavController()?.navigate(IdeasByAuthorFragmentDirections.actionIdeasByAuthorFragmentToVotesFragment(idea.id))
    }

    override fun onLinkClickListener(idea: IdeaModel) {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(idea.link)))
    }
}