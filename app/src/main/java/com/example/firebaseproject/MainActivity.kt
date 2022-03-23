package com.example.firebaseproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.firebaseproject.databinding.ActivityMainBinding
import com.example.firebaseproject.notification.PushService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var pushBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//----------------------------push--------------------------------------------------
        pushBroadcastReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val extras = intent?.extras
                extras?.keySet()?.firstOrNull {it == PushService.KEY_ACTION}?.let{key ->
                    when(extras.getString(key)){
                        PushService.ACTION_SHOW_MESSAGE -> {
                            extras.getString(PushService.KEY_MESSAGE)?.let {message ->
                                Log.e("MyLog", message)
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {Log.e("MyLog", "No needed key found -> ${extras.getString(key)}")}
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(PushService.INTENT_FILTER)

        registerReceiver(pushBroadcastReceiver, intentFilter)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(!task.isSuccessful){
                return@addOnCompleteListener
            }

            val token = task.result
            Log.e("MyLog", "Token -> $token")
        }
//----------------------------push--------------------------------------------------
        auth = Firebase.auth
        setUpActionBar()
        val database = Firebase.database
        val myRef = database.getReference("messages")
        binding.btnSend.setOnClickListener{
            myRef.setValue(binding.edittextMessage.text.toString())
        }
        onChangeDatabase(myRef)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.sign_out){
            auth.signOut()

            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onChangeDatabase(dRef: DatabaseReference){
        dRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyLog", "onData set changes working ${snapshot.value.toString()}")
               binding.apply {
                   tvMessage.append("\n")
                   tvMessage.append(snapshot.value.toString())
               }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyLog", "Error -> $error")
            }

        })
    }

    private fun setUpActionBar(){
        val actionbar = supportActionBar
      CoroutineScope(Dispatchers.IO).launch {
          val bMap = Picasso.get().load(auth.currentUser?.photoUrl).get()
          val dIcon = BitmapDrawable(resources, bMap)
          runOnUiThread {
              actionbar?.setDisplayHomeAsUpEnabled(true)
              actionbar?.setHomeAsUpIndicator(dIcon)
              actionbar?.title = auth.currentUser?.displayName
          }

      }

    }

    override fun onDestroy() {
        unregisterReceiver(pushBroadcastReceiver)
        super.onDestroy()
    }
}
