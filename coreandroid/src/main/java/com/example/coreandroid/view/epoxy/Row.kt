package com.example.coreandroid.view.epoxy

import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.example.coreandroid.R

class Row(
    models: Collection<EpoxyModel<*>>,
    private val clicked: View.OnClickListener? = null
) : EpoxyModelGroup(R.layout.row, models) {

    constructor(
        clicked: View.OnClickListener? = null,
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), clicked)

    constructor(
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), null)

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        clicked?.let { holder.rootView.setOnClickListener(it) }
    }
}
