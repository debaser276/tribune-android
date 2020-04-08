package ru.debaser.projects.tribune.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_reg.*

class RegFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        haveAccountTv.setOnClickListener {
            view.findNavController().navigate(RegFragmentDirections.actionRegFragmentToAuthFragment())
        }
    }
}