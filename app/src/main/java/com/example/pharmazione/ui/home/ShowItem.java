package com.example.pharmazione.ui.home;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.pharmazione.R;
import com.example.pharmazione.databinding.ActivityShowItemsBinding;
import com.example.pharmazione.persistance.Comment;
import com.example.pharmazione.persistance.Document;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.pharmazione.utils.Util.load;
import static com.example.pharmazione.utils.Util.previewImages;

public class ShowItem extends AppCompatActivity {

    private FirestorePagingAdapter<Comment, RecyclerView.ViewHolder> adapter;
    private CollectionReference dbCollection;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    ActivityShowItemsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        dbCollection = FirebaseFirestore.getInstance().collection("med-dwa")
                .document(getIntent().getStringExtra("documentID")).collection("message");

        // set an exit transition
        postponeEnterTransition();
        getWindow().setExitTransition(new Explode());

        binding =  DataBindingUtil.setContentView(this, R.layout.activity_show_items);
        View root = binding.getRoot();


        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator( getResources().getDrawable(R.drawable.ic_back_white));

        AppBarLayout appBarLayout = root.findViewById(R.id.app_bar);


        Document item = new Document();
        item.setName(getIntent().getStringExtra("name"));
        item.setDescription(getIntent().getStringExtra("description"));
        item.setLocation(getIntent().getStringExtra("location"));
        item.setCategory(getIntent().getStringExtra("category"));
        //getIntent().getStringExtra("scanned");
        item.setPath(getIntent().getStringExtra("path"));
        item.setUserID(getIntent().getStringExtra("userID"));
        item.setUserName(getIntent().getStringExtra("userName"));
        item.setUserUrl(getIntent().getStringExtra("userURL"));
        dbCollection = FirebaseFirestore.getInstance().collection("med-dwa")
                .document(getIntent().getStringExtra("documentID")).collection("message");



        load(binding.imageSlider,item.getPath());

        if(item.getUserUrl() != null)
            load(binding.userImage,item.getUserUrl());



        ArrayList<String> listUrl = new ArrayList<>();
        listUrl.add(item.getPath());
        binding.imageSlider.setOnClickListener(v->previewImages(listUrl,this));
        // data binding
        binding.setItem(item);

        binding.itemSendButtonId.setOnClickListener(this::sendMessage);



        //get back icon <- view
        final View v = toolbar.getChildAt(0);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
        {
            int scrollRange = -1;
            boolean isShow;
            @Override
            public void onOffsetChanged (AppBarLayout appBarLayout, int verticalOffset){
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //collapse menu
                    //TODO: change share icon color - set white share icon
                    isShow = true;
                    mItem.findItem(R.id.action_settings).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    if(v instanceof ImageButton) {
                        ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(ShowItem.this, R.color.white), PorterDuff.Mode.SRC_IN);
                    }
                } else if (isShow) {
                    //expanded menu
                    //TODO: change share icon color - set dark share icon
                    isShow = false;
                    mItem.findItem(R.id.action_settings).getIcon().setColorFilter(getResources().getColor(R.color.quantum_black_100), PorterDuff.Mode.SRC_ATOP);
                    if(v instanceof ImageButton) {
                        ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(ShowItem.this, R.color.quantum_black_100), PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        });

        recyclerView = binding.commentReceylerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUp();
    }


    public void setUp(){
        Query baseQuery = dbCollection.orderBy("timestamp", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(7)
                .setPageSize(5)
                .build();

        FirestorePagingOptions<Comment> options = new FirestorePagingOptions.Builder<Comment>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, Comment.class)
                .build();


        adapter = new FirestorePagingAdapter<Comment, RecyclerView.ViewHolder>(options) {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                        View view = layoutInflater.inflate(R.layout.empty_activity, viewGroup, false);
                        return new CommentViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                                    int position,
                                                    @NonNull Comment model) {
                        CommentViewHolder doc = (CommentViewHolder) holder;
                        doc.bindTo(model);
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                        switch (state) {
                            case LOADING_INITIAL:
                                System.out.println("Load INITIAL");

                            case LOADING_MORE:
                                //swipeRefreshLayout.setRefreshing(true);
                                break;
                            case LOADED:
                                //swipeRefreshLayout.setRefreshing(false);
                                break;
                            case FINISHED:
                                //swipeRefreshLayout.setRefreshing(false);
                                //add(true);
                                break;
                            case ERROR:
                                //showToast("An error occurred.");
                                retry();
                                break;
                        }
                    }
                    @Override
                    protected void onError(@NonNull Exception e) {
                        //swipeRefreshLayout.setRefreshing(false);
                        Log.e("Home Layout", e.getMessage(), e);
                    }
                };
        recyclerView.setAdapter(adapter);
        commentsDelay();
        //swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
    }

    public void commentsDelay(){
        new Handler().postDelayed(() -> {
            recyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }, 400L);

    }





    Menu mItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItem = menu;
        final View v = binding.toolbar.getChildAt(0);
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        mItem.findItem(R.id.action_settings).getIcon().setColorFilter(getResources().getColor(R.color.quantum_black_100), PorterDuff.Mode.SRC_ATOP);
        if(v instanceof ImageButton) {
            ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(ShowItem.this, R.color.quantum_black_100), PorterDuff.Mode.SRC_IN);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// API 5+ solution
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPostponedEnterTransition();
    }


    public void sendMessage(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            return;
        }
        String content = binding.editText.getText().toString();
        if(content.isEmpty()) return;
        binding.editText.setText("");
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserID(user.getUid());
        if(user.getPhotoUrl() != null)
        comment.setUserURL(user.getPhotoUrl().toString());
        comment.setUserName(user.getDisplayName());
        dbCollection.add(comment);
        adapter.refresh();
    }
}
