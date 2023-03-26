package com.google.sample.cloudvision.function;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable { // 데이터 베이스 정보 처리용

    private String giftName;
    private String barCode;
    private String store;
    private String date;
    private String imgUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String giftName, String barCode, String store, String date,String imgUrl) {
        this.giftName = giftName;
        this.barCode = barCode;
        this.store=store;
        this.date=date;
        this.imgUrl = imgUrl;
    }

    protected User(Parcel in) {
        giftName = in.readString();
        barCode = in.readString();
        store = in.readString();
        date = in.readString();
        imgUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    @Override
    public String toString() {
        return "User{" +
                "GiftName='" + giftName + '\'' +
                ", BarCode='" + barCode + '\'' +
                ", Store='" + store + '\'' +
                ", Date='" + date + '\'' +
                ", ImgUrl='" + imgUrl + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(giftName);
        dest.writeString(barCode);
        dest.writeString(store);
        dest.writeString(date);
        dest.writeString(imgUrl);
    }
}
