package com.wwylele.hatechnion;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HebrewTranslator {
    public static final int HINT_COURSE = 1, HINT_YEAR = 2, HINT_ROOM = 3;

    public static HebrewDbHelper hebrewDictionary = null;

    public static String tryTranslateYear(String origin) {
        try {
            int yr = origin.indexOf('/');
            if (yr == -1) return null;
            String result = origin.substring(yr - 4, yr + 3);
            if (origin.contains("אביב")) result += " Spring";
            if (origin.contains("חורף")) result += " Winter";
            return result;
        } catch (Exception ignored) {
            return null;
        }

    }

    public static void requestTranslation(Context context, String origin, int hint, TranslationCallBack translationCallBack) {

        if (hint == HINT_YEAR) {
            String result = tryTranslateYear(origin);
            if (result != null) {
                translationCallBack.callback(result);
                return;
            }
        } else if (hint == HINT_ROOM) {
            translationCallBack.callback(origin.replace("חדר", "Room")
                    .replace("רבין", "Rabin")
                    .replace("אולמן", "Ullman"));
            return;
        }

        if (hebrewDictionary == null) {
            hebrewDictionary = new HebrewDbHelper(context);
        }
        new TranslationTask(translationCallBack).execute(origin);
    }

    public interface TranslationCallBack {
        void callback(String result);
    }

    static class TranslationTask extends AsyncTask<String, Void, String> {

        static String msTranslatorTicket = null;

        final TranslationCallBack translationCallBack;

        public TranslationTask(TranslationCallBack translationCallBack) {
            this.translationCallBack = translationCallBack;
        }

        private static void buyMsTranslatorTicket() {
            HttpURLConnection urlConnection = null;
            OutputStream out = null;
            OutputStreamWriter post = null;
            BufferedReader in = null;
            try {
                String request = "grant_type=client_credentials&client_id=" +
                        URLEncoder.encode(BuildConfig.xxx_mstranslator_client, "UTF-8") +
                        "&client_secret=" +
                        URLEncoder.encode(BuildConfig.xxx_mstranslator_secret, "UTF-8") +
                        " &scope=http://api.microsofttranslator.com";
                urlConnection = (HttpURLConnection) (
                        new URL("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13")
                                .openConnection());
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                post = new OutputStreamWriter(out = urlConnection.getOutputStream(), "UTF-8");
                post.write(request);
                post.flush();
                post.close();
                post = null;
                out.close();
                out = null;
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK)
                    throw new Exception("HttpURLConnection." + responseCode);
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                JSONObject result = new JSONObject(buffer.toString());
                msTranslatorTicket = "Bearer " + result.getString("access_token");
                Log.v("HaTechnion", "bought ticket from ms translator.");
            } catch (Exception e) {
                Log.e("HaTechnion", "bought ticket failed.", e);
                msTranslatorTicket = null;
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
        protected String doInBackground(String... params) {
            String origin = params[0];

            String fromDictionary = hebrewDictionary.get(origin);
            if (fromDictionary != null) return fromDictionary;

            synchronized (TranslationTask.class) {
                if (msTranslatorTicket == null) {
                    buyMsTranslatorTicket();
                }
            }

            if (msTranslatorTicket != null) {
                Log.v("HaTechnion", "try ms translator");
                HttpURLConnection urlConnection = null;
                BufferedReader in = null;
                try {
                    urlConnection = (HttpURLConnection) (
                            new URL("http://api.microsofttranslator.com/v2/Http.svc/Translate?text=" +
                                    URLEncoder.encode(origin, "UTF-8")
                                    + "&from=he&to=en")
                                    .openConnection());
                    urlConnection.setRequestProperty("Authorization", msTranslatorTicket);
                    urlConnection.setDoInput(true);
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("GET");

                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    xpp.setInput(in);
                    xpp.nextTag();
                    String fromWeb = XmlUtility.readTaggedText(xpp, "string");
                    hebrewDictionary.add(origin, fromWeb);
                    return fromWeb;
                } catch (Exception e) {
                    Log.e("HaTechnion", "MsTranslator failed", e);
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }

                    try {
                        if (in != null) in.close();
                    } catch (IOException ignored) {

                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) translationCallBack.callback(result);
        }
    }
}
