package us.sigsegv.beerbubbler.ui.main.le

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber

class BubblerWorker(appContext: Context, workerParams: WorkerParameters) :
Worker(appContext, workerParams) {
    private val tag : String = "BubblerWorker"
    override fun doWork(): Result {
        val bleManager : BluetoothManager? = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return if (bleManager != null && bleManager.adapter != null && bleManager.adapter.isEnabled) {
            val leManager = BubblerLeManager.getInstance(applicationContext)
            Timber.tag(tag).i("Starting Work : Finding beer bubbler")
            try {
                leManager.startActiveInteraction()
            } catch (throwable: Throwable) {
                Timber.tag(tag).e(throwable, "Failed to interact")
                return Result.failure()
            }
            Timber.tag(tag).i("Finished Work : Found beer bubbler")
            Result.success()
        } else {
            Result.failure()
        }
    }
}