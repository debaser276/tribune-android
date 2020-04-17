package ru.debaser.projects.tribune.view

import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_ideas.*
import retrofit2.Response
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.model.IdeaModel

class IdeasByAuthorFragment : IdeasFragment() {
    override fun setAdapter(list: List<IdeaModel>) {
        super.setAdapter(list)
        ideaAdapter.onAvatarClickListener = null
    }
    override suspend fun getRecentFromRepository(): Response<List<IdeaModel>> {
        val args = IdeasByAuthorFragmentArgs.fromBundle(arguments!!)
        return Repository.getRecentByAuthor(args.authorId)
    }

    override suspend fun getAfterFromRepository(id: Long): Response<List<IdeaModel>> {
        val args = IdeasByAuthorFragmentArgs.fromBundle(arguments!!)
        return Repository.getAfterByAuthor(id, args.authorId)
    }

    override fun onVotesClickListener(idea: IdeaModel) {
        view?.findNavController()?.navigate(IdeasByAuthorFragmentDirections.actionIdeasByAuthorFragmentToVotesFragment(idea.id))
    }


}