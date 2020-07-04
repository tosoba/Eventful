package com.example.eventdetails

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.coreandroid.description
import com.example.coreandroid.eventInfo
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.view.binding.eventRequestOptions
import com.example.coreandroid.view.epoxy.kindsCarousel
import com.example.coreandroid.view.epoxy.simpleController
import com.example.eventdetails.databinding.FragmentEventDetailsBinding
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailsFragment : Fragment() {

    private var event: Event by FragmentArgument()
    private var statusBarColor: Int? = null

    private val epoxyController: AsyncEpoxyController by lazy(LazyThreadSafetyMode.NONE) {
        simpleController {
            eventInfo {
                id("${event.id}i")
                event(event)
                margin(requireContext().toPx(15f).toInt())
            }
            event.kindsCarousel.addTo(this)
            description {
                id("${event.id}d")
                text(event.info ?: "No details available")
                margin(requireContext().toPx(15f).toInt())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = DataBindingUtil.inflate<FragmentEventDetailsBinding>(
        inflater, R.layout.fragment_event_details, container, false
    ).apply {
        event = this@EventDetailsFragment.event
        eventDetailsRecyclerView.setController(epoxyController)
        setupToolbarWithDrawerToggle(eventDetailsToolbar)
        Glide.with(expandedImage)
            .load(this@EventDetailsFragment.event.imageUrl)
            .apply(eventRequestOptions)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { drawable ->
                        lifecycleScope.launch {
                            statusBarColor = withContext(Dispatchers.Default) {
                                drawable.bitmap.dominantColor
                            }.also {
                                activity?.statusBarColor = it
                                //TODO: save it in onSaveInstanceState -> set in onResume -> reset back to primary in WeatherFragment and on popped from back stack
                            }
                        }
                    }
                    return false
                }
            })
            .into(expandedImage)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        epoxyController.requestModelBuild()
    }

    override fun onResume() {
        super.onResume()
        event_details_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
        }
    }

    companion object {
        fun new(event: Event): EventDetailsFragment = EventDetailsFragment().apply {
            this.event = event
        }
    }
}
