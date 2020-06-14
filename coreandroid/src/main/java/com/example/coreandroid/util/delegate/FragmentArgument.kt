package com.example.coreandroid.util.delegate

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.example.coreandroid.util.ext.putAny
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class FragmentArgument<T : Any> : ReadWriteProperty<Fragment, T> {

    private var value: T? = null

    @MainThread
    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (value == null) {
            val args = thisRef.arguments
                ?: throw IllegalStateException("Cannot read property ${property.name} if no arguments have been set")
            @Suppress("UNCHECKED_CAST")
            value = args.get(property.name) as T
        }
        return value ?: throw IllegalStateException("Property ${property.name} could not be read")
    }

    @MainThread
    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        check(this.value == null) { "Argument value cannot be overridden once it is set." }
        if (thisRef.arguments == null) thisRef.arguments = Bundle()
        thisRef.arguments!!.putAny(property.name, value)
    }
}
