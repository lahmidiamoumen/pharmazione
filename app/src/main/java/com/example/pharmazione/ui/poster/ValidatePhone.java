package com.example.pharmazione.ui.poster;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.jkb.vcedittext.VerificationCodeEditText;
import com.example.pharmazione.R;
import com.example.pharmazione.databinding.ActivityPhoneValidateBinding;
import com.example.pharmazione.persistance.User;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.pharmazione.utils.Util.RC_SIGN_IN;
import static com.example.pharmazione.utils.Util.START_ACTIVIY_VALIDATION;

public class ValidatePhone extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private FirebaseAuth mAuth;


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


        IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);
        if(response == null)
            initDialogPhone();
        else if( response.isNewUser() )
            createUser(Objects.requireNonNull(mAuth.getCurrentUser()));
        else
            initDialogPhone();

    }

    private void showSandbar(@StringRes int errorMessageRes) {
        Snackbar.make(findViewById(android.R.id.content), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void back(int result){
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

    void createUser(FirebaseUser mUser){
        User user = new User(mUser.getEmail(),mUser.getPhoneNumber(),mUser.getDisplayName(),mUser.getPhotoUrl().toString());
        db.collection("med-dwa-users").document(mUser.getUid()).set(user)
                .addOnSuccessListener(aVoid2 -> initDialogPhone())
                .addOnFailureListener(e -> showSandbar(R.string.authentication_error));
    }


    void initDialogPhone(){
        Context c = ValidatePhone.this;
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_phon_input, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
        alertDialogBuilderUserInput.setView(mView);
        TextInputEditText phone = mView.findViewById(R.id.phone_edit_text);
        TextInputEditText age = mView.findViewById(R.id.age_edit_text);
        Button save = mView.findViewById(R.id.poster);
        save.setOnClickListener(o->{
            if(phone.getText() == null || !phone.getText().toString().matches("0[567][0-9]{8,}")){
                phone.setError("Numéro de téléphone erroné");
            }else if(age.getText() == null || !age.getText().toString().matches("[1-9][0-9]?"))
                phone.setError("age erreur");
            else {
                int d = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(age.getText().toString());
                initialData = new HashMap<>();
                initialData.put("mPhoneNumber", phone.getText().toString());
                initialData.put("dateOfBirth", d);

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

                visibilityGoneViews(mVerifyButton,mResendButton);
                visibilityVisibleViews(binding.back,binding.textVerified,binding.iconVerified);
                db.collection("med-dwa-users")
                        .document(Objects.requireNonNull(mAuth.getUid())).set(initialData, SetOptions.merge());
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
