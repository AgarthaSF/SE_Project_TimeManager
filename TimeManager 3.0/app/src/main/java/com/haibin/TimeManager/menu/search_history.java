package com.haibin.TimeManager.menu;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haibin.TimeManager.Adapter.DragTouchAdapter;
import com.haibin.TimeManager.EditTodoDialog.EditTodoDialog;
import com.haibin.TimeManager.EditTodoDialog.OnTodoEditListener;
import com.haibin.TimeManager.Pomodoro.PomodoroActivity;
import com.haibin.TimeManager.R;
import com.haibin.TimeManager.Statistics.StatisticsActivity;
import com.haibin.TimeManager.Todo.Todo;
import com.haibin.TimeManager.calendar.full.FullActivity;
import com.haibin.TimeManager.cancel_delete_fragment;
import com.haibin.TimeManager.showActivity;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.touch.OnItemStateChangedListener;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

//import org.litepal.crud.DataSupport;


public class search_history extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected SearchView mSearchView = null;
    String search_str=null;
    protected List<Todo> HistoryList;
    protected SwipeRecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager=new LinearLayoutManager(this);
    protected DragTouchAdapter mAdapter;

    private Calendar calendar= Calendar.getInstance(Locale.CHINA);//??????????????????
    private String mdate;
    private LocalReceiver localReceiver;    //?????????????????????
    private LocalBroadcastManager localBroadcastManager;   //?????????????????????   ????????????????????????
    private IntentFilter intentFilter;
    private IntentFilter intentFilter1;
    private Timer timer;
    private TimerTask timerTask;
    cancel_delete_fragment cancelDeleteFragment;
    protected Todo tempTodo;
    boolean fragment_flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history);
        mToolbar=findViewById(R.id.toolbar_history);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSearchView = (SearchView) findViewById(R.id.searchView_history);

        mdate = Integer.toString(calendar.get(Calendar.YEAR))+'/'+
                String.format("%02d",calendar.get(Calendar.MONTH)+1)+'/'+
                String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // ???????????????????????????????????????
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchHistory();
                Log.w("TAG8","search history change");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    search_str=newText;
                }else{
                    HistoryList= LitePal.where("is_delete = ? and date < ?", "0", mdate).
                            order("date desc").find(Todo.class);
                    mAdapter.notifyDataSetChanged(HistoryList);
                }
                return true;
            }
        });

        mRecyclerView=findViewById(R.id.recycler_view_history);
        mAdapter = new DragTouchAdapter(this,mRecyclerView);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setOnItemClickListener(this::onItemClick);
        mRecyclerView.setAdapter(mAdapter);
        //???checkbox????????????mTodolist??????????????????????????????????????????????????????????????????????????????

        mRecyclerView.setLongPressDragEnabled(true); // ??????????????????????????????
        mRecyclerView.setItemViewSwipeEnabled(true); // ??????????????????????????????
        mRecyclerView.setOnItemStateChangedListener(mOnItemStateChangedListener); // ??????Item?????????????????????????????????????????????
        mRecyclerView.setOnItemMoveListener(getItemMoveListener());// ????????????????????????????????????UI???????????????
        //?????????????????????
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        localReceiver=new LocalReceiver();
        intentFilter=new IntentFilter("myaction4");
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
        intentFilter1=new IntentFilter("MyAction4");//cancel_delete
        localBroadcastManager.registerReceiver(localReceiver,intentFilter1);


        fragment_flag=false;

        ImageButton button_todo = (ImageButton)findViewById(R.id.button_todo);
        button_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(search_history.this, showActivity.class);
                startActivity(intent);
            }
        });

        ImageButton button_calendar = (ImageButton)findViewById(R.id.button_calendar);
        button_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(search_history.this, FullActivity.class);
                startActivity(intent);
            }
        });

        ImageButton button_clock = (ImageButton)findViewById(R.id.button_clock);
        button_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(search_history.this, PomodoroActivity.class);
                startActivity(intent);
            }
        });

        ImageButton button_statistics = (ImageButton)findViewById(R.id.button_statistics);
        button_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(search_history.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(1,getIntent());
            if(fragment_flag)
            {
                timerTask.cancel();
                removeFragment2();
            }
            finish();
        }
        return true;
    }

    public void SearchHistory()
    {
        Log.w("TAG6","search history begin"+search_str);
        HistoryList= LitePal.where("todo like ? and is_delete = ? and date<?","%"+search_str+"%", "0",mdate).
                order("date desc").find(Todo.class);
//        HistoryList=LitePal.findAll(Todo.class);
        Log.w("TAG7","search history over"+HistoryList.size());
        mAdapter.notifyDataSetChanged(HistoryList);

    }
    //?????????????????????????????????????????????????????????
    public void init_is_done(){
        for(int i=0;i<HistoryList.size();i++){
            //?????????
            mAdapter.flag[i]=false;//???????????????????????????

        }
        for(int i=0;i<HistoryList.size();i++){
            if(HistoryList.get(i).getIs_done()){//?????????
                mAdapter.flag[i]=true;
            }
        }


    }
    public void swap_position(){
        int k=HistoryList.size();
        for(int i=0;i<k;){
            if(HistoryList.get(i).getIs_done()){
                k--;
                for (int j = i; j <HistoryList.size()-1; j++) {
                    Collections.swap(HistoryList, j, j + 1);//????????????????????????????????????
                    //swap(mAdapter.flag,j,j+1);
                    //mAdapter.notifyItemMoved(j, j + 1);

                }
            }
            else i++;
        }
    }
    private class LocalReceiver extends BroadcastReceiver {//?????????adapter?????????
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("LAG_his_localreceiver","0");
            String action=intent.getAction();
            if("myaction4".equals(action)){
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
                    int toPosition = HistoryList.size() - 1;
                    if (fromPosition < toPosition) {
                        //????????????????????????????????????item?????????list??????item?????????????????????????????????
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(HistoryList, i, i + 1);//????????????????????????????????????
                            mAdapter.notifyItemMoved(i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(HistoryList, i, i - 1);//????????????????????????????????????
                            mAdapter.notifyItemMoved(i, i - 1);
                        }
                    }
                }
                else{
                    int toPosition = 0;
                    if (fromPosition < toPosition) {
                        //????????????????????????????????????item?????????list??????item?????????????????????????????????
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(HistoryList, i, i + 1);//????????????????????????????????????
                            mAdapter.notifyItemMoved(i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(HistoryList, i, i - 1);//????????????????????????????????????
                            mAdapter.notifyItemMoved(i, i - 1);
                        }
                    }
                }
            }
            if("MyAction4".equals(action)){
                //Toast.makeText(search_history.this, "?????????????????????", Toast.LENGTH_LONG).show();
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
                LinearLayout noInfoContent = findViewById(R.id.noInfoContent_history);
                if(HistoryList.size() == 0){
                    noInfoContent.setVisibility(View.VISIBLE);
                }else{
                    noInfoContent.setVisibility(View.INVISIBLE);
                }
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
        EditTodoDialog editTodoDialog = new EditTodoDialog(HistoryList.get(position).getId());
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
                        ContextCompat.getColor(search_history.this, R.color.white_pressed));
            } else if (actionState == OnItemStateChangedListener.ACTION_STATE_SWIPE) {
            } else if (actionState == OnItemStateChangedListener.ACTION_STATE_IDLE) {
                // ????????????????????????????????????
                ViewCompat.setBackground(viewHolder.itemView,
                        ContextCompat.getDrawable(search_history.this, R.drawable.select_white));
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
            //?????????position????????????????????????
            Collections.swap(HistoryList, fromPosition, toPosition);
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;// ??????true??????????????????????????????????????????false??????????????????????????????????????????
        }

        //???Item?????????
        @Override
        public void onItemDismiss(RecyclerView.ViewHolder srcHolder) {
            int position = srcHolder.getAdapterPosition();
            String  Todo_name=HistoryList.get(position).getTodo();
            tempTodo=HistoryList.get(position);
            int Todo_id=HistoryList.get(position).getId();
            Todo Update_Todo=new Todo();
            Update_Todo.setIs_delete(true);
            Update_Todo.updateAll("id = ?",String.valueOf(Todo_id));
            List<Todo> test=LitePal.findAll(Todo.class);

            HistoryList.remove(position);
            mAdapter.notifyItemRemoved(position);
            //????????????todo?????????position,?????????????????????
            if(fragment_flag)
            {
                timerTask.cancel();
                removeFragment2();
            }
            cancelDeleteFragment = new cancel_delete_fragment();
            addFragment(cancelDeleteFragment, "fragment3");
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
            LinearLayout noInfoContent = findViewById(R.id.noInfoContent_history);
            if(HistoryList.size() == 0){
                noInfoContent.setVisibility(View.VISIBLE);
            }else{
                noInfoContent.setVisibility(View.INVISIBLE);
            }
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
        fragment_flag=true;
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.add(R.id.fragment_container_history, fragment, tag);
        transaction.commit();
    }

    private void deleteFragment(Fragment fragment){
        fragment_flag=false;
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }

    private void removeFragment2() {
        fragment_flag=false;

        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("fragment3");
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        HistoryList= LitePal.where("is_delete = ? and date<?", "0",mdate).
                order("date desc").find(Todo.class);
        swap_position();
        init_is_done();//??????????????????
        mAdapter.notifyDataSetChanged(HistoryList);


        LinearLayout noInfoContent = findViewById(R.id.noInfoContent_history);
        if(HistoryList.size() == 0){
            noInfoContent.setVisibility(View.VISIBLE);
        }else{
            noInfoContent.setVisibility(View.INVISIBLE);
        }
    }
}