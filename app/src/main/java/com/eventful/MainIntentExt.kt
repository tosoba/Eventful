package com.eventful

import android.content.Intent
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.notification.AlarmNotifications

val Intent.eventExtra: Event?
    get() = extras?.getParcelable(AlarmNotifications.EVENT_EXTRA)
