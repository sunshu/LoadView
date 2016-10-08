package nus.me.loadview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private LinkedList<String> mDatas;
    private ListView mListView;
    private RefreshLayout mRefreshLayout;
    private BaseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initListener();
    }


    private void initData() {

        mDatas = new LinkedList<>();
        Collections.addAll(mDatas, "第1条数据", "第2条数据", "第3条数据",
                "第4条数据", "第5条数据", "第6条数据", "第7条数据", "第8条数据"
                , "第9条数据", "第10条数据", "第11条数据", "第12条数据", "第13条数据");
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refreshlayout);
        //设置上拉加载更多可用
        mRefreshLayout.setLoadMoreEnable(true);
        mAdapter = new BaseAdapter(){
            @Override
            public int getCount() {
                return mDatas.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setGravity(Gravity.CENTER);
                ImageView iv = new ImageView(MainActivity.this);
                iv.setImageResource(R.mipmap.ic_launcher);
                TextView textView = new TextView(MainActivity.this);
                textView.setText(mDatas.get(position));
                ll.addView(iv);
                ll.addView(textView);
                return ll;
            }
        };
        mListView.setAdapter(mAdapter);
    }


    private void initListener() {
        mRefreshLayout.setRefreshHandler(new RefreshAndLoadImp() {
            @Override
            public void onRefresh(RefreshLayout refreshimp) {
                refreshimp.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.addFirst("下拉刷新增加的数据");
                        mAdapter.notifyDataSetChanged();
                        mRefreshLayout.refreshComplete();
                    }
                },2000);
            }



            @Override
            public void onLoadMore(RefreshLayout refreshimp) {
                refreshimp.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.addLast("上拉增加的数据");
                        mAdapter.notifyDataSetChanged();
                        mRefreshLayout.LoadMoreComplete();
                    }
                },2000);
            }
        });


    }



}
