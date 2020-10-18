package us.sigsegv.beerbubbler

import android.app.Application
import timber.log.Timber
import us.sigsegv.beerbubbler.ui.main.le.BubblerLeManager

class BeerBubblerApplication : Application() {
    lateinit var manager: BubblerLeManager
    override fun onCreate() {
        super.onCreate()
        Timber.v("[Application] onCreate")
        manager = BubblerLeManager.getInstance(this)
        Timber.v("Got instance of manager %s", manager)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        manager.stopActiveInteraction()
        Timber.w("Low memory")
    }
}