package com.udacity.project4.base

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {

    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.showSnackBarIntAction.observe(this, Observer {
            Snackbar.make(this.requireView(), getString(it.first), Snackbar.LENGTH_LONG)
                .setAction(it.second.first, it.second.second).show()
        })

        _viewModel.showDialogMessage.observe(this, Observer { message ->
            AlertDialog.Builder(message.context).apply {
                setTitle(message.title)
                setMessage(message.text)
                setPositiveButton(message.positiveButton.first, message.positiveButton.second)

                message.neutralButton?.let {
                    setNeutralButton(message.neutralButton.first, message.neutralButton.second)
                }

                message.negativeButton?.let {
                    setNegativeButton(message.negativeButton.first, message.negativeButton.second)
                }
            }
                .create()
                .show()
        })

        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )

                else -> {}
            }
        })
    }
}