package com.example.coreandroid.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class MainDispatcherRule : TestRule {

    private val mainThreadSurrogate = newSingleThreadContext("Main Thread")

    override fun apply(
        base: Statement, description: Description?
    ): Statement = AlteredStatement(base)

    inner class AlteredStatement(private val base: Statement) : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(mainThreadSurrogate)
            base.evaluate()
            Dispatchers.resetMain()
        }
    }
}