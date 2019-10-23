package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationState
import com.example.core.usecase.GetLocation
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.base.MainFragmentSelectedStateProvider
import com.example.coreandroid.util.SnackbarState
import com.example.favourites.FavouritesFragment
import com.example.nearby.NearbyFragment
import com.example.search.SearchFragment
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainViewModel(private val getLocation: GetLocation) :
    VectorViewModel<MainState>(MainState.INITIAL),
    ConnectivityStateProvider, LocationStateProvider, MainFragmentSelectedStateProvider {

    override val isConnectedFlow: Flow<Boolean>
        get() = state.map { it.isConnected }

    override val isConnected: Boolean get() = currentState.isConnected

    override val locationStateFlow: Flow<LocationState>
        get() = state.map { it.locationState }

    override val locationState: LocationState get() = currentState.locationState

    override fun <T> isSelectedFlow(fragmentClass: Class<T>): Flow<Boolean> = state.map {
        it.selectedFragmentIndex == when {
            fragmentClass.isAssignableFrom(NearbyFragment::class.java) -> 0
            fragmentClass.isAssignableFrom(SearchFragment::class.java) -> 1
            fragmentClass.isAssignableFrom(FavouritesFragment::class.java) -> 2
            else -> throw IllegalArgumentException("Class is not assignable to any of the main fragments")
        }
    }

    var connected: Boolean
        set(value) = setState { copy(isConnected = value) }
        get() = currentState.isConnected

    var selectedFragmentIndex: Int
        set(value) = setState { copy(selectedFragmentIndex = value) }
        get() = currentState.selectedFragmentIndex

    fun updateSnackbarState(index: Int, newState: SnackbarState) {
        setState { copy(snackbarState = snackbarState + (index to newState)) }
    }

    fun snackbarStateFor(index: Int): SnackbarState = currentState.snackbarState.getValue(index)

    fun loadLocation() = withState {
        if (it.locationState is LocationState.Loading) return@withState
        viewModelScope.launch {
            getLocation().collect { setState { copy(locationState = it) } }
        }
    }

    fun onPermissionDenied() = setState { copy(locationState = LocationState.PermissionDenied) }
}