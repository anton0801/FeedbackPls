package com.feedback.android.app.common

import android.app.Activity
import android.os.Looper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> viewBinding(noinline initializer: (LayoutInflater) -> T) =
    ViewBindingPropertyDelegate(initializer)

class ViewBindingPropertyDelegate<T : ViewBinding>(
    private val initializer: (LayoutInflater) -> T
) : ReadOnlyProperty<Any, T>, LifecycleObserver {

    private var _value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (_value == null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw IllegalThreadStateException("This cannot be called from other threads. It should be on the main thread only.")
            }
            val layoutInflater =
                if (thisRef is Activity) thisRef.layoutInflater else (thisRef as Fragment).layoutInflater
            _value = initializer(layoutInflater)
        }
        return _value!!
    }

}