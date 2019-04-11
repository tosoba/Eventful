package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.core.model.Event
import com.example.coreandroid.util.FragmentArgument


class EventFragment : Fragment() {

    private var event: Event by FragmentArgument()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().apply {
            this.event = event
        }
    }
}
