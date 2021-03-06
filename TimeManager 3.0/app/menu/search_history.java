package com.haibin.TimeManager.menu;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haibin.TimeManager.Adapter.BaseAdapter;
import com.haibin.TimeManager.Adapter.DragTouchAdapter;
import com.haibin.TimeManager.R;
import com.haibin.TimeManager.Todo.Todo;
import com.haibin.TimeManager.activity.MainActivity;
import com.haibin.TimeManager.menu.search_dustbin;
import com.haibin.TimeManager.menu.search_history;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.touch.OnItemStateChangedListener;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

//import org.litepal.crud.DataSupport;


public class search_history extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected SearchView mSearchView = null;
    String search_str=null;
    protected List<Todo> HistoryList;
    protected SwipeRecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager=new LinearLayoutManager(this);
    protected RecyclerView.ItemDecoration mItemDecoration;
    protected BaseAdapter mAdapter;

    private Calendar calendar= Calendar.getInstance(Locale.CHINA);//??????????????????
    private String mdate;
    private LocalReceiver localReceiver;    //?????????????????????
    private LocalBroadcastManager localBroadcastManager;   //?????????????????????   ????????????????????????
    private IntentFilter intentFilter;
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
                    HistoryList= LitePal.where("is_delete = ? and date < ?","false",mdate).
                            order("date desc").find(Todo.class);
                    mAdapter.notifyDataSetChanged(HistoryList);
                }
                return true;
            }
        });
        mItemDecoration = createItemDecoration();
        mAdapter = createAdapter();

        mRecyclerView=findViewById(R.id.recycler_view_history);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mItemDecoration);
        mRecyclerView.setOnItemClickListener(this::onItemClick);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLongPressDragEnabled(true); // ??????????????????????????????
        mRecyclerView.setItemViewSwipeEnabled(true); // ??????????????????????????????
        mRecyclerView.setOnItemStateChangedListener(mOnItemStateChangedListener); // ??????Item?????????????????????????????????????????????
        mRecyclerView.setOnItemMoveListener(getItemMoveListener());// ????????????????????????????????????UI???????????????
        //?????????????????????
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        localReceiver=new search_history.LocalReceiver();
        intentFilter=new IntentFilter("myaction");
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
        HistoryList= LitePal.where("is_delete = ? and date<?","false",mdate).
                order("date desc").find(Todo.class);
        mAdapter.notifyDataSetChanged(HistoryList);
    }
    protected BaseAdapter createAdapter() {
        return new DragTouchAdapter(this,mRecyclerView);
    }
    protected int getContentView() {
        return R.layout.activity_main_temp;
    }
    protected RecyclerView.ItemDecoration createItemDecoration() {
        return new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divider_color));
    }
    public void onItemClick(View itemView, int position) {
        Toast.makeText(this, "???" + position + "???", Toast.LENGTH_SHORT).show();
        //???????????????????????????
        EditTodoDialog editTodoDialog = new EditTodoDialog(HistoryList.get(position).getId());
        editTodoDialog.setOnTodoEditListener(new OnTodoEditListener() {
            @Override
            public void onTodoEdit() {//????????????
                HistoryList= LitePal.where("todo like ? and is_delete = ? and date < ?","%"+search_str+"%","false",mdate).
                        order("date desc").find(Todo.class);
                mAdapter.notifyDataSetChanged(HistoryList);
            }
        });
        editTodoDialog.show(getSupportFragmentManager(),"EditDialog");
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(1,getIntent());
            finish();
        }
        return true;
    }

    public void SearchHistory()
    {
        Log.w("TAG6","search history begin"+search_str);
        HistoryList= LitePal.where("todo like ? and is_delete = ? and date<?","%"+search_str+"%","false",mdate).
                order("date desc").find(Todo.class);
//        HistoryList=LitePal.findAll(Todo.class);
        Log.w("TAG7","search history over"+HistoryList.size());
        mAdapter.notifyDataSetChanged(HistoryList);

    }
    private class LocalReceiver extends BroadcastReceiver {//?????????adapter?????????
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if("myaction".equals(action)){
                Log.d( "?????????" + intent.getStringExtra( "data" )  , "????????? " + Thread.currentThread().getName() ) ;
            }
            String todoname=intent.getStringExtra("todoname");
            boolean isdone=intent.getBooleanExtra("is_done",false);

            //???????????????????????????????????????position????????????getadapterposition???????????????
            int fromPosition=intent.getIntExtra("position",0);

            //?????????????????????is_done
            Todo updatetodo=new Todo();
            if(isdone) updatetodo.setIs_done(isdone);
            else updatetodo.setToDefault("is_done");//???set???false?????????????????????
            updatetodo.updateAll("todo=?",todoname);

            //??????????????????????????????todo???position??????????????????
            //????????????????????????is_done??????true?????????is_done??????false???
            if(isdone) {
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
    }



    //?????????????????????
    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
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
            String  todoname=HistoryList.get(position).getTodo();
            //??????????????????????????????
            new AlertDialog.Builder(search_history.this)
                    .setMessage("???????????????????????????"+todoname)
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(search_history.this, "????????????????????????~", Toast.LENGTH_SHORT).show();
                            Todo updatetodo=new Todo();
                            updatetodo.setIs_delete("true");
                            updatetodo.updateAll("todo=?",todoname);

                            HistoryList.remove(position);
                            //LitePal.deleteAll(Todo.class,"todo=?",todo);
                            mAdapter.notifyItemRemoved(position);

                            Toast.makeText(search_history.this, "????????????" + position + "???????????????", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((DragTouchAdapter)mAdapter).updateItemsData(HistoryList);//????????????
                        }
                    })//?????????????????????????????????????????????????????????????????????????????????
                    .show();
//            //???????????????todo????????????????????????
//
//            Todo updatetodo=new Todo();
//            updatetodo.setIs_delete(true);
//            updatetodo.updateAll("todo=?",todoname);
//
//            mToDoList.remove(position);
//            //LitePal.deleteAll(Todo.class,"todo=?",todo);
//            mAdapter.notifyItemRemoved(position);
//
//            Toast.makeText(MainActivity.this, "????????????" + position + "???????????????", Toast.LENGTH_SHORT).show();
        }

    };
//    public void refresh(){  //??????recyclerView
//        mToDoList=LitePal.findAll(Todo.class);
    //recyclerView=findViewById(R.id.recycler_view);
//        TaskAdapter adapter=new TaskAdapter(toDoList);//??????adapter??????????????????recyclerview
//        recyclerView.setAdapter(adapter);

    //    }
    public void onClick_Dialog(View view){//????????????

        switch(view.getId()){
            case R.id.fab:
                AddTodoDialog addTodoDialog = new AddTodoDialog();
                //????????????????????????????????????recyclerView
                addTodoDialog.setOnTodoAddListener(new OnTodoAddListener() {
                    @Override
                    public void onTodoAdd() {
                        HistoryList=LitePal.findAll(Todo.class);
                        mAdapter.notifyDataSetChanged(HistoryList);
                    }
                } );
                addTodoDialog.show(getSupportFragmentManager(),"tag");//???????????????

                break;
            default: break;
        }
    }
}