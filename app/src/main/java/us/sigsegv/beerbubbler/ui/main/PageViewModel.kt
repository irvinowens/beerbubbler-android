package us.sigsegv.beerbubbler.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import us.sigsegv.beerbubbler.ui.main.le.BubbleDatabase
import us.sigsegv.beerbubbler.ui.main.le.BubbleEntry
import us.sigsegv.beerbubbler.ui.main.le.BubblerLeManager
import java.util.*

class PageViewModel(application: Application) : AndroidViewModel(application) {
    private val _index = MutableLiveData<Int>(0)
    private var entries : LiveData<List<BubbleEntry>>
    private val bubbleDatabase : BubbleDatabase = BubblerLeManager.BubbleDatabaseHolder.getInstance(application).database

    init {
        entries = bubbleDatabase.bubbleDao().getSortedRecords()
    }
    fun getAllEntries() : LiveData<List<BubbleEntry>> {
        return entries
    }

    fun getRecordsForGraph() : List<BubbleEntry> {
        val invertedList : ArrayList<BubbleEntry> = ArrayList(200)
        invertedList.addAll(bubbleDatabase.bubbleDao().getRecordsForGraph())
        invertedList.sortBy { it.time }
        return invertedList
    }

    fun index() : Int {
        return _index.value ?: 0
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}