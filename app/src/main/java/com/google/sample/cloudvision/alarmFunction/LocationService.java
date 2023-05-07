package com.google.sample.cloudvision.alarmFunction;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.sample.cloudvision.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

public class LocationService extends Service {



    final int REFERENCEDISTANCE = 500; //알람 받을 반경
    private GregorianCalendar mCalender;
    static long diff;
    static double cx = 126.9071288; //초기 경도
    static double cy = 37.5154133; //초기 위도
    static float distanceResult;

    private int MY_PERMISSIONS_REQUEST_LOCATION = 10;


    Switch check;
    String addr, distanceName;
    String placeName = "스타벅스";
    DownloadFilesTask task;
    Vector<String> setStoreAdd = new Vector<>();
    Vector<Location> locations = new Vector<Location>();
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double lat = locationResult.getLastLocation().getLatitude();
                double lon = locationResult.getLastLocation().getLongitude();
                Location locationB = new Location("Point B");
                locationB.setLatitude(lat);
                locationB.setLongitude(lon);
                cy = lat;
                cx = lon;
                task = new DownloadFilesTask();
                task.execute(locationB);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {

        String channelId = "location_notification_channel";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent();

        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {

            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setContentTitle("***어플");
        builder.setContentText("***어플이 동작중입니다.");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Loc Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(25000);
        locationRequest.setFastestInterval(20000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
        startForeground(Constants.SERVICE_ID, builder.build());
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        //task.cancel(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        placeName = intent.getStringExtra("placeTitle");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.START_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.STOP_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadFilesTask extends AsyncTask<Location, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Location... location) {


            //new Thread(new Runnable() {
            //@Override
            //public void run() {

            try {
                data(cx, cy, "I2"); //주소 벡터setStoreAdd 생성, Q는 업종 대분류 '음식' ex)BBQ, 스타벅스등..
                if(setStoreAdd.size() == 0){
                    data(cx, cy, "G2"); //Q는 업종 대분류 '소매' ex)편의점
                }
                if(setStoreAdd.size() != 0) {
                    //벡터 setStoreAdd는 주소를 저장하는 벡터
                    serchPlace();//벡터 v이용
                    sendVibrator(location[0], locations); //벡터 v이용
                    //editText2.setText(nearestStore);
                    //editText: 거리 + 진동유무, place: 사용처, editText2: 사용처 주소명 / distance, placeName, addr;
                    addr = Constants.nearestStore;
                }
                else{
                    //editText.setText(null);
                    //editText2.setText("주변에 사용처 없음");
                    distanceName = "null";
                    addr = addr.concat("주변에 사용처 없음.");
                }
            }catch (IOException | JSONException | NullPointerException e){
                e.printStackTrace();
                task.cancel(true);
                System.out.println("오류 발생*****");
            }
            //}
            // }).start();
            setStoreAdd.clear();
            return "성공";
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

    }

    public Vector<String> data(double cx, double cy, String indsLclsCd) throws IOException, JSONException{

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B553077/api/open/sdsc2/storeListInRadius"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=ll3gVQiF4hDO3071xqUFnsbtaIxxRio626WMJelDnrjNZB7geUgbP3Ku%2FMx0n%2B%2FVEFJhtJS67ncoDMaGSn16lg%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("radius", "UTF-8") + "=" + URLEncoder.encode("500", "UTF-8")); /* 반경입력, (미터단위, 최대2000 미터)*/
        urlBuilder.append("&" + URLEncoder.encode("cx", "UTF-8") + "=" + URLEncoder.encode(Double.toString(cx), "UTF-8")); /* 원형의 중심점의 경도값으로 WGS84 좌표계 사용*/
        urlBuilder.append("&" + URLEncoder.encode("cy", "UTF-8") + "=" + URLEncoder.encode(Double.toString(cy), "UTF-8")); /* 원형의 중심점의 위도값으로 WGS84 좌표계 사용*/
        urlBuilder.append("&" + URLEncoder.encode("indsLclsCd", "UTF-8") + "=" + URLEncoder.encode(indsLclsCd, "UTF-8")); /* 입력된 대분류 업종에 해당하는 것만 조회*/
        //urlBuilder.append("&" + URLEncoder.encode("indsMclsCd", "UTF-8") + "=" + URLEncoder.encode("I212", "UTF-8")); /* 입력된 중분류 업종에 해당하는 것만 조회*/
        //urlBuilder.append("&" + URLEncoder.encode("indsSclsCd", "UTF-8") + "=" + URLEncoder.encode("Q12A01", "UTF-8")); /* 입력된 소분류 업종에 해당하는 것만 조회*/
        urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /* xml / json*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();

        //---파싱 구간---//
        System.out.println("파싱 시작");
        JSONObject jsonObj_1 = new JSONObject(result);
        String response = jsonObj_1.getString("header");

        JSONObject jsonObj_2 = new JSONObject(result);
        String body = jsonObj_2.getString("body");
        Log.i("body",body);

        JSONObject jsonObj_3 = new JSONObject(body);
        JSONArray jsonArray = jsonObj_3.getJSONArray("items");

        for(int i = 0; i<jsonArray.length(); i++){

            jsonObj_3 = jsonArray.getJSONObject(i);
            String bizesNm = jsonObj_3.getString("bizesNm"); //bizesNm은 상호명
            String lnoAdr = jsonObj_3.getString("lnoAdr"); //lnoAdr은 주소명
            if(bizesNm.contains(placeName)){
                setStoreAdd.add(lnoAdr); //주소명을 벡터에 삽입.
            }
            else{
                continue;
            }

        }
        return setStoreAdd;
    }

    public void sendVibrator(Location location, Vector<Location> locations){
        if(true){
            if(isNear(location, locations)){ //Location 벡터 locations에 요소들과 location의 최소 거리, 상점명 set, 근처에 존재시 true 리턴
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if(Build.VERSION.SDK_INT >= 26){
                    vibrator.vibrate(VibrationEffect.createOneShot(1000,10));
                    //editText: 거리 + 진동유무, place: 사용처, editText2: 사용처 주소명 / distance, placeName, addr;
                    //editText.setText("진동 있음 " + distanceResult + "m");

                    distanceName = "진동 있음" + distanceResult + "m";
                }else{
                    vibrator.vibrate(1000);
                }

            }
            else{
                distanceName = "진동 없음" + distanceResult + "m";
            }
        }
        else{

            distanceName= "진동 받지 않음";
        }
    }

    public boolean isNear(Location A, Vector<Location> B){
        float minDis = 999999;
        for(int i = 0; i<B.size(); i++) {
            float distance = B.get(i).distanceTo(A); //여기까지 손 봄
            if(minDis > distance){
                minDis = distance;
                Constants.nearestStore = setStoreAdd.get(i);
            }
            distanceResult = minDis;
        }
        B.clear();
        if (distanceResult <= REFERENCEDISTANCE) {
            return true;
        } else {
            return false;
        }
    }

    public void serchPlace(){
        for(int i = 0; i < setStoreAdd.size(); i++) {
            String value = setStoreAdd.get(i); // EX) xx역, 63빌딩, 롯데월드 등등
            List<Address> list = null;
            String str = value;
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {

                list = geocoder.getFromLocationName
                        (str, // 지역 이름
                                1// 읽을 개수
                        );

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (list != null) {

                if (list.size() == 0) {

                    Toast.makeText(getApplicationContext(), "해당되는 주소 정보를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                } else {

                    Address addr = list.get(0);

                    locations.add(new Location("Point")); //벡터locations에 Location객체 삽입

                    locations.get(i).setLatitude((float) addr.getLatitude()); //삽입한 Location객체에 위도, 경도 설정

                    locations.get(i).setLongitude((float) addr.getLongitude());
                }
            }
        }
    }


}