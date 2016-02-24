package com.wwylele.hatechnion;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class LoginActivity extends Activity {


    private UserLoginTask authTask = null;


    private EditText usernameView;
    private EditText passwordView;
    private View loginButton;
    private View progressView;
    private CheckBox rememberMeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        rememberMeView = (CheckBox) findViewById(R.id.remember_me);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        loginButton = findViewById(R.id.sign_in_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        progressView = findViewById(R.id.login_progress);

        String username = Account.getUsername(this);
        usernameView.setText(username);
        passwordView.setText(Account.getPassword(this));
        if (!username.equals("")) rememberMeView.setChecked(true);
    }


    private void attemptLogin() {
        if (authTask != null) {
            return;
        }

        usernameView.setError(null);
        passwordView.setError(null);

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {

            if (rememberMeView.isChecked()) Account.set(this, username, password);
            else Account.set(this, "", "");

            showProgress(true);
            authTask = new UserLoginTask(username, password);
            authTask.execute((Void) null);
        }
    }


    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        usernameView.setEnabled(!show);
        passwordView.setEnabled(!show);
    }

    class UserLoginResult {
        public static final int E_SUCCESS = 0, E_USERNAME = -1, E_PASSWORD = -2, E_OTHER = -100;
        public final int errorCode;
        public final String errorDescription;
        public final String username;
        public final String real;
        public final String ticket;

        public UserLoginResult(int ec, String error, String name, String r, String t) {
            errorCode = ec;
            errorDescription = error;
            username = name;
            real = r;
            ticket = t;
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, UserLoginResult> {

        private final String username;
        private final String password;

        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }


        @Override
        protected UserLoginResult doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            OutputStream out = null;
            OutputStreamWriter post = null;
            BufferedReader in = null;
            try {
                XmlSerializer xs = Xml.newSerializer();
                StringWriter sw = new StringWriter();
                xs.setOutput(sw);
                xs.startTag(null, "app");
                XmlUtility.writeTaggedText(xs, "req", "203");
                XmlUtility.writeTaggedText(xs, "club", "81");
                XmlUtility.writeTaggedText(xs, "usingStoredCredentials", "1");
                XmlUtility.writeTaggedText(xs, "id", "");
                XmlUtility.writeTaggedText(xs, "unique_id", DeviceInfo.getAndroidId(LoginActivity.this));
                XmlUtility.writeTaggedText(xs, "login_name", username);
                XmlUtility.writeTaggedText(xs, "pass", password);
                XmlUtility.writeTaggedText(xs, "api_version", "12");
                XmlUtility.writeTaggedText(xs, "m_model", DeviceInfo.getDeviceName());
                XmlUtility.writeTaggedText(xs, "m_version", Build.VERSION.RELEASE);
                xs.endTag(null, "app");
                xs.flush();
                String loginRequest = "appRequest=" + URLEncoder.encode(
                        sw.toString(), "UTF-8");
                Log.v("HaTechnion", loginRequest);

                urlConnection = (HttpURLConnection) (new URL(BuildConfig.xxx_api_url).openConnection());
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");

                post = new OutputStreamWriter(out = urlConnection.getOutputStream(), "UTF-8");
                post.write(loginRequest);
                post.flush();
                post.close();
                post = null;
                out.close();
                out = null;
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK)
                    throw new Exception("HttpURLConnection." + responseCode);
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xpp.setInput(in);
                xpp.nextTag();
                xpp.require(XmlPullParser.START_TAG, null, "app");
                xpp.next();
                xpp.require(XmlPullParser.START_TAG, null, "result");
                xpp.next();
                int retCode = Integer.parseInt(XmlUtility.readTaggedText(xpp, "ret_code"));
                xpp.next();
                String retText = XmlUtility.readTaggedText(xpp, "text");
                xpp.next();
                Log.v("HaTechnion", "retCode=" + retCode + " retText=" + retText);
                xpp.require(XmlPullParser.END_TAG, null, "result");
                xpp.next();

                if (retCode != 0) {
                    switch (retText) {
                        case "bad login name":
                            return new UserLoginResult(UserLoginResult.E_USERNAME,
                                    retText, null, null, null);
                        case "wrong password":
                            return new UserLoginResult(UserLoginResult.E_PASSWORD,
                                    retText, null, null, null);
                        default:
                            return new UserLoginResult(UserLoginResult.E_OTHER,
                                    retText, null, null, null);
                    }
                }

                xpp.require(XmlPullParser.START_TAG, null, "data");
                xpp.next();
                String ticket = XmlUtility.readTaggedText(xpp, "id");
                xpp.next();
                String real = XmlUtility.readTaggedText(xpp, "name");
                xpp.next();
                String username_ret = XmlUtility.readTaggedText(xpp, "user_id");
                xpp.next();

                in.close();
                in = null;
                return new UserLoginResult(UserLoginResult.E_SUCCESS,
                        "", username_ret, real, ticket);

            } catch (Exception e) {
                Log.e("HaTechnion", "login connection failed", e);
                return new UserLoginResult(UserLoginResult.E_OTHER,
                        getString(R.string.error_connection), null, null, null);
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                try {
                    if (post != null) post.close();
                    if (out != null) out.close();
                    if (in != null) in.close();
                } catch (IOException ignored) {

                }

            }
        }

        @Override
        protected void onPostExecute(final UserLoginResult result) {
            showProgress(false);
            authTask = null;
            switch (result.errorCode) {
                case UserLoginResult.E_SUCCESS:
                    Bundle extras = new Bundle();
                    extras.putString("ticket", result.ticket);
                    extras.putString("username", result.username);
                    extras.putString("real", result.real);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class)
                            .putExtras(extras));
                    finish();

                    break;
                case UserLoginResult.E_PASSWORD:
                    passwordView.setError(getString(R.string.error_incorrect_password));
                    passwordView.requestFocus();
                    break;
                case UserLoginResult.E_USERNAME:
                    usernameView.setError(getString(R.string.error_incorrect_username));
                    usernameView.requestFocus();
                    break;
                default:
                    Toast.makeText(LoginActivity.this, "Sign in failed!\n" + result.errorDescription,
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }
}

