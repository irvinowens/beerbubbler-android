package us.sigsegv.beerbubbler.ui.main.le

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BubbleDao {
    @Query("SELECT * FROM bubbleentry ORDER BY time DESC")
    fun getSortedRecords() : LiveData<List<BubbleEntry>>
    @Query("SELECT * FROM bubbleentry WHERE uid >= :start LIMIT :limit")
    fun getRange(start: Int,limit: Int) : List<BubbleEntry>

    @Query("SELECT COUNT(uid) FROM bubbleentry")
    fun getBubbleEntryCount(): Int

    @Query("SELECT uid FROM bubbleentry LIMIT 1")
    fun getInitialIndex(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg bubbleEntries: BubbleEntry)

    @Delete
    fun delete(bubbleEntry: BubbleEntry)
}