package com.hb.codescanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dc.codescanner.CodeScannerActivity
import com.dc.codescanner.CodeScannerConfig
import com.dc.codescanner.controls.ScannerResult

import com.hb.codescanner.databinding.ActivityCodeScannerSampleBinding

class CodeScannerSampleActivity : AppCompatActivity() {
    lateinit var binding: ActivityCodeScannerSampleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_code_scanner_sample)

        binding.btnOpenScanner.setOnClickListener {

            startActivityForResult(
                CodeScannerActivity.createIntent(this@CodeScannerSampleActivity,
                    CodeScannerConfig.Builder().apply {
                        setCodeType(when (binding.rgCodeType.checkedRadioButtonId) {
                            R.id.rbQrOnly -> {
                                CodeScannerConfig.CodeType.ALL_QR
                            }
                            R.id.rbAllCode -> {
                                CodeScannerConfig.CodeType.ALL
                            }
                            R.id.rbBarcode -> {
                                CodeScannerConfig.CodeType.ALL_BARCODE
                            }
                            else -> {
                                CodeScannerConfig.CodeType.ALL
                            }
                        })
                    }.build()), 111)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val scannerResult = data?.getParcelableExtra<ScannerResult>(CodeScannerActivity.RESULT_KEY)
            binding.tvResult.text = scannerResult?.result
        }
    }
}