package com.example.pharmazione.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.FragmentNavigator;
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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.pharmazione.ProfileNotifActivity;
import com.example.pharmazione.R;
import com.example.pharmazione.SharedViewModel;
import com.example.pharmazione.databinding.CardviewProfileBinding;
import com.example.pharmazione.databinding.FragmentNotificationsBinding;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.ui.home.DocumentViewHolderProfile;
import com.example.pharmazione.ui.poster.ValidatePhone;
import com.example.pharmazione.utils.OnActivityListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.example.pharmazione.utils.Util.RC_SIGN_IN;

public class ProfileFragment extends Fragment {
    private static final String TAG = "SignedInActivity";

    private LinearLayout scrollView;
    private Query baseQuery;
    private FirebaseUser user;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> adapter;
    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers;

    private SharedViewModel sharedViewModel;
    private long duration;

    private CollectionReference dbCollection;
    private FragmentNotificationsBinding binding;

    private ConstraintLayout constraintLayout;

    private OnActivityListener mListener;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnActivityListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        sharedViewModel =  new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        //FireBase init
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user == null) return;

        // Queries
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        dbCollection = db.collection("med-dwa");
        baseQuery = dbCollection.orderBy("scanned", Query.Direction.DESCENDING).whereEqualTo("userID",user.getUid());
        setUpAdapter();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.single_select_menu, menu);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    public void onClick(View v){
        PopupMenu popup = new PopupMenu(getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.single_select_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    private void addListener(){
        mListener.onAtachListener();
    }

    private void detachListener(){
        mListener.onDetachListener();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                signOut();
                return true;
            case R.id.menu_contact:
                nosContacter();
                return true;
            case R.id.menu_meddwak:
                deleteAccountClicked();
                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();


        scrollView = binding.signed;
        binding.menu.setOnClickListener(this::onClick);
        constraintLayout =binding.constraintLayout;
        binding.imageButton.setOnClickListener( e -> goToNotifications());
        onBindNotification();
        mListener.onBindNotification(binding.imageButton,getContext());


        Button mSignIn = binding.btn;
        if(adapter != null){
            setAdapt();
        }
//        swipeRefreshLayout = binding.swipeContainer;
//        swipeRefreshLayout.setRefreshing(false);

        mSignIn.setOnClickListener(s -> signIn());
        updateUI(mAuth.getCurrentUser());
    }

    private void goToNotifications(){
        if(mAuth.getCurrentUser() == null){
            return;
        }
        Intent intent = new Intent(getContext(), ProfileNotifActivity.class);
        startActivity(intent);
    }

    private void setAdapt(){
        swipeRefreshLayout = binding.swipeContainer;
        swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {
//                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.listView.setVisibility(View.VISIBLE);
                    binding.emptyLayout.setVisibility(View.GONE);
                }else {
                    binding.emptyLayout.setVisibility(View.GONE);
//                    binding.progressBar.setVisibility(View.GONE);
                    binding.listView.setVisibility(View.VISIBLE);
                }
            }
        });
        if(adapter.getItemCount() == 0) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.listView.setVisibility(View.GONE);
//            binding.progressBar.setVisibility(View.GONE);
        }else {
//            binding.progressBar.setVisibility(View.GONE);
            binding.emptyLayout.setVisibility(View.GONE);
            binding.listView.setVisibility(View.VISIBLE);
        }

        binding.listView.setAdapter(adapter);
    }

    private void setUpAdapter() {
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
                CardviewProfileBinding binding = CardviewProfileBinding.inflate(layoutInflater, viewGroup, false);
                return new DocumentViewHolderProfile(binding, (item, sliderLayout) -> onClick(item,sliderLayout));
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                            int position,
                                            @NonNull Document model) {

                DocumentViewHolderProfile doc = (DocumentViewHolderProfile) holder;
                doc.bindToProfile(model);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADING_MORE:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADED:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case FINISHED:{
                        swipeRefreshLayout.setRefreshing(false);
                        break;}
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(true);
                        retry();
                        break;
                }
            }

            @Override
            protected void onError(@NonNull Exception e) {
                Log.e("Home Layout", e.getMessage(), e);
            }
        };
    }

    @SuppressLint("RestrictedApi")
    private void updateUI(FirebaseUser currentUser) {
        //Show Sign In Button Only
        if(currentUser == null) {
            scrollView.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        }else{
            adapter = null;
            user = currentUser;
            scrollView.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.GONE);
            populateProfile();
            if(adapter == null){
                if( dbCollection == null) {
                    dbCollection = FirebaseFirestore.getInstance().collection("med-dwa");
                }
                baseQuery = dbCollection.orderBy("scanned", Query.Direction.DESCENDING).whereEqualTo("userID",user.getUid());
                setUpAdapter();
                setAdapt();
            }
        }
    }

    private void populateProfile() {
        binding.setItem(user);
    }

    private void showDigAskPhone() {
        new AlertDialog.Builder(context)
                .setMessage("Le numéro de téléphone est obligatoire")
                .setPositiveButton("Ajouter", (dialogInterface, i) ->
                    startActivityForResult(ValidatePhone.createIntent(context, null),521))
                .setNegativeButton("Annuler", (dialogInterface, i) -> updateUI(mAuth.getCurrentUser()))
                .show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
           case 521: {
                if (resultCode == RESULT_OK) {
                    adapter = null;
                    updateUI(mAuth.getCurrentUser());
                } else {
                    showDigAskPhone();
                }
               break;
           }
            case RC_SIGN_IN: {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (resultCode == RESULT_OK && response != null) {
                    addListener();
                    if (Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber() != null &&
                            !mAuth.getCurrentUser().getPhoneNumber().trim().isEmpty())
                        updateUI(mAuth.getCurrentUser());
                    else
                        startActivityForResult(ValidatePhone.createIntent(context, response), 521);
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
                break;
            }
        }
    }

    private void signIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.FullscreenTheme)      // Set theme
                        .setLogo(R.drawable.start_logo)
                        .build(),
                RC_SIGN_IN);
    }

    private void showSandbar(String errorMessageRes) {
        Snackbar.make(binding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void signOut() {
        detachListener();
        adapter.stopListening();
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUI(null);
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        showSandbar(getResources().getString(R.string.sign_out_failed));
                    }
                });
    }

    private void deleteAccountClicked() {
        new AlertDialog.Builder(context)
                .setMessage("MedDwak est la première plateforme algérienne gratuite de mise en relation dans le domaine médical")
                .setPositiveButton("J'adore", (dialogInterface, i) -> showSandbar(
                        "Merci "))
                .setNegativeButton("Sortie", null)
                .show();
    }

    private void nosContacter() {
        TextView showText = new TextView(context);
        showText.setText(fromHtml(getSearchDescription()));
        showText.setPadding(30,16,16,16);
        showText.setTextColor(Color.BLACK);
        showText.setTextIsSelectable(true);

        new AlertDialog.Builder(context)
                .setView(showText)
                .setTitle("Contactez-nous")
                // .setMessage(fromHtml(getSearchDescription()))
                .setPositiveButton("J'adore", (dialogInterface, i) -> showSandbar(
                        "Merci "))
                .setNegativeButton("Sortie", null)
                .show();
    }

    private String getSearchDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("<b>MedDwak</b> est toujours à votre écoute.");
        desc.append("<br>Si vous rencontrez des difficultés merci de nous contacter");
        desc.append("<ul>    <li> Adresse mail : <a href=\"mailto:info@meddwak.com\">info@meddwak.com</a> </li>");
        desc.append("    <li> Téléphone : <a href=\"tel:+213555240058\">+213 555 240 058</a> </li></ul>");
        desc.append("<i>n’hésitez pas à nous envoyer vos feedback.</i>");
        return desc.toString();
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public void onClick(Document item, View sliderLayout) {
        sharedViewModel.getDocData().setValue(item);

        this.setReenterTransition(  new MaterialElevationScale( true)
                .setDuration(duration));

        this.setExitTransition(  new MaterialElevationScale( false)
                .setDuration(duration));



        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(sliderLayout, sliderLayout.getTransitionName())
                .build();

        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_notifications_to_showFragment,
                null,
                null,
                extras);
    }


    private void onBindNotification() {
        binding.imageButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void onGlobalLayout() {
                BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
                badgeDrawable.setVerticalOffset(20);
                badgeDrawable.setHorizontalOffset(15);
                badgeDrawable.setBadgeTextColor(Color.WHITE);
                badgeDrawable.setVisible(true);
                BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.imageButton, getActivity().findViewById(R.id.layout));
                binding.imageButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
}