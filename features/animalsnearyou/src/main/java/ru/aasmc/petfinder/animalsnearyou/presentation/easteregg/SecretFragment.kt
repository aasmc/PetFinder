package ru.aasmc.petfinder.animalsnearyou.presentation.easteregg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.aasmc.petfinder.animalsnearyou.databinding.FragmentSecretBinding
import ru.aasmc.petfinder.common.utils.setImage
import ru.aasmc.petfinder.remoteconfig.RemoteConfigUtil

class SecretFragment : Fragment() {
    private var _binding: FragmentSecretBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecretBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.secretImage.setImage(RemoteConfigUtil.getSecretImageUrl())
    }
}