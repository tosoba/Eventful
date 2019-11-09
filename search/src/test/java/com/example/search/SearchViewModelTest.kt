package com.example.search

import com.example.test.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

import org.junit.Rule

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
}