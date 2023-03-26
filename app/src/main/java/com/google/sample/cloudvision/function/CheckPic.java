package com.google.sample.cloudvision.function;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;

public class CheckPic {

    // 이미지 유사도 확인
    public static int compareFeature(Bitmap fileName1, Bitmap fileName2){
        int retval=0;
        long startTime=System.currentTimeMillis();

        Mat img1 = new Mat();
        Utils.bitmapToMat(fileName1,img1);
        Mat img2 = new Mat();
        Utils.bitmapToMat(fileName2,img2);

        MatOfKeyPoint keypoints1=new MatOfKeyPoint();
        MatOfKeyPoint keypoints2=new MatOfKeyPoint();
        Mat descriptors1=new Mat();
        Mat descriptors2=new Mat();
        ORB detector=ORB.create();
        ORB extractor=ORB.create();

        detector.detect(img1,keypoints1);
        detector.detect(img2,keypoints2);

        extractor.compute(img1,keypoints1,descriptors1);
        extractor.compute(img2,keypoints2,descriptors2);

        DescriptorMatcher matcher=DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfDMatch matches=new MatOfDMatch();

        if(descriptors2.cols()==descriptors1.cols()){
            matcher.match(descriptors1,descriptors2,matches);

            DMatch[]match=matches.toArray();
            double max_dist=0;double min_dist=100;

            for(int i=0;i<descriptors1.rows();i++){
                double dist=match[i].distance;
                if(dist<min_dist)min_dist=dist;
                if(dist>max_dist)max_dist=dist;
            }
            System.out.println("max_dist="+max_dist+", min_dist="+min_dist);
            for (int i=0;i<descriptors1.rows();i++){
                if(match[i].distance<=15){
                    retval++;
                }
            }
            System.out.println("matching count="+retval);
        }
        long estimatedTime=System.currentTimeMillis()-startTime;
        System.out.println("estimatedTime="+estimatedTime+"ms");
        return retval;
    }
}
