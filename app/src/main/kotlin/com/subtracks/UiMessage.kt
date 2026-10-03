package com.subtracks

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes

data class UiMessage(
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList(),
) {
    fun resolve(resources: Resources): String = resources.getString(resId, *args.toTypedArray())

    fun resolve(context: Context): String = resolve(context.resources)
}

class UiException(
    val uiMessage: UiMessage,
    message: String = "",
) : Exception(message)
