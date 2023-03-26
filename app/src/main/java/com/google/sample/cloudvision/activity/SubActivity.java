package com.google.sample.cloudvision.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.fragment.Calendar;
import com.google.sample.cloudvision.fragment.GalleryFragment;
import com.google.sample.cloudvision.function.AdditionalFunction;
import com.google.sample.cloudvision.function.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SubActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAFbxR-qcDMQf_ZZC2J5ihlPzyUmrll_ks";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final String TAG = SubActivity.class.getSimpleName();
    private static String nameS;
    private static String barS;
    private static String storeS;
    private static String dateS;
    private static String urlS;
    public static Uri uris;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    private StorageReference mStorge = FirebaseStorage.getInstance().getReference(firebaseUser.getUid());
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("찾아조").child("UserAccount").child(firebaseUser.getUid());
    private ImageView mMainImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.next_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setTitle("쿠폰 등록"); // 툴바 제목 설정
        mMainImage = findViewById(R.id.main_image);
        byte[] byteArray = getIntent().getByteArrayExtra("image"); // mainActivity에서 들어오는 이미지
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); // mainActivity에서 들어오는 이미지 자료를 Bitmap으로 변한
            ImgCheck(bitmap);
        }
        Button button1 = (Button) findViewById(R.id.btn_Save);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto(uris, barS, nameS, storeS, dateS);
            }
        });
    }

    // 이미지 추출 함수
    public void ImgCheck(Bitmap bitmap) {
        Bitmap name = Bitmap.createBitmap(bitmap, 82, 770, 530, 150);
        Bitmap barCode = Bitmap.createBitmap(bitmap, 190, 1050, 530, 100);
        Bitmap placeDate = Bitmap.createBitmap(bitmap, 250, 1150, 530, 170);
        Bitmap[] listBmp = {placeDate, barCode, name};
        callCloudVision(mergeMultiple(listBmp));
        mMainImage.setImageBitmap(bitmap);
    }

    // 구글 비전 함수
    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = com.google.sample.cloudvision.googleFunction.PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    // 구글 비전 함수
    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<SubActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(SubActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            SubActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView dateView = activity.findViewById(R.id.dateText);
                dateView.setText(AdditionalFunction.dateSet(nameS + "\n" + storeS + "\n" + dateS + "\n" + barS));
            }
        }
    }

    // 구글 비전 실행 함수수
    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new SubActivity.LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    // 이미지 텍스트 추출후 저장
    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            message = texts.get(0).getDescription();
        } else {
            message = "nothing";
        }
        String[] arr = message.split("\n");
        storeS = arr[0];
        dateS = arr[1];
        barS = arr[2];
        nameS = arr[3];
        return message;
    }

    // 이미지 합병 함수
    private Bitmap mergeMultiple(Bitmap[] parts) {

        Bitmap result = Bitmap.createBitmap(parts[0].getWidth() * 3, parts[0].getHeight() * 3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        for (int i = 0; i < parts.length; i++) {
            canvas.drawBitmap(parts[i], parts[i].getWidth() * (i), 0, paint);
        }
        return result;
    }

    // 데이터베이스에 정보 저장
    private void writeNewUser(String barCode, String giftName, String store, String date, String imgUrl) {
        User user = new User(giftName, barCode, store, date, imgUrl);

        mDatabase.child("gifticon").child(barCode).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // Write was successful!
                    Toast.makeText(SubActivity.this, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Write failed
                    Toast.makeText(SubActivity.this, "저장을 실패했습니다.", Toast.LENGTH_SHORT).show();
                });

    }

    // Storge에 이미지 저장 및 데이터베이스 저장 함수
    private void uploadPhoto(Uri uri, String barCode, String giftName, String store, String date) {
        String fileName = barS + ".png";
        mStorge.child("GiftImg").child(fileName).putFile(uri).addOnCompleteListener(SubActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    //urlS = mStorge.child("GiftImg").child(fileName).getDownloadUrl().toString();
                    mStorge.child("GiftImg").child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>(){
                        @Override
                        public void onSuccess(Uri uri) {
                            urlS=String.valueOf(uri);
                            writeNewUser(barCode, giftName, store, date, urlS);
                        }
                    });
                }else {
                    Toast.makeText(getApplicationContext(), "실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}