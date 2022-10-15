package com.demo.bledemoapplication.save_data_activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.common.BLEConstants
import com.demo.bledemoapplication.databinding.FragmentSaveReadValueScreenBinding

/**
 * The fragment class represents that the save the value of the BLE selected device to the Server and DB
 */
class SaveReadValueFragment : Fragment() {
    private lateinit var mContext: Context
    private var _binding: FragmentSaveReadValueScreenBinding? = null
    private val saveValueBinding get() = _binding!!
    private val saveValueViewModel: SaveReadValueViewModel by lazy {
        ViewModelProvider(this)[SaveReadValueViewModel::class.java]
    }

    companion object {
        private val TAG = this::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param bundle The bundle data
         * @return A new instance of fragment ShowValueFragment.
         */
        fun newInstance(bundle: Bundle) = SaveReadValueFragment().apply { arguments = bundle }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            with(saveValueViewModel) {
                dataToSave = it.getString(BLEConstants.BUNDLE_KEY_DATA_TO_SAVE) ?: "Not able to Read the data"
                deviceName = it.getString(BLEConstants.BUNDLE_KEY_DEVICE_NAME) ?: "Device"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSaveReadValueScreenBinding.inflate(inflater, container, false)
        return saveValueBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_item, menu)
        initializeMenuItems(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_save) {
            saveValueViewModel.saveData()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        observerViewModel()
    }

    /**
     * To initialize the menu items
     *
     * @param menu The detail screen menu
     */
    private fun initializeMenuItems(menu: Menu) {
        menu.findItem(R.id.action_save).isVisible = true
    }

    /**
     * To set the UI
     */
    private fun setUI() {
        with(saveValueViewModel) {
            saveValueBinding.tvReadValue.text = getString(R.string.value, deviceName, dataToSave)
        }
    }

    /**
     * To observe the view model
     */
    private fun observerViewModel() {
        with(saveValueViewModel) {
            isLoading.observe(viewLifecycleOwner) {
                saveValueBinding.pbProgress.isVisible = it
            }
            apiSuccess.observe(viewLifecycleOwner) {
                showToast(it)

                // Below function used for demo to show the notification. Will remove this for real implementation
                showNotification()

                // Finish the activity after successfully saved
                (mContext as FragmentActivity).finish()
            }
            apiFailure.observe(viewLifecycleOwner) {
                showToast(it)
            }
            noNetwork.observe(viewLifecycleOwner) {
                showToast(R.string.no_internet)
            }
        }
    }

    /**
     * To show the toast
     *
     * @param messageResId The toast message id
     */
    private fun showToast(messageResId: Int) {
        Toast.makeText(mContext, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    /**
     * To show the notification
     */
    private fun showNotification() {
        val notificationTitle = "BLEDemoApp"
        val notificationMessage = "Read Data Successfully Saved !!!"

        val notificationBuilder =
            NotificationCompat.Builder(mContext, getString(R.string.app_name)).setContentTitle(notificationTitle)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setAutoCancel(true).setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_bluetooth_icon)

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(getString(R.string.app_name),
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
        }
        notificationManager.notify(0, notificationBuilder.build())
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(mContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}