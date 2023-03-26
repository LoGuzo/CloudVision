package com.google.sample.cloudvision.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.function.User;

public class Gift_Inform extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gifticon_inform);
        Toolbar toolbar = findViewById(R.id.gift_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setTitle("기프티콘 정보"); // 툴바 제목 설정
        User user=getIntent().getParcelableExtra("gift_inform");

        ImageView gifticon=findViewById(R.id.gift_image);
        TextView store=findViewById(R.id.StoreText);
        TextView name=findViewById(R.id.NameText);
        TextView date=findViewById(R.id.DateText);

        Glide.with(this)
                .load(user.getImgUrl())
                .into(gifticon);
        store.setText(user.getStore());
        name.setText(user.getGiftName());
        date.setText(user.getDate());

    }
}
