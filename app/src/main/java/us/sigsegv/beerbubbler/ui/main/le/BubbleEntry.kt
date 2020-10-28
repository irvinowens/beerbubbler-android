package us.sigsegv.beerbubbler.ui.main.le

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(indices = [Index(value=["uid", "time"])])
data class BubbleEntry(
    @PrimaryKey( autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo( name = "time") val time : Long?,
    @ColumnInfo( name = "bubble_count") val bubbleCount : Int?,
    @ColumnInfo( name = "temperature") val temperature : Int?,
    @ColumnInfo( name = "humidity") val humidity : Int?
)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX index_a_uid_time ON bubbleentry(uid, time)")
    }
}