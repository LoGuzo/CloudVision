package com.google.sample.cloudvision.login;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.sample.cloudvision.MainActivity;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.function.BitmapConverter;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파베 인증처리
    private DatabaseReference mDatabaseReference;   // 실시간 데베
    private EditText mEtId, mEtPwd;    // 로그인 필드
    private GoogleSignInClient googleSignInClient;  // 구글 api client
    private SignInButton btn_google;    // 구글 로그인 버튼 추가
    private static final  int REQ_SIGN_GOOGLE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("찾아조");

        mEtId = findViewById(R.id.et_id);
        mEtPwd = findViewById(R.id.et_pwd);

        GoogleSignInOptions googleSignInOptions = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        btn_google = findViewById(R.id.btn_google);
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent google = googleSignInClient.getSignInIntent();
                startActivityForResult(google, REQ_SIGN_GOOGLE);
            }
        });

        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    // 로그인 요청
                    String strId = mEtId.getText().toString();
                    String strPwd = mEtPwd.getText().toString();

                    mFirebaseAuth.signInWithEmailAndPassword(strId, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                // 로그인 성공
                                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(main);

                                Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "!!이메일과 비밀번호를 다시 입력해주세요!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch (IllegalArgumentException e){
                    Toast.makeText(LoginActivity.this, "!!아이디와 비번을 입력해주세요!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 회원가입 화면으로 이동
                Intent create = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(create);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {   // 구글 로그인 인증을 요청 했을 때 결과 값을 되돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_SIGN_GOOGLE) {
            Task<GoogleSignInAccount>
                    task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();     // account 라는 데이터는 구글로그인 정보를 담고있습니다. (닉네임, 프로필사진 Url, 이메일 주소 등..)
                resultLogin(account);   // 로그인 결과 값 출력을 수행하는 메소드
            }
        }
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){   // 로그인이 성공했으면
                            Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                            Intent google = new Intent(getApplicationContext(), MainActivity.class);
                            google.putExtra("이름", account.getDisplayName());
                            google.putExtra("프로필 사진", String.valueOf(account.getPhotoUrl()));
                            google.putExtra("이메일", account.getEmail());

                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            UserAccount inform = new UserAccount();
                            inform.setIdToken(user.getUid());
                            inform.setEmailId(user.getEmail());
                            inform.setName(account.getDisplayName());

                            mDatabaseReference.child("UserAccount").child(user.getUid()).setValue(inform);
                            updateUi(user);
                            startActivity(google);
                            finish();
                        } else {    // 로그인 실패했으면
                            Toast.makeText(LoginActivity.this, "!!로그인 실패!!", Toast.LENGTH_SHORT).show();
                            updateUi(null);
                            setContentView(R.layout.activity_login);
                        }
                    }
                });
    }

    private void updateUi(FirebaseUser user){

    }
}
