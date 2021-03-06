package com.haibin.TimeManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haibin.TimeManager.Adapter.BaseAdapter;
import com.haibin.TimeManager.Adapter.DragTouchAdapter;
import com.haibin.TimeManager.AddTodoDialog.AddTodoDialog;
import com.haibin.TimeManager.AddTodoDialog.OnTodoAddListener;
import com.haibin.TimeManager.EditTodoDialog.EditTodoDialog;
import com.haibin.TimeManager.EditTodoDialog.OnTodoEditListener;
import com.haibin.TimeManager.Pomodoro.PomodoroActivity;
import com.haibin.TimeManager.Statistics.StatisticsActivity;
import com.haibin.TimeManager.Todo.Date;
import com.haibin.TimeManager.Todo.Todo;
import com.haibin.TimeManager.calendar.full.FullActivity;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.touch.OnItemStateChangedListener;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class showDailyTodoActivity extends AppCompatActivity {

    protected SwipeRecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager=new LinearLayoutManager(this);
    protected RecyclerView.ItemDecoration mItemDecoration;
    protected List<Todo> mToDoList;
    protected DragTouchAdapter mAdapter;
    private showDailyTodoActivity.LocalReceiver localReceiver;   //?????????????????????
    private LocalBroadcastManager localBroadcastManager;   //?????????????????????   ????????????????????????
    private IntentFilter intentFilter;
    private String curDate;

    private IntentFilter intentFilter1;
    private Timer timer;
    private TimerTask timerTask;
    cancel_delete_fragment cancelDeleteFragment;
    protected Todo tempTodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_daily_todo);

        Intent intent = getIntent();
        String currentDate = intent.getStringExtra("extra_data");
        curDate = currentDate;
        String showDate = "????????????  " + currentDate.substring(6, 7) + "???" + currentDate.substring(8, 10) + "???";
        this.setTitle(showDate);

        mRecyclerView=findViewById(R.id.recycler_view);
        mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divider_color));
        mAdapter = new DragTouchAdapter(this,mRecyclerView);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mItemDecoration);
        mRecyclerView.setOnItemClickListener(this::onItemClick);
        mRecyclerView.setAdapter(mAdapter);
        //???checkbox????????????mTodolist??????????????????????????????????????????????????????????????????????????????

        mRecyclerView.setLongPressDragEnabled(true); // ??????????????????????????????
        mRecyclerView.setItemViewSwipeEnabled(true); // ??????????????????????????????
        mRecyclerView.setOnItemStateChangedListener(mOnItemStateChangedListener); // ??????Item?????????????????????????????????????????????
        mRecyclerView.setOnItemMoveListener(getItemMoveListener());// ????????????????????????????????????UI???????????????

        FloatingActionButton adddata=findViewById(R.id.fab);
        adddata.setOnClickListener(this::onClick_Dialog);//????????????

        //?????????????????????
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        localReceiver=new showDailyTodoActivity.LocalReceiver();
        intentFilter=new IntentFilter("myaction6");
        intentFilter1=new IntentFilter("MyAction5");//cancel_delete
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
        localBroadcastManager.registerReceiver(localReceiver,intentFilter1);



        mToDoList= LitePal.where("is_delete = ? and date=?", "0",curDate).
                order("date desc").find(Todo.class);
        mAdapter.notifyDataSetChanged(mToDoList);
        LinearLayout noInfoContent = findViewById(R.id.noInfoContent);
        if(mToDoList.size() == 0){
            noInfoContent.setVisibility(View.VISIBLE);
        }else{
            noInfoContent.setVisibility(View.INVISIBLE);
        }


        ImageButton button_todo = (ImageButton)findViewById(R.id.button_todo);
        button_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(showDailyTodoActivity.this, showActivity.class);
                startActivity(intent);
            }
        });

        ImageButton button_calendar = (ImageButton)findViewById(R.id.button_calendar);
        button_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(showDailyTodoActivity.this, FullActivity.class);
                startActivity(intent);
            }
        });
        button_calendar.setBackgroundColor(Color.parseColor("#D7D7D7"));

        ImageButton button_clock = (ImageButton)findViewById(R.id.button_clock);
        button_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(showDailyTodoActivity.this, PomodoroActivity.class);
                startActivity(intent);
            }
        });

        ImageButton button_statistics = (ImageButton)findViewById(R.id.button_statistics);
        button_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(showDailyTodoActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });
    }














    //?????????????????????????????????????????????????????????
    public void init_is_done(){
        for(int i=0;i<mToDoList.size();i++){
            //?????????
            mAdapter.flag[i]=false;//???????????????????????????

        }
        for(int i=0;i<mToDoList.size();i++){
            if(mToDoList.get(i).getIs_done()){//?????????
                mAdapter.flag[i]=true;
            }
        }
    }

    public void swap_position(){
        // ??????????????????????????????  ???????????????????????? ???????????????????????????????????????????????????
        Collections.sort(mToDoList);
        // ??????????????????????????????????????????
        int k=mToDoList.size();
        for(int i=0;i<k;){
            if(mToDoList.get(i).getIs_done()){
                k--;
                for (int j = i; j <mToDoList.size()-1; j++) {
                    Collections.swap(mToDoList, j, j + 1);//????????????????????????????????????
                }
            }
            else i++;
        }
    }

    private class LocalReceiver extends BroadcastReceiver {//?????????adapter?????????
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if("myaction6".equals(action)){
                Log.d( "?????????" + intent.getStringExtra( "data" )  , "????????? " + Thread.currentThread().getName() ) ;
                String todo_name=intent.getStringExtra("to_doname");
                int todo_id=intent.getIntExtra("todo_id",0);
                boolean is_done=intent.getBooleanExtra("is_done",false);
                //???????????????????????????????????????position????????????getAdapterPosition???????????????
                int fromPosition=intent.getIntExtra("position",0);

                Todo UpdateTodo=new Todo();
                if(is_done) UpdateTodo.setIs_done(is_done);
                else UpdateTodo.setToDefault("is_done");//???set???false?????????????????????
                UpdateTodo.updateAll("id = ?",String.valueOf(todo_id));

                //flag???????????????

                List<Todo> test=LitePal.findAll(Todo.class);

                //??????????????????????????????todo???position??????????????????
                //????????????????????????is_done??????true?????????is_done??????false???
                if(is_done) {
                    onResume();
                }
                else{
                    onResume();
                }
            }

            if("MyAction1".equals(action)){
                //Toast.makeText(showActivity.this, "?????????????????????", Toast.LENGTH_LONG).show();
                //?????????????????????????????????????????????????????????????????????
                List<Todo> TestList1=LitePal.findAll(Todo.class);

                int Todo_id=tempTodo.getId();
                Todo UpdateTodo=new Todo();
                UpdateTodo.setToDefault("is_delete");
                UpdateTodo.updateAll("id = ?",String.valueOf(Todo_id));
                List<Todo> TestList=LitePal.findAll(Todo.class);
                //?????????????????????
                onResume();
                removeFragment2();
            }
        }
    }


    //?????????????????????
    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }
    //????????????
    public void onItemClick(View itemView, int position) {
        //Toast.makeText(this, "???" + position + "???", Toast.LENGTH_SHORT).show();
        //???????????????????????????
        EditTodoDialog editTodoDialog = new EditTodoDialog(mToDoList.get(position).getId());
        editTodoDialog.setOnTodoEditListener(new OnTodoEditListener() {
            @Override
            public void onTodoEdit() {//????????????
                onResume();
            }
        });
        editTodoDialog.show(getSupportFragmentManager(),"EditDialog");
    }
    protected OnItemMoveListener getItemMoveListener() {
        return onItemMoveListener;
    }
    //Item?????????/???????????????????????????????????????????????????
    //????????????Item?????????/??????????????????????????????
    private final OnItemStateChangedListener mOnItemStateChangedListener = new OnItemStateChangedListener() {
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == OnItemStateChangedListener.ACTION_STATE_DRAG) {
                // ?????????????????????????????????????????????????????????????????????????????????
                viewHolder.itemView.setBackgroundColor(
                        ContextCompat.getColor(showDailyTodoActivity.this, R.color.white_pressed));
            } else if (actionState == OnItemStateChangedListener.ACTION_STATE_SWIPE) {
            } else if (actionState == OnItemStateChangedListener.ACTION_STATE_IDLE) {
                // ????????????????????????????????????
                ViewCompat.setBackground(viewHolder.itemView,
                        ContextCompat.getDrawable(showDailyTodoActivity.this, R.drawable.select_white));
            }
        }
    };
    //????????????????????????????????????UI????????????
    private final OnItemMoveListener onItemMoveListener = new OnItemMoveListener() {
        @Override
        public boolean onItemMove(RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
            // ?????????ViewType????????????????????????
            if (srcHolder.getItemViewType() != targetHolder.getItemViewType()) return false;

            int fromPosition = srcHolder.getAdapterPosition();
            int toPosition = targetHolder.getAdapterPosition();
            int fix = 0; //??????????????????????????????????????????????????????????????????
            for (int i = 0; i < mToDoList.size(); i++) {
                if (mToDoList.get(i).getIs_done() == false) {
                    fix++;
                }
            }
            if (fromPosition >= fix || toPosition >= fix) return false;
            else {
                //?????????position?????????????????????
                //??????pos??????
                int from = mToDoList.get(fromPosition).getPos();
                int to=mToDoList.get(toPosition).getPos();
                Todo UpdateTodo_from = new Todo();
                if(to==0) UpdateTodo_from.setToDefault("pos");
                else UpdateTodo_from.setPos(to);
                UpdateTodo_from.updateAll("id=?", String.valueOf(mToDoList.get(fromPosition).getId()));
                Todo UpdateTodo_to = new Todo();
                if(to==0) UpdateTodo_to.setToDefault("pos");
                else UpdateTodo_to.setPos(from);
                UpdateTodo_to.updateAll("id=?", String.valueOf(mToDoList.get(toPosition).getId()));
                Collections.swap(mToDoList, fromPosition, toPosition);
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;// ??????true??????????????????????????????????????????false??????????????????????????????????????????
            }
        }

        //???Item?????????
        @Override
        public void onItemDismiss(RecyclerView.ViewHolder srcHolder) {

            if(timer!=null) timer.cancel();
            if(timerTask!=null) timerTask.cancel();
            if(cancelDeleteFragment!=null)deleteFragment(cancelDeleteFragment);


            int position = srcHolder.getAdapterPosition();
            String  Todo_name=mToDoList.get(position).getTodo();
            tempTodo=mToDoList.get(position);
            int Todo_id=mToDoList.get(position).getId();
            Todo Update_Todo=new Todo();
            Update_Todo.setIs_delete(true);
            Update_Todo.updateAll("id = ?",String.valueOf(Todo_id));
            List<Todo> test=LitePal.findAll(Todo.class);

            mToDoList.remove(position);
            mAdapter.notifyItemRemoved(position);

            LinearLayout noInfoContent = findViewById(R.id.noInfoContent);
            if(mToDoList.size() == 0){
                noInfoContent.setVisibility(View.VISIBLE);
            }else{
                noInfoContent.setVisibility(View.INVISIBLE);
            }


            //????????????todo?????????position,?????????????????????
            cancelDeleteFragment = new cancel_delete_fragment();
            addFragment(cancelDeleteFragment, "fragment1");
            //??????????????????10s???cancel_delete Fragment??????????????????
            timerTask =new TimerTask() {
                @Override
                public void run() {
                    Message msg=new Message();
                    msg.what=0;
                    handler.sendMessage(msg);
                }
            };
            timer=new Timer();
            timer.schedule(timerTask,5000);

        }
    };


    //?????????????????????????????????
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()

    {
        public void handleMessage(Message msg) {
            deleteFragment(cancelDeleteFragment);
        }

    };
    //???????????????cancel_delete Fragment
    private void addFragment(Fragment fragment, String tag) {
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment, tag);
        transaction.commitAllowingStateLoss();
    }
    private void deleteFragment(Fragment fragment){
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }
    private void removeFragment2() {
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("fragment1");
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }
    public void onClick_Dialog(View view){//????????????
        switch(view.getId()){
            case R.id.fab:

                int year = Integer.valueOf(curDate.substring(0, 4));
                int month = Integer.valueOf(curDate.substring(6, 7));
                int day = Integer.valueOf(curDate.substring(8, 10));
                Date currentDate = new Date(year, month, day);

                AddTodoDialog addTodoDialog = new AddTodoDialog(currentDate);
                //????????????????????????????????????recyclerView
                addTodoDialog.setOnTodoAddListener(new OnTodoAddListener() {
                    @Override
                    public void onTodoAdd() {
                        onResume();
                    }
                } );
                addTodoDialog.show(getSupportFragmentManager(),"tag");//???????????????

                break;
            default: break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        int year = Integer.valueOf(curDate.substring(0, 4));
        int month = Integer.valueOf(curDate.substring(6, 7));
        int day = Integer.valueOf(curDate.substring(8, 10));
        String currentDate = String.valueOf(year) + "/" + String.format("%02d", month) + "/" +String.format("%02d", day);

        mToDoList= LitePal.where("is_delete = ? and date=?", "0",currentDate).
                order("date desc").find(Todo.class);
        swap_position();
        init_is_done();//??????????????????
        mAdapter.notifyDataSetChanged(mToDoList);


        LinearLayout noInfoContent = findViewById(R.id.noInfoContent);
        if(mToDoList.size() == 0){
            noInfoContent.setVisibility(View.VISIBLE);
        }else{
            noInfoContent.setVisibility(View.INVISIBLE);
        }
    }
}
