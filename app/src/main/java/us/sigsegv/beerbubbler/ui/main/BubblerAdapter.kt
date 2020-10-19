package us.sigsegv.beerbubbler.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import us.sigsegv.beerbubbler.R
import us.sigsegv.beerbubbler.ui.main.le.BubbleEntry
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BubblerAdapter(private val index: Int) : RecyclerView.Adapter<BubblerEntryViewHolder>() {
    private val list: ArrayList<BubbleEntry>  = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BubblerEntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_main, parent, false)
        return BubblerEntryViewHolder(view)
    }

    fun setBubbleEntries(list: List<BubbleEntry>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BubblerEntryViewHolder, position: Int) {
        val entry : BubbleEntry = list.get(position)
        val textView = holder.textView
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.getDefault())
        val stringBuffer = StringBuffer().append("Date: ")
        simpleDateFormat.format(entry.time as Long, stringBuffer, FieldPosition(0))
        stringBuffer.append(", ")
        if (index == 1) {
            stringBuffer.append("Bubble Count: ")
            stringBuffer.append(entry.bubbleCount)
        }
        if (index == 2) {
            stringBuffer.append("Temperature: ")
            stringBuffer.append(entry.temperature)
            stringBuffer.append(" celsius")
        }
        if (index == 3) {
            stringBuffer.append("Humidity: ")
            stringBuffer.append(entry.humidity)
            stringBuffer.append("%")
        }
        textView?.text = stringBuffer.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}