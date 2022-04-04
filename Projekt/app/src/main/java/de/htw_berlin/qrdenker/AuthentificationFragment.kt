package de.htw_berlin.qrdenker

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
import java.lang.Exception

class AuthentificationFragment : Fragment() {

    val ref = FirebaseAuth.getInstance()

    private var binding : FragmentAuthentificationBinding? = null
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
        if (binding == null) throw Exception("AuthentificationFragmentBinding is null")
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        loginTxtEditxt = requireNotNull(binding?.loginEmailEditText)
        loginPwdEditxt = requireNotNull(binding?.loginPasswordEditText)
        loginBtn = requireNotNull(binding?.loginButton)

        regisEmailEditxt = requireNotNull(binding?.emailEditText)
        regisPwdEditText = requireNotNull(binding?.passwordEditText)
        reRegisPwdEditText = requireNotNull(binding?.rePasswordEditText)
        regBtn = requireNotNull(binding?.regButton)

        logSwitchTxtView = requireNotNull(binding?.loginTxtview)
        regSwitchTxtView = requireNotNull(binding?.registrationTxtview)
        logRegSwitch = requireNotNull(binding?.authentificationSwitch)

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
                val pwToast = Toast.makeText(this.requireActivity(), "Your Application has no internet connection\n\nThe app will not work properly!", Toast.LENGTH_LONG)
                pwToast.setGravity(Gravity.CENTER,0, 0)
                pwToast.show()
            }
        }

        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun registerUser() {

        val email : String = binding?.emailEditText?.text.toString().trim()
        val password: String = binding?.passwordEditText?.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireActivity(), "Registration Successful", Toast.LENGTH_SHORT).show()
                    //startActivity(Intent(this, ChatActivity::class.java))
                    binding?.emailEditText?.text?.clear()
                    binding?.passwordEditText?.text?.clear()
                } else {
                    Toast.makeText(this.requireActivity(), "An error occurred", Toast.LENGTH_SHORT).show()
                    binding?.emailEditText?.text?.clear()
                    binding?.passwordEditText?.text?.clear()
                }
            }
    }

    private fun loginUser() {
        val email: String = binding?.loginEmailEditText?.text.toString().trim()
        val password: String = binding?.loginPasswordEditText?.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireActivity(), "Login successful", Toast.LENGTH_SHORT).show()
                    //startActivity(Intent(this, ChatActivity::class.java))
                    binding?.loginEmailEditText?.text?.clear()
                    binding?.loginPasswordEditText?.text?.clear()
                    (activity as MainActivity).privilege = true
                    StateManager.getInstance().removeAllFragmentStream(SCANNER_ID, ScannerFragment())
                } else {
                    Toast.makeText(this.requireActivity(), "Unable to login. Check your input or try again later", Toast.LENGTH_SHORT).show()
                    binding?.loginEmailEditText?.text?.clear()
                    binding?.loginPasswordEditText?.text?.clear()
                }
            }
    }

    private fun checkPassword() : Boolean
    {
        val pwOne : String = binding?.passwordEditText?.text.toString()
        val pwTwo : String = binding?.rePasswordEditText?.text.toString()

        return pwOne == pwTwo
    }
}