package de.htw_berlin.qrdenker

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.eu.fragmentstatemanager.StateManager
import de.htw_berlin.qrdenker.databinding.FragmentScannerBinding
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModel
import java.lang.Exception
import java.util.jar.Manifest

private const val CAMERA_REQUEST_CODE = 101

class ScannerFragment: Fragment() {
    //internal lateinit var binding : FragmentScannerBinding
    internal val documentViewModel : DocumentViewModel by activityViewModels()
    private lateinit var  codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = this.requireActivity()
        checkPermission()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            try {
                documentViewModel.fetch("testCollection", it.text)
                if((activity as MainActivity).privilege){
                    StateManager.getInstance().showFragment(MainActivity.CREATING_ID, RecordFragment())
                }
                else{
                    StateManager.getInstance().showFragment(MainActivity.SEARCH_ID, RecordReadOnlyFragment())
                }
            }
            catch (e: Exception){
                Log.e("Error Message: ", e.message.toString())

            }

        }
        scannerView.setOnClickListener{
            codeScanner.startPreview()
        }
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this.requireContext(),android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 123)
        }
        else{
            //start scanning
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this.requireContext(),"Camera permission granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this.requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }
}