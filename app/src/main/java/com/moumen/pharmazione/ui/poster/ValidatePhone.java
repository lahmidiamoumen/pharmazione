package com.moumen.pharmazione.ui.poster;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jkb.vcedittext.VerificationCodeEditText;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.databinding.ActivityPhoneValidateBinding;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.utils.ClickListener;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import id.zelory.compressor.Compressor;

import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_USER;
import static com.moumen.pharmazione.utils.Util.load;

public class ValidatePhone extends AppCompatActivity implements ClickListener,
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    LinearLayout linearLayout;
    RelativeLayout relativeLayout;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;
    private ArrayList<AlbumFile> mAlbumFiles;
    AlbumAdapter mAdapter;


    AlertDialog alertDialogPhone;
    Map<String, Object> initialData ;
    FirebaseFirestore db;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    private EditText mPhoneNumberField;
    private VerificationCodeEditText mVerificationField;

    private Button mVerifyButton;
    private Button mResendButton;
    private ImageButton imageButton;
    ImageView imageView;
    ActivityPhoneValidateBinding binding;


    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, ValidatePhone.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, response);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneValidateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();


        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        // Assign views

        mPhoneNumberField = binding.fieldPhoneNumber;
        mPhoneNumberField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        mVerificationField = binding.fieldVerificationCode;

        storage = FirebaseStorage.getInstance();

        mVerifyButton = binding.buttonVerifyPhone;
        mResendButton = binding.buttonResend;

        // Assign click listeners
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);

        mPhoneNumberField.setFocusable(false);

        visibilityGoneViews(mVerificationField,mVerifyButton,mResendButton,mPhoneNumberField);

        mAuth = FirebaseAuth.getInstance();

        visibilityVisibleViews(binding.back);
        binding.back.setOnClickListener(view -> back(RESULT_CANCELED));


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                visibilityGoneViews(binding.progressBar);
                signInWithPhoneAuthCredential(credential);
                //updateUI(STATE_VERIFY_SUCCESS, credential);
            }


            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                visibilityGoneViews(binding.progressBar);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mPhoneNumberField.setError("Invalid phone number.");

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    showSandbar(R.string.depasse_limit);
                }
                visibilityVisibleViews(binding.back);
                binding.back.setOnClickListener(view -> back(RESULT_CANCELED));
            }


            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                visibilityGoneViews(binding.progressBar);
                visibilityVisibleViews(mVerificationField,mVerifyButton,mResendButton,mPhoneNumberField);

            }
        };
//
//        IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);
//        if(response == null)
//            initDialogPhone();
//        else if( response.isNewUser() )
        createUser(Objects.requireNonNull(mAuth.getCurrentUser()));

    }

    private void showSandbar(@StringRes int errorMessageRes) {
        Snackbar.make(findViewById(android.R.id.content), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void back(int result){
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

    private String lowerToUpper( String hash) {
        return hash.substring(0,1).toUpperCase() + hash.substring(1).toLowerCase();
    }

    void createUser(FirebaseUser mUser){
        User user = new User(mUser.getEmail(),mUser.getPhoneNumber(),lowerToUpper(mUser.getDisplayName()) ,mUser.getPhotoUrl().toString());
        initDialogPhone();
        db.collection(PATH_USER).document(mUser.getUid()).set(user, SetOptions.merge())
            .addOnFailureListener(e -> showSandbar(R.string.authentication_error));
    }

    private void previewImage(int position) {
        if (mAlbumFiles != null || mAlbumFiles.size() != 0) {
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
                        relativeLayout.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                        linearLayout.setVisibility( result.size() > 0 ? View.GONE : View.VISIBLE);
                    })
                    .start();
        }
    }

    private void selectImage() {

        mAdapter = new AlbumAdapter(this,this, (view, position) -> previewImage(position), 1);
        Album.image(this)
                .multipleChoice()
                .camera(true)
                .columnCount(3)
                .selectCount(1)
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
                    load(imageView,mAlbumFiles.get(0).getPath());

                    relativeLayout.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                    linearLayout.setVisibility(result.size() > 0 ? View.GONE : View.VISIBLE);

                    imageButton.setOnClickListener(o->{
                        mAlbumFiles.clear();
                        linearLayout.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    });
                })
                //.onCancel( result -> showSandbar("selection annuler"))
                .start();
    }

    @Override
    public void onClick() {
        selectImage();
    }

    private void toggle(boolean show, View v,int i, int tran) {
        View secondPage = v.findViewById(R.id.secondPage);
        View firstPage = v.findViewById(R.id.firstPage);
        ViewGroup parent = v.findViewById(R.id.layout);

        Transition transition = new Slide(tran);
        transition.setDuration(200L);
        transition.addTarget(i);
        TransitionManager.beginDelayedTransition(parent, transition);
        secondPage.setVisibility(show ? View.VISIBLE : View.GONE);
        firstPage.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    void initDialogPhone(){
        Context c = ValidatePhone.this;
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_phon_input, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c,R.style.DialogTheme);
        alertDialogBuilderUserInput.setView(mView);

        ImageButton takeImage = mView.findViewById(R.id.takeImagesOreden);
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
        linearLayout = mView.findViewById(R.id.linearLayout);
        relativeLayout = mView.findViewById(R.id.recycler_view55);
        imageButton = mView.findViewById(R.id.close);
        imageView = mView.findViewById(R.id.image_close2);

        nom.setText(mAuth.getCurrentUser() == null ? "" : mAuth.getCurrentUser().getDisplayName());
        takeImage.setOnClickListener(o-> selectImage());


        String[] sele = new String[1];
        String[] cites = getResources().getStringArray(R.array.cities2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, cites);
        wilaya.setAdapter(adapter3);
        wilaya.setOnItemClickListener((parent, view, position, rowId) -> sele[0] = (String)parent.getItemAtPosition(position));

        Button save = mView.findViewById(R.id.poster);
        Button next = mView.findViewById(R.id.next);
        Button retour = mView.findViewById(R.id.retour);

        retour.setOnClickListener(o-> toggle(false, mView,R.id.firstPage, Gravity.LEFT));
        initialData = new HashMap<>();


        next.setOnClickListener(o-> {
            if(phone.getText() == null || !phone.getText().toString().matches("0[567][0-9]{8,}")) {
                phone.setError("Numéro de téléphone erroné");
            }  else if(wilaya.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Veuillez vérifier les champs",Toast.LENGTH_LONG).show();
            }
            else {
                initialData.put("mName", nom.getText().toString());
                initialData.put("wilaya", wilaya.getText().toString());
                initialData.put("addresseOfficine", lowerToUpper(address.getText().toString()));
                initialData.put("mPhoneNumber", phone.getText().toString());
                db.collection("med-dwa-users")
                    .document(Objects.requireNonNull(mAuth.getUid()))
                    .set(initialData, SetOptions.merge());
                toggle(true,mView, R.id.secondPage, Gravity.RIGHT);
            }
        });

        save.setOnClickListener(o->{
           if(mAlbumFiles == null || mAlbumFiles.size() <= 0) {
                Toast.makeText(getApplicationContext(), "Veuillez ajouter votre carte professionnelle.",Toast.LENGTH_LONG).show();
            } else if(officine.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Veuillez vérifier les champs",Toast.LENGTH_LONG).show();
            } else {
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

               db.collection("med-dwa-users")
                       .document(Objects.requireNonNull(mAuth.getUid())).set(initialData, SetOptions.merge());

                String st = phone.getText().toString().replaceFirst("0","+213");
                mPhoneNumberField.setText(st);
                mPhoneNumberField.setText(phone.getText());
                startPhoneNumberVerification(st);
                alertDialogPhone.dismiss();
            }
        });
        alertDialogPhone = alertDialogBuilderUserInput.create();
        alertDialogPhone.show();
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


    private void uploadImage(){

        StorageReference storageRef = storage.getReference();
        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
        if(mAlbumFiles.size() == 0 ) return;

        AlbumFile image = mAlbumFiles.get(0);
        Uri file = Uri.fromFile(getCompressTaken(image.getPath()));
        StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file, metadata);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return riversRef.getDownloadUrl();
        }).addOnSuccessListener(uri -> {


            initialData.put("carte",  uri.toString());

            db.collection("med-dwa-users")
                .document(Objects.requireNonNull(mAuth.getUid())).set(initialData, SetOptions.merge())
                .addOnSuccessListener(s -> {
                    visibilityGoneViews(mVerifyButton,mResendButton,binding.progressBar);
                    visibilityVisibleViews(binding.back,binding.textVerified,binding.iconVerified);
                });
        });

//        int finalI = urlsDownloded.size() + 1;
//        uploadTask
//                .addOnProgressListener(taskSnapshot -> {
//                    double progress = (( 100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
//                    //dialog("Chargement photo  "+ finalI +" " +((int) progress )+"%");
//                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phone) {

        visibilityVisibleViews(mPhoneNumberField);
        visibilityGoneViews(binding.back);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,              // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,  // Unit of timeout
                this,       // Activity (for callback binding)
                mCallbacks);       // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                updateUI(STATE_VERIFY_SUCCESS, credential);
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    mVerificationField.setError(task.getException().getMessage());
                    updateUI(STATE_SIGNIN_FAILED);

                }else{
                    binding.textVerified.setText("Ce numéro de téléphone est déjà utilisé");
                    visibilityGoneViews(mVerifyButton,mResendButton,mVerificationField);
                    visibilityVisibleViews(binding.back,binding.textVerified);
                    binding.back.setOnClickListener(view -> back(RESULT_CANCELED));
                }
            }
        });
    }


    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {

            case STATE_CODE_SENT:
            case STATE_VERIFY_FAILED:
                enableViews(mVerifyButton, mResendButton, mPhoneNumberField, mVerificationField);

                break;
            case STATE_VERIFY_SUCCESS:{
                if (cred.getSmsCode() != null) {
                    mVerificationField.setText(cred.getSmsCode());
                } else {
                    mVerificationField.setText("instant validation");
                }

                mAuth.getCurrentUser().updatePhoneNumber(cred);
                visibilityVisibleViews(binding.progressBar);
                uploadImage();
                binding.back.setOnClickListener(view -> back(RESULT_OK));
            }
                break;
            case STATE_SIGNIN_FAILED:
                Toast.makeText(this,"SIGNIN FAILED", Toast.LENGTH_LONG).show();
                enableViews(mVerifyButton, mResendButton, mPhoneNumberField, mVerificationField);
                break;
        }
    }

    private void visibilityVisibleViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void visibilityGoneViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_verify_phone:
                String code = Objects.requireNonNull(mVerificationField.getText()).toString();
                if (!code.matches("[0-9]{6}")) {
                    mVerificationField.setError("verifier le code");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.button_resend:
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                break;
        }
    }

}
