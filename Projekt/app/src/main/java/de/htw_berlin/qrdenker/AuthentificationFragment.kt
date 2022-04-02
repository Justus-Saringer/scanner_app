package de.htw_berlin.qrdenker

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.eu.fragmentstatemanager.StateManager
import de.htw_berlin.qrdenker.databinding.FragmentAuthentificationBinding
import kotlinx.android.synthetic.main.fragment_authentification.*
import com.google.firebase.auth.FirebaseAuth
import de.htw_berlin.qrdenker.MainActivity.Companion.SCANNER_ID

class AuthentificationFragment : Fragment() {

    val ref = FirebaseAuth.getInstance()

    lateinit var binding : FragmentAuthentificationBinding
    lateinit var auth : FirebaseAuth

    internal lateinit var loginTxtEditxt : EditText
    internal lateinit var loginPwdEditxt : EditText
    internal lateinit var loginBtn : Button

    internal lateinit var regisEmailEditxt : EditText
    internal lateinit var regisPwdEditText: EditText
    internal lateinit var reRegisPwdEditText: EditText
    internal lateinit var regBtn : Button

    internal lateinit var logSwitchTxtView : TextView
    internal lateinit var regSwitchTxtView : TextView
    internal lateinit var logRegSwitch : Switch

    // false: login, true registration
    private var loginOrRegistration : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAuthentificationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        loginTxtEditxt = binding.loginEmailEditText
        loginPwdEditxt = binding.loginPasswordEditText
        loginBtn = binding.loginButton

        regisEmailEditxt = binding.emailEditText
        regisPwdEditText = binding.passwordEditText
        reRegisPwdEditText = binding.rePasswordEditText
        regBtn = binding.regButton

        logSwitchTxtView = binding.loginTxtview
        regSwitchTxtView = binding.registrationTxtview
        logRegSwitch = binding.authentificationSwitch

        logSwitchTxtView.setTypeface(null, Typeface.BOLD)

        logRegSwitch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked){
                logSwitchTxtView.setTypeface(null, Typeface.NORMAL)
                regSwitchTxtView.setTypeface(null, Typeface.BOLD)
                authentification_layout.isVisible = false
                registration_layout.isVisible = true
                loginOrRegistration = true
            }
            else{
                logSwitchTxtView.setTypeface(null, Typeface.BOLD)
                regSwitchTxtView.setTypeface(null, Typeface.NORMAL)
                authentification_layout.isVisible = true
                registration_layout.isVisible = false
                loginOrRegistration = false
            }
        }

        regBtn.setOnClickListener {
            if (checkPassword())
            {
                registerUser()
            }
            else
            {
                var pwToast = Toast.makeText(this.requireActivity(), "Your Application has no internet connection\n\nThe app will not work properly!", Toast.LENGTH_LONG)
                pwToast.setGravity(Gravity.CENTER,0, 0)
                pwToast.show()
            }
        }

        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun registerUser() {

        var email : String = binding.emailEditText.text.toString().trim()
        var password: String = binding.passwordEditText.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireActivity(), "Registration Successful", Toast.LENGTH_SHORT).show()
                    //startActivity(Intent(this, ChatActivity::class.java))
                    binding.emailEditText.text.clear()
                    binding.passwordEditText.text.clear()
                } else {
                    Toast.makeText(this.requireActivity(), "An error occurred", Toast.LENGTH_SHORT).show()
                    binding.emailEditText.text.clear()
                    binding.passwordEditText.text.clear()
                }
            }
    }

    private fun loginUser() {
        var email: String = binding.loginEmailEditText.text.toString().trim()
        var password: String = binding.loginPasswordEditText.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireActivity(), "Login successful", Toast.LENGTH_SHORT).show()
                    //startActivity(Intent(this, ChatActivity::class.java))
                    binding.loginEmailEditText.text.clear()
                    binding.loginPasswordEditText.text.clear()
                    (activity as MainActivity).privilege = true
                    StateManager.getInstance().removeAllFragmentStream(SCANNER_ID, ScannerFragment())
                } else {
                    Toast.makeText(this.requireActivity(), "Unable to login. Check your input or try again later", Toast.LENGTH_SHORT).show()
                    binding.loginEmailEditText.text.clear()
                    binding.loginPasswordEditText.text.clear()
                }
            }
    }

    private fun checkPassword() : Boolean
    {
        var pwOne : String = binding.passwordEditText.text.toString()
        var pwTwo : String = binding.rePasswordEditText.text.toString()

        return pwOne == pwTwo
    }
}