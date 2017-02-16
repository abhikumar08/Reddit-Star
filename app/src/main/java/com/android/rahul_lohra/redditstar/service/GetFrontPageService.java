package com.android.rahul_lohra.redditstar.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.rahul_lohra.redditstar.application.Initializer;
import com.android.rahul_lohra.redditstar.modal.frontPage.FrontPageResponse;
import com.android.rahul_lohra.redditstar.retrofit.ApiInterface;
import com.android.rahul_lohra.redditstar.utility.UserState;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class GetFrontPageService extends IntentService {

    @Inject
    @Named("withoutToken")
    Retrofit retrofitWithoutToken;

    @Inject
    @Named("withToken")
    Retrofit retrofitWithToken;

    ApiInterface apiInterface;

    public static String after = null;
    public boolean isUserLoggedIn = false;
    private final  String TAG = GetFrontPageService.class.getSimpleName();
    public GetFrontPageService() {
        super(GetFrontPageService.class.getSimpleName());
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ((Initializer)getApplication()).getNetComponent().inject(this);
        isUserLoggedIn = UserState.isUserLoggedIn(getApplicationContext());
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            apiInterface = (isUserLoggedIn)?retrofitWithToken.create(ApiInterface.class):retrofitWithoutToken.create(ApiInterface.class);
            after = intent.getStringExtra("after");
            Map<String,String> map = new HashMap<>();
            map.put("limit","10");
            map.put("after",after);
            String token = (isUserLoggedIn) ? "bearer " + UserState.getAuthToken(getApplicationContext()) : "";

            try {
                Response<FrontPageResponse> res = apiInterface.getFrontPage(token,map).execute();
                if(res.code()==200)
                {
                    EventBus.getDefault().post(res.body().getData());
                }
                Log.d(TAG,"response:"+res.code());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                after = null;
            }
        }
    }

}
