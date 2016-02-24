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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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


public class GradesFragment extends Fragment {

    ExpandableListView gradeListView;

    public GradesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grades, container, false);
        gradeListView = (ExpandableListView)
                rootView.findViewById(R.id.gradeListView);
        new FetchGradesTask().execute();
        return rootView;
    }

    static class CourseGrade {
        public String courseName = "";
        public String finalGrade = "";
        public String credits = "";
    }

    static class YearGrade {
        public String year = "";
        public String yearSystem = "";
        public String averageGrade = "";
        public CourseGrade[] grades;
    }

    static class GradesXmlParser {
        private static CourseGrade parseGrade(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "grade");
            CourseGrade grade = new CourseGrade();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case "name":
                        grade.courseName = XmlUtility.readTaggedText(parser, "name");
                        break;
                    case "final_grade":
                        grade.finalGrade = XmlUtility.readTaggedText(parser, "final_grade");
                        break;
                    case "credits":
                        grade.credits = XmlUtility.readTaggedText(parser, "credits");
                        break;
                    default:
                        throw new XmlPullParserException("unknown tag:" + name);
                }
            }
            return grade;
        }

        private static CourseGrade[] parseGrades(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "grades");
            ArrayList<CourseGrade> grades = new ArrayList<>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                grades.add(parseGrade(parser));
            }
            return grades.toArray(new CourseGrade[grades.size()]);
        }

        private static YearGrade parseYear(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "year_grade");
            YearGrade yearGrade = new YearGrade();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case "year":
                        yearGrade.year = XmlUtility.readTaggedText(parser, "year");
                        break;
                    case "year_system":
                        yearGrade.yearSystem = XmlUtility.readTaggedText(parser, "year_system");
                        break;
                    case "average_grade":
                        yearGrade.averageGrade = XmlUtility.readTaggedText(parser, "average_grade");
                        break;
                    case "grades":
                        yearGrade.grades = parseGrades(parser);
                        break;
                    default:
                        throw new XmlPullParserException("unknown tag:" + name);
                }
            }
            return yearGrade;
        }

        public static YearGrade[] parseYears(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "year_grades");
            ArrayList<YearGrade> years = new ArrayList<>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                years.add(parseYear(parser));
            }
            return years.toArray(new YearGrade[years.size()]);
        }
    }

    static class FetchGradesResult {
        public static final int E_SUCCESS = 0, E_OTHER = -1;
        public final int errorCode;
        public final String errorDescription;
        public final YearGrade[] gradeSet;

        public FetchGradesResult(int ec, String error, YearGrade[] g) {
            errorCode = ec;
            errorDescription = error;
            gradeSet = g;
        }
    }

    public class FetchGradesTask extends AsyncTask<Void, Void, FetchGradesResult> {


        @Override
        protected FetchGradesResult doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            OutputStream out = null;
            OutputStreamWriter post = null;
            BufferedReader in = null;
            try {
                XmlSerializer xs = Xml.newSerializer();
                StringWriter sw = new StringWriter();
                xs.setOutput(sw);
                xs.startTag(null, "app");
                XmlUtility.writeTaggedText(xs, "req", "185");
                XmlUtility.writeTaggedText(xs, "club", "81");
                XmlUtility.writeTaggedText(xs, "id", ((MainActivity) getActivity()).ticket);
                XmlUtility.writeTaggedText(xs, "unique_id", DeviceInfo.getAndroidId(getActivity()));
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
                    return new FetchGradesResult(FetchGradesResult.E_OTHER, retText, null);
                }
                xpp.require(XmlPullParser.START_TAG, null, "data");
                xpp.next();
                YearGrade[] gradeSet = GradesXmlParser.parseYears(xpp);

                in.close();
                in = null;
                return new FetchGradesResult(FetchGradesResult.E_SUCCESS, null, gradeSet);

            } catch (Exception e) {
                Log.e("HaTechnion", "fetch grades failed", e);
                return new FetchGradesResult(FetchGradesResult.E_OTHER, "fetch failed", null);
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
        protected void onPostExecute(final FetchGradesResult result) {
            if (result.errorCode == FetchGradesResult.E_SUCCESS) {
                final GradeListAdapter adapter = new GradeListAdapter(result.gradeSet);
                gradeListView.setAdapter(adapter);

                for (final YearGrade year : result.gradeSet) {
                    HebrewTranslator.requestTranslation(
                            year.year,
                            HebrewTranslator.HINT_YEAR, new HebrewTranslator.TranslationCallBack() {
                                @Override
                                public void callback(String result) {
                                    year.year = result;
                                    adapter.notifyDataSetChanged();
                                }
                            });
                    for (final CourseGrade course : year.grades) {
                        HebrewTranslator.requestTranslation(
                                course.courseName,
                                HebrewTranslator.HINT_COURSE, new HebrewTranslator.TranslationCallBack() {
                                    @Override
                                    public void callback(String result) {
                                        course.courseName = result;
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                }

            } else {
                Snackbar.make(getView(),
                        "Failed to fetch grades: " + result.errorDescription,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    public class GradeListAdapter extends BaseExpandableListAdapter {

        final YearGrade[] gradeSet;

        GradeListAdapter(YearGrade[] grades) {
            gradeSet = grades;
        }

        @Override
        public int getGroupCount() {
            return gradeSet.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return gradeSet[groupPosition].grades.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return gradeSet[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return gradeSet[groupPosition].grades[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition * 10000 + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = GradesFragment.this.getActivity().getLayoutInflater().inflate(
                        R.layout.grade_list_item_year, null
                );
            }
            ((TextView) convertView.findViewById((R.id.grade_list_item_year_text)))
                    .setText(gradeSet[groupPosition].year);
            ((TextView) convertView.findViewById((R.id.grade_list_item_avg_text)))
                    .setText(gradeSet[groupPosition].averageGrade.isEmpty() ? "" :
                            getString(R.string.average_grade) + ":" + gradeSet[groupPosition].averageGrade);

            return convertView;
        }

        private String extractGrade(String raw) {
            StringBuilder result = new StringBuilder();
            for (char c : raw.toCharArray()) {
                if (!Character.isDigit(c) && c != '.') break;
                result.append(c);
            }
            String r = result.toString();
            if (r.isEmpty()) return "-";
            return r;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = GradesFragment.this.getActivity().getLayoutInflater().inflate(
                        R.layout.grade_list_item_course, null
                );
            }
            ((TextView) convertView.findViewById((R.id.grade_list_item_course_text)))
                    .setText(gradeSet[groupPosition].grades[childPosition].courseName);
            ((TextView) convertView.findViewById((R.id.grade_list_item_grade_text)))
                    .setText(extractGrade(gradeSet[groupPosition].grades[childPosition].finalGrade));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
