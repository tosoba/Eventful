package com.eventful.event.details

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.*
import com.eventful.core.android.description
import com.eventful.core.android.eventInfo
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.ViewPagerPageSelectedListener
import com.eventful.core.android.view.binding.eventRequestOptions
import com.eventful.core.android.view.epoxy.asyncController
import com.eventful.core.android.view.epoxy.kindsCarousel
import com.eventful.core.android.view.ext.hideAndShow
import com.eventful.core.android.wideButton
import com.eventful.event.details.databinding.FragmentEventDetailsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

@ExperimentalCoroutinesApi
@FlowPreview
class EventDetailsFragment :
    DaggerViewModelFragment<EventDetailsViewModel>(),
    SnackbarController,
    HasArgs {

    private var event: Event by FragmentArgument()
    override val args: Bundle get() = bundleOf(EVENT_ARG_KEY to event)

    private var statusBarColor: Int? = null

    private val epoxyController: AsyncEpoxyController by lazy(LazyThreadSafetyMode.NONE) {
        asyncController {
            val marginValue = requireContext().toPx(15f).toInt()
            eventInfo {
                id("${event.id}i")
                event(event)
                margin(marginValue)
            }
            event.kindsCarousel.addTo(this)
            description {
                id("${event.id}d")
                text(event.info ?: "No details available")
                margin(marginValue)
            }
            wideButton {
                id("${event.id}url")
                text(getString(R.string.go_to_website))
                margin(marginValue)
                clicked { _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    private val onPageSelectedListener: ViewPagerPageSelectedListener by lazy(LazyThreadSafetyMode.NONE) {
        EventNavigationController.onPageSelectedListenerWith(event_details_bottom_nav_view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = FragmentEventDetailsBinding.inflate(inflater, container, false).apply {
        event = this@EventDetailsFragment.event
        eventDetailsRecyclerView.setController(epoxyController)
        setupToolbarWithDrawerToggle(eventDetailsToolbar)

        eventDetailsBottomNavView.setOnNavigationItemSelectedListener(
            eventNavigationItemSelectedListener
        )
        addOnEventPageChangeListener(onPageSelectedListener)

        eventFavFab.clicks()
            .onEach { viewModel.intent(EventDetailsIntent.ToggleFavourite) }
            .launchIn(lifecycleScope)
        snackbarStateChannel = handleSnackbarState(eventFavFab)

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

    override fun onDestroyView() {
        snackbarStateChannel.close()
        removeOnEventPageChangeListener(onPageSelectedListener)
        super.onDestroyView()
    }

    override fun transitionToSnackbarState(newState: SnackbarState) {
        if (!snackbarStateChannel.isClosedForSend) snackbarStateChannel.offer(newState)
    }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        statusBarColor?.let { outState.putInt(KEY_STATUS_BAR_COLOR, it) }
    }

    private var viewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()

        event_details_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
        }
        statusBarColor?.let { activity?.statusBarColor = it }

        viewUpdatesJob = viewModel.viewUpdates
            .onEach {
                when (it) {
                    is EventDetailsViewUpdate.FloatingActionButtonDrawable -> event_fav_fab
                        ?.updateDrawable(it.isFavourite)
                    is EventDetailsViewUpdate.FavouriteStatusSnackbar -> transitionToSnackbarState(
                        SnackbarState.Shown(
                            text = if (it.isFavourite) getString(R.string.event_added)
                            else getString(R.string.event_removed),
                            length = Snackbar.LENGTH_SHORT
                        )
                    )
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onPause() {
        viewUpdatesJob?.cancel()
        super.onPause()
    }

    private fun FloatingActionButton.updateDrawable(isFavourite: Boolean) {
        context?.let {
            setImageDrawable(
                ContextCompat.getDrawable(
                    it,
                    if (isFavourite) R.drawable.delete else R.drawable.favourite
                )
            )
            hideAndShow()
        }
    }

    companion object {
        fun new(event: Event): EventDetailsFragment = EventDetailsFragment().also {
            it.event = event
        }

        const val EVENT_ARG_KEY = "event"

        private const val KEY_STATUS_BAR_COLOR = "KEY_STATUS_BAR_COLOR"
    }
}
