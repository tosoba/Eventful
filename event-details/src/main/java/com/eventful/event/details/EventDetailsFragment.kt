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
import com.airbnb.epoxy.TypedEpoxyController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.SnackbarController
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.controller.eventNavigationItemSelectedListener
import com.eventful.core.android.controller.handleSnackbarState
import com.eventful.core.android.description
import com.eventful.core.android.eventInfo
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.binding.eventRequestOptions
import com.eventful.core.android.view.epoxy.EpoxyThreads
import com.eventful.core.android.view.epoxy.kindsCarousel
import com.eventful.core.android.view.epoxy.typedController
import com.eventful.core.android.wideButton
import com.eventful.event.details.databinding.FragmentEventDetailsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventDetailsFragment :
    DaggerViewModelFragment<EventDetailsViewModel>(), SnackbarController, HasArgs {

    private var event: Event by FragmentArgument(EventDetailsArgs.EVENT.name)
    private var bottomNavItemsToRemove: IntArray by FragmentArgument()
    override val args: Bundle
        get() = bundleOf(EventDetailsArgs.EVENT.name to event)

    private var statusBarColor: Int? = null

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    @Inject internal lateinit var epoxyThreads: EpoxyThreads

    private val epoxyController: TypedEpoxyController<Event> by
        lazy(LazyThreadSafetyMode.NONE) {
            typedController<Event>(epoxyThreads) { event ->
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

    private lateinit var binding: FragmentEventDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        FragmentEventDetailsBinding.inflate(inflater, container, false)
            .apply {
                setupToolbarWithDrawerToggle(eventDetailsToolbar, R.drawable.drawer_toggle_outline)

                eventFavFab
                    .clicks()
                    .onEach { viewModel.intent(EventDetailsIntent.ToggleFavourite) }
                    .launchIn(lifecycleScope)

                snackbarStateChannel = handleSnackbarState(eventFavFab)

                with(eventDetailsBottomNavView) {
                    setOnNavigationItemSelectedListener(eventNavigationItemSelectedListener)
                    selectedItemId = R.id.bottom_nav_event_details
                    bottomNavItemsToRemove.forEach(menu::removeItem)
                }

                binding = this
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            viewModel.viewUpdates
                .onEach { update ->
                    when (update) {
                        is EventDetailsViewUpdate.FloatingActionButtonDrawable ->
                            eventFavFab.updateDrawable(update.isFavourite)
                        is EventDetailsViewUpdate.NewEvent -> {
                            event = update.event
                            epoxyController.setData(update.event)
                            Glide.with(expandedImage)
                                .load(update.event.imageUrl)
                                .apply(eventRequestOptions)
                                .addListener(
                                    object : RequestListener<Drawable> {
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
                                                    statusBarColor =
                                                        withContext(Dispatchers.Default) {
                                                                drawable.bitmap.dominantColor
                                                            }
                                                            .also {
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
                    }
                }
                .launchIn(lifecycleScope)

            eventDetailsRecyclerView.setController(epoxyController)
        }
    }

    override fun onDestroyView() {
        snackbarStateChannel.close()
        super.onDestroyView()
    }

    override fun transitionToSnackbarState(newState: SnackbarState) {
        if (!snackbarStateChannel.isClosedForSend) snackbarStateChannel.offer(newState)
    }

    private var snackbarStateUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()

        binding.eventDetailsToolbar.let {
            setupToolbar(it)
            showBackNavArrow()
            it.navigationIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_outline)
        }
        statusBarColor?.let { activity?.statusBarColor = it }

        binding.eventDetailsBottomNavView.selectedItemId = R.id.bottom_nav_event_details

        snackbarStateUpdatesJob =
            viewModel.viewUpdates
                .filterIsInstance<EventDetailsViewUpdate.FavouriteStatusSnackbar>()
                .onEach {
                    transitionToSnackbarState(
                        SnackbarState.Shown(
                            res =
                                if (it.isFavourite) R.string.event_added
                                else R.string.event_removed,
                            length = Snackbar.LENGTH_SHORT))
                }
                .launchIn(lifecycleScope)
    }

    override fun onPause() {
        snackbarStateUpdatesJob?.cancel()
        super.onPause()
    }

    private fun FloatingActionButton.updateDrawable(isFavourite: Boolean) {
        setImageResource(if (isFavourite) R.drawable.delete else R.drawable.favourite)
    }

    companion object {
        fun new(event: Event, bottomNavItemsToRemove: IntArray): EventDetailsFragment =
            EventDetailsFragment().also {
                it.event = event
                it.bottomNavItemsToRemove = bottomNavItemsToRemove
            }
    }
}
