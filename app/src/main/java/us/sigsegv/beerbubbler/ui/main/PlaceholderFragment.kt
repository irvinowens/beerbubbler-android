package us.sigsegv.beerbubbler.ui.main

import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import us.sigsegv.beerbubbler.R
import us.sigsegv.beerbubbler.ui.main.le.BubbleEntry


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment(), OnChartGestureListener {
    private var chart : BarChart? = null
    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val localActivity = activity
        chart = BarChart(localActivity)
        val localChart = chart
        if (localChart != null && localActivity != null) {
            localChart.description.isEnabled = false
            localChart.onChartGestureListener = this

            localChart.setDrawGridBackground(false)
            localChart.setDrawBarShadow(false)

            val tf = Typeface.createFromAsset(
                localActivity.applicationContext.assets,
                "OpenSans-Light.ttf"
            )
            CoroutineScope(IO).launch {
                val result = generateBarData(pageViewModel, pageViewModel.index())
                CoroutineScope(Main).launch {
                    localChart.data = result
                    localChart.invalidate()
                }
            }
            val l = localChart.legend
            l.typeface = tf
            l.textSize = 8f
            val typedValue = TypedValue()
            val theme: Resources.Theme = context!!.theme
            theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
            l.textColor = typedValue.data
            val leftAxis = localChart.axisLeft
            leftAxis.typeface = tf
            leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


            localChart.axisRight.isEnabled = false

            val xAxis = localChart.xAxis
            xAxis.isEnabled = false

            // programmatically add the chart
            val parent: FrameLayout = root.findViewById(R.id.graph)
            parent.addView(chart)
        }
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        if (context != null) {
            val adapter = BubblerAdapter(pageViewModel.index())
            recyclerView.adapter = adapter
            pageViewModel.getAllEntries().observe(viewLifecycleOwner, Observer {
                adapter.setBubbleEntries(it)
            })
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
        return root
    }

    private suspend fun generateBarData(pageViewModel: PageViewModel, section: Int): BarData? = withContext(
        IO
    ) {
        val sets: ArrayList<IBarDataSet> = ArrayList()
        val barEntries : ArrayList<BarEntry> = ArrayList(200)
        val entries : List<BubbleEntry> = pageViewModel.getRecordsForGraph()
        var previousValue = 0.0f
        var count = 0
        for (bubble in entries) {
            count++
            if (section == 1) {
                if (previousValue == 0.0f) {
                    previousValue = bubble.bubbleCount?.toFloat() ?: 0.0f
                } else {
                    barEntries.add(
                        BarEntry(
                            count.toFloat(),
                            (bubble.bubbleCount?.toFloat() ?: 0.0f) - previousValue
                        )
                    )
                    previousValue = bubble.bubbleCount?.toFloat() ?: 0.0f
                }
            } else if (section == 2) {
                barEntries.add(
                    BarEntry(
                        count.toFloat(),
                        bubble.temperature?.toFloat() ?: 0.0f
                    )
                )
            } else if (section == 3) {
                barEntries.add(
                    BarEntry(
                        count.toFloat(),
                        bubble.humidity?.toFloat() ?: 0.0f
                    )
                )
            }
        }
        var label = ""
        when (section) {
            1 -> {
                label = "Bubbles"
            }
            2 -> {
                label = "Temp"
            }
            3 -> {
                label = "Humidity"
            }
        }
        Timber.d("Number of bar entries: %d", barEntries.size)
        val ds = BarDataSet(barEntries, label)
        val typedValue = TypedValue()
        val theme: Resources.Theme = context!!.theme
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
        ds.setColors(typedValue.data)
        ds.valueTextSize = 8f
        ds.axisDependency = YAxis.AxisDependency.LEFT
        sets.add(ds)
        val barData = BarData(sets)
        val tf = Typeface.createFromAsset(activity?.assets, "OpenSans-Light.ttf")
        barData.setValueTypeface(tf)
        barData
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Timber.i("Gesture start")
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Timber.i("Gesture end")
        chart?.highlightValues(null)
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        Timber.i("Chart long pressed")
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        Timber.i("Chart double tapped")
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        Timber.i("Chart single tapped")
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
        Timber.i("Chart fling. VelocityX: %f, VelocityY: %f", velocityX, velocityY)
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        Timber.i("ScaleX: %f, ScaleY %f", scaleX, scaleY)
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        Timber.i("dX: %f, dY: %f", dX, dY)
    }
}
