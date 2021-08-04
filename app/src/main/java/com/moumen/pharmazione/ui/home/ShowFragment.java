package com.moumen.pharmazione.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SharedViewModel;
import com.moumen.pharmazione.ShowProfileActiv;
import com.moumen.pharmazione.databinding.FragmentShowBinding;
import com.moumen.pharmazione.logic.user.UserViewModel;
import com.moumen.pharmazione.persistance.Comment;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.Notification;
import com.moumen.pharmazione.persistance.User;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.moumen.pharmazione.utils.Util.EMPTY_IMAGE;
import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_NOTIF;
import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class ShowFragment extends Fragment {
    private FragmentShowBinding binding;
    private UserViewModel sharedViewModel;
    private long duration;
    private FirestoreRecyclerAdapter<Comment, RecyclerView.ViewHolder> adapter;
    private CollectionReference dbCollection;
    private FirebaseAuth mAuth;
    private Drawable icon;
    private FirebaseFirestore db;
    private Context c;
    private String token;
    private String documentID;
    private String userID;
    private User user;


    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        sharedViewModel =  new ViewModelProvider(getActivity()).get(UserViewModel.class);
        //prepareTransitions();
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
            userID = doc.userID;
            Task<DocumentSnapshot> task =  db.collection(PATH_USER).document(doc.userID).get();
            task.addOnSuccessListener(documentSnapshot -> {
                user = documentSnapshot.toObject(User.class);
                token = user.getToken();
            });

            documentID = doc.documentID;

            binding.senderProfileImageView.setOnClickListener(o->{
//                 FirebaseFirestore.getInstance().collection(PATH_USER).document(doc.userID).get().addOnSuccessListener(documentSnapshot -> {
//                    User user = documentSnapshot.toObject(User.class);
//                    sharedViewModel.getLiveBlogData().postValue(user);
//                     NavHostFragment.findNavController(this).navigate(R.id.showFragment_to_ProfileFragment);
//                 });
                Intent mIntent = new Intent(c, ShowProfileActiv.class);
                mIntent.putExtra("id_user", doc.userID);
                startActivity(mIntent);
            });

            if(doc.scanned != null) {
                String niceDateStr = DateUtils.getRelativeTimeSpanString(doc.scanned.toDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
                binding.setTimeAgo(niceDateStr);
            } else {
                binding.setTimeAgo("-");
            }

            binding.setEmail(doc);
            binding.setUrlEmpty(EMPTY_IMAGE);
            if(doc.medicines != null && doc.medicines.size() > 0){
                binding.recipientScrollView.setVisibility(View.VISIBLE);
                for (Map<String,String> map : doc.medicines) {
                    String nom = map.get("medName");
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.compose_recipient_chip,binding.recipientChipGroup,false);
                    chip.setText(nom);
                    binding.recipientChipGroup.addView(chip);
                }
            }

            binding.itemSendButtonId.setOnClickListener(o->sendMessage());
            dbCollection = db.collection(PATH)
                    .document(doc.documentID)
                    .collection("message");
            setUpComments(dbCollection.orderBy("created", Query.Direction.DESCENDING).limit(30));
        });

        binding.navigationIcon.setOnClickListener(o-> getActivity().onBackPressed());
        toolBarIcon();
        //startTransitions();
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

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {
//                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.commentReceylerView.setVisibility(View.GONE);
                    binding.emptyLayout.setVisibility(View.VISIBLE);
                }else {
                    binding.emptyLayout.setVisibility(View.GONE);
//                    binding.progressBar.setVisibility(View.GONE);
                    binding.commentReceylerView.setVisibility(View.VISIBLE);
                }
            }
        });

        if(adapter.getItemCount() == 0) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.commentReceylerView.setVisibility(View.GONE);
//            binding.progressBar.setVisibility(View.GONE);
        }else {
//            binding.progressBar.setVisibility(View.GONE);
            binding.emptyLayout.setVisibility(View.GONE);
            binding.commentReceylerView.setVisibility(View.VISIBLE);
        }

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
        if(adapter != null)
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

        sharedViewModel.getLiveBlogData().observe(getActivity(), users -> {
            if (users != null) {
                String id = db.collection(PATH).document().getId();

                String content = Objects.requireNonNull(binding.editText.getText()).toString();
                if(content.isEmpty()) {
                    showToast("vide!");
                    return;
                }

                if(userID != null && mAuth.getUid() != null && !mAuth.getUid().equals(userID) ) {

                    Notification notif = new Notification();
                    HashMap<String, Object> message = new HashMap<>();
                    HashMap<String, Object> notification = new HashMap<>();
                    HashMap<String, Object> android = new HashMap<>();
                    HashMap<String, String> click_action = new HashMap<>();
                    HashMap<String, String> data = new HashMap<>();

                    data.put("id_publication", documentID );

                    notif.setContent(content);
                    notif.publicationId = documentID;
                    notif.commentId = id;
                    notif.userID = user.getUid();
                    notif.userURL = user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString();
                    notif.userName =  users.getmName();
                    notif.setTitle(users.mName +" a commenté votre publication");
                    notif.setToUser(userID);
                    notif.setSeen(false);

                    notification.put("body", content );
                    notification.put("title", users.getmName() +" a commenté votre publication" );
                    notification.put("click_action","OPEN_ACTIVITY_1");

                    click_action.put("click_action", "OPEN_ACTIVITY_1");
                    click_action.put("color", "#7e55c3");

                    android.put("notification",click_action);

                    message.put("to", token);
                    message.put("notification", notification);
                    message.put("android", android);
                    message.put("data", data);

                    JsonObjectRequest logInAPIRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send",
                            new JSONObject(message),
                            response -> {
                                Log.d("Response", response.toString());
                                //Toast.makeText(getContext(), "" + response.toString(), Toast.LENGTH_SHORT).show();
                            }, error -> {
                            VolleyLog.d("Error", "Error: " + error.getMessage());
                            //Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }){
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Accept","application/json");
                            headers.put("Content-Type","application/json");
                            headers.put("Authorization", "key=AAAAepkVf_I:APA91bFsjCWEw2WnWmr97wFvlCb75cjC5Ecu0RFUY03paTo89L781PrzPomVzTAtnAl-2MV6qpsKMJVFwoclj6vcc2lAOP9nDHBe2fVKFObhNtovIH9WDtBsdpvtW_DJkghBgcoIqNOn");
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                    requestQueue.add(logInAPIRequest);
                    db.collection(PATH_NOTIF).document().set(notif);
                    //db.collection(PATH_NOTIF).document(userID).collection("notification").document().set(notif);

                }
                binding.editText.setText("");
                Map<String, Object> updates = new HashMap<>();
                updates.put("created", FieldValue.serverTimestamp());
                updates.put("content", content);
                updates.put("userID", user.getUid());
                updates.put("userURL", user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
                updates.put("userName", users.getmName());
                dbCollection.document(id).set(updates, SetOptions.merge());
                System.out.println("In Message end");
            }
        });
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