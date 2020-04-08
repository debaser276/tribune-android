package ru.debaser.projects.tribune.view

import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_ideas.*
import retrofit2.Response
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.model.IdeaModel

class IdeasByAuthorFragment : IdeasFragment() {
    override fun setAdapter(list: List<IdeaModel>) {
        with (recyclerView) {
            layoutManager = LinearLayoutManager(requireActivity())
            ideaAdapter = IdeaAdapter(list)
            adapter = ideaAdapter
        }
    }
    override suspend fun getRecentFromRepository(): Response<List<IdeaModel>> {
        val args = IdeasByAuthorFragmentArgs.fromBundle(arguments!!)
        return Repository.getRecentByAuthor(args.authorId)
    }
}