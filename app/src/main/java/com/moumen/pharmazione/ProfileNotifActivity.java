package com.moumen.pharmazione;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.Query;
import com.moumen.pharmazione.databinding.ActivityNotificationBinding;
import com.moumen.pharmazione.persistance.DonBesoin;
import com.moumen.pharmazione.persistance.Notification;
import com.moumen.pharmazione.utils.MedClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moumen.pharmazione.utils.Util.PATH_NOTIF;


public class ProfileNotifActivity extends AppCompatActivity implements MedClickListener<Notification>, View.OnClickListener {


    ActivityNotificationBinding binding;
    ProfileNotifAdapter profileNotifAdapter;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        getIntent().getExtras();

        setContentView(view);
        setHorizontalScroll();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        db.collection(PATH_NOTIF)
                .whereEqualTo("toUser",mAuth.getUid())
                .orderBy("created",Query.Direction.DESCENDING)
                .limit(40)
                .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            QuerySnapshot snapshots = task.getResult();
                            assert snapshots != null;
                            if (snapshots.size() == 0) {
                                binding.rwSearch.setVisibility(View.GONE);
                                binding.progressBar.setVisibility(View.GONE);
                                binding.emptySearchList.setVisibility(View.VISIBLE);
                                return;
                            }

                            binding.emptySearchList.setVisibility(View.GONE);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.rwSearch.setVisibility(View.VISIBLE);

                            List<Notification> types = snapshots.toObjects(Notification.class);
                            if (profileNotifAdapter != null && !types.isEmpty()) {
                                profileNotifAdapter.submitList(types);
                            }

                            for (DocumentSnapshot snap : snapshots.getDocuments()) {
                                DonBesoin donBesoin = snap.toObject(DonBesoin.class);
                                assert donBesoin != null;
                                if( donBesoin.seen == null || !donBesoin.seen){
                                    String uid = snap.getId();
                                    Map<String, Boolean> map = new HashMap<>();
                                    map.put("seen", true);
                                    db.collection(PATH_NOTIF).document(uid).set(map, SetOptions.merge());
                                }
                            }
                        }else {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.rwSearch.setVisibility(View.GONE);
                            binding.emptySearchList.setVisibility(View.VISIBLE);
                        }
                });
    }

    private void setHorizontalScroll() {

        profileNotifAdapter = new ProfileNotifAdapter(this,new DiffUtil.ItemCallback<Notification>() {
            @Override
            public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                return oldItem.content.equals(newItem.content) ;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                return oldItem.toUser.equals(newItem.toUser);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rwSearch.setLayoutManager(linearLayoutManager);
        binding.rwSearch.setAdapter(profileNotifAdapter);
    }


    @Override
    public void onClick(View view, Notification phone) {
//      Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.donneur_phone));
//      startActivity(intent);
        if( phone.publicationId == null) return;

        Intent intent = new Intent(this, NotifComment.class);
        intent.putExtra("id_publication", phone.publicationId);
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {
        finish();
    }
}
