package com.dc.codescanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.dc.codescanner.controls.*
import com.dc.codescanner.databinding.ActivityCodeScannerBinding


class CodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCodeScannerBinding
    private lateinit var codeScanner: CodeScanner


    companion object {

        val RESULT_KEY = "result"
        fun createIntent(context: Context, codeScannerConfig: CodeScannerConfig): Intent {
            val intentX = Intent(context, CodeScannerActivity::class.java)
            intentX.putExtra("codeScannerConfig", codeScannerConfig)
            return intentX
        }
    }

    lateinit var codeScannerConfig: CodeScannerConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_code_scanner)

        if (intent.hasExtra("codeScannerConfig")) {
            codeScannerConfig = intent.getSerializableExtra("codeScannerConfig") as CodeScannerConfig
        } else {
            codeScannerConfig = CodeScannerConfig.Builder().setCodeType(CodeScannerConfig.CodeType.ALL).build()
        }


        binding.scannerView.autoFocusView.visibility = View.GONE
        binding.scannerView.setFlashButton(binding.ivFlashButton)


        makeItFullscreen(window)
        changeStatusbarIconLight(true)


        codeScanner = CodeScanner(this, binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id

        when (codeScannerConfig.codeType) {
            CodeScannerConfig.CodeType.ALL_BARCODE -> {
                codeScanner.formats = CodeScanner.ALL_BARCODE_FORMATS // list of type BarcodeFormat,
            }
            CodeScannerConfig.CodeType.ALL_QR -> {
                codeScanner.formats = CodeScanner.ALL_QR_FORMAT
            }
            else -> {
                codeScanner.formats = CodeScanner.ALL_FORMATS
            }
        }

        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val scannerResult = ScannerResult(it.text)
                val intentX = Intent()
                intentX.putExtra(RESULT_KEY, scannerResult)
                setResult(Activity.RESULT_OK, intentX)
                finish()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
            }
        }
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (!hasCameraPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 177)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }


    private fun makeItFullscreen(window: Window) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun changeStatusbarIconLight(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags: Int = window.decorView.systemUiVisibility
            if (isLight) {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 177 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            val showRationale = shouldShowRequestPermissionRationale(permissions[0])
            if (!showRationale) {
                Toast.makeText(this@CodeScannerActivity, "No Camera permission.Please enable it from settings", Toast.LENGTH_LONG).show()
                showSettingScreen()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun showSettingScreen() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}