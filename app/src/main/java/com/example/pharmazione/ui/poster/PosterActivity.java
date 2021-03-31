package com.example.pharmazione.ui.poster;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.pharmazione.R;
import com.example.pharmazione.SearchableMedecinActivity;
import com.example.pharmazione.databinding.ActivityMainBinding;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.utils.PermissionUtil;
import com.example.pharmazione.utils.Util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static com.example.pharmazione.utils.Util.RC_SIGN_IN;
import static com.example.pharmazione.utils.Util.START_ACTIVIY_BESOIN;
import static com.example.pharmazione.utils.Util.START_ACTIVIY_DON;
import static com.example.pharmazione.utils.Util.START_ACTIVIY_VALIDATION;
import static com.example.pharmazione.utils.Util.load;


public class PosterActivity extends AppCompatActivity {

    private static final String TAG = "Poster medicament";
    AlertDialog alertDialogAndroid;
    File compressed = null;
    FirebaseStorage storage;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Document document;
    String mCurrentPhotoPath;
    private List<AuthUI.IdpConfig> providers;

    private final Context c = this;
    View parentLayout;
    TextView dialogText = null;
    Uri ordananceUri;

    ActivityMainBinding binding;
    BottomSheetDialog bottomSheetDialog;


    final String[] sele = new String[3];
    private String medId;
    private String medPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        PermissionUtil.ask(this);
        mAuth = FirebaseAuth.getInstance();
        document = new Document();
        bottomSheetDialog = new BottomSheetDialog(this,R.style.BottomSheetMenuTheme);
        bottomSheetDialog.setContentView(R.layout.for_dialog);


        String[] mobileArray = {"Besoin de don","Don de médicament","Besoin d’orientation"};
        ArrayAdapter adpt = new ArrayAdapter<>(this,
                R.layout.list_text_view, mobileArray);

        ListView listView = bottomSheetDialog.findViewById(R.id.notifications);
        assert listView != null;
        listView.setAdapter(adpt);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            bottomSheetDialog.dismiss();
            TextView textView = view.findViewById(R.id.label);
            switch (textView.getText().toString()){
                case "Besoin de don":
                    besoin();
                    break;
                case "Don de médicament":
                    don();
                    break;
                case "Besoin d’orientation":
                    oreintation();
                    break;
            }
        });

        String st = getIntent().getStringExtra("needBesoin");
        if(st != null && st.equals("needBesoin")){
            besoin();
        }
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());


        parentLayout = findViewById(android.R.id.content);
        String[] lists = {"Neuve","Entamée"};
        String[] cites = getResources().getStringArray(R.array.cities2);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, lists);
        binding.priceTextInput.setAdapter(adapter);


        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, cites);

        binding.regoinBesoinEdit.setAdapter(adapter3);
        binding.lieuEditText.setAdapter(adapter3);

        binding.priceTextInput.setOnItemClickListener((parent, view, position, rowId) -> sele[0] = (String)parent.getItemAtPosition(position));
        binding.regoinBesoinEdit.setOnItemClickListener((parent, view, position, rowId) -> sele[1] = (String)parent.getItemAtPosition(position));
        binding.lieuEditText.setOnItemClickListener((parent, view, position, rowId) -> sele[2] = (String)parent.getItemAtPosition(position));

        binding.selectImagesOreden.setOnClickListener(o-> openGallery());
        binding.takeImagesOreden.setOnClickListener(o-> openCamera());

        Objects.requireNonNull(binding.nomBesoin.getEditText()).setOnClickListener(o-> openSearch(true));
        Objects.requireNonNull(binding.nom.getEditText()).setOnClickListener(o-> openSearch(false));

    }

    public void don(){
        closeVisibility2(null);
        binding.chooserText.setText(getResources().getString(R.string.don));
        binding.donLayout.setVisibility(View.VISIBLE);
        binding.besoinLayout.setVisibility(View.GONE);
        binding.poster.setVisibility(View.VISIBLE);


        setCalender();

        //ordonnance photo
        binding.oredenance.setVisibility(View.GONE);
        binding.oredenanceText.setVisibility(View.GONE);
    }

    public void besoin(){
        closeVisibility2(null);
        ordananceUri = null;
        binding.chooserText.setText(getResources().getString(R.string.besoin));
        binding.besoinLayout.setVisibility(View.VISIBLE);
        binding.donLayout.setVisibility(View.GONE);

        binding.oredenance.setVisibility(View.VISIBLE);
        binding.oredenanceText.setVisibility(View.VISIBLE);

        binding.poster.setVisibility(View.VISIBLE);
    }

    public void oreintation(){
        closeVisibility2(null);
        ordananceUri = null;
        binding.chooserText.setText(getResources().getString(R.string.orientation));
        binding.besoinLayout.setVisibility(View.VISIBLE);
        binding.donLayout.setVisibility(View.GONE);
        binding.poster.setVisibility(View.VISIBLE);

        //ordennance photo
        binding.oredenance.setVisibility(View.GONE);
        binding.oredenanceText.setVisibility(View.GONE);
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
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        binding.dateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    public void openCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri imageUri = FileProvider.getUriForFile(
                        PosterActivity.this,
                        "com.example.pharmazione.provider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, Util.START_CAMERA_REQUEST_CODE);
            }
        }
    }

    public void openGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , Util.PICKFILE_REQUEST_CODE);//one can be replaced with any action code
    }

    public void openSearch(Boolean besoin){
        Intent i = new Intent(this, SearchableMedecinActivity.class);
        if(besoin)
            startActivityForResult(i, START_ACTIVIY_BESOIN);
        else{
            startActivityForResult(i, START_ACTIVIY_DON);
        }
    }


    private boolean checkDon(){
         if (getValue(binding.nom).isEmpty()){
            binding.nom.setError("le champe est vide");
            return false;
        }
        if (getValue(binding.lieu).isEmpty()){
            binding.lieu.setError("le champe est vide");
            return false;
        }

        String med = getValue(binding.nom);
        document.setEtat(sele[0]);
        document.setName(med);
        document.setLotNumber(getValue(binding.lot));
        document.setExpirationDate(getValue(binding.dateLayout));
        document.setLocation(sele[2]);
        document.setCategory(getResources().getString(R.string.don));
        return true;
    }

    private boolean checkInputs(){
        if(ordananceUri == null && "Besoin".equals(binding.chooserText.getText().toString())){
            showSandbar(R.string.ajouter_une_ordennance_r_cente);
            return false;
        }

        if (getValue(binding.nomBesoin).isEmpty()){
            binding.nomBesoin.setError("le champe est vide");
            return false;
        }
        if (getValue(binding.regoinBesoin).isEmpty()){
            binding.regoinBesoin.setError("le champe est vide");
            return false;
        }

        String med = getValue(binding.nomBesoin);
        med = med.substring(0,1).toUpperCase() + med.substring(1).toLowerCase();
        document.setName(med);
        document.setCategory(binding.chooserText.getText().toString());
        document.setLocation(sele[1]);
        document.setDescription(getValue(binding.descBesoinLayout));

        return true;
    }


    String getValue(TextInputLayout tx){
        return Objects.requireNonNull(tx.getEditText()).getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Util.PICKFILE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    ordananceUri = data.getData();
                    load(binding.imageClose2,ordananceUri);
                    imageDisplay2();
                    compressed = getCompressed();
                }
                break;
            case Util.START_CAMERA_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Bitmap mImageBitmap = null;
                    ordananceUri = Uri.parse(mCurrentPhotoPath);
                    try {
                        mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ordananceUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    load(binding.imageClose2,mImageBitmap);
                    imageDisplay2();
                    compressed = getCompressTaken();
                }
                break;
            case START_ACTIVIY_BESOIN:
                if(resultCode == RESULT_OK) {
                    this.medId = data.getStringExtra("id");
                    this.medPath = data.getStringExtra("path");
                    String nom = data.getStringExtra("name");
                    binding.nomBesoin.getEditText().setText(nom);
                }
                break;
            case START_ACTIVIY_DON:
                if(resultCode == RESULT_OK) {
                    this.medId = data.getStringExtra("id");
                    this.medPath = data.getStringExtra("path");
                    String nom = data.getStringExtra("name");
                    binding.nom.getEditText().setText(nom);
                }
                break;
            case START_ACTIVIY_VALIDATION:
                 if( resultCode == RESULT_OK )
                     send2Upload();
                 else
                     showDigAskPhone();
                break;
            case RC_SIGN_IN:
            {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (resultCode == RESULT_OK && response != null)
                {
                     if(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().trim().isEmpty())
                        send2Upload();
                     else
                         startActivityForResult(ValidatePhone.createIntent(this, response),START_ACTIVIY_VALIDATION);
                } else {
                    if (response == null) {
                        showSandbar(R.string.sign_in_cancelled); return;
                    }
                    else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showSandbar(R.string.no_internet_connection);  return;
                    }
                    else if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                        showSandbar(R.string.account_disabled);  return;
                    }
                    else  showSandbar(R.string.unknown_error);
                    Log.e("Auth Frag", "Sign-in error: ", response.getError());
                }
                break;
            }
            default:
                if (resultCode == RESULT_CANCELED)
                    showSandbar("Operation annuler");
        }
    }

    private File getCompressTaken () {
        File file = null;
        try {
            file = new Compressor(this).compressToFile(new File(ordananceUri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert file != null;
        return file;
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

    private File getCompressed() {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(ordananceUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        File file = new File(picturePath);

        File compressedImageFile = null;
        try {
            compressedImageFile = new Compressor(this).compressToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert compressedImageFile != null;
        return compressedImageFile;
    }

    void imageDisplay2(){
        binding.relativeLayout2.setVisibility(View.VISIBLE);
        binding.oredenance.setVisibility(View.GONE);
    }


    public void closeVisibility2(View v){
        binding.relativeLayout2.setVisibility(View.GONE);
        binding.oredenance.setVisibility(View.VISIBLE);
    }


    void upload()
  {
      dialog("");
      String id = db.collection("med-dwa").document().getId();
      document.setUserID(mAuth.getUid());
      document.setSatisfied(false);
      document.setVerified(false);
      document.setMedicamentID(this.medId);
      document.setPath(this.medPath);
      document.setUserName(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
      document.setUserUrl(Objects.requireNonNull(mAuth.getCurrentUser().getPhotoUrl()).toString());
      document.setDocumentID(id);
      if(ordananceUri != null){
          db.collection("med-dwa").document(id).set(document);
          uploadImage(id);
      }else {
          // upload data without-image
          // used to check connection...
          writToFireBae(id);
      }
  }

   private void uploadImage(String id){
        if(compressed == null){
            dialog("Problem avec la photo d'ordonnance!");
            return;
        }
        Uri file = Uri.fromFile(compressed);
       StorageReference storageRef = storage.getReference();
       // Create the file metadata
       StorageMetadata metadata = new StorageMetadata.Builder()
               .setContentType("image/jpeg")
               .build();
       StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
       UploadTask uploadTask = riversRef.putFile(file,metadata);

       uploadTask.continueWithTask(task ->
       {
           if (!task.isSuccessful()) {
               throw Objects.requireNonNull(task.getException());
           }
           return riversRef.getDownloadUrl();
       }).addOnSuccessListener(uri ->
               db.collection("med-dwa").document(id).update("ordPath",uri.toString()));

       // Listen for state changes, errors, and completion of the upload.
       uploadTask.addOnProgressListener(taskSnapshot -> {
           double progress = ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
           dialog("Chargement photo d'ordonnance " +((int) progress )+"%");
       })
        .addOnPausedListener(taskSnapshot -> dialog("Upload suspendu"))
        .addOnCompleteListener(t ->{
           alertDialogAndroid.dismiss();
           showEndDig("Upload terminé");
        });
   }

   public void writToFireBae(String id){
      db.collection("med-dwa").document(id).set(document)
          .addOnSuccessListener(aVoid2 -> dialog("don"))
          .addOnFailureListener(e -> dialog("Upload suspendu"));
   }


    void showSandbar(String text){
        Snackbar.make(parentLayout,text,Snackbar.LENGTH_SHORT).show();
    }

    private void showSandbar(@StringRes int errorMessageRes) {
        Snackbar.make(parentLayout, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    void send2Upload(){
        if ("Don".equals(binding.chooserText.getText().toString())) {
            if (checkDon()) upload();
        } else {
            if (checkInputs()) upload();
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close:
                finish();
                break;
            case R.id.poster:{
                if(mAuth.getCurrentUser() == null)
                    showDig();
                else {
                    if(mAuth.getCurrentUser().getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().isEmpty())
                        send2Upload();
                    else
                        showDigAskPhone();
                }
                break;
            }
            case R.id.chooser:
                bottomSheetDialog.show();
                break;
        }
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

    @Override
    protected void onStop() {
        super.onStop();
        if (bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
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


}
