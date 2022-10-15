package com.demo.bledemoapplication.save_data_activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.databinding.ActivitySaveReadValueScreenBinding
import kotlinx.android.synthetic.main.ble_toolbar.*

/**
 * To save the BLE device data to Server and DB
 */
class SaveReadValueActivity : AppCompatActivity() {
    private lateinit var saveReadValueActivityBinding: ActivitySaveReadValueScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveReadValueActivityBinding = ActivitySaveReadValueScreenBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        setActionBar()
        intent?.extras?.let {
            loadFragment(it)
        } ?: run {
            finish()
        }
    }

    /**
     * To set the action bar
     */
    private fun setActionBar() {
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onBackPressed() {
        popFragment()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                popFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * To load the read value fragment
     *
     * @param bundle The bundle data
     */
    private fun loadFragment(bundle: Bundle) {
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.title = null
            it.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.light_grey)))
        }
        supportFragmentManager.beginTransaction().apply {
            replace(saveReadValueActivityBinding.readValueContainer.id, SaveReadValueFragment.newInstance(bundle))
            commit()
        }
    }

    /**
     * To pop the fragment
     */
    private fun popFragment() {
        val supportFragmentManager = this.supportFragmentManager
        if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack()
        else finish()
    }
}