package us.sigsegv.beerbubbler.ui.main.le

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BubbleEntry(
    @PrimaryKey( autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo( name = "time") val time : Long?,
    @ColumnInfo( name = "bubble_count") val bubbleCount : Int?,
    @ColumnInfo( name = "temperature") val temperature : Int?,
    @ColumnInfo( name = "humidity") val humidity : Int?
)