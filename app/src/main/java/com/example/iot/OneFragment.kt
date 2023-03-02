package com.example.iot

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.iot.databinding.Frag1Binding
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.threeten.bp.DayOfWeek
import java.text.SimpleDateFormat
import java.util.*

class OneFragment : Fragment() {
    private lateinit var binding: Frag1Binding
    private var dates = ArrayList<CalendarDay>()
    private var db : FirebaseFirestore? = null
    private var uid: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = Frag1Binding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        uid = arguments?.getString("Uid")

        val calendarView = binding.cvCalendar
        calendarView.state().edit().setFirstDayOfWeek(DayOfWeek.of(Calendar.MONDAY)).commit()
        calendarView.setTitleFormatter(MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)))
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)))
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)
        calendarView.selectedDate = CalendarDay.today()



        val Ref = db!!.collection("Res").document(uid!!)
        Ref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            calendarView.removeDecorators()
            calendarView.invalidateDecorators()
            var i = 0
            snapshot?.data?.forEach { it ->
                val datalist = arrayListOf<CalendarDay>()
                val reservationdata = it.value
                reservationdata?.let{
                    val reslist = it as java.util.HashMap<String, ModelReservation>
                    reslist.forEach { itreslist ->
                        val year = itreslist.key.substring(0,4).toInt()
                        val month = itreslist.key.substring(5,7).toInt()
                        val day = itreslist.key.substring(8).toInt()
                        val resdate = CalendarDay.from(year, month, day)
                        datalist.add(resdate)

                    } //reslist.forEach

                    val rescolor = resources
                    val colors = rescolor.obtainTypedArray(R.array.reservation)
                    val color = colors.getColor(i,0)
                    calendarView.addDecorator(EventDecorator(color, datalist))

                }
                i += 1
            }
        }

        calendarView.setOnDateChangedListener { widget, date, selected ->
            runBlocking {
                val job = async(Dispatchers.Default) {
                    getdata(db,uid)
                }
                val documentsnapshot = job.await()
                documentsnapshot?.data?.forEach {
                    val datalist = arrayListOf<CalendarDay>()
                    val reservationdata = it.value
                    reservationdata?.let{
                        val reslist = it as java.util.HashMap<String, ModelReservation>
                        reslist.forEach { itreslist ->

                            val year = itreslist.key.substring(0,4).toInt()
                            val month = itreslist.key.substring(5,7).toInt()
                            val day = itreslist.key.substring(8).toInt()
                            val resdate = CalendarDay.from(year, month, day)
                            datalist.add(resdate)

                            if (date == resdate) {
                                val model = itreslist.value as java.util.HashMap<String,Any>

                                val ismorning = model["ismorning"]
                                val morning = model["morning"] as Timestamp?
                                val islunch = model["islunch"]
                                val lunch = model["lunch"] as Timestamp?
                                val isdinner = model["isdinner"]
                                val dinner = model["dinner"] as Timestamp?

                                binding.resDay.text = itreslist.key
                                when(morning) {
                                    null -> binding.morning.text = "없음"
                                    else -> {
                                        val morningtext = SimpleDateFormat("HH:mm",
                                            Locale.getDefault()).format(morning.toDate())
                                        binding.morning.text = morningtext
                                    }
                                }
                                when(lunch) {
                                    null -> binding.lunch.text = "없음"
                                    else -> {
                                        val lunchtext = SimpleDateFormat("HH:mm",
                                            Locale.getDefault()).format(lunch.toDate())
                                        binding.lunch.text = lunchtext
                                    }
                                }
                                when(dinner) {
                                    null -> binding.dinner.text = "없음"
                                    else -> {
                                        val dinnertext = SimpleDateFormat("HH:mm",
                                            Locale.getDefault()).format(dinner.toDate())
                                        binding.dinner.text = dinnertext
                                    }
                                }

                                when(ismorning) {
                                    null -> binding.morningIstake.text = "없음"
                                    "yet" -> binding.morningIstake.text = "예정"
                                    "done" -> binding.morningIstake.text = "복용"
                                    "notdone" -> binding.morningIstake.text = "미복용"
                                }
                                when(islunch) {
                                    null -> binding.lunchIstake.text = "없음"
                                    "yet" -> binding.lunchIstake.text = "예정"
                                    "done" -> binding.lunchIstake.text = "복용"
                                    "notdone" -> binding.lunchIstake.text = "미복용"
                                }
                                when(isdinner) {
                                    null -> binding.dinnerIstake.text = "없음"
                                    "yet" -> binding.dinnerIstake.text = "예정"
                                    "done" -> binding.dinnerIstake.text = "복용"
                                    "notdone" -> binding.dinnerIstake.text = "미복용"
                                }




                            }

                        } //reslist.forEach


                    }
                }
            }


        }





        return binding.root
    } // onCreateView




}

suspend fun getdata(db: FirebaseFirestore?, uid: String?): DocumentSnapshot? {
    return try {
        val result = db?.collection("Res")?.document(uid!!)?.get()
            ?.addOnSuccessListener {

            }?.addOnFailureListener {

            }?.await()
        result
    }catch(e: FirebaseException){
        null
    }
}

class EventDecorator(parseColor: Int, dates: ArrayList<CalendarDay>): DayViewDecorator {
    private var dates: HashSet<CalendarDay> = HashSet(dates)
    val color: Int = parseColor

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(20F, color))
    }

}



