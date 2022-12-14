package com.eventful.core.android.util.delegate

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.eventful.core.android.util.ext.putAny
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NullableFragmentArgument<T : Any> : ReadWriteProperty<Fragment, T?> {

    private var value: T? = null

    @MainThread
    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        if (value == null) {
            val args =
                thisRef.arguments
                    ?: throw IllegalStateException(
                        "Cannot read property ${property.name} if no arguments have been set")
            @Suppress("UNCHECKED_CAST")
            value = args.get(property.name) as? T
        }
        return value
    }

    @MainThread
    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        check(this.value == null) { "Argument value cannot be overridden once it is set." }
        if (value == null) return
        if (thisRef.arguments == null) thisRef.arguments = Bundle()
        thisRef.arguments!!.putAny(property.name, value)
    }
}
