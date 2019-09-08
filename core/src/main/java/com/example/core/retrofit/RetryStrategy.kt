package com.example.core.retrofit

import java.util.concurrent.TimeUnit

sealed class RetryStrategy(val attempts: Int)
class Times(attempts: Int = 1) : RetryStrategy(attempts)
class WithDelay(val delay: Long, val unit: TimeUnit, attempts: Int = 1) : RetryStrategy(attempts)
class WithVariableDelay(
    attempts: Int, val unit: TimeUnit, val getDelay: (Int) -> Long
) : RetryStrategy(attempts)
