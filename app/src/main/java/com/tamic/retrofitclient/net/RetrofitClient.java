package com.tamic.retrofitclient.net;

import android.content.Context;
import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * RetrofitClient
 * Created by Tamic on 2016-06-15.
 * {@link # https://github.com/NeglectedByBoss/RetrofitClient}
 */
public class RetrofitClient {

    private static final int DEFAULT_TIMEOUT = 5;
    private ApiService apiService;
    private static OkHttpClient okHttpClient;
    public static String baseUrl = ApiService.Base_URL;
    private static Context mContext;
    private static RetrofitClient sNewInstance;

    private static Retrofit retrofit;

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl(baseUrl);
    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder()
                    .addNetworkInterceptor(
                            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    private static class SingletonHolder {
        private static RetrofitClient INSTANCE = new RetrofitClient(
                mContext);
    }

    public static RetrofitClient getInstance(Context context) {
        if (context != null) {
            mContext = context;
        }
        return SingletonHolder.INSTANCE;
    }

    public static RetrofitClient getInstance(Context context, String url) {
        if (context != null) {
            mContext = context;
        }
        sNewInstance = new RetrofitClient(context, url);
        return sNewInstance;
    }

    public static RetrofitClient getInstance(Context context, String url, Map headers) {
        if (context != null) {
            mContext = context;
        }
        sNewInstance = new RetrofitClient(context, url, headers);
        return sNewInstance;
    }

   private RetrofitClient() {

   }

    private RetrofitClient(Context context) {

        this(context, baseUrl, null);
    }

    private RetrofitClient(Context context, String url) {

        this(context, url, null);
    }

    private RetrofitClient(Context context, String url, Map headers) {

        if (TextUtils.isEmpty(url)) {
            url = baseUrl;
        }
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(
                        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                .cookieJar(new NovateCookieManger(context))
                .addInterceptor(new BaseInterceptor(headers))
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        retrofit = builder
                .client(okHttpClient)
                .baseUrl(url)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * ApiBaseUrl
     *
     * @param newApiBaseUrl
     */
    public static void changeApiBaseUrl(String newApiBaseUrl) {
        baseUrl = newApiBaseUrl;
        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl);
    }

    /**
     * ApiBaseUrl
     *
     * @param newApiHeaders
     */
    public static void changeApiHeader(Map<String, String> newApiHeaders) {

        okHttpClient.newBuilder().addInterceptor(new BaseInterceptor(newApiHeaders)).build();
        builder.client(httpClient.build()).build();
    }


    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public static <S> S createService(Class<S> serviceClass) {

        return builder.client(httpClient.build()).build().create(serviceClass);
    }


    public void getData(Subscriber<IpResult> subscriber, String ip) {
        apiService.getData(ip)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public Subscription get(String url, Map headers, Map parameters, Subscriber<IpResult> subscriber) {
        return apiService.executeGet(url, parameters)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void post(String url, Map parameters, Subscriber<ResponseBody> subscriber) {
        apiService.executePost(url, parameters)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void download(String url, Subscriber<ResponseBody> subscriber) {
        apiService.downloadFile(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(subscriber);
    }

}