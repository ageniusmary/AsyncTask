package com.example.mary.asynctask2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


/**
 * 项目名:    AsyncTask2
 * 包名：     com.example.mary.asynctask2
 * 创建者：   Mary
 * 创建时间:  2018/1/27 19:57
 * 描述：     TODO
 */

public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;
    //创建cache
    private LruCache<String,Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;

    public ImageLoader (ListView listView){

        this.mListView = listView;
        mTask = new HashSet<>();

        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory/4;
        mCaches = new LruCache<String,Bitmap>(cacheSize){

            //此方法用于获取每一个存进去对象的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用 getByteCount()获取bitmap实际大小
                return value.getByteCount();
            }
        };

    }

    //增加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url) == null){
            mCaches.put(url,bitmap);
        }
    }

    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap((Bitmap)msg.obj);
            }
        }
    };

    //使用多线程进行异步加载
    public void showImageByThread(ImageView imageView, final String url){

        mImageView = imageView;
        mUrl = url;

        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(url);
                //通过Message.obtain() 创建的message可以使用现有的 已经回收掉的 提高message的使用效率
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    public Bitmap getBitmapFromUrl(String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            //将一个Stream转化成bitmap
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //使用AsyncTask进行异步加载
    public void showImageAsyncTask(ImageView imageView,String url){
        //从缓存中取出对应的图片
        Bitmap bitmap = getBitmapFromCache(url);
        //如果缓存中没有，那么去网络中下载
        if(bitmap == null){
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }

    public void cancelAllTask(){
        if(mTask != null){
            for (NewsAsyncTask task:mTask) {
                task.cancel(false);
            }
        }
    }

    //用来加载从start到end的所有图片
    public void loadImages(int start,int end){
        for (int i = start; i < end; i++) {
            String url = newsAdapter.URLS[i];
            //从缓存中取出对应的图片
            Bitmap bitmap = getBitmapFromCache(url);
            //如果缓存中没有，那么去网络中下载
            if(bitmap == null){
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }else{
                ImageView imageView = mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{

       // private ImageView mImageView;
        private String mUrl;

        public NewsAsyncTask(String url) {
          //  this.mImageView = mImageView;
            this.mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            //从网络获取图片
            Bitmap bitmap = getBitmapFromUrl(strings[0]);
            if(bitmap != null){
                //将不再缓存中的图片加入缓存
                addBitmapToCache(url,bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = mListView.findViewWithTag(mUrl);
            if(imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
