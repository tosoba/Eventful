package com.example.nearby

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.events.EventsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.coroutines.CoroutineContext


class NearbyFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

    private val supervisorJob = Job()

    private val viewModel: NearbyViewModel by viewModel()

    private val eventsFragment: EventsFragment by lazy(LazyThreadSafetyMode.NONE) {
        childFragmentManager.findFragmentById(R.id.nearby_events_list_fragment) as EventsFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.viewStateStore.currentState.events.doIfEmptyAndLoadingNotInProgress {
            viewModel.loadEvents()
        }
    }

    override fun onDestroy() {
        supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewStateStore.observe(this) {
            if (it.events.lastLoadingStatus == PagedAsyncData.LoadingStatus.CompletedSuccessfully) {
                eventsFragment.updateEvents(it.events.items)
            }
        }

        launch(Dispatchers.Main) {
            eventsFragment.eventClickedChannel.consumeEach {
                Log.e("EVENT", it.title)
            }
        }
    }
}

