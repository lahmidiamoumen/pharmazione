package com.moumen.pharmazione.ui.poster;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SearchableMedecinActivity;
import com.moumen.pharmazione.databinding.ActivityPosterBinding;
import com.moumen.pharmazione.databinding.ComposeRecipientChipBinding;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.utils.ClickListener;
import com.moumen.pharmazione.utils.MediaLoader;
import com.moumen.pharmazione.utils.PermissionUtil;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;
import com.yanzhenjie.album.widget.divider.Divider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import id.zelory.compressor.Compressor;

import static com.moumen.pharmazione.utils.Util.RC_SIGN_IN;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_BESOIN;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_DON;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_VALIDATION;
import static com.moumen.pharmazione.utils.Util.load;


public class PosterActivity extends AppCompatActivity implements View.OnClickListener, ClickListener {

    private static final String PATH = "med-dwa-pharmazion";
    AlertDialog alertDialogAndroid;
    
    FirebaseStorage storage;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Document document;
    String mCurrentPhotoPath;
    private List<AuthUI.IdpConfig> providers;
    private AlbumAdapter mAdapter;

    private final Context c = this;
    ArrayList<Map<String, String>> history = new ArrayList<>();
    View parentLayout;
    TextView dialogText = null;
    private ArrayList<AlbumFile> mAlbumFiles;


    ActivityPosterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityPosterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PermissionUtil.ask(this);
        mAuth = FirebaseAuth.getInstance();
        document = new Document();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()/*,
                new AuthUI.IdpConfig.FacebookBuilder().build()*/);

        parentLayout = findViewById(android.R.id.content);

        //String[] cites = getResources().getStringArray(R.array.cities2);

        //ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, cites);

//        binding.wilaya.setAdapter(adapter3);
//
//        binding.wilaya.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                sele[0] = parent.getItemAtPosition(position).toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) { }
//        });

        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        Divider divider = new Api21ItemDivider(Color.TRANSPARENT, 10, 10);
        binding.recyclerView.addItemDecoration(divider);

        mAdapter = new AlbumAdapter(this,this, (view, position) -> previewImage(position), 1);
        binding.recyclerView.setAdapter(mAdapter);

        binding.takeImagesOreden.setOnClickListener(o-> selectImage());
        binding.switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                binding.recipientScrollView.setVisibility(View.VISIBLE);
                binding.recipientAddIcon.setVisibility(View.VISIBLE);
                binding.recipientDivider5.setVisibility(View.VISIBLE);
            }else {
                binding.recipientScrollView.setVisibility(View.GONE);
                binding.recipientAddIcon.setVisibility(View.GONE);
                binding.recipientDivider5.setVisibility(View.GONE);
            }
        });

        binding.sendIcon.setOnClickListener(this);
        binding.recipientAddIcon.setOnClickListener(o->openSearch());
        binding.closeIcon.setOnClickListener(v-> finish());
    }

    private void selectImage() {
        Album.image(this)
                .multipleChoice()
                .camera(true)
                .columnCount(3)
                .selectCount(8)
                .checkedList(mAlbumFiles)
                .widget(
                        Widget.newLightBuilder(this)
                                .statusBarColor(Color.WHITE)
                                .toolBarColor(Color.WHITE)
//                                .mediaItemCheckSelector(Color.BLUE, Color.GREEN)
//                                .bucketItemCheckSelector(Color.RED, Color.YELLOW)
                                .buttonStyle(
                                        Widget.ButtonStyle.newLightBuilder(this)
                                                .setButtonSelector(Color.WHITE, Color.WHITE)
                                                .build()
                                )
                                .build()
                )
                .onResult( result -> {
                    mAlbumFiles = result;
                    mAdapter.notifyDataSetChanged(mAlbumFiles);
                    binding.recyclerView.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                    binding.linearLayout.setVisibility(result.size() > 0 ? View.GONE : View.VISIBLE);
                })
                .onCancel( result -> showSandbar("selection annuler"))
                .start();
    }

    private void previewImage(int position) {
        if (mAlbumFiles == null || mAlbumFiles.size() == 0) {
            showSandbar("No Selection");
        } else {
            Album.galleryAlbum(this)
                .checkable(true)
                .checkedList(mAlbumFiles)
                .currentPosition(position)
                .widget(
                        Widget.newDarkBuilder(this)
                            .title("Choisissez entre images")
                            .build()
                )
                .onResult(result -> {
                    mAlbumFiles = result;
                    mAdapter.notifyDataSetChanged(mAlbumFiles);
                    binding.recyclerView.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                    binding.linearLayout.setVisibility( result.size() > 0 ? View.GONE : View.VISIBLE);
                })
                .start();
        }
    }


    public void openSearch(){
        Intent i = new Intent(this, SearchableMedecinActivity.class);
        startActivityForResult(i, START_ACTIVIY_BESOIN);
    }


    private boolean checkDon(){
         if (getValue(binding.titre).isEmpty()){
            binding.titre.setError("Le titre est vide!");
            return false;
        }
        if (getValue(binding.body).isEmpty()){
            binding.body.setError("Le sujet est vide!");
            return false;
        }
//        document.setLocation(sele[0]);
        document.setTitle(getValue(binding.titre));
        document.setBody(getValue(binding.body));

        return true;
    }


    String getValue(EditText tx){
        return tx.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == START_ACTIVIY_BESOIN){
                if(resultCode == RESULT_OK) {
                    System.out.println("In chiups");
                    Map<String, String> map = new HashMap<>();
                    String medId = data.getStringExtra("id");
                    String nom = data.getStringExtra("name");
                    map.put("medId", medId);
                    map.put("medName", nom);
                    history.add(map);

                    Chip chip =(Chip)getLayoutInflater().inflate(R.layout.compose_recipient_chip,binding.recipientChipGroup,false);
                    chip.setText(nom);
                    chip.setCloseIconVisible(true);
                    chip.setOnCloseIconClickListener(o-> {
                        history.remove(map);
                        binding.recipientChipGroup.removeView(chip);

                    });
                    binding.recipientChipGroup.addView(chip);
                }
        }
        else if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK && response != null) {
                if (Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().trim().isEmpty())
                    send2Upload();
                else
                    startActivityForResult(ValidatePhone.createIntent(this, response), START_ACTIVIY_VALIDATION);
            } else {
                if (response == null) {
                    showSandbar(R.string.sign_in_cancelled);
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSandbar(R.string.no_internet_connection);
                    return;
                } else if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                    showSandbar(R.string.account_disabled);
                    return;
                } else showSandbar(R.string.unknown_error);
                Log.e("Auth Frag", "Sign-in error: ", response.getError());
            }
        } else {
            if (resultCode == RESULT_CANCELED)
                showSandbar("Operation annuler");
        }
    }


    private File getCompressTaken (String uriPath) {
        File file = null;
        try {
            file = new Compressor(this).compressToFile(new File(uriPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
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

    void upload()
  {

      dialog("");
      String id = db.collection(PATH).document().getId();
      Task<DocumentSnapshot> task =  db.collection("med-dwa-users").document(mAuth.getUid()).get();
      document.setUserID(mAuth.getUid());
      document.setUserName(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
      document.setUserUrl(Objects.requireNonNull(mAuth.getCurrentUser().getPhotoUrl()).toString());
      document.setToChange(binding.switcher.isChecked());
      document.setMedicines(history);
      document.setDocumentID(id);
      if(mAlbumFiles != null && mAlbumFiles.size() > 0){
          task.addOnSuccessListener(documentSnapshot -> {
              User user = documentSnapshot.toObject(User.class);
              assert user != null;
              if(user.getSatisfied() == null || !user.getSatisfied()) {
                  showEndDig("Votre compte est n'est pas vérifié");
                  return;
              }
              document.setLocation(user.getWilaya());
              document.setToken(user.getToken());
              document.setSatisfied(user.getSatisfied());
              db.collection(PATH)
                      .document(id)
                      .set(document)
                      .addOnSuccessListener(s-> uploadImage(id,  new ArrayList<>()));
          });

      }else {
          task.addOnSuccessListener(documentSnapshot -> {
              User user = documentSnapshot.toObject(User.class);
              assert user != null;
              if(user.getSatisfied() == null || !user.getSatisfied()) {
                  showEndDig("Votre compte est n'est pas vérifié");
                  return;
              }
              document.setLocation(user.getWilaya());
              document.setSatisfied(user.getSatisfied());
              db.collection(PATH)
                      .document(id)
                      .set(document)
                      .addOnSuccessListener(aVoid2 -> dialog("don"))
                      .addOnFailureListener(e -> dialog("Upload suspendu"));
          });
      }
  }



   private void uploadImage(String id, List<String> urlsDownloded){

       StorageReference storageRef = storage.getReference();
       // Create the file metadata
       StorageMetadata metadata = new StorageMetadata.Builder()
               .setContentType("image/jpeg")
               .build();

       AlbumFile image = mAlbumFiles.get(urlsDownloded.size());
       Uri file = Uri.fromFile(getCompressTaken(image.getPath()));
       StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment());
       UploadTask uploadTask = riversRef.putFile(file, metadata);

       uploadTask.continueWithTask(task -> {
           if (!task.isSuccessful()) {
               throw Objects.requireNonNull(task.getException());
           }
           return riversRef.getDownloadUrl();
       }).addOnSuccessListener(uri -> {

           urlsDownloded.add(uri.toString());
           if(urlsDownloded.size() == mAlbumFiles.size()){
               db.collection(PATH).document(id).update("path", urlsDownloded).addOnSuccessListener(s->{
                   alertDialogAndroid.dismiss();
                   showEndDig("Upload terminé");
               });
           }else {
               uploadImage(id, urlsDownloded);
           }
       } );

       int finalI = urlsDownloded.size() + 1;
       uploadTask
        .addOnProgressListener(taskSnapshot -> {
           double progress = (( 100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
           dialog("Chargement photo  "+ finalI +" " +((int) progress )+"%");
            })
        .addOnPausedListener(taskSnapshot -> dialog("Upload suspendu"));

   }
   

    void showSandbar(String text){
        Snackbar.make(parentLayout,text,Snackbar.LENGTH_SHORT).show();
    }

    private void showSandbar(@StringRes int errorMessageRes) {
        Snackbar.make(parentLayout, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    void send2Upload(){
        if (checkDon()) upload();
    }

    private void showDig() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Login to continue")
                .setPositiveButton("Log In", (dialogInterface, i) -> signIn())
                .setNegativeButton("No", null)
                .show();
    }
    private void showDigAskPhone() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Le numéro de téléphone est obligatoire")
                .setPositiveButton("Ajouter", (dialogInterface, i) -> startActivityForResult(ValidatePhone.createIntent(this, null),START_ACTIVIY_VALIDATION))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEndDig(String s) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(s)
                .setPositiveButton("Fermer", (dialogInterface, i) -> finish())
                .show();
    }

    public void dialog(String text ){
        if(alertDialogAndroid != null){
            if(text.equals("don")){
                alertDialogAndroid.dismiss();
                showEndDig("Upload terminé");
            }else if(text.equals("upload suspendu")) {
                alertDialogAndroid.dismiss();
                showEndDig("upload suspendu");
            }else {
                dialogText.setText(text);
            }
        }else {
            initDialog();
            alertDialogAndroid.show();
        }
    }

    void initDialog(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_upload_status, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
        alertDialogBuilderUserInput.setView(mView);
        dialogText = mView.findViewById(R.id.dialog_text);
        alertDialogAndroid = alertDialogBuilderUserInput.create();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    @Override
    public void onClick(View v) {
        if(mAuth.getCurrentUser() == null)
            showDig();
        else {
            if(mAuth.getCurrentUser().getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().isEmpty())
                send2Upload();
            else
                showDigAskPhone();
        }
    }

    @Override
    public void onClick() {
        selectImage();
    }
}
