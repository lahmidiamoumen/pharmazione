package com.example.pharmazione;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.EmailItemLayoutBinding;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.ui.home.DocumentViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class SearchableActivity extends AppCompatActivity {


    private LinearLayout emptyLayout;
    private RecyclerView recyclerView;
    private CollectionReference dbCollection;
    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        getWindow().setStatusBarColor(Color.rgb(30,30,30));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);


        this.recyclerView = findViewById(R.id.rwSearch);

        this.emptyLayout = findViewById(R.id.empty_search_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.searchview_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        MenuItem searchMenuItem = menu.findItem(R.id.menu_searchview);
        searchMenuItem.expandActionView();

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView == null) return false;
        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconified(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);


        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if(newText.length() <1){
                            emptyLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        else if(newText.length() > 1)
                            doMySearch( newText );
                        return false;
                    }
                });

        return true;
    }

    public void doMySearch( String hash ){



        dbCollection = FirebaseFirestore.getInstance().collection("med-dwa");
        hash =  hash.substring(0,1).toUpperCase() + hash.substring(1).toLowerCase();

        com.google.firebase.firestore.Query baseQuery = dbCollection.whereEqualTo("isVerified",true).orderBy("name").startAt(hash).endAt(hash + '~');
        // This configuration comes from the Paging Support Library
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, Document.class)
                .build();



        adapter = new FirestorePagingAdapter<Document, RecyclerView.ViewHolder>(options) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                EmailItemLayoutBinding binding = EmailItemLayoutBinding.inflate(layoutInflater, viewGroup, false);
                return new DocumentViewHolder(binding, (item, sliderLayout) -> onClick(item,sliderLayout));
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                            int position,
                                            @NonNull Document model) {

                DocumentViewHolder doc = (DocumentViewHolder) holder;
                doc.bindTo(model);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.ERROR) {
                    emptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    showToast("An error occurred.");
                    retry();
                }else if(state == LoadingState.LOADED){
                    emptyLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            protected void onError(@NonNull Exception e) {
                Log.e("Home Layout", e.getMessage(), e);
            }
        };
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onClick(Document item, View sliderLayout) {
        //Intent i = new Intent(this, ShowItem.class);
//        i.putExtra("name", item.getTitle());
//        i.putExtra("location", item.getLocation());
//        i.putExtra("description", item.getDescription());
//        i.putExtra("category", item.getCategory());
//        i.putExtra("scanned", item.getScanned().toString());
//        i.putExtra("path", item.getPath());
//        i.putExtra("userID", item.getUserID());
//        i.putExtra("userURL", item.getUserUrl());
//        i.putExtra("userName", item.getUserName());
//        i.putExtra("documentID", item.getDocumentID());
//        startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this,sliderLayout,"idp_secret").toBundle());
    }
}
