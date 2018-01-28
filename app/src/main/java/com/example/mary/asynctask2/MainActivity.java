package com.example.mary.asynctask2;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mlistView;
    private static final String URL = "http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mlistView = findViewById(R.id.lv_list);
        new NewAsyncTask().execute(URL);
    }

        /**
         * 将url对应的json格式数据转化为我们所封装的newsBean
         * @param url
         * @return
         */
        private List<newsBean> getJsonData (String url){
            List<newsBean> newsBeanList = new ArrayList<>();
            try {
                /**  new URL(url).openStream() 此句功能与url.openConnection().getInputStream()相同
                 *   可根据url直接联网获取网络数据，简单粗暴
                 *   返回值类型是inputStream
                 */
                String jsonString = readStream(new URL(url).openStream());
                JSONObject jsonObject;
                newsBean newsBean;
                try {
                    jsonObject = new JSONObject(jsonString);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        newsBean = new newsBean();
                        newsBean.name = jsonObject.getString("name");
                        newsBean.picSmall = jsonObject.getString("picSmall");
                        newsBean.description = jsonObject.getString("description");
                        newsBeanList.add(newsBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newsBeanList;
        }

        /**
         * 通过InputStream解析网页返回的数据
         * @param is
         * @return
         */

    private String readStream(InputStream is) {
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            //字节流转换成字符流
            isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                result += line;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 实现网络的异步访问
     */
    class NewAsyncTask extends AsyncTask<String, Void, List<newsBean>> {
        @Override
        protected List<newsBean> doInBackground(String... strings) {
            return getJsonData(strings[0]);
        }

        @Override
        protected void onPostExecute(List<newsBean> newsBeans) {
            super.onPostExecute(newsBeans);
            newsAdapter adapter = new newsAdapter(MainActivity.this,newsBeans,mlistView);
            mlistView.setAdapter(adapter);
        }
    }
}

