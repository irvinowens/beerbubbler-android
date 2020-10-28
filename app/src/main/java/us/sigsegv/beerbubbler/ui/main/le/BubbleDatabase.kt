package us.sigsegv.beerbubbler.ui.main.le

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BubbleEntry::class], version = 2)
abstract class BubbleDatabase : RoomDatabase() {
    abstract fun bubbleDao() : BubbleDao
}