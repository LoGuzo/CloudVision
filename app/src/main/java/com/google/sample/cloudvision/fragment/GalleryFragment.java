package com.google.sample.cloudvision.fragment;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.alarmFunction.Constants;
import com.google.sample.cloudvision.alarmFunction.LocationService;
import com.google.sample.cloudvision.function.CustomAdapter;
import com.google.sample.cloudvision.function.ItemClickListener;
import com.google.sample.cloudvision.function.OnDataPassListener;
import com.google.sample.cloudvision.function.User;

import java.util.ArrayList;

import kotlinx.coroutines.SchedulerTaskKt;

// 등록된 기프티콘 목록
public class GalleryFragment extends Fragment{
    Context mContext;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    FragmentManager fragmentManager;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> arrayList= new ArrayList<>();
    private ArrayList<User> alarmList= new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    ItemClickListener itemClickListener;
    OnDataPassListener onDataPassListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            onDataPassListener = (OnDataPassListener)context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+"must implement SendEventListener");
        }
    }

    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        mContext=getActivity().getApplicationContext();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.recycler,container,false);
        recyclerView = view.findViewById(R.id.recyclerView); // 아이디 연결
        recyclerView.setHasFixedSize(true); // 기존 성능 강화
        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String s) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
                onDataPassListener.sendM(s);
            }
        };
        initData();
        return view;

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initData(){
        fragmentManager=getActivity().getSupportFragmentManager();
        Bundle bundle = new Bundle();
        database=FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = database.getReference("찾아조").child("UserAccount").child(firebaseUser.getUid()).child("gifticon");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스 데이터 받기
                arrayList.clear(); // 기존 배열 초기화
                alarmList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문 List 추출
                    User user=snapshot.getValue(User.class); // 만들어 뒀던 User객체 받기
                    if(user.getChkRd()){
                        CustomAdapter.setEx(user.getBarCode());
                        onDataPassListener.sendM(user.getStore());
                    }
                    if(!user.getChkAr()){
                        alarmList.add(user);
                        databaseReference.child(user.getBarCode()).child("chkAr").setValue(true);
                    }
                    arrayList.add(user); // 담은 데이터 배열리스트에 저장, 리사이클러뷰로 전송 준비
                }
                if(!alarmList.isEmpty()){
                    Log.d("chk1", String.valueOf(alarmList.size()));
                    onDataPassListener.sendAl(alarmList);
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // DB를 가져오던중 에러 발생 시
                Log.e("GiftTraker",String.valueOf(error.toException())); // 에러 출력
            }
        });
        bundle.putParcelableArrayList("bundleKey",arrayList);
        getParentFragmentManager().setFragmentResult("requsetKey",bundle);
        adapter = new CustomAdapter(getContext(),arrayList,itemClickListener);
        layoutManager =new GridLayoutManager(getActivity(),1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결결
    }
}
