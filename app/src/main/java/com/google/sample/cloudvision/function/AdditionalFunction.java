package com.google.sample.cloudvision.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// 추가적인 함수들

public class AdditionalFunction {

    // 날짜 형식 통일 함수
    public static String dateSet(String s){
        Date date;
        String dateS = null;
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy.MM.dd");
        try{
            if (checkDateK(s)) {
                SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                // String 타입을 Date 타입으로 변환
                date = newDtFormat.parse(s);
                // Date타입의 변수를 새롭게 지정한 포맷으로 변환
                dateS = dtFormat.format(date);
            }
            else if(checkDateH(s)){
                SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd");
                // String 타입을 Date 타입으로 변환
                date = newDtFormat.parse(s);
                // Date타입의 변수를 새롭게 지정한 포맷으로 변환
                dateS = dtFormat.format(date);
            }
            else {
                dateS=s;
            }
            return dateS;
        }
        catch (ParseException e){
            return "";
        }
    }

    // 날짜 형식 확인 함수
    public static boolean checkDateK(String checkDate) {
        try {
            SimpleDateFormat dateFormatParser = new SimpleDateFormat("yyyy년 MM월 dd일"); //검증할 날짜 포맷 설정
            dateFormatParser.setLenient(false); //false일경우 처리시 입력한 값이 잘못된 형식일 시 오류가 발생
            dateFormatParser.parse(checkDate); //대상 값 포맷에 적용되는지 확인
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // 날짜 형식 확인 함수
    public static boolean checkDateH(String checkDate) {
        try {
            SimpleDateFormat dateFormatParser = new SimpleDateFormat("yyyy-MM-dd"); //검증할 날짜 포맷 설정
            dateFormatParser.setLenient(false); //false일경우 처리시 입력한 값이 잘못된 형식일 시 오류가 발생
            dateFormatParser.parse(checkDate); //대상 값 포맷에 적용되는지 확인
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
