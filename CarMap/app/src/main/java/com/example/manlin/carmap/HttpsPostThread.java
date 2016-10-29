package com.example.manlin.carmap;

import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.List;

/**
 * Https Post请求
 */
public class HttpsPostThread extends Thread {

    private Handler handler;
    private String httpUrl;
    private List<NameValuePair> valueList;
    private int mWhat;

    public static final int ERROR = 404;
    public static final int SUCCESS = 200;

    public HttpsPostThread(Handler handler, String httpUrl,
                           List<NameValuePair> list, int what) {
        super();
        this.handler = handler;
        this.httpUrl = httpUrl;
        this.valueList = list;
        this.mWhat = what;
    }

    public HttpsPostThread(Handler handler, String httpUrl,
                           List<NameValuePair> list) {
        super();
        this.handler = handler;
        this.httpUrl = httpUrl;
        this.valueList = list;
        this.mWhat = SUCCESS;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        String result = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            // 设置连接管理器的超时
            ConnManagerParams.setTimeout(httpParameters, 10000);
            // 设置连接超时
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            // 设置socket超时
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
            HttpClient hc = getHttpClient(httpParameters);
            HttpPost post = new HttpPost(httpUrl);
            post.setEntity(new UrlEncodedFormEntity(valueList, HTTP.UTF_8));
            post.setParams(httpParameters);
            HttpResponse response = null;
            try {
                response = hc.execute(post);
            } catch (UnknownHostException e) {
                throw new Exception("Unable to access "
                        + e.getLocalizedMessage());
            } catch (SocketException e) {
                throw new Exception(e.getLocalizedMessage());
            }
            int sCode = response.getStatusLine().getStatusCode();
            if (sCode == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                if (handler != null) {
                    handler.sendMessage(Message.obtain(handler, mWhat, result)); // 请求成功
                }
            } else {
                result = "请求失败" + sCode; // 请求失败
                // 404 - 未找到
                if (handler != null) {
                    handler.sendMessage(Message.obtain(handler, ERROR, result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (handler != null) {
                result = "请求失败,异常退出";
                handler.sendMessage(Message.obtain(handler, ERROR, result));
            }
        }
        super.run();
    }

    /**
     * 获取HttpClient
     *
     * @param params
     * @return
     */
    public static HttpClient getHttpClient(HttpParams params) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);

            // 设置http https支持
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));// SSL/TSL的认证过程，端口为443
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient(params);
        }
    }
}
