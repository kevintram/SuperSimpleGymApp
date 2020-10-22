package com.kiwicorp.supersimplegymapp.ui.addeditroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.kiwicorp.supersimplegymapp.R
import com.kiwicorp.supersimplegymapp.databinding.FragmentChooseActivityBinding
import com.kiwicorp.supersimplegymapp.ui.chooseactivitycommon.ChooseActivityListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseActivityFragment : Fragment() {

    private lateinit var binding: FragmentChooseActivityBinding

    // must pass defaultViewModelProviderFactory https://github.com/google/dagger/issues/1935
    private val viewModel: AddEditRoutineViewModel by navGraphViewModels(R.id.addEditRoutineGraph) {
        defaultViewModelProviderFactory
    }

    private lateinit var adapter: ChooseActivityListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChooseActivityBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        adapter = ChooseActivityListAdapter(viewModel)
        binding.activityRecyclerView.adapter = adapter
        viewModel.activities.observe(viewLifecycleOwner, Observer {
            adapter.addHeadersAndSubmitList(it)
        })
    }
}