package com.dc.codescanner

import java.io.Serializable

class CodeScannerConfig private constructor() : Serializable {


    enum class CodeType {
        ALL_BARCODE, ALL_QR, ALL
    }

    var codeType = CodeType.ALL

    class Builder {

        private val codeScannerConfig = CodeScannerConfig()

        fun setCodeType(codeType: CodeType): Builder {
            codeScannerConfig.codeType = codeType
            return this
        }

        fun build(): CodeScannerConfig {
            return codeScannerConfig
        }
    }
}