package ru.debaser.projects.tribune.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.fragment_ideas.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.debaser.projects.tribune.*
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.adapter.onScrolledToFooter
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.utils.*
import ru.debaser.projects.tribune.viewmodel.IdeasViewModel

class IdeasFragment : Fragment(),
    IdeaAdapter.OnAvatarClickListener,
    IdeaAdapter.OnLikeClickListener,
    IdeaAdapter.OnDislikeClickListener,
    IdeaAdapter.OnVotesClickListener,
    IdeaAdapter.OnLinkClickListener
{
    private val ideasViewModel: IdeasViewModel by viewModel()
    private val sharedPref: SharedPreferences by inject(named(API_SHARED_FILE))
    private val ideaAdapter: IdeaAdapter = IdeaAdapter()
    private val dialog: LoadingDialog by inject { parametersOf(requireActivity()) }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestToken()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_ideas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as AppCompatActivity).supportActionBar?.subtitle = context?.getUsername()

        with (ideasRecV) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = ideaAdapter.apply {
                onAvatarClickListener = this@IdeasFragment
                onLikeClickListener = this@IdeasFragment
                onDislikeClickListener = this@IdeasFragment
                onVotesClickListener = this@IdeasFragment
                onLinkClickListener = this@IdeasFragment
            }
            onScrolledToFooter { ideasViewModel.loadMore() }
        }

        if (!requireActivity().getIsUserReader()) {
            fab.setOnClickListener {
                findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToPostIdeaFragment())
            }
        }

        swipeContainer.setOnRefreshListener {
            ideasViewModel.refresh()
        }

        with (ideasViewModel) {
            showLoadingDialogEvent.observe(viewLifecycleOwner) {
                with (dialog) {
                    if (it != null) {
                        setTitle(it)
                        show()
                    } else dismiss()
                }
            }
            noAuthEvent.observe(viewLifecycleOwner) {
                if (it) {
                    clearCredentialsAndDeletePushToken()
                    ideasViewModel.noAuthEventDone()
                    findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
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
                findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToAuthFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun clearCredentialsAndDeletePushToken() {
        sharedPref.edit { clear() }
        ideasViewModel.deleteToken()
        (activity as AppCompatActivity).supportActionBar?.subtitle = null
    }

    private fun showEmptyError() {
        errorRv.visibility = View.VISIBLE
        errorTv.setText(R.string.error_occurred)
        errorBtn.setOnClickListener {
            ideasViewModel.refresh()
            errorRv.visibility = View.GONE
        }
    }

    private fun requestToken() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(requireActivity())
            when {
                code == ConnectionResult.SUCCESS -> {
                    onActivityResult(PLAY_SERVICES_RESOLUTION_REQUEST, Activity.RESULT_OK, null)
                }
                isUserResolvableError(code) -> {
                    getErrorDialog(requireActivity(), code, PLAY_SERVICES_RESOLUTION_REQUEST).show()
                }
                else -> {
                    toast(R.string.gp_unavailable)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLAY_SERVICES_RESOLUTION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    ideasViewModel.regPushToken()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAvatarClickListener(ideaModel: IdeaModel) {
        findNavController().navigate(
            IdeasFragmentDirections.actionIdeasFragmentToIdeasByAuthorFragment(
                ideaModel.authorId
            )
        )
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
        findNavController().navigate(IdeasFragmentDirections.actionIdeasFragmentToVotesFragment(idea.id))
    }

    override fun onLinkClickListener(idea: IdeaModel) {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(idea.link)))
    }

    override fun onStart() {
        super.onStart()
        if (ideaAdapter.ideas.isEmpty()) {
            ideaAdapter.submit(ideasViewModel.ideas)
        }
    }
}