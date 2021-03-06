package com.wwylele.hatechnion;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
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
import java.util.ArrayList;


public class ExamsFragment extends Fragment {

    ListView examListView;
    ExamListAdapter adapter;
    Exam[] exams;

    public ExamsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exams, container, false);
        examListView = (ListView)
                rootView.findViewById(R.id.examListView);
        new FetchExamsTask().execute();
        return rootView;
    }

    public void beginTranslate() {
        if (((MainActivity) getActivity()).doTranslate)
            for (final Exam exam : exams) {
                exam.name.reset();
                exam.room.reset();
                HebrewTranslator.requestTranslation(
                        getActivity(),
                        exam.name,
                        HebrewTranslator.HINT_COURSE, new HebrewTranslator.TranslationCallBack() {
                            @Override
                            public void callback(String result) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                HebrewTranslator.requestTranslation(
                        getActivity(),
                        exam.room,
                        HebrewTranslator.HINT_ROOM, new HebrewTranslator.TranslationCallBack() {
                            @Override
                            public void callback(String result) {
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        else {
            for (final Exam exam : exams) {
                exam.name.reset();
                exam.room.reset();
            }
            adapter.notifyDataSetChanged();
        }
    }

    static class Exam {
        public String moed;
        public HebrewTranslator.TextPair name;
        public String time;
        public HebrewTranslator.TextPair room;
    }

    static class FetchExamsResult {
        public static final int E_SUCCESS = 0, E_OTHER = -1;
        public final int errorCode;
        public final String errorDescription;
        public final Exam[] exams;

        public FetchExamsResult(int ec, String error, Exam[] e) {
            errorCode = ec;
            errorDescription = error;
            exams = e;
        }
    }

    static class ExamsXmlParser {
        private static Exam parseExam(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "exam");
            Exam exam = new Exam();
            String date = "", time = "";
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case "name":
                        exam.name = new HebrewTranslator.TextPair(XmlUtility.readTaggedText(parser, "name"));
                        break;
                    case "date":
                        date = XmlUtility.readTaggedText(parser, "date");
                        break;
                    case "time":
                        time = XmlUtility.readTaggedText(parser, "time");
                        time = time.replace("עד", "~");
                        break;
                    case "moed":
                        exam.moed = XmlUtility.readTaggedText(parser, "moed");
                        switch (exam.moed) {
                            case "מועד ב":
                                exam.moed = "B";
                                break;
                            case "מועד א":
                                exam.moed = "A";
                                break;
                            default:
                                exam.moed = "?";
                        }
                        break;
                    case "room":
                        exam.room = new HebrewTranslator.TextPair(XmlUtility.readTaggedText(parser, "room"));
                        break;
                    default:
                        XmlUtility.readTaggedText(parser, name);
                }
            }
            exam.time = date + "  " + time;
            return exam;
        }

        public static Exam[] parseExams(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "exams");
            ArrayList<Exam> exams = new ArrayList<>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                exams.add(parseExam(parser));
            }
            return exams.toArray(new Exam[exams.size()]);
        }
    }

    public class ExamListAdapter extends BaseAdapter {

        final Exam[] exams;

        public ExamListAdapter(Exam[] exams) {
            this.exams = exams;
        }

        @Override
        public int getCount() {
            return exams.length;
        }

        @Override
        public Object getItem(int position) {
            return exams[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ExamsFragment.this.getActivity().getLayoutInflater().inflate(
                        R.layout.exam_list_item, parent, false
                );
            }

            ((TextView) convertView.findViewById((R.id.exam_list_item_moed_test)))
                    .setText(exams[position].moed);
            ((TextView) convertView.findViewById((R.id.exam_list_item_name_test)))
                    .setText(exams[position].name.translation);
            ((TextView) convertView.findViewById((R.id.exam_list_item_time_test)))
                    .setText(exams[position].time);
            ((TextView) convertView.findViewById((R.id.exam_list_item_room_test)))
                    .setText(exams[position].room.translation);

            return convertView;
        }
    }

    public class FetchExamsTask extends AsyncTask<Void, Void, FetchExamsResult> {

        @Override
        protected FetchExamsResult doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            OutputStream out = null;
            OutputStreamWriter post = null;
            BufferedReader in = null;
            try {
                XmlSerializer xs = Xml.newSerializer();
                StringWriter sw = new StringWriter();
                xs.setOutput(sw);
                xs.startTag(null, "app");
                XmlUtility.writeTaggedText(xs, "req", "182");
                XmlUtility.writeTaggedText(xs, "club", "81");
                XmlUtility.writeTaggedText(xs, "id", ((MainActivity) getActivity()).ticket);
                XmlUtility.writeTaggedText(xs, "unique_id", DeviceInfo.getAndroidId(getActivity()));
                xs.endTag(null, "app");
                xs.flush();
                String loginRequest = "appRequest=" + URLEncoder.encode(
                        sw.toString(), "UTF-8");

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
                    throw new IOException("HttpURLConnection." + responseCode);
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xpp.setInput(in);
                XmlUtility.readResult(xpp);
                Exam[] exams = ExamsXmlParser.parseExams(xpp);

                in.close();
                in = null;
                return new FetchExamsResult(FetchExamsResult.E_SUCCESS, null, exams);

            } catch (XmlUtility.BadResultException e) {
                Log.e("FetchExamsTask", "bad return code", e);
                return new FetchExamsResult(FetchExamsResult.E_OTHER, e.text, null);
            } catch (XmlPullParserException e) {
                Log.e("FetchExamsTask", "failed to parse xml", e);
                return new FetchExamsResult(FetchExamsResult.E_OTHER, "fetch failed", null);
            } catch (IOException e) {
                Log.e("FetchExamsTask", "IOException", e);
                return new FetchExamsResult(FetchExamsResult.E_OTHER, "fetch failed", null);
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
        protected void onPostExecute(final FetchExamsResult result) {
            if (result.errorCode == FetchExamsResult.E_SUCCESS) {
                adapter = new ExamListAdapter(exams = result.exams);
                examListView.setAdapter(adapter);
                beginTranslate();


            } else {
                Snackbar.make(getView(),
                        "Failed to fetch exams: " + result.errorDescription,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
