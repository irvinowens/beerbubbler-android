package us.sigsegv.beerbubbler.ui.main

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beerbubbler.R

class BubblerEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var textView : TextView? = itemView.findViewById(R.id.textView)
}