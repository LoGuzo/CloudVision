package com.google.sample.cloudvision.fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.sample.cloudvision.R;
import com.google.sample.cloudvision.decorator.EventDecorator;
import com.google.sample.cloudvision.decorator.OneDayDecorator;
import com.google.sample.cloudvision.decorator.SaturdayDecorator;
import com.google.sample.cloudvision.decorator.SundayDecorator;
import com.google.sample.cloudvision.function.AdditionalFunction;
import com.google.sample.cloudvision.function.User;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class Calendar extends Fragment {
    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    public String readDay = null;
    public String str = null;
    public CalendarView calendarView;
    public Button cha_Btn, del_Btn, save_Btn, add_btn;
    public TextView diaryTextView, textView2, textView3;
    public EditText contextEditText;
    private ArrayList<User> arrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    ArrayList<String> toDoList;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    FragmentManager fragmentManager;
    /*protected void addList(){
        fragmentManager=getActivity().getSupportFragmentManager();
        database= FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = database.getReference("gifticon");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스 데이터 받기
                arrayList.clear(); // 기존 배열 초기화
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문 List 추출
                    User user=snapshot.getValue(User.class); // 만들어 뒀던 User객체 받기
                    arrayList.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // DB를 가져오던중 에러 발생 시
                Log.e("GiftTraker",String.valueOf(error.toException())); // 에러 출력
            }
        });
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.axtivity_calendar,container,false);
        getParentFragmentManager().setFragmentResultListener("requsetKey", getActivity(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                arrayList = result.getParcelableArrayList("bundleKey");
            }
        });
        String[] result = {"2023,03,18","2023,04,18","2023,05,18","2023,12,3"};
        Button button1 = view.findViewById(R.id.re);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialCalendarView.removeDecorators();
                new ApiSimulator(result,arrayList).executeOnExecutor(Executors.newSingleThreadExecutor());
                materialCalendarView.addDecorators(
                        new SundayDecorator(),
                        new SaturdayDecorator(),
                        oneDayDecorator);

            }
        });
        listView = view.findViewById(R.id.list_view);
        listView.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;

                textView.setPaintFlags(textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }));
        materialCalendarView = view.findViewById(R.id.calendarView);
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(java.util.Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2022, 1, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2033, 12, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);
        new ApiSimulator(result,arrayList).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();
                String ymd = Year +"."+ Month +"."+ Day;
                //arrayList = new ArrayList<>();
                toDoList = new ArrayList<>();
                //addList();
                for(int i = 0; i < arrayList.size();i++){
                    if (ymd.equals(AdditionalFunction.dateSet(arrayList.get(i).getDate()))){
                        toDoList.add(arrayList.get(i).getStore()+"\n"+arrayList.get(i).getGiftName()+"\n");
                    }
                }
                addItemToList(toDoList);
                materialCalendarView.clearSelection();
            }
        });

        return view;

    }
    private void addItemToList(ArrayList<String> a) {
        adapter = new ArrayAdapter<String>(getContext(), R.layout.list_item,a);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;
        ArrayList<User> dateList;

        ApiSimulator(String[] Time_Result,ArrayList<User>dateList){
            this.Time_Result = Time_Result;
            this.dateList=dateList;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            //arrayList = new ArrayList<>();
            //calendar.setTime(setDate("2022.12.5"));
            for(int i=0;i<dateList.size();i++){
                calendar.setTime(setDate(AdditionalFunction.dateSet(dateList.get(i).getDate())));
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
            }


            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (getActivity().isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays,getActivity()));
        }

    }
    protected Date setDate(String d){
        SimpleDateFormat simple_format = new SimpleDateFormat("yyyy.MM.dd");
        Date date = null;

        try{
            date = simple_format.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}
