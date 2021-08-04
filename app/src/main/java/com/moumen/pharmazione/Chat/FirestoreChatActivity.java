package com.moumen.pharmazione.Chat;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.util.ui.ImeHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.databinding.ActivityChatBinding;
import com.moumen.pharmazione.persistance.ChatCollector;
import com.moumen.pharmazione.persistance.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_CHAT;
import static com.moumen.pharmazione.utils.Util.PATH_USER;

/**
 * Class demonstrating how to setup a {@link RecyclerView} with an adapter while taking sign-in
 * states into consideration. Also demonstrates adding data to a ref and then reading it back using
 * the {@link FirestoreRecyclerAdapter} to build a simple chat app.
 * <p>
 * For a general intro to the RecyclerView, see <a href="https://developer.android.com/training/material/lists-cards.html">Creating
 * Lists</a>.
 */
@SuppressLint("RestrictedApi")
public class FirestoreChatActivity extends AppCompatActivity
        implements FirebaseAuth.AuthStateListener {
    private static final String TAG = "FirestoreChatActivity";
    private  final Map<String, Object> updates = new HashMap<>();


    private static final CollectionReference sChatCollection =
            FirebaseFirestore.getInstance().collection(PATH_CHAT);

    private CollectionReference chatCollection;
    /** Get the last 50 chat messages ordered by timestamp . */
    private static Query sChatQuery;

    static {
        FirebaseFirestore.setLoggingEnabled(true);
    }

    private ActivityChatBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.messagesList.setVisibility(View.INVISIBLE);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        mBinding.messagesList.setHasFixedSize(true);
        mBinding.messagesList.setLayoutManager(manager);

        mBinding.messagesList.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                mBinding.messagesList.postDelayed(() -> mBinding.messagesList.smoothScrollToPosition(0), 100);
            }
        });

//        ImeHelper.setImeOnDoneListener(mBinding.messageEdit, () -> onSendClick());
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (isSignedIn()) {
//            attachRecyclerViewAdapter();
//        }
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        mBinding.sendButton.setEnabled(isSignedIn());
        mBinding.messageEdit.setEnabled(isSignedIn());

        if (isSignedIn()) {
            attachRecyclerViewAdapter();
        } else {
            Toast.makeText(this, R.string.signing_in, Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void attachRecyclerViewAdapter() {
        final String chatWith = getIntent().getStringExtra("chatWith");
        final String chatWithName = getIntent().getStringExtra("chatWithName");
        final String chatWithUrl = getIntent().getStringExtra("chatWithUrl");

        final String messageID = getIntent().getStringExtra("chatID");
        final FirebaseUser mUse = FirebaseAuth.getInstance().getCurrentUser();
        String uID = mUse.getUid();
        if ( messageID != null ) {
            // the message between the chatters already created
            setMessagesUI(messageID);
        } else if( chatWith != null) {
            // create new message page
            sChatCollection
                    .whereArrayContains("chatters", uID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(task.getResult().size() == 0) {
                                System.out.println("is zwero");
                                ChatCollector chatCollector = new ChatCollector(Arrays.asList(chatWith, uID), Arrays.asList(chatWithName, mUse.getDisplayName()), Arrays.asList(chatWithUrl, mUse.getPhotoUrl().toString()));

                                String id = sChatCollection.document().getId();
                                sChatCollection.document(id).set(chatCollector).addOnSuccessListener(c-> setMessagesUI(id));
                            } else {
                                boolean bool = false;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println("=>>> " + document.getData());
                                    if(((List<String>) document.getData().get("chatters")).contains(chatWith)) {
                                        setMessagesUI(document.getId());
                                        bool = true;
                                        break;
                                    }
                                }
                                if(!bool) {
                                    ChatCollector chatCollector = new ChatCollector(Arrays.asList(chatWith, uID), Arrays.asList(chatWithName, mUse.getDisplayName()), Arrays.asList(chatWithUrl, mUse.getPhotoUrl().toString()));

                                    String id = sChatCollection.document().getId();
                                    sChatCollection.document(id).set(chatCollector).addOnSuccessListener(c-> setMessagesUI(id));
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            Toast.makeText(this, "Something went rong" ,Toast.LENGTH_SHORT).show();
        }
    }
    private void setMessagesUI(String uid) {
        chatCollection = sChatCollection.document(uid).collection("messages");
        // update last seen date
        updates.put("lastSeen", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection(PATH_CHAT).document(uid).set(updates, SetOptions.merge());

        mBinding.messagesList.setVisibility(View.VISIBLE);
        mBinding.sendButton.setOnClickListener(view -> onSendClick());
        sChatQuery = chatCollection.orderBy("timestamp", Query.Direction.DESCENDING).limit(50);
        final RecyclerView.Adapter adapter = newAdapter();
        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mBinding.messagesList.smoothScrollToPosition(0);
            }
        });
        mBinding.messagesList.setAdapter(adapter);
    }

    public void onSendClick() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String name = "User " + uid.substring(0, 6);

        onAddMessage(new Chat(name, mBinding.messageEdit.getText().toString(), uid, user.getPhotoUrl().toString(), user.getDisplayName()));

        mBinding.messageEdit.setText("");
    }

    @NonNull
    private RecyclerView.Adapter newAdapter() {
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                        .setQuery(sChatQuery, Chat.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirestoreRecyclerAdapter<Chat, ChatHolder>(options) {
            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ChatHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatHolder holder, int position, @NonNull Chat model) {
                holder.bind(model);
            }

            @Override
            public void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                mBinding.emptyTextView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };
    }

    private void onAddMessage(@NonNull Chat chat) {
        chatCollection.add(chat).addOnFailureListener(this, e -> Log.e(TAG, "Failed to write message", e));
    }
}