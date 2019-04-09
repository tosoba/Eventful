package com.example.nearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.events.EventsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class NearbyFragment : Fragment() {

    private val viewModel: NearbyViewModel by viewModel()

    private val eventsFragment: EventsFragment by lazy(LazyThreadSafetyMode.NONE) {
        childFragmentManager.findFragmentById(R.id.nearby_events_list_fragment) as EventsFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadEvents()
        viewModel.viewStateStore.observe(this) {
            if (it.events.lastLoadingStatus == PagedAsyncData.LoadingStatus.CompletedSuccessfully) {
                eventsFragment.updateEvents(it.events.items)
            }
        }
    }

}
