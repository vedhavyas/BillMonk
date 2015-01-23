package com.digital.bills;

import android.app.Activity;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 5/12/14.
 * Project JaagrT
 */
public class AlertDialogs {

    public static SweetAlertDialog showSweetProgress(Activity activity) {
        SweetAlertDialog pDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(activity.getResources().getColor(R.color.com_red));
        pDialog.getProgressHelper().setBarWidth(15);
        pDialog.getProgressHelper().setCircleRadius(90);
        pDialog.setCancelable(false);
        return pDialog;
    }

}
