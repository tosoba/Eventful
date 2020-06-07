package com.example.coreandroid.view.ext

import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.hideAndShow() {
    hide(object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            fab?.show()
        }
    })
}