package com.eventful.core.android.util.ext

import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.PeriodFormatterBuilder

val Period.printedNormalized: String
    get() = PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix(" yr ")
        .appendMonths()
        .appendSuffix(" months ")
        .appendDays()
        .appendSuffix(" days ")
        .appendHours()
        .appendSuffix(" hr ")
        .appendMinutes()
        .appendSuffix(" min ")
        .appendSeconds()
        .appendSuffix(" sec")
        .toFormatter()
        .print(normalizedStandard(PeriodType.yearMonthDayTime()))
