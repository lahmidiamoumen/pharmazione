package com.moumen.pharmazione.Chat;


import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.databinding.ActivityFirestorePagingBinding;
import com.moumen.pharmazione.databinding.CardviewProfileBinding;
import com.moumen.pharmazione.databinding.ItemItemBinding;
import com.moumen.pharmazione.persistance.ChatCollector;
import com.moumen.pharmazione.ui.home.DocumentViewHolderProfile;
import com.moumen.pharmazione.utils.ClickListener;
import com.moumen.pharmazione.utils.MedClickListener;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.moumen.pharmazione.utils.Util.PATH_CHAT;

public class FirestorePagingActivity extends AppCompatActivity implements MedClickListener<String> {

    private static final String TAG = "FirestorePagingActivity";

    private ActivityFirestorePagingBinding mBinding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItemsCollection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFirestorePagingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mItemsCollection = FirebaseFirestore.getInstance().collection(PATH_CHAT);

        mBinding.close.setOnClickListener(o -> finish());

        setUpAdapter();
    }

    private void setUpAdapter() {
        if (mAuth.getUid() == null) return;
        Query baseQuery = mItemsCollection.whereArrayContains("chatters", mAuth.getUid())/*.orderBy("lastSeen", Query.Direction.ASCENDING)*/;

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<ChatCollector> options = new FirestorePagingOptions.Builder<ChatCollector>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, ChatCollector.class)
                .build();

        final FirestorePagingAdapter<ChatCollector, ItemViewHolder> adapter =
                new FirestorePagingAdapter<ChatCollector, ItemViewHolder>(options) {
                    @NonNull
                    @Override
                    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
                        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                        ItemItemBinding binding = ItemItemBinding.inflate(layoutInflater, parent, false);
                        return new ItemViewHolder(binding, FirestorePagingActivity.this, mAuth.getCurrentUser());

//                        View view = LayoutInflater.from(parent.getContext())
//                                .inflate(R.layout.item_item, parent, false);
//                        return new ItemViewHolder(view, FirestorePagingActivity.this, mAuth.getCurrentUser());
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull ItemViewHolder holder,
                                                    int position,
                                                    @NonNull ChatCollector model) {
                        holder.bind(model);
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                        switch (state) {
                            case LOADING_INITIAL:
                            case LOADING_MORE:
                                mBinding.swipeRefreshLayout.setRefreshing(true);
                                break;
                            case LOADED:
                                mBinding.swipeRefreshLayout.setRefreshing(false);
                                break;
                            case FINISHED:
                                mBinding.swipeRefreshLayout.setRefreshing(false);
                                //showToast("Reached end of data set.");
                                break;
                            case ERROR:
                                showToast("An error occurred.");
                                retry();
                                break;
                        }
                    }

                    @Override
                    protected void onError(@NonNull Exception e) {
                        mBinding.swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, e.getMessage(), e);
                    }
                };

        mBinding.pagingRecycler.setLayoutManager(new LinearLayoutManager(this));
        mBinding.pagingRecycler.setAdapter(adapter);

        mBinding.swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_paging, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_add_data) {
            showToast("Adding data...");
            createItems().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    showToast("Data added.");
                } else {
                    Log.w(TAG, "addData", task.getException());
                    showToast("Error adding data.");
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Task<Void> createItems() {
        WriteBatch writeBatch = mFirestore.batch();

        for (int i = 0; i < 250; i++) {
            String title = "Item " + i;

            String id = String.format(Locale.getDefault(), "item_%03d", i);
            Item item = new Item(title, i);

            writeBatch.set(mItemsCollection.document(id), item);
        }

        return writeBatch.commit();
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view, String item) {
        Intent  chatActivity = new Intent(this, FirestoreChatActivity.class);
        chatActivity.putExtra("chatID", item);
        startActivity(chatActivity);
    }

    public static class Item {

        @Nullable public String text;
        public int value;

        public Item() {}

        public Item(@Nullable String text, int value) {
            this.text = text;
            this.value = value;
        }

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemItemBinding binding;
        MedClickListener<String> clickListener;
        FirebaseUser user;

        ItemViewHolder(@NonNull ItemItemBinding binding, MedClickListener<String> clickListener, FirebaseUser user) {
            super(binding.getRoot());
            this.clickListener = clickListener;
            this.user = user;
            this.binding = binding;
        }

        void bind(@NonNull ChatCollector item) {
            binding.setHandler(clickListener);

            if(item.lastSeen != null) {
                String niceDateStr = DateUtils.getRelativeTimeSpanString(item.lastSeen.toDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
                binding.setTimeAgo(niceDateStr);
            } else {
                binding.setTimeAgo("-");
            }

            item.chattersNames.remove(user.getDisplayName());
            item.chattersUrls.remove(user.getPhotoUrl().toString());

            binding.setItem(item);
        }
    }

}