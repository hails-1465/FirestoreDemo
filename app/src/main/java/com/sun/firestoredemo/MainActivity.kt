package com.sun.firestoredemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var currentPoint: Long? = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPointListener()
        buttonBoost.setOnClickListener {
            addPoint()
        }
    }

    private fun addPoint() {
        val pointMap = HashMap<String, Any>()
        pointMap["point"] = currentPoint?.plus(1) as Any
        db.collection("boost")
            .document("3Kklje1XHMtYnNvsqUTV")
            .update(pointMap)
            .addOnSuccessListener { documentReference ->
                showToast("Boost successfully")
            }
            .addOnFailureListener { exception ->
                exception.message?.let { showToast(it) }
            }
    }

    private fun setupPointListener() {
        val pointRef = db.collection("boost").document("3Kklje1XHMtYnNvsqUTV")
        pointRef.addSnapshotListener(EventListener<DocumentSnapshot> {snapshot, e ->
            if (e != null) {
                Log.w("MainActivity", "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                currentPoint = snapshot.data?.get("point") as Long?
                textPoint.text = currentPoint?.toString()
            }
        })
    }
}

fun AppCompatActivity.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}
