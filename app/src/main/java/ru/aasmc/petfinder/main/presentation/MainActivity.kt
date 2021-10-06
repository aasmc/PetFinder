package ru.aasmc.petfinder.main.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.aasmc.petfinder.R
import ru.aasmc.petfinder.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainActivityViewModel>()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(ru.aasmc.petfinder.R.id.nav_host_fragment) as DynamicNavHostFragment)
            .navController
    }

    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.onboardingFragment,
                R.id.animalsNearYouFragment,
                R.id.searchFragment
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupBottomNav()
        triggerStartDestinationEvent()
        observeViewEffects()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.theme_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val themeMode = when (item.itemId) {
            R.id.light_theme -> {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            R.id.dark_theme -> {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            else -> {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setupWithNavController(navController)
        hideBottomNavWhenInOnboarding()
    }

    private fun hideBottomNavWhenInOnboarding() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.onboardingFragment) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    private fun triggerStartDestinationEvent() {
        viewModel.onEvent(MainActivityEvent.DefineStartDestination)
    }

    private fun observeViewEffects() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewEffect.collect { reactTo(it) }
            }
        }
    }

    private fun reactTo(effect: MainActivityViewEffect) {
        when (effect) {
            is MainActivityViewEffect.SetStartDestination -> setNavGraphStartDestination(effect.destination)
        }
    }

    private fun setNavGraphStartDestination(startDestination: Int) {
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navGraph.startDestination = startDestination
        navController.graph = navGraph
    }

}
































