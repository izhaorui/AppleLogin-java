package com.zhaouri.applelogin.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;


/**
 * http请求处理类
 *
 * @Author: zhaorui
 * @Date: 2018/12/10 14:04
 */
public class HttpClientUtil {

    private static PoolingHttpClientConnectionManager cm;
    private static String EMPTY_STR = "";
    private static String CONTENT_TYPE_UTF_8 = "UTF-8";
    private static String CONTENT_TYPE_GBK = "GBK";
    private static String CONTENT_TYPE_JSON = "application/json";
    private static final int CONNECTION_TIMEOUT_MS = 60000;
    private static final int SO_TIMEOUT_MS = 60000;

    private static final String proxy_host = "127.0.0.1";
    private static final int proxy_port = 1080;

    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数,默认值是2
            SocketConfig sc = SocketConfig.custom().setSoTimeout(SO_TIMEOUT_MS).build();
            cm.setDefaultSocketConfig(sc);
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(cm).setConnectionManagerShared(true).build();
    }

    /**
     * Get 发送请求
     *
     * @param url
     * @return
     */
    public static String httpGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    /**
     * Get 发送 Map请求
     *
     * @param url    请求url
     * @param params 请求Map参数
     * @return 返回结果
     * @throws URISyntaxException 抛出URL地址错误
     */
    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

//    public static String httpGetRequest(String url, Map headers, Map params)
//            throws URISyntaxException {
//        URIBuilder ub = new URIBuilder();
//        ub.setPath(url);
//
//        ArrayList pairs = covertParams2NVPS(params);
//        ub.setParameters(pairs);
//
//        HttpGet httpGet = new HttpGet(ub.build());
//        for (Map.Entry param : headers.entrySet()) {
//            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
//        }
//        return getResult(httpGet);
//    }

    /**
     * Post 发送请求
     *
     * @param url 请求url地址
     * @return 返回结果
     */
    public static String httpPostRequest(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    /**
     * Post发送请求
     *
     * @param url    请求地址
     * @param params map对象
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String httpPostRequest(String url, Map<String, Object> params, boolean proxy) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, CONTENT_TYPE_UTF_8));
        if (proxy) {
            return getProxyResult(httpPost);
        } else {
            return getResult(httpPost);
        }
    }

    /**
     * post 发送 Map参数请求
     *
     * @param url     发送URL
     * @param headers Header参数
     * @param params  发送请求参数
     * @return 返回字符串
     * @throws UnsupportedEncodingException 抛出异常
     */
    public static String httpPostRequest(String url, Map<String, Object> headers, Map params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry param : headers.entrySet()) {
            httpPost.addHeader((String) param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, CONTENT_TYPE_UTF_8));

        return getResult(httpPost);
    }

    /**
     * post 发送json格式参数
     *
     * @param url
     * @param json
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String httpPostJSON(String url, String json) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        StringEntity s = new StringEntity(json);
        s.setContentEncoding(CONTENT_TYPE_UTF_8);
        s.setContentType(CONTENT_TYPE_JSON);
        httpPost.setEntity(s);
        return getResult(httpPost);
    }

    private static ArrayList covertParams2NVPS(Map<String, Object> params) {
        ArrayList pairs = new ArrayList<>();
        for (Map.Entry param : params.entrySet()) {
            pairs.add(new BasicNameValuePair((String) param.getKey(), (String) param.getValue()));
        }

        return pairs;
    }

    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request) {

        RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT);
        config.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS);
        config.setSocketTimeout(SO_TIMEOUT_MS);

        request.setConfig(config.build());

        /// CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            /// response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放Socket流
                assert response != null;
                response.close();
                // 释放Connection
                /// httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return EMPTY_STR;
    }

    /**
     * 获取设置代理请求的结果
     *
     * @param request
     * @return
     */
    private static String getProxyResult(HttpRequestBase request) {
        HttpHost httpHost = new HttpHost(proxy_host, proxy_port);

        RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT);
        config.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS);
        config.setSocketTimeout(SO_TIMEOUT_MS);
        config.setProxy(httpHost);

        request.setConfig(config.build());

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert response != null;
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return EMPTY_STR;
    }
}
