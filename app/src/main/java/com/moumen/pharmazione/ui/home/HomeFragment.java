package com.moumen.pharmazione.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.moumen.pharmazione.Chat.FirestoreChatActivity;
import com.moumen.pharmazione.Chat.FirestorePagingActivity;
import com.moumen.pharmazione.FilterDialogFragment;
import com.moumen.pharmazione.Filters;
import com.moumen.pharmazione.ProfileNotifActivity;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SearchableActivity;
import com.moumen.pharmazione.databinding.EmailItemLayoutBinding;
import com.moumen.pharmazione.databinding.FragmentHomeBinding;
import com.moumen.pharmazione.logic.user.UserViewModel;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.HorizantallContent;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.ui.poster.PosterActivity;
import com.moumen.pharmazione.ui.poster.ValidatePhone;
import com.moumen.pharmazione.utils.Connectivity;
import com.moumen.pharmazione.utils.ItemClickListener;
import com.moumen.pharmazione.utils.MedClickListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_USER;
import static com.moumen.pharmazione.utils.Util.RC_SIGN_IN;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_VALIDATION;

public class HomeFragment extends Fragment implements ItemClickListener, FilterDialogFragment.FilterListener, MedClickListener<HorizantallContent> {

    private DrawerLayout mDrawer;
    private UserViewModel sharedViewModel;
    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> adapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentHomeBinding binding;
    private Query query;
    FirebaseUser users;
    private FilterDialogFragment mFilterDialog;
    private boolean isVerified;
    FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers;
    PagedList.Config config;
    Boolean neverBefore = true;
    private String TAG = "HomeFragment";

    public HomeFragment() { }

    @Override
    public void onCreate(@Nullable Bundle sss) {
        super.onCreate(sss);
        setHasOptionsMenu(true);

        sharedViewModel =  new ViewModelProvider(getActivity()).get(UserViewModel.class);

        FirebaseApp.initializeApp(getContext());
        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

//      setEnterTransition(new MaterialFadeThrough().setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
        Query dbCollection = FirebaseFirestore.getInstance().collection(PATH);
        //query = dbCollection.whereEqualTo("isVerified",true).whereEqualTo("satisfied",false).orderBy("scanned", Query.Direction.DESCENDING);
        query = dbCollection.whereEqualTo("satisfied",true).orderBy("scanned", Query.Direction.DESCENDING);
        //swipeRefreshLayout = binding.swipeContainer;

        adapter = getPaging();
//        binding.listView.setAdapter(adapter);
//        swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
        mFilterDialog = FilterDialogFragment.getInstance();
        mFilterDialog.setCallback(this);
        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        Boolean uid = getActivity().getIntent().getBooleanExtra("firstTime", false);
        if(uid && neverBefore && sss == null) {
            this.neverBefore = false;
            signIn();
        }
    }


    void updateUI(User user) {
        if( user == null) {
            binding.navView.getMenu().clear();
            binding.navView.inflateMenu(R.menu.pharmazione_menu);

            binding.scroll.setVisibility(View.GONE);
            binding.uncomplete.setVisibility(View.GONE);
            binding.text.setVisibility(View.GONE);
            binding.auth.setVisibility(View.VISIBLE);
            binding.fab.setVisibility(View.GONE);
            if(adapter != null)
                adapter.stopListening();
            if(mAuth.getCurrentUser() != null)
                AuthUI.getInstance()
                        .signOut(getContext())
                        .addOnCompleteListener(tasks -> {
                            if (tasks.isSuccessful()) {
                                getActivity().getViewModelStore().clear();
                            }
                        });
        } else if( user.getmPhoneNumber() == null || user.getmPhoneNumber().isEmpty()) {
            binding.navView.getMenu().clear();
            binding.navView.inflateMenu(R.menu.pharmazione_menu);

            binding.buttonResend
                    .setOnClickListener(o ->
                    startActivityForResult(ValidatePhone
                            .createIntent(getContext(), null),
                            START_ACTIVIY_VALIDATION));
            binding.scroll.setVisibility(View.GONE);
            binding.text.setVisibility(View.GONE);
            binding.auth.setVisibility(View.GONE);
            binding.uncomplete.setVisibility(View.VISIBLE);
            binding.fab.setVisibility(View.GONE);
        } else if (user.satisfied == null || !user.satisfied){
            binding.navView.getMenu().clear();
            binding.navView.inflateMenu(R.menu.pharmazione_menu_wait);

            binding.auth.setVisibility(View.GONE);
            binding.scroll.setVisibility(View.GONE);
            binding.uncomplete.setVisibility(View.GONE);
            binding.text.setVisibility(View.VISIBLE);
            binding.fab.setVisibility(View.GONE);
        } else {
            binding.fab.setVisibility(View.VISIBLE);
            binding.editText.setOnClickListener(o-> onFilterClicked());
            binding.filter.setOnClickListener(o->goToSearch());
            binding.scroll.setVisibility(View.VISIBLE);
            binding.uncomplete.setVisibility(View.GONE);
            binding.auth.setVisibility(View.GONE);
            adapter.startListening();
            swipeRefreshLayout = binding.swipeContainer;
            binding.listView.setAdapter(adapter);
            swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
            // Navigation Items
            binding.navView.getMenu().clear();
            binding.navView.inflateMenu(R.menu.activity_main_drawer);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == START_ACTIVIY_VALIDATION ) {
            if (resultCode == RESULT_OK) {
                User user = (User) data.getExtras().getSerializable("KEY_GOES_HERE");
                sharedViewModel.getLiveBlogData().postValue(user);
            } else {
                showDigAskPhone();
                Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection(PATH_USER).document(mAuth.getUid()).get();
                task.addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    sharedViewModel.getLiveBlogData().postValue(user);
                });
            }
        } else if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK && response != null) {
                Task<DocumentSnapshot> task =  FirebaseFirestore.getInstance().collection(PATH_USER).document(mAuth.getCurrentUser().getUid()).get();
                task.addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user.getmPhoneNumber() == null || user.getmPhoneNumber().isEmpty()) {
                        startActivityForResult(ValidatePhone.createIntent(getContext(), response), START_ACTIVIY_VALIDATION);
                    } else {
                        sharedViewModel.getLiveBlogData().postValue(user);
                    }
                });
            } else {
                if (response == null) {
                    showSandbar(getResources().getString(R.string.sign_in_cancelled));
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSandbar(getResources().getString(R.string.no_internet_connection));
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                    showSandbar(getResources().getString(R.string.account_disabled));
                    return;
                } else showSandbar(getResources().getString(R.string.unknown_error));
                Log.e("Auth Frag", "Sign-in error: ", response.getError());
            }
        }
    }

    private void showDigAskPhone() {
        new AlertDialog.Builder(getContext())
                .setMessage("Compléter votre profil")
                .setPositiveButton("Configurer", (dialogInterface, i) ->
                        startActivityForResult(ValidatePhone.createIntent(getContext(), null),START_ACTIVIY_VALIDATION))
                .setNegativeButton("Annuler", (dialogInterface, i) -> {})
                .show();
    }


    private void showSandbar(String errorMessageRes) {
        Snackbar.make(binding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        binding.swipeContainer.setRefreshing(savedInstanceState == null);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.navigation_drawer, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        startPostponedEnterTransition();
        sharedViewModel.getLiveBlogData().observe(getActivity(), this::updateUI);

        ExtendedFloatingActionButton fab = binding.fab;
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), PosterActivity.class)));
        mDrawer = binding.drawerLayout;
        binding.menuBtn.setOnClickListener(view1 -> {
            mDrawer.openDrawer(GravityCompat.START);
            getActivity().invalidateOptionsMenu();
        });

        binding.navView.setNavigationItemSelectedListener(menu -> {
            onOptionsItemSelected(menu);
            mDrawer.close();
            return true;
        });

        binding.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCREEN_STATE_OFF) {
//                    fab.extend();
//                }
//                super.onScrollStateChanged(recyclerView, newState);
//            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isExtended()) {
                    fab.shrink();
                } /*else  if (dy < -15 && !fab.isExtended())
                    fab.extend();*/
            }
        });

        //setHorizontalScroll();

        mAuth = FirebaseAuth.getInstance();
        users = mAuth.getCurrentUser();
        if(users == null) {
            binding.btn.setOnClickListener(s -> signIn());
            binding.scroll.setVisibility(View.GONE);
            binding.auth.setVisibility(View.VISIBLE);
            binding.fab.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout = binding.swipeContainer;
            binding.listView.setAdapter(adapter);
            swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
            swipeRefreshLayout.setRefreshing(savedInstanceState == null);
            users = mAuth.getCurrentUser();
            if(users == null) {
                showSandbar("Something went wrong during the authentication process. Please try signing in again.");
            }
        }

        if(!Connectivity.isConnected(getContext())) {
            showSandbar(" la connexion internet est perdue");
        }
        else if(!Connectivity.isConnectedFast(getContext())) {
            showSandbar("Votre connection internet est lente");
        }
    }

    public void setUp(Query baseQuery) {
        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(baseQuery, config, Document.class)
                .build();

        adapter.updateOptions(options);

    }


    private void showToast() {
        Toast.makeText(getContext(), "An error occurred.", Toast.LENGTH_SHORT).show();
    }


    private void onFilterClicked() {
        mFilterDialog.show(requireActivity().getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @Override
    public void onFilter(Filters filters) {
        String category = filters.getCategory();
        String city = filters.getCity();

        if (filters.hasCategory() && filters.hasCity()) {
            switch (category) {
                case "Orientation vers une pharmacie":
                    setQuery("Orientation", city);
                    break;
                case "Dons de medicaments":
                    setQuery("Don", city);
                    break;
                case "Besoin de médicament":
                    setQuery("Besoin", city);
                    break;
                default:
                    setQuery(null, city);
                    break;
            }
        }
        else if (filters.hasCategory()) {
            if(category.equals("Orientation vers une pharmacie")){
                setQuery("Orientation",null );
            }
            if(category.equals("Dons de medicaments")){
                setQuery("Don",null );
            }
            if(category.equals("Besoin de médicament")){
                setQuery("Besoin",null );
            }else
                setQuery(null,null);

        }
        else if (filters.hasCity()) {
            setQuery(null,city);
        }else {
            setQuery(null,null);
        }
        binding.textCat.setText(Html.fromHtml(filters.getSearchDescription()));
    }


    private void goToSearch(){
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        startActivityForResult(intent, 0);
    }


    @Override
    public void onClick(Document item, View sliderLayout) {


//       new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//
//       this.setEnterTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//        .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//
       this.setExitTransition( new MaterialSharedAxis(MaterialSharedAxis.X, true)
        .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//        this.setEnterTransition( new MaterialElevationScale(true)
//                .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//        this.setExitTransition( new MaterialContainerTransform()
//                .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));

        sharedViewModel.getDocData().setValue(item);

//        this.setReenterTransition(  new MaterialElevationScale( true)
//                .setDuration(duration));

//        this.setExitTransition(  new MaterialElevationScale( false)
//                .setDuration(duration));

        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_showFragment);
    }


    private void  setQuery(String opt,String location){
        if(location == null && opt == null)
            setUp(query);
//        else if(location == null)
//            setUp(query.whereEqualTo("category",opt));
//        else if (opt == null){
//            setUp(query.whereEqualTo("location",location));
        else {
            setUp(query.whereEqualTo("location",location));
        }
    }

    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> getPaging() {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                //.setLifecycleOwner(getActivity())
                .setQuery(query, config, Document.class)
                .build();

        return new FirestorePagingAdapter<Document, RecyclerView.ViewHolder>(options) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                EmailItemLayoutBinding binding = EmailItemLayoutBinding.inflate(layoutInflater, viewGroup, false);
                return new DocumentViewHolder(binding, HomeFragment.this);
            }


            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Document model) {
                DocumentViewHolder doc = (DocumentViewHolder) holder;
                doc.bindTo(model);
            }


            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADING_MORE:
                        //swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADED:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case FINISHED:{
                        swipeRefreshLayout.setRefreshing(false);
                        break;}
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        showToast();
                        //retry();
                        break;
                }
            }
            @Override
            protected void onError(@NonNull Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("Home Layout", e.getMessage(), e);
            }
        };
    }

    private void signIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.FullscreenTheme)      // Set theme
                        .setLogo(R.drawable.logo)
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    public void onClick(View view, HorizantallContent item) {

//        this.setReenterTransition(  new MaterialElevationScale( false)
//                .setDuration(duration));


//        this.setExitTransition(  new MaterialElevationScale( true)
//                .setDuration(duration));


        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_specialityFrag);
    }


    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                NavHostFragment.findNavController(this).navigate(R.id.home_to_profile);
                break;
            case R.id.menu_logout:
                signOut();
                return true;
            case R.id.menu_contact:
                nosContacter();
                return true;
            case R.id.menu_meddwak:
                deleteAccountClicked();
                break;
            case R.id.nav_gallery:
                startActivity(new Intent(getContext(), ProfileNotifActivity.class /*FirestorePagingActivity.class*/));
                break;
            case R.id.nav_slideshow:
                startActivity(new Intent(getContext(), FirestoreChatActivity.class));
                break;
            case R.id.menu_shar:
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle("Pharmazione")
                        .setText("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())
                        .startChooser();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void signOut() {
        //detachListener();
        updateUI(null);
    }

    private void deleteAccountClicked() {
        new AlertDialog.Builder(getContext())
                .setMessage("Pharmazione a pour but principal la création d’un réseau solidaire entre pharmaciens d’officine, en leur permettant d’élargir leur réseau professionnel, d’échanger des informations avec leurs confrères et d’avoir une plus grande visibilité sur le marché.")
                .setPositiveButton("J'adore", (dialogInterface, i) -> showSandbar(
                        "Merci "))
                .setNegativeButton("Sortie", null)
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    void  nosContacter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_layout, null, false);
        // Set up the input
        EditText body =  viewInflated.findViewById(R.id.bodyss);
        builder.setView(viewInflated);

        builder.setTitle("");
        // Set up the buttons
        builder.setPositiveButton("Envoyer", (dialog, which) -> {

            if( body.getText().toString().isEmpty()){
                Toast.makeText(getContext(),"le champ est vide!", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("created", FieldValue.serverTimestamp());
            updates.put("content", body.getText().toString());
            if( mAuth.getCurrentUser() != null) {
                updates.put("userID", mAuth.getCurrentUser().getUid());
                updates.put("userURL", mAuth.getCurrentUser().getPhotoUrl() == null ? "" : mAuth.getCurrentUser().getPhotoUrl().toString());
                updates.put("userName", Objects.requireNonNull(mAuth.getCurrentUser().getDisplayName()));
            }
            FirebaseFirestore.getInstance().collection("pharmazion-feedback").document().set(updates, SetOptions.merge())
                    .addOnSuccessListener(o-> Toast.makeText(getContext(),"Message envoyé", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(o-> Toast.makeText(getContext(),"Une erreur s'est produite!", Toast.LENGTH_LONG).show());
            dialog.dismiss();

        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }



//    private void setHorizontalScroll() {
//        List<HorizantallContent> list = new ArrayList<>();
//        HorizantallContent h1 = new HorizantallContent("1",null,"Find Doctors","https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fdoc.jpg?alt=media&token=5a0093eb-1400-4eee-8e66-cbbf640f375d","Diagnostic test and checkups");
//        HorizantallContent h2 = new HorizantallContent("2",null,"Find Pharmacies","https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fpharm.jpg?alt=media&token=cc589390-e242-42b3-9235-2fbb260ddc49","Check fo medicines");
//        list.add(h1);
//        list.add(h2);
//
//        HorizontalAdapter horizantallAdapter = new HorizontalAdapter(this,new DiffUtil.ItemCallback<HorizantallContent>() {
//            @Override
//            public boolean areItemsTheSame(@NonNull HorizantallContent oldItem, @NonNull HorizantallContent newItem) {
//                return oldItem.ID.equals(newItem.ID) ;
//            }
//
//            @Override
//            public boolean areContentsTheSame(@NonNull HorizantallContent oldItem, @NonNull HorizantallContent newItem) {
//                return oldItem.mDescription.equals(newItem.mDescription);
//            }
//        });
//        horizantallAdapter.submitList(list);
//        binding.alsoLikeList.setAdapter(horizantallAdapter);
//    }
}