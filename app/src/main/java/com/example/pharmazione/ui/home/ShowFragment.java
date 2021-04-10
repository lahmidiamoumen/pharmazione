package com.example.pharmazione.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.ui.poster.AlbumAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.example.pharmazione.R;
import com.example.pharmazione.SharedViewModel;
import com.example.pharmazione.databinding.FragmentShowBinding;
import com.example.pharmazione.persistance.Comment;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.ui.poster.PosterActivity;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;
import com.yanzhenjie.album.widget.divider.Divider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.pharmazione.utils.Util.EMPTY_IMAGE;
import static com.example.pharmazione.utils.Util.load;

public class ShowFragment extends Fragment {
    private FragmentShowBinding binding;
    private SharedViewModel sharedViewModel;
    private long duration;
    private FirestoreRecyclerAdapter<Comment, RecyclerView.ViewHolder> adapter;
    private CollectionReference dbCollection;
    private FirebaseAuth mAuth;
    private Drawable icon;
    private FirebaseFirestore db;
    private Context c;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        sharedViewModel =  new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        prepareTransitions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShowBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        c = getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sharedViewModel.getDocData().observe(getViewLifecycleOwner(), doc ->  {

            ShowFragmentImagesAdapter adapter = new ShowFragmentImagesAdapter(c);
            if(doc.path != null){
                adapter.submitList(doc.path);
                binding.attachmentRecyclerView.setAdapter(adapter);
            }


            binding.setEmail(doc);
            binding.setUrlEmpty(EMPTY_IMAGE);
            binding.itemSendButtonId.setOnClickListener(o->sendMessage());
            dbCollection = db.collection("med-dwa-pharmazion")
                    .document(doc.documentID)
                    .collection("message");
            setUpComments(dbCollection.orderBy("created", Query.Direction.DESCENDING).limit(30));
        });
        binding.navigationIcon.setOnClickListener(o-> {});
        toolBarIcon();
        startTransitions();
    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_scrolling, menu);
        final View v = binding.toolbar.getChildAt(0);
        icon = menu.findItem(R.id.action_settings).getIcon();
        icon.setColorFilter(getResources().getColor(R.color.quantum_black_100), PorterDuff.Mode.SRC_ATOP);
        if(v instanceof ImageButton) {
            ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.quantum_black_100), PorterDuff.Mode.SRC_IN);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavHostFragment.findNavController(this).navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toolBarIcon() {
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator( getResources().getDrawable(R.drawable.ic_back_white));

        AppBarLayout appBarLayout = binding.appBar;
        appBarLayout.setVisibility(View.GONE);


        final CollapsingToolbarLayout collapsingToolbar = binding.toolbarLayout;
        collapsingToolbar.setContentScrimColor(MaterialColors.getColor(getContext(), R.attr.colorOnSecondary, Color.TRANSPARENT));
    }

    private void startTransitions() {
        binding.executePendingBindings();
        startPostponedEnterTransition();
    }

    private void prepareTransitions() {
        postponeEnterTransition();
        Context cn = getContext();
        assert cn != null;
        @SuppressLint("Recycle")
        int array = cn.obtainStyledAttributes(new int[]{R.attr.motionInterpolatorPersistent}).getResourceId(0, android.R.interpolator.fast_out_slow_in);
        Interpolator interpolator =  AnimationUtils.loadInterpolator(cn,array);

        @SuppressLint("Recycle")
        int array2 = cn.obtainStyledAttributes(new int[]{R.attr.colorSurface}).getColor(0, Color.MAGENTA);

        MaterialContainerTransform mat = new MaterialContainerTransform();
        mat.setDuration(duration);
        mat.setDrawingViewId(R.id.navigation_home);
        mat.setScrimColor(Color.TRANSPARENT);
        mat.setAllContainerColors(array2);
        mat.setInterpolator(interpolator);
        setSharedElementEnterTransition(mat);

        this.setEnterTransition(  new MaterialElevationScale(false)
                .setDuration(duration).setInterpolator(interpolator));

        this.setReturnTransition(  new MaterialContainerTransform()
                .setDuration(duration).setInterpolator(interpolator));

//        this.setExitTransition(  new MaterialElevationScale( false)
//                .setDuration(duration));
//
//        this.setExitTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//                .setDuration(duration));
//
//
//        this.setEnterTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//                .setDuration(duration));
    }

    private void setUpComments(Query baseQuery){

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, Comment.class)
                .setLifecycleOwner(getViewLifecycleOwner())
                .build();

        adapter = new FirestoreRecyclerAdapter<Comment, RecyclerView.ViewHolder>(options) {
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
            public void onDataChanged() {
                // Called each time there is a new query snapshot. You may want to use this method
                // to hide a loading spinner or check for the "no documents" state and update your UI.
                // ...
                //this.notifyItemInserted(getItemCount()-1);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.e("Home Layout", e.getMessage(), e);
                showToast( "Pas de résultats");
            }

         };
        binding.commentReceylerView.setAdapter(adapter);
        binding.commentReceylerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsDelay();
        //swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
    }

    private void commentsDelay(){
        new Handler().postDelayed(() -> {
            binding.commentReceylerView.setVisibility(View.VISIBLE);
            //binding.progressBar.setVisibility(View.GONE);
        }, 300L);

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showToast(String s){
        Toast.makeText(getContext(), s,Toast.LENGTH_SHORT).show();
    }

    private void sendMessage() {
        System.out.println("In Message");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            showToast("Vous devez d'abord vous inscrire! ");
            return;
        }

        String content = binding.editText.getText().toString();
        if(content.isEmpty()) {
            showToast("vide!");
            return;
        }

        binding.editText.setText("");
        Map<String, Object> updates = new HashMap<>();
        updates.put("created", FieldValue.serverTimestamp());
        updates.put("content", content);
        updates.put("userID", content);
        updates.put("userURL", user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
        updates.put("userName", Objects.requireNonNull(user.getDisplayName()));
        dbCollection.document().set(updates, SetOptions.merge());
        System.out.println("In Message end");
    }

    private void showDig(Document doc) {
        if(mAuth.getCurrentUser() == null) {
            showToast("vous devez s'inscrire d'abord");
            return;
        }
        if(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber() == null){
            showToast("Vous n'avez pas ajouté le numéro de téléphone");
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("J'ai un "+doc.category.toLowerCase())
                .setMessage("Un message sera envoyé à la personne une question et elle vous contactera par téléphone")
                .setPositiveButton("Continuer", (dialogInterface, i) -> {})
                .setNegativeButton("Annuler", null)
                .show();
    }

}