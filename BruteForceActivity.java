package com.example.godspower.gtucvote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.example.godspower.gtucvote.adapter.CandidatesListAdapter;
import com.example.godspower.gtucvote.dialog.LoadingSpinner;
import com.example.godspower.gtucvote.model.Vote;
import com.example.godspower.gtucvote.model.Vote.Candidate;
import com.example.godspower.gtucvote.util.C;
import com.example.godspower.gtucvote.util.Crypto;
import com.example.godspower.gtucvote.util.RegexMatcher;
import com.example.godspower.gtucvote.util.Util;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Brute force analysis of the vote.
 */
public class BruteForceActivity extends Activity {

    private static final String TAG = BruteForceActivity.class.getSimpleName();

    private String qrCode;
    private String webResult;
    private String versionNumber;

    private String publicKey;
    private ListView list;
    private Vote vote;
    private TextView lblChoice;
    private View lblShadow;
    private TextView lblCloseTimeout;
    private View lblcloseTimeoutShadow;
    private CustomCountDownTimer countDownTimer;
    private LoadingSpinner mLoadingSpinner;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!Util.SpecialModels.contains(Util.getDeviceName())) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }

        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
        setContentView(R.layout.bruteforce_activity);

        countDownTimer = new CustomCountDownTimer(C.closeTimeout,
                C.closeInterval);

        Intent intent = getIntent();
        qrCode = intent.getStringExtra(Util.QR_CODE);
        webResult = intent.getStringExtra(Util.WEB_RESULT);
        versionNumber = intent.getStringExtra(Util.VERSION_NUMBER);

        list = (ListView) findViewById(R.id.list);

        lblChoice = (TextView) findViewById(R.id.choice_title_label);
        lblChoice.setText(C.lblChoice);
        lblChoice.setTypeface(C.typeFace);
        lblChoice.setTextColor(Util.generateHexColorValue(C.lblForeground));
        lblChoice.setBackgroundColor(Util
                .generateHexColorValue(C.lblBackground));

        lblShadow = (View) findViewById(R.id.choice_title_label_shadow);
        lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

        lblCloseTimeout = (TextView) findViewById(R.id.close_timeout_label);
        lblCloseTimeout.setTypeface(C.typeFace);
        lblCloseTimeout.setText(C.lblCloseTimeout);
        lblCloseTimeout.setTextColor(Util
                .generateHexColorValue(C.lblCloseTimeoutForeground));
        int colors[] = new int[3];
        colors[0] = Util
                .generateHexColorValue(C.lblCloseTimeoutBackgroundStart);
        colors[1] = Util
                .generateHexColorValue(C.lblCloseTimeoutBackgroundCenter);
        colors[2] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundEnd);

        GradientDrawable bgCloseTimeoutShape = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        bgCloseTimeoutShape.setCornerRadius(5);
        lblCloseTimeout.setBackgroundDrawable(bgCloseTimeoutShape);

        lblcloseTimeoutShadow = (View) findViewById(R.id.close_timeout_label_shadow);
        GradientDrawable bgCloseTimeoutShadowShape = (GradientDrawable) lblcloseTimeoutShadow
                .getBackground();
        bgCloseTimeoutShadowShape.setColor(Util
                .generateHexColorValue(C.lblCloseTimeoutShadow));
        bgCloseTimeoutShadowShape.setCornerRadius(5);

        lblChoice.setVisibility(View.INVISIBLE);
        lblShadow.setVisibility(View.INVISIBLE);
        lblCloseTimeout.setVisibility(View.INVISIBLE);
        lblcloseTimeoutShadow.setVisibility(View.INVISIBLE);

        publicKey = C.publicKey;
        doBruteForce();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BruteForce Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.godspower.gtucvote/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BruteForce Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.godspower.gtucvote/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    abstract class GetBruteForceTask extends
            AsyncTask<Void, Void, ArrayList<Candidate>> {

        @Override
        protected void onPreExecute() {
            mLoadingSpinner = Util.startSpinner(BruteForceActivity.this, false);
        }

        @Override
        protected void onPostExecute(ArrayList<Candidate> candidates) {
            Util.stopSpinner(mLoadingSpinner);
            if (candidates.size() > 0) {

                CandidatesListAdapter adapter = new CandidatesListAdapter(
                        getApplicationContext(), candidates, vote);
                list.setAdapter(adapter);

                sendNotification(C.notificationTitle, C.notificationMessage);

                lblChoice.setVisibility(View.VISIBLE);
                lblShadow.setVisibility(View.VISIBLE);
                lblCloseTimeout.setVisibility(View.VISIBLE);
                lblcloseTimeoutShadow.setVisibility(View.VISIBLE);

                countDownTimer.start();
            } else {
                Util.startErrorIntent(BruteForceActivity.this,
                        C.badVerificationMessage, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(1000123);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void doBruteForce() {
        new GetBruteForceTask() {

            @Override
            protected ArrayList<Candidate> doInBackground(Void... arg0) {
                try {
                    if (Util.DEBUGGABLE) {
                        Log.d("QR_CODE", qrCode);
                    }

                    ArrayList<Candidate> candidates = new ArrayList<Candidate>();

                    vote = new Vote();
                    vote.parseHeader(webResult);
                    List<Candidate> cands = vote.parseBody(webResult);
                    String newEnc = "";

                    if (qrCode.split("\n").length > 1) {
                        for (int i = 1; i < qrCode.split("\n").length; i++) {
                            String hexControlCode = qrCode.split("\n")[i]
                                    .split("\t")[1].split("\n")[0];
                            if (!RegexMatcher.IsFortyCharacters(hexControlCode)) {
                                Util.startErrorIntent(BruteForceActivity.this,
                                        C.badServerResponseMessage, true);
                                return null;
                            }
                            String electionId = qrCode.split("\n")[i]
                                    .split("\t")[0];

                            newEnc = vote.encBallots.get(electionId)
                                    .replaceAll("\n", "");

                            if (Util.DEBUGGABLE) {
                                Log.d(TAG, electionId);
                                Log.d(TAG, hexControlCode);
                                Log.d(TAG, newEnc);
                            }

                            for (Candidate c : cands) {

                                String decodedVote = versionNumber + "\n"
                                        + electionId + "\n" + c.number + "\n";

                                String bruteenc = new String(
                                        Crypto.encrypt(decodedVote,
                                                hexControlCode, publicKey),
                                        Util.ENCODING);

                                if (newEnc.equals(bruteenc)) {
                                    candidates.add(c);
                                }
                            }
                        }
                    }
                    return candidates;

                } catch (Exception e) {
                    if (Util.DEBUGGABLE) {
                        Log.d(TAG, "Error: " + e.getMessage(), e);
                    }
                    return null;
                }
            }
        }.execute();
    }

    public class CustomCountDownTimer extends CountDownTimer {

        public CustomCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Util.EXIT, true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(1000123);
            startActivity(intent);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            lblCloseTimeout.setText(C.lblCloseTimeout.replace("XX", String
                    .valueOf(TimeUnit.MILLISECONDS
                            .toSeconds(millisUntilFinished))));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String notificationTitle,
                                  String notificationMessage) {
        Notification notification = new Notification(R.drawable.icon,
                C.notificationMessage, System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, BruteForceActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;


        // notification.setLatestEventInfo(BruteForceActivity.class,
        //notificationTitle, notificationMessage, contentIntent);

        //((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        // .notify(1000123, notification);
    }
}