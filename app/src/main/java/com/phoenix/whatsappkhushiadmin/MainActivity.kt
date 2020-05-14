package com.phoenix.whatsappkhushiadmin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    var intentUpload : Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intentUpload = Intent(applicationContext, UploadActivity::class.java)

        if(intent.action==Intent.ACTION_SEND){
            val receivedUri: Uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
            intentUpload!!.putExtra("receivedVideo",receivedUri)
        }
    }

    fun onClick(view:View){
        val category : String = when(view.id){
            R.id.imageButtonAttitude -> { "Attitude" }
            R.id.imageButtonComedy -> { "Comedy" }
            R.id.imageButtonDevotion -> { "Devotion" }
            R.id.imageButtonFamily -> { "Family" }
            R.id.imageButtonFestival -> { "Festival" }
            R.id.imageButtonInspiration -> { "Inspiration" }
            R.id.imageButtonLyrical -> { "Lyrical" }
            R.id.imageButtonMelodies -> { "Melodies" }
            R.id.imageButtonNewReleases -> { "NewReleases" }
            R.id.imageButtonPatriotic -> { "Patriotic" }
            R.id.imageButtonPopular -> { "Popular" }
            R.id.imageButtonOld1990s-> { "Old1990s" }
            R.id.imageButtonSad -> { "Sad" }
            R.id.imageButtonSports -> { "Sports" }
            else -> {""}
        }

        intentUpload!!.putExtra("category",category)
        startActivity(intentUpload)
    }
}