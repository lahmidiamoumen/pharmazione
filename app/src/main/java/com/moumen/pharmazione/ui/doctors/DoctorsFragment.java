package com.moumen.pharmazione.ui.doctors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SharedViewModel;
import com.moumen.pharmazione.ShowProfileActiv;
import com.moumen.pharmazione.databinding.DoctItemBinding;
import com.moumen.pharmazione.databinding.EmailItemLayoutBinding;
import com.moumen.pharmazione.databinding.FragmentDoctorsBinding;
import com.moumen.pharmazione.databinding.PharmaItemBinding;
import com.moumen.pharmazione.persistance.Doctors;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.ui.home.DocumentViewHolder;
import com.moumen.pharmazione.utils.MedClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class DoctorsFragment extends Fragment implements MedClickListener<User> {
    private LinearLayout emptyLayout;
    private RecyclerView recyclerView;
    private SharedViewModel sharedViewModel;
    private FirestorePagingAdapter<User, RecyclerView.ViewHolder> adapter = null;
    private FragmentDoctorsBinding binding;
    Query baseQuery;
    FirestorePagingOptions<User> options;
    PagedList.Config config;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        recyclerView = binding.recyclerView;
        emptyLayout = binding.emptySearchList;

        binding.filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() <1){
                    emptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                else if(s.length() > 1)
                    setUp( s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        CollectionReference dbCollection = FirebaseFirestore.getInstance().collection(PATH_USER);
        baseQuery = dbCollection.whereEqualTo("satisfied", true).orderBy("mName");

        options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, User.class)
                .build();

//        binding.navigationIcon.setOnClickListener(o-> NavHostFragment.findNavController(this).navigateUp());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        doMySearch(options);
    }

    public void setUp(String hash) {
        hash =  hash.substring(0,1).toUpperCase() + hash.substring(1).toLowerCase();

        Query as =  baseQuery.startAt(hash).endAt(hash + '~');
        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(as, config, User.class)
                .build();

        if( adapter == null) {
            doMySearch(options);
        } else {
            adapter.updateOptions(options);
        }
    }

    public void doMySearch(FirestorePagingOptions<User> options){
        adapter = new FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                PharmaItemBinding binding = PharmaItemBinding.inflate(layoutInflater, viewGroup, false);
                return new DoctorsViewHolder(binding, DoctorsFragment.this, getContext());
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                            int position,
                                            @NonNull User model) {

                DoctorsViewHolder doc = (DoctorsViewHolder) holder;
                doc.bind(model);
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    private void showToast(@NonNull String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view, User item) {
        Intent mIntent = new Intent(getContext(), ShowProfileActiv.class);
        mIntent.putExtra("id_user", item.userId);
        startActivity(mIntent);
    }
}
