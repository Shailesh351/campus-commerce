package `in`.webdevlabs.campuscommerce

import `in`.webdevlabs.campuscommerce.adapters.ChatViewHolder
import `in`.webdevlabs.campuscommerce.model.Chat
import `in`.webdevlabs.campuscommerce.utils.FirebaseUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var ruid: String
    private lateinit var suid: String
    private lateinit var pid: String
    private var gid: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val bundle = intent.extras
        if (bundle != null) {
            ruid = bundle.getString("uid")
            pid = bundle.getString("pid")
        }
        suid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseUtil.addGroupToDatabase(suid, ruid, pid)

        gid = ruid.hashCode() + suid.hashCode() + pid.hashCode()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val mChatReference = database.reference

        val rView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val recyclerAdapter = object : FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat::class.java, R.layout.item_chat,
                ChatViewHolder::class.java, mChatReference.child("groups").child(gid.toString())) {
            override fun populateViewHolder(viewHolder: ChatViewHolder?, chat: Chat?, position: Int) {
                if (chat != null) {
                    viewHolder?.bindPost(chat)
                }
            }
        }
        rView.setHasFixedSize(true)
        rView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rView.adapter = recyclerAdapter

        setup()

    }

    private fun setup() {
        sendButton.setOnClickListener({
            send()
        })
        msgEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(msg: CharSequence?, p1: Int, p2: Int, p3: Int) {
                msg?.let {
                    when (it.toString().isEmpty()) {
                        true -> {
                            sendButton.isEnabled = false
                        }
                        false -> {
                            sendButton.isEnabled = true
                        }
                    }
                }
            }
        })

    }

    private fun send() {
        val msg = msgEditText.text.toString()

        val currentLocalTime = Calendar.getInstance(TimeZone.getDefault()).time
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val time = date.format(currentLocalTime)

        val chat = Chat(msg, time)
        FirebaseUtil.addChatToFirebaseDatabase(gid, chat)
    }
}
