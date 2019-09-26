package com.example.search

import android.os.Bundle
import android.view.*
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.setHasOptionsMenuIfVisible


class SearchFragment : InjectableVectorFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenuIfVisible()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.menuView?.menu?.clear()
        menuController?.menuView?.let { inflater.inflate(R.menu.search_menu, it.menu) }
        super.onCreateOptionsMenu(menu, inflater)
    }
}
