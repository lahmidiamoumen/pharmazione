package com.moumen.pharmazione.ui.poster;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SearchableMedecinActivity;
import com.moumen.pharmazione.databinding.ActivityPosterBinding;
import com.moumen.pharmazione.logic.user.UserViewModel;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.Notification;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.utils.ClickListener;
import com.moumen.pharmazione.utils.Connectivity;
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
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;
import com.yanzhenjie.album.widget.divider.Divider;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_NOTIF;
import static com.moumen.pharmazione.utils.Util.PATH_USER;
import static com.moumen.pharmazione.utils.Util.RC_SIGN_IN;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_BESOIN;
import static com.moumen.pharmazione.utils.Util.START_ACTIVIY_VALIDATION;
import static com.moumen.pharmazione.utils.Util.load;


public class PosterActivity extends AppCompatActivity implements View.OnClickListener, ClickListener {

    AlertDialog alertDialogAndroid;
    
    FirebaseStorage storage;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    User users;
    Document document;
    String mCurrentPhotoPath;
    private List<AuthUI.IdpConfig> providers;
    private AlbumAdapter mAdapter;

    private final Context c = this;
    ArrayList<Map<String, String>> history = new ArrayList<>();
    View parentLayout;
    TextView dialogText = null;
    private ArrayList<AlbumFile> mAlbumFiles;
    private UserViewModel sharedViewModel;


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
        sharedViewModel =  new ViewModelProvider(this).get(UserViewModel.class);
        sharedViewModel.getLiveBlogData().observe(this, user -> {
            users = user;
            binding.sendIcon.setOnClickListener(this);
        });

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
        binding.setSwitchers(false);

        setCalender();
        binding.switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {binding.setSwitchers(isChecked);});
//        binding.switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if(isChecked){
//                binding.recipientScrollView.setVisibility(View.VISIBLE);
//                binding.recipientAddIcon.setVisibility(View.VISIBLE);
//                binding.recipientDivider5.setVisibility(View.VISIBLE);
//            }else {
//                binding.recipientScrollView.setVisibility(View.GONE);
//                binding.recipientAddIcon.setVisibility(View.GONE);
//                binding.recipientDivider5.setVisibility(View.GONE);
//            }
//        });

        binding.recipientAddIcon.setOnClickListener(o->openSearch());
        binding.closeIcon.setOnClickListener(v-> finish());

        if(!Connectivity.isConnected(this)) {
            showSandbar(" la connexion internet est perdue");
        }
        else if(!Connectivity.isConnectedFast(this)) {
            showSandbar("Votre connection internet est lente");
        }
    }

    void setCalender(){
        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(myCalendar);
        };

        binding.dateEditText.setOnClickListener(v -> new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        binding.dateEditText.setText(sdf.format(myCalendar.getTime()));
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


    public void sendNotif(String documentID) {
        FirebaseUser user = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser() == null || users == null) return;
        Notification notif = new Notification();
        HashMap<String, Object> message = new HashMap<>();
        HashMap<String, Object> notification = new HashMap<>();
        HashMap<String, Object> android = new HashMap<>();
        HashMap<String, String> click_action = new HashMap<>();
        HashMap<String, String> data = new HashMap<>();

        data.put("id_publication", documentID );

        String content = "Nouvelle publication ";

        notif.setContent(content);
        notif.publicationId = documentID;
        notif.userID = user.getUid();
        notif.userURL = user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString();
        notif.userName =  users.getmName();
        notif.setTitle("Nouvelle publication par"+ users.getmName());
        notif.setToUser("global");
        notif.setSeen(false);

        notification.put("body", content );
        notification.put("title", "Nouvelle publication par"+ users.getmName());
        notification.put("click_action","OPEN_ACTIVITY_1");

        click_action.put("click_action", "OPEN_ACTIVITY_1");
        click_action.put("color", "#7e55c3");

        android.put("notification",click_action);

        message.put("to", "/topics/global");
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(logInAPIRequest);
        db.collection(PATH_NOTIF).add(notif);
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
        if (binding.getSwitchers() && getValue(binding.dateLayout.getEditText()).isEmpty()){
            binding.dateLayout.setError("La date est vide!");
            return false;
        }
        if (binding.getSwitchers() && getValue(binding.qte.getEditText()).isEmpty()){
            binding.qte.setError("Ce champ est vide!");
            return false;
        }
        if (binding.getSwitchers() && getValue(binding.lot.getEditText()).isEmpty()){
            binding.lot.setError("Ce cahmpe est vide!");
            return false;
        }

//        document.setLocation(sele[0]);
        document.setTitle(caseSensitive(getValue(binding.titre)));
        document.setBody(caseSensitive(getValue(binding.body)));
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
                send2Upload();
                /*if (Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().trim().isEmpty())
                    send2Upload();
                else
                    startActivityForResult(ValidatePhone.createIntent(this, response), START_ACTIVIY_VALIDATION);*/
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

    String caseSensitive(String hash){
        return hash.substring(0,1).toUpperCase() + hash.substring(1).toLowerCase();
    }

    void upload()
  {
      dialog("");
      String id = db.collection(PATH).document().getId();
      document.setUserID(mAuth.getUid());
      document.setToChange(binding.getSwitchers());
      if(binding.getSwitchers()) {
          document.setMedicines(history);
          document.setDateExpiration(getValue(binding.dateEditText));
          document.setNumLot(getValue(binding.lotEditText));
          document.setQte(Integer.valueOf(getValue(binding.qteEditText)));
      }
      document.setDocumentID(id);
      if( users == null) return;
      if(users.getSatisfied() == null || !users.getSatisfied()) {
          showEndDig("Votre compte est n'est pas vérifié");
          return;
      }
      document.setUserName(caseSensitive(users.mName));
      document.setUserUrl(users.mPhotoUri);
      document.setLocation(users.getWilaya());
      document.setToken(users.getToken());
      document.setSatisfied(users.getSatisfied());
      db.collection(PATH)
              .document(id)
              .set(document)
              .addOnSuccessListener(s-> {
                  if(mAlbumFiles != null && mAlbumFiles.size() > 0)
                    uploadImage(id, new ArrayList<>());
                   else
                      dialog("don");

                  sendNotif(id);
                  System.out.println("INN 1");
              }).addOnFailureListener(e -> dialog("Upload suspendu"));
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
        System.out.println("Called times");
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
            send2Upload();
            /*if(mAuth.getCurrentUser().getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().isEmpty())
                send2Upload();
            else
                showDigAskPhone();*/
        }
    }

    @Override
    public void onClick() {
        selectImage();
    }
}
