package ru.debaser.projects.tribune.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_votes.*
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.adapter.VoteAdapter
import ru.debaser.projects.tribune.model.VoteModel
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.toast
import java.io.IOException

class VotesFragment : Fragment(),
    VoteAdapter.OnItemClickListener
{

    private lateinit var voteAdapter: VoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_votes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getVotes()
    }

    private fun getVotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dialog = LoadingDialog(
                requireContext(),
                R.string.getting_votes).apply { show() }
            try {
                val args = VotesFragmentArgs.fromBundle(arguments!!)
                val result = Repository.getVotes(args.ideaId)
                if (result.isSuccessful) {
                    setAdapter(result.body()!!)
                } else {
                    toast(R.string.cant_upload_image)
                }
            } catch (e: IOException) {
                toast(R.string.error_occurred)
            } finally {
                dialog.dismiss()
            }
        }
    }

    private fun setAdapter(list: List<VoteModel>) {
        with (votesRecV) {
            layoutManager = LinearLayoutManager(requireActivity())
            voteAdapter = VoteAdapter(list.toMutableList()).apply {
                onItemClickListener = this@VotesFragment
            }
            adapter = voteAdapter
        }
    }

    override fun onItemClickListener(vote: VoteModel) {
        view?.findNavController()?.navigate(VotesFragmentDirections.actionVotesFragmentToIdeasByAuthorFragment(vote.authorId))
    }
}