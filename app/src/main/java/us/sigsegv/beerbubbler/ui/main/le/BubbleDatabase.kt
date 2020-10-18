package us.sigsegv.beerbubbler.ui.main.le

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(BubbleEntry::class), version = 1)
abstract class BubbleDatabase : RoomDatabase() {
    abstract fun bubbleDao() : BubbleDao
}