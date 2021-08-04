package com.moumen.pharmazione.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.moumen.pharmazione.BottomNavigation;
import com.moumen.pharmazione.Chat.FirestoreChatActivity;
import com.moumen.pharmazione.Chat.FirestorePagingActivity;
import com.moumen.pharmazione.ProfileNotifActivity;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SharedViewModel;
import com.moumen.pharmazione.databinding.CardviewProfileBinding;
import com.moumen.pharmazione.databinding.FragmentNotificationsBinding;
import com.moumen.pharmazione.logic.user.UserViewModel;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.ui.home.DocumentViewHolderProfile;
import com.moumen.pharmazione.ui.home.ShowFragment;
import com.moumen.pharmazione.ui.poster.ValidatePhone;
import com.moumen.pharmazione.utils.Connectivity;
import com.moumen.pharmazione.utils.OnActivityListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

public class ProfileFragment extends Fragment {
    private static final String TAG = "SignedInActivity";

    private CoordinatorLayout scrollView;
    private Query baseQuery;
    private User user;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private Drawable icon;
    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> adapter;
    private FirebaseAuth mAuth;
    private Intent chatActivity;
    private List<AuthUI.IdpConfig> providers;

    private UserViewModel sharedViewModel;

    private CollectionReference dbCollection;
    private FragmentNotificationsBinding binding;

    android.app.AlertDialog alertDialogPhone;
    Map<String, Object> initialData ;

    private ConstraintLayout constraintLayout;

    public ProfileFragment() {}

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_scrolling, menu);
        final View v = binding.toolbar.getChildAt(0);
        icon = menu.findItem(R.id.action_settings).getIcon();

        int clr = MaterialColors.getColor(context, R.attr.colorOnSurface, Color.BLACK);
        icon.setColorFilter(clr, PorterDuff.Mode.SRC_ATOP);
        if(v instanceof ImageButton) {
            ((ImageButton)v).getDrawable().setColorFilter(clr, PorterDuff.Mode.SRC_IN);
        }
    }

    private String lowerToUpper( String hash) {
        return hash.substring(0,1).toUpperCase() + hash.substring(1).toLowerCase();
    }
    private void toggle(boolean show, View v) {
        View secondPage = v.findViewById(R.id.secondPage);
        View first = v.findViewById(R.id.firstPage);
        ViewGroup parent = v.findViewById(R.id.layout);

        Transition transition = new Slide(Gravity.LEFT);
        transition.setDuration(600L);
        transition.addTarget(R.id.secondPage);

        Transition transitionFirst = new Fade();
        transition.setDuration(300L);
        transition.addTarget(R.id.firstPage);

        TransitionManager.beginDelayedTransition(parent, transition);
        TransitionManager.beginDelayedTransition(parent, transitionFirst);
        secondPage.setVisibility(show ? View.VISIBLE : View.GONE);
        first.setVisibility(!show ? View.VISIBLE : View.GONE);

    }

    void initDialogPhone(){
        Context c = getContext();
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_phon_input, null);
        android.app.AlertDialog.Builder alertDialogBuilderUserInput = new android.app.AlertDialog.Builder(c,R.style.DialogTheme);
        alertDialogBuilderUserInput.setView(mView);

        TextView ttile = mView.findViewById(R.id.title);
        ttile.setText("Modifier votre profil");
        TextInputEditText phone = mView.findViewById(R.id.phone_edit_text);
        TextInputEditText officine = mView.findViewById(R.id.nom_officine_edit_text);
        TextInputEditText nom = mView.findViewById(R.id.nom_edit_text);
        TextInputEditText address = mView.findViewById(R.id.address_edit_text);
        AutoCompleteTextView wilaya = mView.findViewById(R.id.wilaya_edit_text);
        TextInputEditText founisseure = mView.findViewById(R.id.founisseure);
        CheckBox checkBox1 = mView.findViewById(R.id.checkbox1);
        CheckBox checkBox2 = mView.findViewById(R.id.checkbox2);
        CheckBox checkBox3 = mView.findViewById(R.id.checkbox3);
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroup);
        LinearLayout linearLayout = mView.findViewById(R.id.linearLayout);
        linearLayout.setVisibility(View.GONE);

        nom.setText(user.getmName());
        phone.setText(user.getmPhoneNumber());
        officine.setText(user.getNomOffificine());
        address.setText(user.getAddresseOfficine());
        wilaya.setText(user.getWilaya() == null ? "Vide" : user.getWilaya());
        checkBox1.setChecked(user.getConvention_cnas() == null ? false : user.getConvention_cnas());
        checkBox2.setChecked(user.getConvention_casnos() == null ? false : user.getConvention_casnos());
        checkBox3.setChecked(user.getConvention_militair() == null ? false : user.getConvention_militair());
        founisseure.setText(user.getFournisseure());

        String[] sele = new String[1];
        String[] cites = getResources().getStringArray(R.array.cities2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getActivity(), R.layout.list_item, cites);
        wilaya.setAdapter(adapter3);
        wilaya.setOnItemClickListener((parent, view, position, rowId) -> sele[0] = (String)parent.getItemAtPosition(position));

        Button save = mView.findViewById(R.id.poster);
        Button next = mView.findViewById(R.id.next);
        Button retour = mView.findViewById(R.id.retour);
        ImageButton close = mView.findViewById(R.id.close_icon);
        close.setVisibility(View.VISIBLE);
        close.setOnClickListener(l-> alertDialogPhone.dismiss());
        retour.setOnClickListener(o-> toggle(false, mView));
        initialData = new HashMap<>();

        next.setOnClickListener(o-> {
            if(phone.getText() == null || !phone.getText().toString().matches("0[567][0-9]{8,}")) {
                phone.setError("Numéro de téléphone erroné");
            }  else if(wilaya.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Veuillez vérifier les champs",Toast.LENGTH_LONG).show();
            }
            else {
                toggle(true,mView);
            }
        });

        save.setOnClickListener(o->{
            if(officine.getText().toString().isEmpty()){
                Toast.makeText(c, "Veuillez vérifier les champs",Toast.LENGTH_LONG).show();
            } else {
                save.setText("Enregistrement...");
                initialData.put("mName", nom.getText().toString());
                initialData.put("mPhoneNumber", phone.getText().toString());
                initialData.put("nomOffificine", lowerToUpper(officine.getText().toString()));
                initialData.put("addresseOfficine", lowerToUpper(address.getText().toString()));
                initialData.put("wilaya", wilaya.getText().toString());
                initialData.put("fournisseure", lowerToUpper(founisseure.getText().toString()));
                initialData.put("convention_cnas", checkBox1.isChecked());
                initialData.put("convention_casnos", checkBox2.isChecked());
                initialData.put("convention_militair", checkBox3.isChecked());
                initialData.put("type", radioGroup.getCheckedRadioButtonId() == R.id.radio_button_1 ? "titulaire" : "assistant " );

                User user = new User();
                user.userId = mAuth.getUid();
                user.setmPhotoUri(mAuth.getCurrentUser().getPhotoUrl().toString());
                user.setmEmail(mAuth.getCurrentUser().getEmail());
                user.setmName( nom.getText().toString());
                user.setmPhoneNumber(phone.getText().toString());
                user.nomOffificine =  lowerToUpper(officine.getText().toString());
                user.setAddresseOfficine( lowerToUpper(address.getText().toString()));
                user.setWilaya( wilaya.getText().toString());
                user.setFournisseure(lowerToUpper(founisseure.getText().toString()));
                user.setConvention_casnos(checkBox2.isChecked());
                user.setConvention_cnas(checkBox1.isChecked());
                user.setConvention_militair(checkBox3.isChecked());
                user.setType(radioGroup.getCheckedRadioButtonId() == R.id.radio_button_1 ? "titulaire" : "assistant " );

                FirebaseFirestore.getInstance().collection(PATH_USER)
                    .document(Objects.requireNonNull(mAuth.getUid()))
                    .set(initialData, SetOptions.merge())
                    .addOnSuccessListener(aVoid ->  sharedViewModel.getLiveBlogData().postValue(user));

                alertDialogPhone.dismiss();
            }
        });
        alertDialogPhone = alertDialogBuilderUserInput.create();
        alertDialogPhone.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        chatActivity = new Intent(getContext(), FirestoreChatActivity.class);


        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
                /*new AuthUI.IdpConfig.FacebookBuilder().build()*/);

        sharedViewModel =  new ViewModelProvider(requireActivity()).get(UserViewModel.class);
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
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.single_select_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                signOut();
                return true;
            case R.id.menu_meddwak:
                if (user != null && user.getmPhoneNumber() != null && !user.getmPhoneNumber().isEmpty())
                    initDialogPhone();
                else
                    showDigAskPhone();
                return true;
            case R.id.menu_shar: {
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle("Pharmazione")
                        .setText("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())
                        .startChooser();
            }
                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        //ViewCompat.setNestedScrollingEnabled(binding.listView, false);

        scrollView = binding.signed;
        //binding.menu.setOnClickListener(this::onClick);
        constraintLayout = binding.constraintLayout;

//        onBindNotification();
//        mListener.onBindNotification(binding.imageButton,getContext());
        String uid = getActivity().getIntent().getStringExtra("id_user");
        sharedViewModel.getLiveBlogData(uid).observe(getActivity(), doc -> {
            user = doc;
            dbCollection = FirebaseFirestore.getInstance().collection(PATH);
            baseQuery = dbCollection.orderBy("scanned", Query.Direction.DESCENDING).whereEqualTo("userID", user.getUserId());
            adapter = setUpAdapter();
            setAdapt();
            updateUI(user);
        });

        if(!isSignedIn()) {
            binding.constraintLayout.setVisibility(View.VISIBLE);
        }

        binding.btn.setOnClickListener(s -> signIn());
        binding.menu.setOnClickListener(this::onClick);
        //updateUI(user);
        if(!Connectivity.isConnected(getContext())) {
            showSandbar(" la connexion internet est perdue");
        }
        else if(!Connectivity.isConnectedFast(getContext())) {
            showSandbar("Votre connection internet est lente");
        }
        //toolBarIcon();
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void setAdapt(){
        swipeRefreshLayout = binding.swipeContainer;
        swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                System.out.println("Total Number Of Items: "+totalNumberOfItems);

                if(totalNumberOfItems == 0) {
//                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.listView.setVisibility(View.GONE);
                    binding.emptyLayout.setVisibility(View.VISIBLE);
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

    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> setUpAdapter() {
        // This configuration comes from the Paging Support Library
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                //.setLifecycleOwner(this)
                .setQuery(baseQuery, config, Document.class)
                .build();

        return new FirestorePagingAdapter<Document, RecyclerView.ViewHolder>(options) {
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
                if(swipeRefreshLayout == null) return;
                switch (state) {
                    case LOADING_INITIAL:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
//                    case LOADING_MORE:
//                        swipeRefreshLayout.setRefreshing(true);
//                        break;
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
    private void updateUI(User currentUser) {
        //Show Sign In Button Only
        if(currentUser == null) {
            if( adapter != null) {
                adapter.stopListening();
                adapter = null;
            }
            binding.constraintLayout.setVisibility(View.VISIBLE);
            binding.menu.setVisibility(View.GONE);
            binding.parameters.setVisibility(View.GONE);
            binding.imageButton.setVisibility(View.GONE);
            binding.notifications.setVisibility(View.GONE);
            binding.signed.setVisibility(View.GONE);
        } else {

            binding.imageButton.setVisibility(View.VISIBLE);
            binding.notifications.setVisibility(View.VISIBLE);
            if( mAuth.getUid() != null && currentUser.userId != null && currentUser.userId.equals(mAuth.getUid())) {
                binding.menu.setVisibility(View.VISIBLE);
                binding.parameters.setVisibility(View.VISIBLE);
                binding.imageButton.setOnClickListener( e -> startActivity(new Intent(getContext(), FirestorePagingActivity.class)));
            } else {
                binding.menu.setVisibility(View.GONE);
                binding.parameters.setVisibility(View.GONE);
                binding.imageButton.setOnClickListener( e -> {
                    chatActivity.putExtra("chatWith",currentUser.userId);
                    chatActivity.putExtra("chatWithName",currentUser.getmName());
                    chatActivity.putExtra("chatWithUrl",currentUser.getmPhotoUri());
                    startActivity(chatActivity);
                });
            }

            scrollView.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.GONE);
            populateProfile();

            if(adapter == null){
                if( dbCollection == null) {
                    dbCollection = FirebaseFirestore.getInstance().collection(PATH);
                }
                if(user.getUserId() == null || user.getUserId().isEmpty()) {
                    showSandbar("Il y a un problem!");
                } else {
                    baseQuery = dbCollection.orderBy("scanned", Query.Direction.DESCENDING).whereEqualTo("userID",user.getUserId());
                    adapter = setUpAdapter();
                    setAdapt();
                }
            } else {
                adapter.startListening();
            }
        }
    }

    private void populateProfile() {
        binding.setItem(user);
    }

    private void showDigAskPhone() {
        new AlertDialog.Builder(context)
                .setMessage("Compléter votre profil")
                .setPositiveButton("Configurer", (dialogInterface, i) ->
                    startActivityForResult(ValidatePhone.createIntent(context, null),START_ACTIVIY_VALIDATION))
                .setNegativeButton("Annuler", (dialogInterface, i) -> {})
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
           case START_ACTIVIY_VALIDATION: {
                if (resultCode == RESULT_OK) {
                    adapter = null;
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
               break;
           }
            case RC_SIGN_IN: {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (resultCode == RESULT_OK && response != null) {
                    Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection(PATH_USER).document(mAuth.getUid()).get();
                    task.addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user.getmPhoneNumber() != null && !user.getmPhoneNumber().isEmpty() ) {
                            sharedViewModel.getLiveBlogData().postValue(user);
                        } else {
                            startActivityForResult(ValidatePhone.createIntent(context, response), START_ACTIVIY_VALIDATION);
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
                        .setLogo(R.drawable.logo)
                        .build(),
                RC_SIGN_IN);
    }

    private void showSandbar(String errorMessageRes) {
        Snackbar.make(binding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void signOut() {
        //detachListener();
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUI(null);
                        getActivity().getViewModelStore().clear();
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        showSandbar(getResources().getString(R.string.sign_out_failed));
                    }
                });
        mAuth.signOut();
    }

    private void deleteAccountClicked() {
        new AlertDialog.Builder(context)
                .setMessage("Pharmazione a pour but principal la création d’un réseau solidaire entre pharmaciens d’officine, en leur permettant d’élargir leur réseau professionnel, d’échanger des informations avec leurs confrères et d’avoir une plus grande visibilité sur le marché.")
                .setPositiveButton("J'adore", (dialogInterface, i) -> showSandbar(
                        "Merci "))
                .setNegativeButton("Sortie", null)
                .show();
    }

//    private void nosContacter() {
//        TextView showText = new TextView(context);
//        showText.setText(fromHtml(getSearchDescription()));
//        showText.setPadding(30,16,16,16);
//        showText.setTextColor(Color.BLACK);
//        showText.setTextIsSelectable(true);
//
//        new AlertDialog.Builder(context)
//                .setView(showText)
//                .setTitle("Contactez-nous")
//                // .setMessage(fromHtml(getSearchDescription()))
//                .setPositiveButton("J'adore", (dialogInterface, i) -> showSandbar(
//                        "Merci "))
//                .setNegativeButton("Sortie", null)
//                .show();
//    }

    void  nosContacter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            updates.put("userID", user.getUserId());
            updates.put("userURL", user.getmPhotoUri() == null ? "" : user.getmPhotoUri());
            updates.put("userName", Objects.requireNonNull(user.getmName()));
            FirebaseFirestore.getInstance().collection("pharmazion-feedback").document().set(updates, SetOptions.merge())
                    .addOnSuccessListener(o-> Toast.makeText(getContext(),"Message envoyé", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(o-> Toast.makeText(getContext(),"Une erreur s'est produite!", Toast.LENGTH_LONG).show());
            dialog.dismiss();

        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String getSearchDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("<b>Pharmazione</b> est toujours à votre écoute.");
        desc.append("<br>Si vous rencontrez des difficultés merci de nous contacter");
        desc.append("<ul>    <li> Adresse mail : <a href=\"mailto:contact@pharmazione.com\">contact@pharmazione.com</a> </li>");
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

//        this.setReenterTransition(  new MaterialElevationScale( true)
//                .setDuration(duration));
//
//        this.setExitTransition(  new MaterialElevationScale( false)
//                .setDuration(duration));

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(sliderLayout, sliderLayout.getTransitionName())
                .build();

        if(isAdded()) {
            if(getActivity().getClass().getSimpleName().equals("BottomNavigation")){
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_notifications_to_showFragment,
                        null,
                        null,
                        extras);
            }else {
                Fragment fragment = new ShowFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(android.R.id.content, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
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