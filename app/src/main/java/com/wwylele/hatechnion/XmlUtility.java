package com.wwylele.hatechnion;

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
}
