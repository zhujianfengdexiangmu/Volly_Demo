package com.baidu.volly_demo;

import java.io.IOException;

import org.apache.http.protocol.ResponseConnControl;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

    /**
     * 本例中所用到图片的url
     */
    protected static final String IMAGEREQUEST_URL = "http://e.hiphotos.baidu.com/image/pic/item/d043ad4bd11373f0e646d28ca60f4bfbfbed04a3.jpg";
    protected static final String IMAGEVIEW_URL = "http://bbs.unpcn.com/attachment.aspx?attachmentid=3732311";
    protected static final String NETWORKIMAGEVIEW_URL = "http://img0.bdstatic.com/img/image/2043d07892fc42f2350bebb36c4b72ce1409212020.jpg";
    protected static final String HTML_RETURN_URL = "http://www.baidu.com";
    protected static final String JSON_RETURN_URL = "http://m.weather.com.cn/data/101010100.html";
    protected static final String XML_RETURN_URL = "http://flash.weather.com.cn/wmaps/xml/china.xml";
    protected static final String TAG = "MainActivity";

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {

        // 普通的Http请求用法类似
        // 1. 创建一个RequestQueue对象。
        // 2. 创建一个Request对象。
        // 3. 将Request对象添加到RequestQueue里面。

        // 一个简单的http请求

        mRequestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(HTML_RETURN_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
        mRequestQueue.add(stringRequest);

        // 一个简单的Json请求,与之对应的还有一个JsonArrayRequest用来接收Json数组
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                JSON_RETURN_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });
        mRequestQueue.add(jsonObjectRequest);

        // 接下来看看Volly怎么从网络获取图片资源
        // 方法一：上面的request不出意外仍然有一个ImageRequest方法下面我们来演示一下
        final ImageView imageView02 = (ImageView) findViewById(R.id.imageview02);
        ImageRequest imageRequest = new ImageRequest(IMAGEREQUEST_URL,
                new Response.Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        imageView02.setImageBitmap(response);
                    }
                }, 0, 0, Config.RGB_565, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imageView02.setImageResource(R.drawable.ic_launcher);
                    }
                });
        mRequestQueue.add(imageRequest);

        // 方法二：volly中提供了更优化的方法ImageLoader，
        // ImageLoader也可以用于加载网络上的图片，并且它的内部也是使用ImageRequest来实现的，
        // 不过ImageLoader明显要比ImageRequest更加高效，因为它不仅可以帮我们对图片进行缓存，还可以过滤掉重复的链接，避免重复发送请求。

        // 1. 创建一个RequestQueue对象。

        // 2. 创建一个ImageLoader对象。

        // 3. 获取一个ImageListener对象。

        // 4. 调用ImageLoader的get()方法加载网络上的图片。
        ImageView imageView01 = (ImageView) findViewById(R.id.imageview01);
        // ImageLoader中使用了自定义的图片缓存方法
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache());
        ImageListener listener = ImageLoader
                .getImageListener(imageView01,
                        android.R.drawable.ic_menu_rotate,
                        android.R.drawable.ic_delete);
        mImageLoader.get(IMAGEVIEW_URL, listener);

        // 方法三：volly提供了 NetworkImageView控件，demo中已经在布局文件中定义
        NetworkImageView networkImageView = (NetworkImageView) findViewById(R.id.networkimageview01);
        networkImageView.setImageUrl(NETWORKIMAGEVIEW_URL, mImageLoader);

        // 使用自定义的解析XML的方法解析XML
        XMLRequest xmlRequest = new XMLRequest(XML_RETURN_URL,
                new Response.Listener<XmlPullParser>() {

                    @Override
                    public void onResponse(XmlPullParser response) {
                        try {
                            int eventType = response.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                switch (eventType) {
                                case XmlPullParser.START_TAG:
                                    String nodeName = response.getName();
                                    if ("city".equals(nodeName)) {
                                        String pName = response
                                                .getAttributeValue(0);
                                        Log.d(TAG, "pName is" + pName);
                                    }
                                    break;

                                }
                                eventType = response.next();
                            }
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });
        mRequestQueue.add(xmlRequest);
    }

    @Override
    protected void onStop() {
        mRequestQueue.cancelAll(this);
        super.onStop();
    }

}
