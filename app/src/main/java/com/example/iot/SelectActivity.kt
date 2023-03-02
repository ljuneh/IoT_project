package com.example.iot

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.iot.databinding.ActivitySelectBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SelectActivity: AppCompatActivity() {
    private var uid: String? = null
    private var db : FirebaseFirestore? = null
    private lateinit var datePickerText: TextView
    private lateinit var timePickerText1: TextView
    private lateinit var timePickerText2: TextView
    private lateinit var timePickerText3: TextView
    private lateinit var calendar: Calendar
    private lateinit var binding: ActivitySelectBinding
    private lateinit var startDate: String
    private lateinit var endDate: String
    private var startday: Long? = null
    private var endday: Long? = null
    private var calcudate: Long? = null
    private var morning_hourOfDay: Int? = null
    private var morning_minute: Int? = null
    private var lunch_hourOfDay: Int? = null
    private var lunch_minute: Int? = null
    private var dinner_hourOfDay: Int? = null
    private var dinner_minute: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("Uid")
        Log.d("collectiontest2", uid.toString())
        db = FirebaseFirestore.getInstance()






        datePickerText = binding.datePickerText
        timePickerText1 = binding.timePickerText1
        timePickerText2 = binding.timePickerText2
        timePickerText3 = binding.timePickerText3
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        val dateRangePicker = binding.dateRangePickerBtn
        dateRangePicker.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()

            builder.setTitleText("Date Picker")
            val picker = builder.build()
            picker.show(supportFragmentManager, picker.toString())
            picker.addOnNegativeButtonClickListener{ picker.dismiss() }
            picker.addOnPositiveButtonClickListener {
                startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.first)
                endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.second)
                startday = it.first
                endday = it.second
                Log.d("test", "startDate: $startDate, endDate : $endDate")
                datePickerText.text = (startDate + "\n" + endDate)
                calcudate= (it.second - it.first)/(60 * 60 * 24 * 1000)
                Log.d("collectiontest3", "날짜 차이는 $calcudate 만큼 남")

                val nextday = it.first + (60 * 60 * 24 * 1000)
                val nextdate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(nextday)
                Log.d("collectiontest3", nextdate)

            }
        }

        binding.timePickerBtn1.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val timeString = "${hourOfDay}시 ${minute}분"
                timePickerText1.text = timeString
                morning_hourOfDay = hourOfDay
                morning_minute = minute
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()

        }

        binding.timePickerBtn2.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val timeString = "${hourOfDay}시 ${minute}분"
                timePickerText2.text = timeString
                lunch_hourOfDay = hourOfDay
                lunch_minute = minute
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()

        }

        binding.timePickerBtn3.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val timeString = "${hourOfDay}시 ${minute}분"
                timePickerText3.text = timeString
                dinner_hourOfDay = hourOfDay
                dinner_minute = minute
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()

        }

        binding.next.setOnClickListener {
            db?.collection("Res")?.document(uid!!)?.get()
                ?.addOnSuccessListener { documents->
                    val documentskey = documents.id
                    val documentsdata = documents.data
                    Log.d("collectiontest2", documentskey)
                    Log.d("collectiontest2", documentsdata.toString())
                    val empty: HashMap<String, ModelReservation> = hashMapOf()


                    val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")


                    calcudate?.let{
                        var morningtimetext: String? = null
                        if ((morning_hourOfDay != null) and (morning_minute != null)) {
                            morningtimetext = "$morning_hourOfDay:$morning_minute:00"
                        }
                        var lunchtimetext: String? = null
                        if ((lunch_hourOfDay != null) and (lunch_minute != null)) {
                            lunchtimetext = "$lunch_hourOfDay:$lunch_minute:00"
                        }
                        var dinnertimetext: String? = null
                        if ((dinner_hourOfDay != null) and (dinner_minute != null)) {
                            dinnertimetext = "$dinner_hourOfDay:$dinner_minute:00"
                        }

                        val data = hashMapOf<String,ModelReservation>()

                        for (i in 0..it) {
                            val resInfo = ModelReservation()
                            val nowday = startday?.plus((60 * 60 * 24 * 1000 * i))
                            val nowdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(nowday)
                            if (morningtimetext != null) {
                                val morningtext = "$nowdate $morningtimetext"
                                val morning = sf.parse(morningtext)
                                resInfo.ismorning = "yet"
                                resInfo.morning = morning?.let { it1 -> Timestamp(it1) }
                            }
                            if (lunchtimetext != null) {
                                val lunchtext = "$nowdate $lunchtimetext"
                                val lunch = sf.parse(lunchtext)
                                resInfo.islunch = "yet"
                                resInfo.lunch = lunch?.let { it1 -> Timestamp(it1) }
                            }
                            if (dinnertimetext != null) {
                                val dinnertext = "$nowdate $dinnertimetext"
                                val dinner = sf.parse(dinnertext)
                                resInfo.isdinner = "yet"
                                resInfo.dinner = dinner?.let { it1 -> Timestamp(it1) }
                            }

                            data[nowdate] = resInfo

                        }

                        run breaker@ {
                            documentsdata?.forEach { it2 ->
                                if (it2.value.equals(empty)) {
                                    Log.d("collectiontest2", "Empty")
                                    db?.collection("Res")?.document(uid!!)?.update(it2.key,data)
                                    return@breaker
                                }
                                Log.d("collectiontest2", it2.key)
                                Log.d("collectiontest2", it2.value.toString())
                            }
                        }



                    }


                } // addOnSuccessListener

            finish()


        } // setOnClickListener

    } // onCreate
}