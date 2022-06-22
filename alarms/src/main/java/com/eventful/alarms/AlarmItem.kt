package com.eventful.alarms

import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.eventful.core.android.SelectableBackgroundBindingModel_
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.Selectable

class SelectableAlarmItem(
    private val clicked: View.OnClickListener,
    private val longClicked: View.OnLongClickListener,
    private val optionsButtonClicked: (View) -> Unit,
    background: SelectableBackgroundBindingModel_,
    info: AlarmInfoBindingModel_,
    dateTime: AlarmDateTimeBindingModel_
) : EpoxyModelGroup(R.layout.alarm_item, background, info, dateTime) {

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        with(holder.rootView) {
            findViewById<AppCompatImageButton>(R.id.alarm_item_options_btn).setOnClickListener {
                optionsButtonClicked(this)
            }
            setOnClickListener(clicked)
            setOnLongClickListener(longClicked)
        }
    }
}

fun Selectable<Alarm>.listItem(
    clicked: View.OnClickListener,
    longClicked: View.OnLongClickListener,
    optionsButtonClicked: (View) -> Unit
) =
    SelectableAlarmItem(
        clicked,
        longClicked,
        optionsButtonClicked,
        SelectableBackgroundBindingModel_().id("${item.id}ab").selected(selected),
        AlarmInfoBindingModel_().id("${item.id}i").alarm(item),
        AlarmDateTimeBindingModel_().id("${item.id}dt").alarm(item))
