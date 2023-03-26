package com.google.sample.cloudvision.login;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.sample.cloudvision.MainActivity;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.function.BitmapConverter;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파베 인증처리
    private DatabaseReference mDatabaseReference;   // 실시간 데베
    private EditText mEtId, mEtPwd;    // 로그인 필드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("찾아조");

        mEtId = findViewById(R.id.et_id);
        mEtPwd = findViewById(R.id.et_pwd);

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
                                finish(); // 로그인 액티비티 파괴
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
}
