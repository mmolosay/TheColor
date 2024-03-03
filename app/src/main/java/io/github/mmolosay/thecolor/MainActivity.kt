package io.github.mmolosay.thecolor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.home.HomeFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val fragmentContainer by lazy {
        findViewById<FragmentContainerView>(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().apply {
            val homeFragment = HomeFragment()
            add(fragmentContainer.id, homeFragment, "home_fragment")
        }.commit()
    }
}