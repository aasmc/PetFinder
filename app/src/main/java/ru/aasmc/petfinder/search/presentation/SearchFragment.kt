package ru.aasmc.petfinder.search.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okio.IOException
import retrofit2.HttpException
import ru.aasmc.petfinder.R
import ru.aasmc.petfinder.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinder.common.presentation.AnimalsAdapter
import ru.aasmc.petfinder.common.presentation.Event
import ru.aasmc.petfinder.databinding.FragmentSearchBinding

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ITEMS_PER_ROW = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        // todo call prepare for search method here
    }

    private fun setupUI() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
        observeViewStateUpdates(adapter)
    }

    private fun createAdapter(): AnimalsAdapter {
        return AnimalsAdapter()
    }

    private fun setupRecyclerView(searchAdapter: AnimalsAdapter) {
        binding.searchRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            setHasFixedSize(true)
        }
    }

    private fun observeViewStateUpdates(searchAdapter: AnimalsAdapter) {
        // todo observe state updates here
    }

    private fun handleFailures(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return
        handleThrowable(unhandledFailure)
    }

    private fun handleThrowable(exception: Throwable) {
        val fallBackMessage = getString(R.string.an_error_occurred)
        val snackBarMessage = when (exception) {
            is NoMoreAnimalsException -> exception.message ?: fallBackMessage
            is IOException, is HttpException -> fallBackMessage
            else -> ""
        }

        if (snackBarMessage.isNotEmpty()) {
            Snackbar.make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



























