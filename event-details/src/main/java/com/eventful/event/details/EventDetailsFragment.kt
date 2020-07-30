package com.eventful.event.details

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.eventful.core.android.description
import com.eventful.core.android.eventInfo
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.binding.eventRequestOptions
import com.eventful.core.android.view.epoxy.asyncController
import com.eventful.core.android.view.epoxy.kindsCarousel
import com.eventful.event.details.databinding.FragmentEventDetailsBinding
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailsFragment : Fragment() {

    private var event: Event by FragmentArgument()
    private var statusBarColor: Int? = null

    private val epoxyController: AsyncEpoxyController by lazy(LazyThreadSafetyMode.NONE) {
        asyncController {
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = FragmentEventDetailsBinding.inflate(inflater, container, false).apply {
        event = this@EventDetailsFragment.event
        eventDetailsRecyclerView.setController(epoxyController)
        setupToolbarWithDrawerToggle(eventDetailsToolbar)
        if (savedInstanceState?.containsKey(KEY_STATUS_BAR_COLOR) != true) {
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
                                    statusBarColor = it
                                    activity?.statusBarColor = it
                                }
                            }
                        }
                        return false
                    }
                })
                .into(expandedImage)
        }
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        epoxyController.requestModelBuild()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(KEY_STATUS_BAR_COLOR) == true) {
            statusBarColor = savedInstanceState.getInt(KEY_STATUS_BAR_COLOR)
        }
    }

    override fun onResume() {
        super.onResume()
        event_details_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
        }
        statusBarColor?.let { activity?.statusBarColor = it }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        statusBarColor?.let { outState.putInt(KEY_STATUS_BAR_COLOR, it) }
    }

    companion object {
        fun new(event: Event): EventDetailsFragment = EventDetailsFragment().also {
            it.event = event
        }

        private const val KEY_STATUS_BAR_COLOR = "KEY_STATUS_BAR_COLOR"
    }
}
