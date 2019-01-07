package com.prithviraj.myvollyimplementation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.prithviraj.myvollyimplementation.DataModelClasses.DataPart;
import com.prithviraj.myvollyimplementation.UtilityClasses.Constant;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

/**
 * Created by Prithviraj Acharya on 31-12-2018.
 */

public abstract class VolleyServiceCall {
    private Map<String, String> param;
    private JSONObject object = null;
    private Map<String, DataPart> partData;
    private Map<String, String> header;
    private String url;
    private Context context;
    private static RequestQueue queue;
    private int requestType;
    private String tag = null;

    public abstract void onResponse(String s);
    public abstract void onError(VolleyError error, String errorMessage);

    public class TAG {
        public static final String LOCATION = "LOCATION";
        public static final String OTHER = "TAG";
    }

    public VolleyServiceCall(int requestType, String url, Map<String, String> header, Map<String, String> param, Map<String, DataPart> partData, Context context) {
        this.param = param;
        this.partData = partData;
        this.header = header;
        this.url = url;
        this.context = context;
        this.requestType = requestType;
        queue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public VolleyServiceCall(int requestType, String url, Map<String, String> header, JSONObject param, Context context) {
        this.object = param;
        this.header = header;
        this.url = url;
        this.context = context;
        this.requestType = requestType;
        queue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public VolleyServiceCall(Context context) {
        queue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void setAllData(Map<String, String> param, Map<String, DataPart> partData, Map<String, String> header) {
        this.param = param;
        this.partData = partData;
        this.header = header;
    }

    public synchronized void start() {
        call();
    }

    public synchronized void start(String tag) {
        this.tag = tag;
        call();
    }

    private void call() {
        final Request request;
        if (object==null) {
            if (partData == null) {
                request = new JsonObjectRequest(requestType, url, getJsonParam(param), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progress.dismiss();
                        VolleyServiceCall.this.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null)
                            if (error.networkResponse != null)
                                if (error.networkResponse.data != null)
                                    Log.d("ERROR_", new String(error.networkResponse.data));

                        onError(error,returnErrorMsg(error));
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        if (header != null)
                            return header;
                        return super.getHeaders();
                    }
                };
            } else {
                request = new VolleyMultipartRequest(requestType, url, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        Log.d("ZXCV-complete", new String(response.data));
                        VolleyServiceCall.this.onResponse(new String(response.data));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null)
                            if (error.networkResponse != null)
                                if (error.networkResponse.data != null)
                                    Log.d("ERROR_ZXCV", new String(error.networkResponse.data));

                        onError(error,returnErrorMsg(error));
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        if (header != null)
                            return header;
                        return super.getHeaders();
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        if (param != null) {
                            Log.d("ZXCV", param.toString());
                            return param;
                        }
                        return super.getParams();
                    }

                    @Override
                    protected Map<String, DataPart> getByteData() throws AuthFailureError {
                        if (partData != null)
                            return partData;
                        return super.getByteData();
                    }
                };
            }
        } else {
            request = new JsonObjectRequest(requestType, url, object, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    VolleyServiceCall.this.onResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null)
                        if (error.networkResponse != null)
                                if (error.networkResponse.data != null)
                                Log.d("ERROR_", new String(error.networkResponse.data));

                    onError(error,returnErrorMsg(error));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    if (header != null)
                        return header;
                    return super.getHeaders();
                }
            };
        }
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 90000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        if (tag == null)
            request.setTag(TAG.OTHER);
        else
            request.setTag(tag);
        if (isNetConnected()) {
            queue.add(request);
        } else {
            Constant.GLOBAL_VARIABLE_CLASS.requestList.add(request);
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("No internet connection. Status of the trip(s) and data will be updated automatically once your internet connection is back.");
            dialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
                dialog.create().show();
        }
    }

    private JSONObject getJsonParam(Map<String, String> param) {
        JSONObject object = new JSONObject();
        if (param!=null) {
            for (Map.Entry<String, String> set : param.entrySet()) {
                try {
                    object.put(set.getKey(), set.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    private boolean isNetConnected(){
        ConnectivityManager ConnectionManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=ConnectionManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void stopNetworkCall(String tag) {
        queue.cancelAll(tag);
    }

    public void stopNetworkCall() {
        queue.cancelAll(TAG.OTHER);
        queue.cancelAll(TAG.LOCATION);
    }

    private String returnErrorMsg(VolleyError error){
        String errorMsg = "";
        if(error instanceof TimeoutError){
            errorMsg = "Server Timeout";
        }else if(error instanceof NoConnectionError){
            errorMsg = "No network connection found";
        }else if(error instanceof AuthFailureError){
            errorMsg = "Authentication Failure";
        }else if(error instanceof ServerError){
            errorMsg = "Server down";
        }else if(error instanceof NetworkError){
            errorMsg = "No internet";
        }else if(error instanceof ParseError){
            errorMsg = "Parsing Failure";
        }else{
            errorMsg = error.getMessage();
        }
        return errorMsg;
    }

}
