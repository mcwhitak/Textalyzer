package com.whitaker.textalyzer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.whitaker.textalyzer.util.TextalyzerApplication;
import com.whitaker_iacob.textalyzer.R;

/**
 * A task used to analyze text messages.
 */
public class AnalyzeTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final TextalyzerApplication app;
    private final Runnable doAfter;
    private ProgressDialog progressDialog;

    /**
     * Constructor used when the analysis is being run in the foreground.
     * In this case, a ProgressDialog will be shown.
     *
     * @param contextIn The context in which this asynchronous task is running
     */
    public AnalyzeTask(Context contextIn, TextalyzerApplication appIn, Runnable doAfterIn) {
        context = contextIn;
        app = appIn;
        doAfter = doAfterIn;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.analyzing_inbox));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(1);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(!app.isReady())
        {
            app.setProgressDialog(progressDialog);
            app.initMap();
            app.grabNumbers();

            app.grabInbox();
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(context.getString(R.string.analyzing_sent));
                }
            });
            progressDialog.setProgress(0);
            app.grabOutbox();
            app.populateMapEnd();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        progressDialog.dismiss();
        doAfter.run();
    }

}