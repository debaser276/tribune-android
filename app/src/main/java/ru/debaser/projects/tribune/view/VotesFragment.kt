package ru.debaser.projects.tribune.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_votes.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.adapter.VoteAdapter
import ru.debaser.projects.tribune.model.VoteModel
import ru.debaser.projects.tribune.utils.toast
import ru.debaser.projects.tribune.viewmodel.VotesViewModel

class VotesFragment : Fragment(),
    VoteAdapter.OnItemClickListener
{
    private val votesViewModel: VotesViewModel by viewModel()
    private val dialog: LoadingDialog by inject { parametersOf(requireActivity()) }
    private val voteAdapter: VoteAdapter = VoteAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_votes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (votesRecV) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = voteAdapter.apply {
                onItemClickListener = this@VotesFragment
            }
        }
        val args = VotesFragmentArgs.fromBundle(arguments!!)
        votesViewModel.getVotes(args.ideaId)

        with(votesViewModel) {
            votes.observe(viewLifecycleOwner) {
                voteAdapter.submit(it.toMutableList())
            }
            showLoadingDialogEvent.observe(viewLifecycleOwner) {
                with (dialog) {
                    if (it != null) {
                        setTitle(it)
                        show()
                    } else dismiss()
                }
            }
            showToastEvent.observe(viewLifecycleOwner) {
                toast(it)
            }
        }
    }

    override fun onItemClickListener(vote: VoteModel) {
        findNavController().navigate(VotesFragmentDirections.actionVotesFragmentToIdeasByAuthorFragment(vote.authorId))
    }
}