package com.atarusov.daylightnet

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.atarusov.appComponent
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var usersRepository: UsersRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        applicationContext.appComponent.inject(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.registerFragment) {
                hideBottomNavView()
            } else {
                showBottomNavView()
            }
        }

        if (usersRepository.currentUserId.value != null) {
            navController.navigate(R.id.bottomNavFragments)
        }
    }

    private fun hideBottomNavView() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    private fun showBottomNavView() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }
}