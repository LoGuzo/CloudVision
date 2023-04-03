package com.google.sample.cloudvision.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.sample.cloudvision.R;

public class LogoutActivity extends AppCompatActivity {

    private TextView tv_name;   // 이름 text
    private TextView tv_email;
    private ImageView iv_profile;   // 이미지 view
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        Intent intent = getIntent();
        String name = intent.getStringExtra("이름");  // LoginActivity로부터 닉네임 전달받음
        String photoUrl = intent.getStringExtra("프로필 사진");  // LoginActivity로부터 프로필사진 Url 전달받음
        String email = intent.getStringExtra("이메일");    //  LoginActivity로부터 이메일을 전달 받음

        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_name.setText(name);  // 이름 text를 뷰에 세팅
        tv_email.setText(email);    // 이메일 text를 뷰에 세팅

        iv_profile = findViewById(R.id.iv_profile);
        Glide.with(this).load(photoUrl).into(iv_profile);   // 프로필 사진 Url을 이미지 뷰에 세팅

        firebaseAuth = FirebaseAuth.getInstance();

        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                googleSignInClient.signOut();
                googleSignInClient.revokeAccess();

                Intent login = new Intent(LogoutActivity.this, LoginActivity.class);
                startActivity(login);
                finish();

                Toast.makeText(LogoutActivity.this, "로그아웃 되었습니다!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
