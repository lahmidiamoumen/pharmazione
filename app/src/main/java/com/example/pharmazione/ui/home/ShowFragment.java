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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.example.pharmazione.R;
import com.example.pharmazione.SharedViewModel;
import com.example.pharmazione.databinding.FragmentShowBinding;
import com.example.pharmazione.persistance.Comment;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.ui.poster.PosterActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.pharmazione.utils.Util.EMPTY_IMAGE;
import static com.example.pharmazione.utils.Util.load;

public class ShowFragment extends Fragment {
    private FragmentShowBinding binding;
    private SharedViewModel sharedViewModel;
    private long duration;
    private FirestorePagingAdapter<Comment, RecyclerView.ViewHolder> adapter;
    private CollectionReference dbCollection;
    private FirebaseAuth mAuth;
    private static String typeButton = "J'ai un besoin";
    private static String typeButtonDon = "J'ai un don";
    private Drawable icon;
    private FirebaseFirestore db;


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
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();



        sharedViewModel.getDocData().observe(getViewLifecycleOwner(), doc ->  {
            String type = doc.category.toLowerCase();
            if(doc.userID.equals(mAuth.getUid()) || type.equals("orientation")){
                binding.extendedFab.setVisibility(View.GONE);
            }
            else if(type.equals("besoin")){
                binding.extendedFab.setText(typeButtonDon);
                binding.extendedFab.setExtended(true);
                binding.extendedFab.setOnClickListener(o -> showDig(doc));
            }
            else {
                binding.extendedFab.setText(typeButton);
                binding.extendedFab.setExtended(true);
                binding.extendedFab.setOnClickListener(o -> jaiBesoin());
            }
            binding.setItem(doc);
            binding.setUrlEmpty(EMPTY_IMAGE);
            binding.itemSendButtonId.setOnClickListener(o->sendMessage());
            dbCollection = db.collection("med-dwa")
                    .document(doc.documentID)
                    .collection("message");
            setUpComments();
        });
        toolBarIcon();
        startTransitions();
    }

    private void jaiBesoin() {
        if(mAuth.getCurrentUser() != null){
            showToast("vous devez s'inscrire d'abord");
            return;
        }
        Intent intent = new Intent(getContext(), PosterActivity.class);
        intent.putExtra("needBesoin","needBesoin");
        new AlertDialog.Builder(getContext())
                .setMessage("Votre besoin est pris en considération, vous devez ajouter une ordonnance pour valider le bsoin")
                .setPositiveButton("Continuer", (dialogInterface, i) -> startActivity(intent))
                .setNegativeButton("Sortie", null)
                .show();
    }

    private void extendAction(Document document) {
        if(mAuth.getCurrentUser() != null) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Boolean> satisfied = new HashMap<>();

            satisfied.put("satisfied",true);

            FirebaseUser user = mAuth.getCurrentUser();
            data.put("seen",false );
            data.put("name", document.getTitle());
            data.put("path",  document.getPath());
            data.put("donneur_phone", user.getPhoneNumber());
            data.put("donneur_name", user.getDisplayName());
            data.put("user_id",document.getUserID() );

            db.collection("med-dwa-users")
                    .document(document.getUserID()).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token =  task.getResult().getData().get("token").toString();
                    System.out.println("Token is "+token);
                    data.put("token", token);
                    db.collection("don-besoin-binds").document().set(data,SetOptions.merge())
                      .addOnSuccessListener(o->{
                        binding.extendedFab.setBackgroundColor(Color.rgb(90,158,11));
                        binding.extendedFab.setTextColor(Color.BLACK);
                        binding.extendedFab.setExtended(false);
                        binding.extendedFab.setOnClickListener(d-> {});
                    });
                    db.collection("'med-dwa").document(document.documentID).set(satisfied,SetOptions.merge());
                }
            });

//            data.put("id_med", document.medicamentID);
//            data.put("id_added_on", document.documentID);
//            data.put("name_med", document.name);
//            data.put("photo_med", document.path);
//            data.put("id_user", user.getUid());
//            data.put("name_user", user.getDisplayName());
//            data.put("photo_user", user.getPhotoUrl().toString());
//            data.put("phone_user", user.getPhoneNumber());
//            data.put("bind_user_id", "");
//
//
//            db.collection("don-besoin")
//                    .document().set(data).addOnSuccessListener(o->{
//                        binding.extendedFab.setBackgroundColor(Color.rgb(90,158,11));
//                        binding.extendedFab.setTextColor(Color.BLACK);
//                        binding.extendedFab.setExtended(false);
//                        binding.extendedFab.setOnClickListener(d-> {});
//            });
        } else {
            showToast("Vous devez s'inscrire d'abord");
        }
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


        final View v = toolbar.getChildAt(0);
        final CollapsingToolbarLayout collapsingToolbar = binding.toolbarLayout;
        collapsingToolbar.setContentScrimColor(MaterialColors.getColor(getContext(), R.attr.colorOnSecondary, Color.WHITE));

        // int color1 = MaterialColors.getColor(getContext(), R.attr.colorOnSurface, Color.BLACK);

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if ((collapsingToolbar.getHeight() + verticalOffset) < (2 * ViewCompat.getMinimumHeight(collapsingToolbar)) && icon != null) {
                icon.setColorFilter(getResources().getColor(R.color.quantum_black_100), PorterDuff.Mode.SRC_ATOP);
                if(v instanceof ImageButton) {
                    ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.quantum_black_100), PorterDuff.Mode.SRC_IN);
                }
            } else if(icon != null) {
               icon.setColorFilter(getResources().getColor(R.color.quantum_grey600), PorterDuff.Mode.SRC_ATOP);
                if(v instanceof ImageButton) {
                    ((ImageButton)v).getDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.quantum_grey600), PorterDuff.Mode.SRC_IN);
                }
            }
        });

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

    private void setUpComments(){
        Query baseQuery  = dbCollection.orderBy("created", Query.Direction.DESCENDING);

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
                        showToast("An error occurred.");
                        retry();
                        break;
                }
            }
            @Override
            protected void onError(@NonNull Exception e) {
                //swipeRefreshLayout.setRefreshing(false);
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
            binding.progressBar.setVisibility(View.GONE);
        }, 300L);

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
        dbCollection.document().set(updates, SetOptions.merge()).addOnSuccessListener(o-> adapter.refresh());
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
                .setPositiveButton("Continuer", (dialogInterface, i) -> extendAction(doc))
                .setNegativeButton("Annuler", null)
                .show();
    }

}

//
//package com.example.pharmazione.ui.home;
//
//        import android.content.Context;
//        import android.content.res.TypedArray;
//        import android.graphics.Color;
//        import android.os.Bundle;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.ViewGroup;
//        import android.view.animation.AnimationUtils;
//        import android.view.animation.Interpolator;
//
//        import androidx.annotation.NonNull;
//        import androidx.annotation.Nullable;
//        import androidx.fragment.app.Fragment;
//        import androidx.lifecycle.Observer;
//        import androidx.lifecycle.ViewModelProviders;
//        import androidx.navigation.fragment.NavHostFragment;
//
//        import com.google.android.material.transition.MaterialContainerTransform;
//        import com.google.android.material.transition.MaterialSharedAxis;
//        import com.example.pharmazione.R;
//        import com.example.pharmazione.SharedViewModel;
//        import com.example.pharmazione.databinding.FragmentShowBinding;
//        import com.example.pharmazione.persistance.Document;
//
//        import org.jetbrains.annotations.NotNull;
//
//        import java.util.Objects;
//        import java.util.concurrent.TimeUnit;
//
//        import kotlin.jvm.internal.Intrinsics;
//
//        import static com.example.pharmazione.utils.Util.load;
//
//public class ShowFragment extends Fragment {
//    FragmentShowBinding binding;
//    private SharedViewModel sharedViewModel;
//
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
//
//        startPostponedEnterTransition();
//        prepareTransitions();
//
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentShowBinding.inflate(inflater, container, false);
//
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        binding.navigationIcon.setOnClickListener(o->{
//            NavHostFragment.findNavController(this).navigateUp();
//        });
//
//        sharedViewModel.getDocData().observe(getViewLifecycleOwner(), doc ->  {
//            binding.setItem(doc);
//            load(binding.imageSlider,doc.getPath());
//            load(binding.userPhoto,doc.getUserUrl());
//        });
//        startTransitions();
//    }
//
//    private void startTransitions() {
//        binding.executePendingBindings();
//        startPostponedEnterTransition();
//    }
//
//    private void prepareTransitions() {
//        postponeEnterTransition(300L, TimeUnit.MILLISECONDS);
//        Interpolator interpolator =  AnimationUtils.loadInterpolator(getContext(),android.R.interpolator.fast_out_slow_in);
//        MaterialContainerTransform mat = new MaterialContainerTransform();
//        mat.setDuration(300L);
//        mat.setDrawingViewId(R.id.showFragment);
//        mat.setInterpolator(interpolator);
//
//        setSharedElementEnterTransition(mat);
//
//        this.setEnterTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, true)
//                .setDuration(300L).setInterpolator(interpolator));
//
//        setSharedElementReturnTransition(mat);
//
//        this.setReturnTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//                .setDuration(300L).setInterpolator(interpolator));
//    }
//
//
//
//}
