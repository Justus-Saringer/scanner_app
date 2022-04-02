package de.htw_berlin.qrdenker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.eu.fragmentstatemanager.StateManager
import com.eu.fragmentstatemanager.StateManagerBuilder
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModel
import kotlinx.android.synthetic.main.activity_main.*
import android.net.NetworkInfo

import android.net.ConnectivityManager
import android.view.Gravity


class MainActivity : AppCompatActivity() {

    companion object{
        const val SCANNER_ID = 574254
        const val CREATING_ID = 342623
        const val SEARCH_ID = 281506
        const val AUTHENT_ID = 850848
    }

    var privilege : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         bottom_navigation.selectedItemId = R.id.scanner

        StateManager.buildInstance(
            StateManagerBuilder(SCANNER_ID, CREATING_ID, SEARCH_ID, AUTHENT_ID)
                .withSupportFragmentManager(supportFragmentManager)
                .withViewGroup(fl_wrapper)
        ).showOnNavigationClick(SCANNER_ID, ScannerFragment())


        bottom_navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.scanner -> {
                    StateManager.getInstance().showOnNavigationClick(SCANNER_ID, ScannerFragment())
                    checkInternetConnection()
                }
                R.id.addQRCode -> {
                    if (privilege)
                    {
                        StateManager.getInstance().showOnNavigationClick(CREATING_ID, RecordFragment())
                        checkInternetConnection()
                    }
                    else
                    {
                        Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.search -> {
                    StateManager.getInstance().showOnNavigationClick(SEARCH_ID,SearchFragment())
                    checkInternetConnection()
                }
                R.id.authent -> {
                    StateManager.getInstance().showOnNavigationClick(AUTHENT_ID, AuthentificationFragment())
                    checkInternetConnection()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun checkInternetConnection()
    {
        var connected : Boolean
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connected =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        if (!connected)
        {
            var connectionToast = Toast.makeText(this, "Your Application has no internet connection\n\nThe app will not work properly!", Toast.LENGTH_LONG)
            connectionToast.setGravity(Gravity.CENTER,0, 0)

            connectionToast.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StateManager.getInstance().removeAll()
    }
}