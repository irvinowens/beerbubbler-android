package us.sigsegv.beerbubbler.ui.main.le

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class BubblerWorker(appContext: Context, workerParams: WorkerParameters) :
Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val bleManager : BluetoothManager? = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bleManager != null && bleManager.adapter != null && bleManager.adapter.isEnabled) {
            val leManager = BubblerLeManager.getInstance(applicationContext)
            leManager.startActiveInteraction()
            return Result.success()
        } else {
            return Result.failure()
        }
    }
}