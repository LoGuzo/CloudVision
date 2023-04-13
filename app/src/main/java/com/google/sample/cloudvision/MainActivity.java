/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.sample.cloudvision.activity.SubActivity;
import com.google.sample.cloudvision.fragment.Calendar;
import com.google.sample.cloudvision.fragment.GalleryFragment;
import com.google.sample.cloudvision.function.BitmapConverter;
import com.google.sample.cloudvision.function.CheckPic;
import com.google.sample.cloudvision.googleFunction.PermissionUtils;
import com.google.sample.cloudvision.login.LoginActivity;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    TabLayout tabs;
    private static final int GALLERY_PERMISSIONS_REQUEST = 0; // 갤러리 허가 요청 함수
    private static final int GALLERY_IMAGE_REQUEST = 1; // 갤러리에서 이미지 선택 요청 함수
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;

    private final String[] page_titles=new String[]{
            "기프티콘",
            "캘린더"
    };
    private final Fragment[]pages=new Fragment[]{
            new GalleryFragment(),
            new Calendar()
    };

    // 안스와 openCV연동
    static{
        System.loadLibrary("opencv_java3");
        System.loadLibrary("cloudvision");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager=findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager()));
        tabs=findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGalleryChooser();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();

                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                finish();

                Toast.makeText(MainActivity.this, "로그아웃 되었습니다!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 갤러리 띄우기
    public void startGalleryChooser() {
        if (com.google.sample.cloudvision.googleFunction.PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
        }
    }
    // 허가 요청 코드와 결과 코드를 통해 결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            SubActivity.uris=data.getData();
            uploadImage(data.getData());
        }
    }

    // 허가 요청 코드에 맞는 함수 실행
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSIONS_REQUEST) {
            if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                startGalleryChooser();
            }
        }
    }

    // 기프티콘 확인
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                Intent intent= new Intent(MainActivity.this,SubActivity.class);
                AssetManager am = getResources().getAssets();
                InputStream is= am.open("gifticon8.jpg");
                Bitmap bm= BitmapFactory.decodeStream(is);
                try {
                    Bitmap bitmap =
                            scaleBitmapDown(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
                    intent.putExtra("image",BitmapConverter.BitmapToByteArray(bitmap));
                    int ret;
                    ret = CheckPic.compareFeature(bm, bitmap);

                    if(ret>0){
                        intent.putExtra("image",BitmapConverter.BitmapToByteArray(bitmap));
                        startActivityForResult(intent,101);
                    }else{
                        Toast.makeText(getApplicationContext(),"기프티콘이 아닙니다. 다시 선택해주세요.",Toast.LENGTH_LONG).show();
                        startGalleryChooser();
                    }
                    // scale the image to save on bandwidth

                } catch (IOException e) {
                    Log.d(TAG, "Image picking failed because " + e.getMessage());
                    Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    // 이미지 resize
    private Bitmap scaleBitmapDown(Bitmap bitmap) {
        int resizedWidth = 800;
        int resizedHeight = 1609;

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }

    public class TabPagerAdapter extends FragmentPagerAdapter {
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return page_titles[position];
        }

        public TabPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return pages[position];
        }

        @Override
        public int getCount() {
            return page_titles.length;
        }

    }
}