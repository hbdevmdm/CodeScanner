package com.dc.codescanner.controls

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScannerResult(var result: String) : Parcelable
