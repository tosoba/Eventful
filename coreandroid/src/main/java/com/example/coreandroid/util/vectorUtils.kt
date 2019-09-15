package com.example.coreandroid.util

import com.haroldadmin.vector.VectorState
import com.haroldadmin.vector.VectorViewModel

inline fun <S1 : VectorState, S2 : VectorState> withState(
    viewModel1: VectorViewModel<S1>,
    viewModel2: VectorViewModel<S2>,
    crossinline block: (S1, S2) -> Unit
) {
    block(viewModel1.currentState, viewModel2.currentState)
}
