package ru.aasmc.petfinder.main.presentation

import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.aasmc.petfinder.R
import ru.aasmc.petfinder.animalsnearyou.presentation.main.AnimalsNearYouFragmentViewModel
import ru.aasmc.petfinder.common.data.api.Authenticator
import ru.aasmc.petfinder.common.data.api.ReportManager
import ru.aasmc.petfinder.common.data.preferences.PetSavePreferences
import ru.aasmc.petfinder.common.data.preferences.Preferences
import ru.aasmc.petfinder.common.domain.model.user.User
import ru.aasmc.petfinder.common.domain.repositories.UserRepository
import ru.aasmc.petfinder.common.utils.DataValidator.Companion.isValidEmailString
import ru.aasmc.petfinder.common.utils.Encryption.Companion.createLoginPassword
import ru.aasmc.petfinder.common.utils.Encryption.Companion.decryptPassword
import ru.aasmc.petfinder.common.utils.Encryption.Companion.generateSecretKey
import ru.aasmc.petfinder.common.utils.FileConstants
import ru.aasmc.petfinder.common.utils.Timing
import ru.aasmc.petfinder.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel by viewModels<MainActivityViewModel>()
    private val animalsNearYouViewModel by viewModels<AnimalsNearYouFragmentViewModel>()

    private var isSignedUp = false
    private var workingFile: File? = null

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as DynamicNavHostFragment)
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

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    val clientAuthenticator = Authenticator()
    var serverPublicKeyString = ""
    val reportManager = ReportManager()

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        // disables screenshots
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragment()
        setupActionBar()
        setupBottomNav()
        setupWorkingFiles()
        updateLoggedInState()
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
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
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
        var success = false
        val email = binding.loginEmail.text.toString()
        if (isSignedUp || isValidEmailString(email)) {
            success = true
        } else {
            showToast("Please enter a valid email.")
        }
        if (success) {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    displayLogin(view, false)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    displayLogin(view, true)
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    showToast("Biometric features are currently unavailable.")
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    showToast("Please associate a biometric credential with your account.")
                }
                else -> {
                    showToast("An unknown error occurred. Please check your biometric settings.")
                }
            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
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
        val executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {
                        showToast("Authentication error: $errString")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        showToast("Authentication failed.")
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    runOnUiThread {
                        showToast("Authentication succeeded.")
                        if (!isSignedUp) {
                            generateSecretKey()
                        }
                        performLoginOperation(view)
                    }
                }
            })

        promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for Pet Finder")
                .setSubtitle("Login using your biometric credentials.")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()


        biometricPrompt.authenticate(promptInfo)
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
                    val password = decryptPassword(
                        Base64.decode(firstUser.password, Base64.NO_WRAP),
                        preferences
                    )
                    if (password.isNotEmpty()) {
                        // here is the place to send the password to the server to authenticat
                        // login to a simulated server with password and public key
                        // once the server verifies that info, it returns its public key as a string
                        serverPublicKeyString = reportManager.login(
                            Base64.encodeToString(password, Base64.NO_WRAP),
                            clientAuthenticator.publicKey()
                        )
                        success = serverPublicKeyString.isNotEmpty()
                    }
                }
                // Prevent timing attack by adding random delay
                Timing.doRandomWork()
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
                val encryptedInfo = createLoginPassword(preferences)
                UserRepository.createDataSource(applicationContext, it, encryptedInfo)
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
































