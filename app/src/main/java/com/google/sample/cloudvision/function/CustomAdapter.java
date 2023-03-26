package com.google.sample.cloudvision.function;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.sample.cloudvision.MainActivity;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.activity.Gift_Inform;
import com.google.sample.cloudvision.activity.SubActivity;
import com.google.sample.cloudvision.decorator.EventDecorator;
import com.google.sample.cloudvision.fragment.GalleryFragment;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private StorageReference mStorge = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("찾아조");
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    private ArrayList<User> arrayList;
    MaterialCalendarView materialCalendarView;
    private Context context;
    Bundle bundle = new Bundle();
    interface OnItemClickListener{
        void onItemClick(View v, int position); //뷰와 포지션값
        void onDeleteClick(View v, int positon);//삭제
    }

    public CustomAdapter(Context context,ArrayList<User> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main,parent,false);
        CustomViewHolder holder= new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getImgUrl())
                .into(holder.iv_profile);
        holder.tv_st.setText(arrayList.get(position).getStore());
        holder.tv_na.setText(arrayList.get(position).getGiftName());
        holder.tv_dt.setText(arrayList.get(position).getDate());
        holder.listCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent= new Intent(v.getContext(), Gift_Inform.class);
                intent.putExtra("gift_inform",arrayList.get(position));
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
                return true;
            }
        });
        holder.burn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStorge.child(firebaseUser.getUid()).child("GiftImg").child(arrayList.get(position).getBarCode()+".png").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mDatabase.child("UserAccount").child(firebaseUser.getUid()).child("gifticon").child(arrayList.get(position).getBarCode()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();
                                arrayList.remove(position);
                                notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        // 삼항 연산자
        return (arrayList != null ? arrayList.size() : 0);
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder{
        LinearLayout listCard;
        ImageView iv_profile; // 프로필
        TextView tv_st;
        TextView tv_na;
        TextView tv_dt;
        Button burn;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.listCard=itemView.findViewById(R.id.listCard);
            this.iv_profile = itemView.findViewById(R.id.iv_profile);
            this.tv_st=itemView.findViewById(R.id.tv_st);
            this.tv_na=itemView.findViewById(R.id.tv_na);
            this.tv_dt=itemView.findViewById(R.id.tv_dt);
            this.burn=itemView.findViewById(R.id.btn_burn);
        }
    }
}
