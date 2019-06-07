package com.sun.firestoredemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val NUMBER_OF_SHARDS = 10
    }

    private val db = FirebaseFirestore.getInstance()
    private val pointRef: DocumentReference by lazy {
        db.collection("boost").document("hai")
    }
    private var currentPoint: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createBoostCounter(pointRef, NUMBER_OF_SHARDS).addOnCompleteListener { showToast("Done") }
        setupPointListener()
        buttonBoost.setOnClickListener {
            incrementPoint()
        }
    }

    private fun incrementPoint(): Task<Void> {
        val shardId = Math.floor(Math.random() * NUMBER_OF_SHARDS).toInt()
        val shardRef = pointRef.collection("shards").document(shardId.toString())
        return shardRef.update("count", FieldValue.increment(1))
    }

    private fun getCount(ref: DocumentReference): Task<Int> {
        // Sum the count of each shard in the subcollection
        return ref.collection("shards").get()
            .continueWith { task ->
                var count = 0
                for (snap in task.result!!) {
                    val shard = snap.toObject(Shard::class.java)
                    count += shard.count!!
                }
                currentPoint = count.toLong()
                count
            }
    }

    private fun setupPointListener() {
        db.collection("boost")
            .document("hai")
            .collection("shards")
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("MainActivity", "Listen failed.", e)
                    return@EventListener
                }

                if (value != null) {
                    getCount(pointRef)
                    textPoint.text = currentPoint?.toString()
                }
            })
    }

    fun createBoostCounter(ref: DocumentReference, numShards: Int): Task<Void> {
        // Initialize the counter document, then initialize each shard.
        return ref.set(BoostCounter(numShards))
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }

                val tasks = arrayListOf<Task<Void>>()

                // Initialize each shard with count=0
                for (i in 0 until numShards) {
                    val makeShard = ref.collection("shards")
                        .document(i.toString())
                        .set(Shard(0), SetOptions.merge())
                    tasks.add(makeShard)
                }

                Tasks.whenAll(tasks)
            }
    }
}

fun AppCompatActivity.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}
