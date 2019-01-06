package com.prithviraj.myvollyimplementation.UtilityClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public abstract class ApiErrorAction {

    private String errorMsg;
    private VolleyError error;
    private Context context;
    private boolean shouldReturn = false;

    public abstract void setAction(boolean action);

    protected ApiErrorAction(VolleyError error, String errorMsg, Context context) {
        this.errorMsg = errorMsg;
        this.error = error;
        this.context = context;
    }

    public void createDialog(){

        Log.d("Error",error.toString()+"  "+errorMsg);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setMessage("Something went wrong. Please try again! ");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shouldReturn = true;
                dialogInterface.cancel();
                setAction(shouldReturn);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shouldReturn = false;
                dialogInterface.cancel();
                setAction(shouldReturn);
            }
        });

        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            alertDialog.show();
        } else if (error instanceof AuthFailureError) {
            alertDialog.show();
        } else if (error instanceof ServerError) {
            alertDialog.show();
        } else if (error instanceof NetworkError) {
            alertDialog.show();
        } else if (error instanceof ParseError) {
            alertDialog.show();
        } else {
            alertDialog.show();
        }
    }
}
