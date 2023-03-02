package com.example.iot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iot.databinding.ActivityThumbnailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ThumbnailActivity : AppCompatActivity(){
    private var stdb : FirebaseStorage? = null
    private var db : FirebaseFirestore? = null
    private var uriPhoto : Uri? = null
    private var uid: String? = null
    private lateinit var binding: ActivityThumbnailBinding
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThumbnailBinding.inflate(layoutInflater)
        setContentView(binding.root)



        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        uid = intent.getStringExtra("Uid")
        stdb = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        val imgFileName = "IMAGE_" + uid + "_.png"
        Log.d("imgFileName", imgFileName)
        val storageRef = stdb?.reference?.child("images")?.child(imgFileName)
        val imageView = findViewById<ImageView>(R.id.xml_frg_prf_img_profile)
        storageRef!!.downloadUrl.addOnSuccessListener {
            Log.d("imgFileName", "image load success")
            Glide.with(this).load(it).into(imageView)
            Log.d("imgFileName", it.toString())
        }.addOnFailureListener {
            Log.d("imgFileName", "image load fail")
        }

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                uriPhoto = result.data?.data
                Log.d("imgFileNameUri", uriPhoto.toString())
                binding.xmlFrgPrfImgProfile.setImageURI(uriPhoto)
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    funImageUpload(uriPhoto!!)
                }
            }

        }

        binding.xmlFrgPrfBtnUpload.setOnClickListener {
            //Open Album
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activityLauncher.launch(photoPickerIntent)
        }

    } // oncreate

    private fun funImageUpload(uri: Uri){


        val imgFileName = "IMAGE_" + uid + "_.png"
        val storageRef = stdb?.reference?.child("images")?.child(imgFileName)

        storageRef?.putFile(uri)?.addOnSuccessListener {
            Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show()
        }
        storageRef?.downloadUrl?.addOnSuccessListener {
            Log.d("imgFileNameNext", it.toString())
            db?.collection("Users")?.document(uid!!)?.update("url", it.toString())
        }
    }

}