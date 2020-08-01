package com.eventful.alarms

import android.view.View
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.eventful.core.model.Selectable
import com.eventful.core.android.SelectableBackgroundBindingModel_
import com.eventful.core.android.model.alarm.Alarm

class SelectableAlarmItem(
    private val clicked: View.OnClickListener,
    private val longClicked: View.OnLongClickListener,
    background: SelectableBackgroundBindingModel_,
    info: AlarmInfoBindingModel_,
    dateTime: AlarmDateTimeBindingModel_
) : EpoxyModelGroup(R.layout.alarm_item, background, info, dateTime) {

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        with(holder.rootView) {
            setOnClickListener(clicked)
            setOnLongClickListener(longClicked)
        }
    }
}

fun Selectable<Alarm>.listItem(
    clicked: View.OnClickListener,
    longClicked: View.OnLongClickListener
) = SelectableAlarmItem(
    clicked,
    longClicked,
    SelectableBackgroundBindingModel_().id("${item.id}ab")
        .selected(selected),
    AlarmInfoBindingModel_().id("${item.id}i")
        .alarm(item),
    AlarmDateTimeBindingModel_().id("${item.id}dt")
        .alarm(item)
)