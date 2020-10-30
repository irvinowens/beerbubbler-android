package us.sigsegv.beerbubbler

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.work.Constraints
import androidx.work.WorkManager
import us.sigsegv.beerbubbler.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import timber.log.Timber
import us.sigsegv.beerbubbler.ui.main.SectionsPagerAdapter
import us.sigsegv.beerbubbler.ui.main.le.BubblerLeManager
import us.sigsegv.beerbubbler.ui.main.le.BubblerWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var leBubbler : BubblerLeManager
    private val permissionRequestCode = 17652
    private val workerTag = "us.sigsegv.beerbubber.worker.BeerWorker"
    private lateinit var workManager : WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.d("onCreate")
        workManager = WorkManager.getInstance(applicationContext)
        leBubbler = BubblerLeManager.getInstance(this)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume")
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            leBubbler.startActiveInteraction()
            startWorkManager()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showInContextUI(true)
            } else {
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION), permissionRequestCode)
            }
        }
    }

    private fun showInContextUI(shouldShowButtons: Boolean) {
        val builder = AlertDialog.Builder(this)
            .setTitle("Need Location For Bluetooth")
            .setMessage("Location is required for scanning bluetooth devices")
        if (shouldShowButtons) {
            builder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), permissionRequestCode
                )
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    leBubbler.startActiveInteraction()
                    startWorkManager()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    showInContextUI(false)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
                Timber.d("Strange other request")
            }
        }
    }

    private fun startWorkManager() {
        Timber.i("Restarting Beer Bubbler Job")
        workManager.cancelAllWorkByTag(workerTag)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        val saveRequest =
            androidx.work.PeriodicWorkRequestBuilder<BubblerWorker>(
                1, TimeUnit.HOURS,
                    20, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(workerTag)
                .build()

        workManager.enqueue(saveRequest)
        workManager.getWorkInfosByTag(workerTag).get().forEach {
            Timber.i("Job status: %s", it.state)
        }
    }
}