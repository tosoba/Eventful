package com.eventful.search

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cursoradapter.widget.CursorAdapter
import kotlinx.android.synthetic.main.search_suggestion.view.*

internal class SearchSuggestionsAdapter(
    context: Context,
    cursor: Cursor?
) : CursorAdapter(context, cursor, false) {

    override fun newView(
        context: Context,
        cursor: Cursor?,
        parent: ViewGroup
    ): View? = cursor?.let {
        LayoutInflater.from(context).inflate(
            R.layout.search_suggestion, parent, false
        ).apply {
            this.suggestion_text_view.text = cursor.getString(1)
            this.suggestion_used_ago_text_view.text = cursor.getLong(2).toString()
        }
    }

    override fun bindView(view: View, context: Context, cursor: Cursor?) {
        cursor?.let {
            view.suggestion_text_view.text = cursor.getString(1)
            view.suggestion_used_ago_text_view.text = cursor.getLong(2).toString()
        }
    }

    companion object {
        val COLUMN_NAMES: Array<String> = arrayOf("_id", "searchText", "timestampMs")
    }
}
