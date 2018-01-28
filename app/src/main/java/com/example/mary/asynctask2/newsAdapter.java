package com.example.mary.asynctask2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * 项目名:    AsyncTask2
 * 包名：     com.example.mary.asynctask2
 * 创建者：   Mary
 * 创建时间:  2018/1/27 16:27
 * 描述：     TODO
 */

public class newsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<newsBean> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart, mEnd;
    //用于保存所有图片的url地址
    public static String[] URLS;
    private boolean mFirstIn;

    public newsAdapter(Context context, List<newsBean> mList, ListView listView) {
        this.mList = mList;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);
        URLS = new String[mList.size()];
        for (int i = 0; i < mList.size(); i++) {
            URLS[i] = mList.get(i).picSmall;
        }
        mFirstIn = true;
        //一定要注册对应的事件
        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout, null);
            viewHolder.lv_image = convertView.findViewById(R.id.iv_image);
            viewHolder.tv_title = convertView.findViewById(R.id.tv_title);
            viewHolder.tv_content = convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.lv_image.setImageResource(R.mipmap.ic_launcher);

        String url = mList.get(position).picSmall;
        viewHolder.lv_image.setTag(url);

        //使用多线程方法
        //new ImageLoader().showImageByThread(viewHolder.lv_image,url);

        //使用AsyncTask方法
        mImageLoader.showImageAsyncTask(viewHolder.lv_image, url);

        viewHolder.tv_title.setText(mList.get(position).name);
        viewHolder.tv_content.setText(mList.get(position).description);
        return convertView;
    }

    class ViewHolder {
        public TextView tv_title, tv_content;
        public ImageView lv_image;
    }

    //listView滑动状态切换的时候才会调用
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            //停止状态，加载可见项
            mImageLoader.loadImages(mStart, mEnd);
        } else {
            //滑动状态，停止任务
            mImageLoader.cancelAllTask();
        }
    }

    //在整个滑动过程中都会调用
    //firstVisibleItem第一个可见元素 visibleItemCount 可见元素长度
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        //第一次显示的时候调用
        if (mFirstIn && visibleItemCount > 0) {
            mImageLoader.loadImages(mStart,mEnd);
            mFirstIn = false;
        }
    }

}
