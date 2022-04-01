package com.example.todolist;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

class RepeatSet{
    int RepeatMode;//重复模式
    Date date_begin;//开始日期
    Date date_end;//结束日期
    List<String> DaysOfWeek;//周任务中的哪几天
    String DayOfMonth;//月任务中的哪一天
    String DayOfYear;//年任务中的哪一天
    RepeatSet(int RepeatMode,Date date_begin,Date date_end,List<String> DaysOfWeek,String DayOfMonth,
              String DayOfYear){
        this.RepeatMode=RepeatMode;
        this.date_begin=date_begin;
        this.date_end=date_end;
        this.DaysOfWeek=DaysOfWeek;
        this.DayOfMonth=DayOfMonth;
        this.DayOfYear=DayOfYear;
    }
    void setRepeatSet(int RepeatMode,Date date_begin,Date date_end,List<String> DaysOfWeek,String DayOfMonth,
                 String DayOfYear){
        this.RepeatMode=RepeatMode;
        this.date_begin=date_begin;
        this.date_end=date_end;
        this.DaysOfWeek=DaysOfWeek;
        this.DayOfMonth=DayOfMonth;
        this.DayOfYear=DayOfYear;
    }
}

// 重复设置监听
interface OnRepeatSetListener {
    // 回调方法
    void onRepeatSet(RepeatSet repeatSet);
}

public class RepeatSetDialog extends DialogFragment {
    private List<Fragment> fragments=new LinkedList<>();//重复模式不同下的fragment
    private ViewPager2 viewPager;
    private boolean is_ViewPagerOnClick=false;//viewpager中信号槽是否连接
    private BottomNavigationView bottomNavigationView;
    private TextView textView_begin;//开始日期
    private TextView textView_end;//结束日期
    private ImageButton button_close;//取消按钮
    private Button button_ok;//确认按钮
    private Calendar calendar=Calendar.getInstance(Locale.CHINA);//日历
    private DatePickerDialog datePickerDialog;//日期选择对话框
    boolean is_SelectBegin;//是选择开始日期还是结束日期
    //重复任务设置
    private RepeatSet repeatSet=new RepeatSet(0, new Date(0, 0, 0),
            new Date(0, 0, 0), new LinkedList<>(),"", "");
    OnRepeatSetListener onRepeatSetListener;//监听者

    public RepeatSetDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //添加布局
        return inflater.inflate(R.layout.repeat_set,container,false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*
        此方法在视图View已经创建后返回的，但是这个view 还没有添加到父级中。
        我们在这里可以重新设定view的各个数据，但是不能修改对话框最外层的ViewGroup的布局参数。
        因为这里的view还没添加到父级中，我们需要在下面onStart生命周期里修改对话框尺寸参数
         */
        viewPager=(ViewPager2)getView().findViewById(R.id.RepeatSelect);//获取布局中的对象
        bottomNavigationView=(BottomNavigationView)getView().findViewById(R.id.RepeatMode);
        textView_begin=(TextView) getView().findViewById(R.id.textView_begin);
        textView_end=(TextView)getView().findViewById(R.id.textView_end);
        button_close=(ImageButton)getView().findViewById(R.id.button_Repeatclose);
        button_ok=(Button)getView().findViewById(R.id.button_Repeatok);

        fragments.add(new DayFragment());//加入需要的各个fragment
        fragments.add(new WeekFragment());
        fragments.add(new MonthFragment());
        SelectRepeatFragmentPagerAdapter adapter=new SelectRepeatFragmentPagerAdapter(getParentFragmentManager(),
                getLifecycle(),fragments);//自定义adapter对象
        viewPager.setAdapter(adapter);//为viewpager设置adapter
        viewPager.setCurrentItem(0);//初始页面
        viewPager.setOffscreenPageLimit(3);//预加载
        viewPager.setUserInputEnabled(false);//设置不可滑动
        initOnClick();//信号槽连接
        initDatePicker();//初始化日期选择器
    }

    public void initOnClick(){
        //信号槽连接
        bottomNavigationView.setOnItemSelectedListener(this::onClick_Menu);
        textView_begin.setOnClickListener(this::onClick);
        textView_end.setOnClickListener(this::onClick);
        button_ok.setOnClickListener(this::onClick);
        button_close.setOnClickListener(this::onClick);

    }

    public void initViewPagerOnClick(){
        //viewPager中信号槽连接
        is_ViewPagerOnClick=true;
        //周任务
        ((Button)viewPager.findViewById(R.id.button_Mon)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Tues)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Wed)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Thur)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Fri)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Sat)).setOnClickListener(this::onClick_week);
        ((Button)viewPager.findViewById(R.id.button_Sun)).setOnClickListener(this::onClick_week);
        //月任务
        List<Fragment> list=((SelectRepeatFragmentPagerAdapter)viewPager.getAdapter()).fragmentList;
        MonthFragment monthFragment=(MonthFragment) list.get(2);
        monthFragment.GetNumberPicker().setOnValueChangedListener(this::onClick_month);

    }

    private void onClick_week(View view) {
        String day=null;
        switch(view.getId()){
            case R.id.button_Mon://星期一
                day="星期一";
                break;
            case R.id.button_Tues://星期二
                day="星期二";
                break;
            case R.id.button_Wed://星期三
                day="星期三";
                break;
            case R.id.button_Thur://星期四
                day="星期四";
                break;
            case R.id.button_Fri://星期五
                day="星期五";
                break;
            case R.id.button_Sat://星期六
                day="星期六";
                break;
            case R.id.button_Sun://星期天
                day="星期天";
                break;
            default:break;
        }
        if(((Button)view).isSelected()){
            repeatSet.DaysOfWeek.remove(day);
            ((Button)view).setSelected(false);
        }
        else{
            repeatSet.DaysOfWeek.add(day);
            ((Button)view).setSelected(true);
        }
    }

    private void onClick_month(NumberPicker numberPicker, int oldVal, int newVal) {
        String[] list=numberPicker.getDisplayedValues();
        repeatSet.DayOfMonth=list[newVal];
    }

    private void onClick(View view) {
        switch(view.getId()){
            case R.id.textView_begin://开始日期选择
                is_SelectBegin=true;
                datePickerDialog.show();
                break;
            case R.id.textView_end://结束日期选择
                is_SelectBegin=false;
                datePickerDialog.show();
                break;
            case R.id.button_Repeatclose://取消按钮
                dismiss();//关闭
                break;
            case R.id.button_Repeatok://确认按钮
                //此处返回设置的参数
                //repeatSet.setRepeatSet(viewPager.getCurrentItem(),date_begin,date_end,DaysOfWeek,DayOfMonth,DayOfYear);
                repeatSet.RepeatMode=viewPager.getCurrentItem();
                onRepeatSetListener.onRepeatSet(repeatSet);//返回重复设置参数
                dismiss();//关闭
                break;
            default: break;
        }
    }

    private boolean onClick_Menu(MenuItem menuItem) {//RepeatMode菜单栏监听
        if(!is_ViewPagerOnClick) initViewPagerOnClick();//viewpager中信号槽连接
        switch(menuItem.getItemId()){
            case R.id.select_day://每天
                viewPager.setCurrentItem(0,true);
                break;
            case R.id.select_week://每星期
                viewPager.setCurrentItem(1,true);
                break;
            case R.id.select_month://每月
                viewPager.setCurrentItem(2,true);
                break;

            case R.id.select_year://每年
                viewPager.setCurrentItem(3,true);
                break;
            default: break;
        }
        return true;
    }

    public void initDatePicker(){
        //初始化日期选择器
        datePickerDialog = new DatePickerDialog(getContext(), 0,
                new DatePickerDialog.OnDateSetListener(){//日期选中监听
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day){
                        if(is_SelectBegin){
                            //选择开始日期
                            repeatSet.date_begin.SetDate(year,month+1,day);
                            textView_begin.setText(repeatSet.date_begin.tostring());
                        }
                        else{
                            //选择结束日期
                            repeatSet.date_end.SetDate(year,month+1,day);
                            textView_end.setText(repeatSet.date_end.tostring());
                        }
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void setOnRepeatSetListener(OnRepeatSetListener onRepeatSetListener){
        this.onRepeatSetListener=onRepeatSetListener;//函数回调定义
    }


}