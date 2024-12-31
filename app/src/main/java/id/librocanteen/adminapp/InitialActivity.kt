package id.librocanteen.adminapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import id.librocanteen.adminapp.dashboard.objects.SharedViewModel

class InitialActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        sharedViewModel.isUserValidated.observe(this, { isValidated ->
            if (isValidated) {
                findViewById<TextView>(R.id.appNameText).visibility = View.GONE
                toolbar.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.GONE
                findViewById<TextView>(R.id.appNameText).visibility = View.VISIBLE
            }
        })
        setContentView(R.layout.activity_initial)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_threebar_black)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> navigateToHome()
                R.id.nav_messages -> navigateToMessages()
                R.id.nav_whoIs -> navigateToWhoIs()
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun navigateToHome() {
        // Navigation logic
    }

    private fun navigateToMessages() {
        // Navigation logic
    }

    private fun navigateToWhoIs() {
        // Navigation logic
    }

    private fun logout() {
        // Handle logout logic
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

