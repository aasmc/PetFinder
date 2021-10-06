package ru.aasmc.petfinder.main.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.aasmc.petfinder.R
import ru.aasmc.petfinder.animalsnearyou.presentation.main.AnimalsNearYouFragmentViewModel
import ru.aasmc.petfinder.common.data.preferences.PetSavePreferences
import ru.aasmc.petfinder.common.data.preferences.Preferences
import ru.aasmc.petfinder.common.domain.model.user.User
import ru.aasmc.petfinder.common.domain.repositories.UserRepository
import ru.aasmc.petfinder.common.utils.FileConstants
import ru.aasmc.petfinder.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel by viewModels<MainActivityViewModel>()
    private val animalsNearYouViewModel by viewModels<AnimalsNearYouFragmentViewModel>()

    private var isSignedUp = false
    private var workingFile: File? = null

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(ru.aasmc.petfinder.R.id.nav_host_fragment) as DynamicNavHostFragment)
            .navController
    }

    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.onboardingFragment,
                R.id.animalsNearYouFragment,
                R.id.searchFragment,
                R.id.report
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        // todo disable screenshots

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragment()
        setupActionBar()
        setupBottomNav()
        setupWorkingFiles()
        updateLoggedInState()
//        triggerStartDestinationEvent()
        observeViewEffects()
    }

    private fun setupFragment() {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .hide(nav_host_fragment)
            .commit()
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
        binding.bottomNavigation.visibility = View.GONE
        binding.bottomNavigation.setupWithNavController(navController)
        hideBottomNavWhenInOnboarding()
    }

    private fun setupWorkingFiles() {
        workingFile = File(
            filesDir.absolutePath + File.separator +
                    FileConstants.DATA_SOURCE_FILE_NAME
        )
    }

    fun loginPressed(view: View) {
        displayLogin(view, false)
    }

    private fun updateLoggedInState() {
        val fileExists = workingFile?.exists() ?: false
        if (fileExists) {
            isSignedUp = true
            binding.loginButton.text = getString(R.string.login)
            binding.loginEmail.visibility = View.INVISIBLE
        } else {
            binding.loginButton.text = getString(R.string.signup)
        }
    }

    private fun displayLogin(view: View, fallback: Boolean) {
        performLoginOperation(view)
    }

    private fun performLoginOperation(view: View) {
        var success = false
        val preferences: Preferences = PetSavePreferences(this)

        workingFile?.let {
            // check if already signed up
            if (isSignedUp) {
                val fileInputStream = FileInputStream(it)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val list = objectInputStream.readObject() as ArrayList<User>
                val firstUser = list.first() as? User
                if (firstUser is User) {
                    // todo replace below with implementation that decrypts password
                    success = true
                }
                if (success) {
                    Toast.makeText(
                        this,
                        "Last login: ${preferences.getLastLoggedIn()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Please check your credentials and try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                objectInputStream.close()
                fileInputStream.close()
            } else {
                UserRepository.createDataSource(applicationContext, it, ByteArray(0))
                success = true
            }
        }
        if (success) {
            preferences.putLastLoggedInTime()
            animalsNearYouViewModel.setIsLoggedIn(true)

            binding.loginEmail.visibility = View.GONE
            binding.loginButton.visibility = View.GONE

            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction()
                .show(nav_host_fragment)
                .commit()
            fragmentManager.executePendingTransactions()
            binding.bottomNavigation.visibility = View.VISIBLE
            triggerStartDestinationEvent()
        }
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
        mainViewModel.onEvent(MainActivityEvent.DefineStartDestination)
    }

    private fun observeViewEffects() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.viewEffect.collect { reactTo(it) }
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
































