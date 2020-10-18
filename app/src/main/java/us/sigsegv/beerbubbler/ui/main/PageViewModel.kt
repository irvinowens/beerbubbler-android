package us.sigsegv.beerbubbler.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import us.sigsegv.beerbubbler.ui.main.le.BubbleEntry
import us.sigsegv.beerbubbler.ui.main.le.BubblerLeManager

class PageViewModel(application: Application) : AndroidViewModel(application) {
    private val _index = MutableLiveData<Int>(0)
    private var entries : LiveData<List<BubbleEntry>>
    init {
        val bubbleDatabase = BubblerLeManager.BubbleDatabaseHolder.getInstance(application).database
        entries = bubbleDatabase.bubbleDao().getSortedRecords()
    }
    fun getAllEntries() : LiveData<List<BubbleEntry>> {
        return entries
    }

    fun index() : Int {
        return _index.value ?: 0
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}