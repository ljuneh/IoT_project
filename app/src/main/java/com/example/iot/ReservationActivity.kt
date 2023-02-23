package com.example.iot

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.iot.databinding.ActivityReservationBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*




class ReservationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityReservationBinding


    private var backPressedTime : Long = 0

    var db : FirebaseFirestore? = null

    private var uid: String? = null

    var itemdata = arrayListOf<resitem>()



    suspend fun getdata() :ArrayList<resitem>{
        return try{
            val tempdata = arrayListOf<resitem>()
            uid = intent.getStringExtra("Uid")
            db = FirebaseFirestore.getInstance()
            var result = false

            var test = db!!.collection("Res").document(uid!!).get()
                .addOnSuccessListener {
                    result = true
                }.addOnFailureListener {
                    result = false
                }.await()
            test.data?.forEach{
                val reservation = it.key
                val replacelist = ArrayList<Long>()
                val reservationdata = it.value
                reservationdata?.let{
                    val reslist = it as java.util.HashMap<String, ModelReservation>
                    reslist.forEach { itreslist ->

                        val sf = SimpleDateFormat("yyyy-MM-dd")
                        sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                        val datetolong = sf.parse(itreslist.key).time
                        replacelist.add(datetolong)

                    }
                    if (replacelist.isNotEmpty()) {
                        val startdaytolong = Collections.min(replacelist)
                        val enddaytolong = Collections.max(replacelist)
                        val startdaytostring = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startdaytolong)
                        val enddaytostring = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(enddaytolong)

                        tempdata.add(resitem(startdaytostring,enddaytostring,reservation))
                    }
                }
            }
            tempdata
        }catch(e: FirebaseException){
            arrayListOf<resitem>()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarReservation.toolBarReservation.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu_dot) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        uid = intent.getStringExtra("Uid")
        db = FirebaseFirestore.getInstance()

        val name = intent.getStringExtra("Name")
        val email = intent.getStringExtra("Email")
        val photoUrl = intent.getStringExtra("PhotoUrl")



        runBlocking {
            val job = async(Dispatchers.Default) {
//                testdata()
                getdata()
            }
            itemdata = job.await()
        }


        binding.appBarReservation.rvProfile.layoutManager = LinearLayoutManager(this)
        binding.appBarReservation.rvProfile.adapter = ReservationRvAdapter(this,itemdata,
            onClickDeleteIcon = {
                deleteTask(it)
            }
        )


        val change = db!!.collection("Res").document(uid!!)

        change.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            val itemtest = arrayListOf<resitem>()
            snapshot?.data?.forEach {
                val reservation = it.key
                val replacelist = ArrayList<Long>()
                val reservationdata = it.value
                reservationdata?.let{
                    val reslist = it as java.util.HashMap<String, ModelReservation>
                    reslist.forEach { itreslist ->

                        val sf = SimpleDateFormat("yyyy-MM-dd")
                        sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                        val datetolong = sf.parse(itreslist.key).time
                        replacelist.add(datetolong)

                    }
                    if (replacelist.isNotEmpty()) {
                        val startdaytolong = Collections.min(replacelist)
                        val enddaytolong = Collections.max(replacelist)
                        val startdaytostring = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startdaytolong)
                        val enddaytostring = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(enddaytolong)

                        itemtest.add(resitem(startdaytostring,enddaytostring,reservation))
                    }
                }
            }
            if (itemdata != itemtest) {
                itemdata.clear()
                for (i in itemtest) {
                    itemdata.add(i)
                }
                binding.appBarReservation.rvProfile.adapter?.notifyDataSetChanged()


            }
        }

        db?.collection("Users")?.get()
            ?.addOnSuccessListener { collection ->
                val collectionlist: MutableList<String> = emptyList<String>().toMutableList()

                for (collect in collection) {
                    collectionlist.add(collect.id)

                }
                if (!collectionlist.contains(uid)) {
                    db?.collection("Users")?.document(uid!!)?.set({})
                }

            }






        val navigationView = binding.activityReservationDrawer
        navigationView.setNavigationItemSelectedListener(this)
        val navheaderview = navigationView.getHeaderView(0)
        val headername = navheaderview.findViewById<TextView>(R.id.header_name)
        val headeremail = navheaderview.findViewById<TextView>(R.id.header_email)
        val headerimage = navheaderview.findViewById<ImageView>(R.id.header_icon)
        headername.text = name
        headeremail.text = email
        Glide.with(this).load(photoUrl).override(100,100).into(headerimage)


        val result = uid!!
        val fragment = OneFragment()
        val bundle = Bundle()
        bundle.putString("Uid",result)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_view, fragment)
            .commit()


        db?.collection("Res")?.get()
            ?.addOnSuccessListener { collection ->
                val collectionlist: MutableList<String> = emptyList<String>().toMutableList()

                for (collect in collection) {
                    collectionlist.add(collect.id)

                }
                if (!collectionlist.contains(uid)) {
                    val reservationList = ModelReservationList()
                    db?.collection("Res")?.document(uid!!)?.set(reservationList)
                }

            }



        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result


            db?.collection("Users")?.document(uid!!)?.update("token", token)
            Log.d(TAG, token)
        })


    } // onCreate

    fun deleteTask(ResItem: resitem) {
        itemdata.remove(ResItem)
        uid = intent.getStringExtra("Uid")
        db = FirebaseFirestore.getInstance()
        val emptyreservation: HashMap<String, ModelReservation> = hashMapOf()
        db?.collection("Res")?.document(uid!!)?.update(ResItem.reservation,emptyreservation)

        binding.appBarReservation.rvProfile.adapter?.notifyDataSetChanged()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item1 -> {
                Toast.makeText(this,"이미지 추가 실행",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ThumbnailActivity::class.java)
                intent.putExtra("Uid", uid)
                startActivity(intent)
            }
            R.id.item2 -> {
                Toast.makeText(this,"예약 추가 실행",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SelectActivity::class.java)
                intent.putExtra("Uid", uid)
                startActivity(intent)

            }
        }
        return false
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_reservation_drawer, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.root.openDrawer(GravityCompat.START)
                true
            }
            R.id.item1 -> {
                Toast.makeText(this,"이미지 추가 실행",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ThumbnailActivity::class.java)
                intent.putExtra("Uid", uid)
                startActivity(intent)

                true
            }
            R.id.item2 -> {
                Toast.makeText(this,"예약 추가 실행",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SelectActivity::class.java)

                intent.putExtra("Uid", uid)

                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }




    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            ActivityCompat.finishAffinity(this)
            return
        }
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }
}


