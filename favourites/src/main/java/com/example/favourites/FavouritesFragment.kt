package com.example.favourites

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.coreandroid.util.ext.menuController


class FavouritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.menuView?.menu?.clear()
    }
}
