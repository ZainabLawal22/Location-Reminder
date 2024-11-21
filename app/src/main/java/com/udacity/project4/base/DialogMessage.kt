package com.udacity.project4.base

import android.content.Context
import android.content.DialogInterface
import com.udacity.project4.R


/**
 * Data class used to display Dialog Message with the live data to navigate between the fragments
 */
data class DialogMessage(
    val context: Context,
    val title: String,
    val text: String,
    val positiveButton: Pair<String, DialogInterface.OnClickListener> = Pair(
        context.getString(R.string.okay),
        DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }),
    val neutralButton: Pair<String, DialogInterface.OnClickListener>? = null,
    val negativeButton: Pair<String, DialogInterface.OnClickListener>? = null
)
