package com.example.coreandroid.view.binding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_margin")
fun setMargin(view: View, margin: Int) {
    (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
        it.setMargins(margin, margin, margin, margin)
        view.layoutParams = it
    }
}
