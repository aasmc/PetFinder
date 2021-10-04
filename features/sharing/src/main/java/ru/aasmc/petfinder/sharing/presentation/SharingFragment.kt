package ru.aasmc.petfinder.sharing.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.aasmc.petfinder.common.utils.setImage
import ru.aasmc.petfinder.di.SharingModuleDependencies
import ru.aasmc.petfinder.sharing.databinding.FragmentSharingBinding
import ru.aasmc.petfinder.sharing.di.DaggerSharingComponent
import ru.aasmc.petfinder.sharing.di.ViewModelFactory
import javax.inject.Inject

class SharingFragment : Fragment() {

    companion object {
        const val ANIMAL_ID = "id"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var _binding: FragmentSharingBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel by viewModels<SharingFragmentViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerSharingComponent.builder()
            .context(requireActivity())
            .moduleDependencies(
                EntryPointAccessors.fromApplication(
                    requireActivity().applicationContext,
                    SharingModuleDependencies::class.java
                )
            )
            .build()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewStateUpdates()
    }

    private fun setupUI() {
        val animalId = requireArguments().getLong(ANIMAL_ID)
        viewModel.onEvent(SharingEvent.GetAnimalToShare(animalId))

        binding.shareButton.setOnClickListener {
            Snackbar.make(requireView(), "Shared! Or not :]", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun observeViewStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { render(it) }
            }
        }
    }

    private fun render(viewState: SharingViewState) {
        val (image, message) = viewState.animalToShare

        binding.image.setImage(image)
        binding.messageToShareEditText.setText(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





























