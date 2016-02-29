package com.wwylele.hatechnion;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;


public class XmlUtility {
    private static String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    public static String readTaggedText(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return result;
    }

    public static void writeTaggedText(XmlSerializer xs, String tag, String text)
            throws IOException {
        xs.startTag(null, tag);
        xs.text(text);
        xs.endTag(null, tag);
    }

    public static void readResult(XmlPullParser xpp)
            throws BadResultException, XmlPullParserException, IOException {
        xpp.nextTag();
        xpp.require(XmlPullParser.START_TAG, null, "app");
        xpp.nextTag();
        xpp.require(XmlPullParser.START_TAG, null, "result");
        xpp.nextTag();
        int retCode = Integer.parseInt(XmlUtility.readTaggedText(xpp, "ret_code"));
        xpp.nextTag();
        String retText = XmlUtility.readTaggedText(xpp, "text");
        if (retCode != 0) throw new BadResultException(retCode, retText);
        xpp.nextTag();
        Log.v("readResult", "retCode=" + retCode + " retText=" + retText);
        xpp.require(XmlPullParser.END_TAG, null, "result");
        xpp.nextTag();
        xpp.require(XmlPullParser.START_TAG, null, "data");
        xpp.nextTag();
    }

    public static class BadResultException extends Exception {
        public final int code;
        public final String text;

        public BadResultException(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }
}
