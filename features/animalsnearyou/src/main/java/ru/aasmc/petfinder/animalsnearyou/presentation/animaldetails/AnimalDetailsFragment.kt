package ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.*
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_HIGH_BOUNCY
import androidx.dynamicanimation.animation.SpringForce.STIFFNESS_VERY_LOW
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.aasmc.petfinder.animalsnearyou.R
import ru.aasmc.petfinder.animalsnearyou.databinding.FragmentDetailsBinding
import ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import ru.aasmc.petfinder.common.utils.setImageWithCrossFade
import ru.aasmc.petfinder.common.utils.toEmoji
import ru.aasmc.petfinder.common.utils.toEnglish

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {

    companion object {
        const val ANIMAL_ID = "id"
    }

    private val FLING_SCALE = 1f

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnimalDetailsFragmentViewModel by viewModels()

    private var animalId: Long? = null

    private val springForce: SpringForce by lazy {
        SpringForce().apply {
            // determines how quickly values change over time
            // DAMPING_RATIO_HIGH_BOUNCY makes a very bouncy spring
            dampingRatio = DAMPING_RATIO_HIGH_BOUNCY
            // sets the force with which the objects or views move
            stiffness = STIFFNESS_VERY_LOW
        }
    }

    // To increase the button size we increase the button's scaleX and scaleY properties
    private val callScaleXSpringAnimation: SpringAnimation by lazy {
        SpringAnimation(binding.call, DynamicAnimation.SCALE_X).apply {
            spring = springForce
        }
    }

    private val callScaleYSpringAnimation: SpringAnimation by lazy {
        SpringAnimation(binding.call, DynamicAnimation.SCALE_Y).apply {
            spring = springForce
        }
    }

    private val FLING_FRICTION = 2f

    private val callFlingXAnimation: FlingAnimation by lazy {
        FlingAnimation(binding.call, DynamicAnimation.X).apply {
            friction = FLING_FRICTION
            setMinValue(0f)
            setMaxValue(binding.root.width.toFloat() - binding.call.width.toFloat())
        }
    }

    private val callFlingYAnimation: FlingAnimation by lazy {
        FlingAnimation(binding.call, DynamicAnimation.Y).apply {
            friction = FLING_FRICTION
            setMinValue(0f)
            setMaxValue(binding.root.height.toFloat() - binding.call.width.toFloat())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        animalId = requireArguments().getLong(ANIMAL_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.share) {
            navigateToSharing()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToSharing() {
        val animalId = requireArguments().getLong(ANIMAL_ID)

        val directions = AnimalDetailsFragmentDirections.actionDetailsToSharing(animalId)

        findNavController().navigate(directions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()

        val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
        viewModel.handleEvent(event)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AnimalDetailsViewState.Loading -> {
                    displayLoading()
                }
                is AnimalDetailsViewState.Failure -> {
                    displayError()
                }
                is AnimalDetailsViewState.AnimalDetails -> {
                    displayPetDetails(state.animal, state.adopted)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun displayPetDetails(animalDetails: UIAnimalDetailed, adopted: Boolean) {
        binding.call.scaleX = 0.6f
        binding.call.scaleY = 0.6f
        binding.call.isVisible = true
        binding.scrollView.isVisible = true
        stopAnimation()
        binding.name.text = animalDetails.name
        binding.description.text = animalDetails.description
        binding.image.setImageWithCrossFade(animalDetails.photo)
        binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEmoji()
        binding.specialNeeds.text = animalDetails.specialNeeds.toEmoji()
        binding.declawed.text = animalDetails.declawed.toEmoji()
        binding.shotsCurrent.text = animalDetails.shotsCurrent.toEmoji()

        val doubleTapGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                (binding.heartImage.drawable as Animatable?)?.start()
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }
        }

        val doubleTapGestureDetector = GestureDetector(requireContext(), doubleTapGestureListener)

        binding.image.setOnTouchListener { _, event ->
            doubleTapGestureDetector.onTouchEvent(event)
        }

        callScaleXSpringAnimation.animateToFinalPosition(FLING_SCALE)
        callScaleYSpringAnimation.animateToFinalPosition(FLING_SCALE)

        val flingGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                callFlingXAnimation.setStartVelocity(velocityX).start()
                callFlingYAnimation.setStartVelocity(velocityY).start()
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }
        }

        val flingGestureDetector = GestureDetector(requireContext(), flingGestureListener)

        binding.call.setOnTouchListener { _, motionEvent ->
            flingGestureDetector.onTouchEvent(motionEvent)
        }

        callFlingYAnimation.addEndListener { _, _, _, _ ->
            if (areViewsOverlapping(binding.call, binding.image)) {
                val action = AnimalDetailsFragmentDirections.actionDetailsToSecret()
                findNavController().navigate(action)
            }
        }
        binding.adoptButton.setOnClickListener {
            binding.adoptButton.startLoading()
            viewModel.handleEvent(AnimalDetailsEvent.AdoptAnimal)
        }

        if (adopted) {
            binding.adoptButton.done()
        }
    }

    private fun displayError() {
        startAnimation(R.raw.lazy_cat)
        binding.scrollView.isVisible = false
        Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
    }

    private fun displayLoading() {
        startAnimation(R.raw.happy_dog)
        binding.scrollView.isVisible = false
    }

    private fun startAnimation(@RawRes animationRes: Int) {
        binding.loader.apply {
            isVisible = true
            setMinFrame(50)
            setMaxFrame(112)
            speed = 1.2f
            setAnimation(animationRes)
            playAnimation()
        }

        binding.loader.addValueCallback(
            KeyPath("icon_circle", "**"),
            LottieProperty.COLOR_FILTER,
            {
                PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP)
            }
        )
    }

    private fun stopAnimation() {
        binding.loader.apply {
            cancelAnimation()
            isVisible = false
        }
    }

    private fun areViewsOverlapping(view1: View, view2: View): Boolean {
        val firstRect = Rect()
        view1.getHitRect(firstRect)

        val secondRect = Rect()
        view2.getHitRect(secondRect)

        return Rect.intersects(firstRect, secondRect)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




























