package com.example.netrasagar.Aadhar;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class AadhaarXMLParser {
    // We don't use namespaces
    private static final String ns = null;
    private static final int NUMBER_OF_PARAMS_IN_SECURE_QR_CODE = 20;

    private AadhaarCard aadhaarCard;

    public AadhaarCard parse(String xmlContent) throws XmlPullParserException, IOException {
        aadhaarCard = new AadhaarCard();
        aadhaarCard.originalXML = xmlContent;
        if(xmlContent.contains("xml")) {
            InputStream in = new ByteArrayInputStream(xmlContent.getBytes());
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                readFeed(parser);
            } finally {
                in.close();
            }
        } else if(xmlContent.matches("[0-9]*")){
            decode_aadhar_qr(xmlContent);
        }
        return aadhaarCard;
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {


        parser.require(XmlPullParser.START_TAG, ns, "PrintLetterBarcodeData");

        aadhaarCard.uid = parser.getAttributeValue(null, "uid");//
        aadhaarCard.name =parser.getAttributeValue(null, "name");// F  L
        aadhaarCard.gender = parser.getAttributeValue(null, "gender"); // M F
        aadhaarCard.yob = parser.getAttributeValue(null, "yob");// Year
        aadhaarCard.co =parser.getAttributeValue(null, "co");
        aadhaarCard.house =parser.getAttributeValue(null, "house"); //
        aadhaarCard.street =parser.getAttributeValue(null, "street"); //
        aadhaarCard.lm = parser.getAttributeValue(null, "lm");
        aadhaarCard.loc = parser.getAttributeValue(null, "loc");
        aadhaarCard.vtc = parser.getAttributeValue(null, "vtc"); //
        aadhaarCard.po = parser.getAttributeValue(null, "po");
        aadhaarCard.dist =parser.getAttributeValue(null, "dist"); //
        aadhaarCard.subdist = parser.getAttributeValue(null, "subdist");
        aadhaarCard.state = parser.getAttributeValue(null, "state"); //
        aadhaarCard.pincode =parser.getAttributeValue(null, "pc"); //
        aadhaarCard.dob =parser.getAttributeValue(null, "dob");
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private void decode_aadhar_qr(String rawString) {
        byte[] msgInBytes = null;
        try {
            msgInBytes = decompressByteArray(new BigInteger(rawString).toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (msgInBytes != null) {
            int[] delimiters = locateDelimiters(msgInBytes);
            int ii=0;
            String referenceId;
            do{
                referenceId= getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
                ii++;
            } while (referenceId.length()!=21);
            aadhaarCard.name = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.dob = formatDate(getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]), new String[] {"dd-MM-yyyy", "dd/MM/yyyy"});
            ii++;
            aadhaarCard.gender = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.co = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.dist = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.lm = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.house = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.loc = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.pincode = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.po = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.state = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.street = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;
            aadhaarCard.subdist = getValueInRange(msgInBytes, delimiters[ii] + 1, delimiters[ii+1]);
            ii++;

        }
    }
    private static byte[] decompressByteArray(byte[] bytes) throws IOException {
        java.io.ByteArrayInputStream bytein = new ByteArrayInputStream(bytes);
        java.util.zip.GZIPInputStream gzin = new GZIPInputStream(bytein);
        java.io.ByteArrayOutputStream byteout = new ByteArrayOutputStream();

        int res = 0;
        byte buf[] = new byte[1024];
        while (res >= 0) {
            res = gzin.read(buf, 0, buf.length);
            if (res > 0) {
                byteout.write(buf, 0, res);
            }
        }
        return byteout.toByteArray();
    }
    private static int[] locateDelimiters(byte[] msgInBytes) {
        int[] delimiters = new int[NUMBER_OF_PARAMS_IN_SECURE_QR_CODE + 1];
        int index = 0;
        int delimiterIndex;
        for (int i = 0; i <= NUMBER_OF_PARAMS_IN_SECURE_QR_CODE && index< msgInBytes.length; i++) {
            delimiterIndex = getNextDelimiterIndex(msgInBytes, index);
            delimiters[i] = delimiterIndex;
            index = delimiterIndex + 1;
        }
        return delimiters;
    }
    private static int getNextDelimiterIndex(byte[] msgInBytes, int index) {
        int i = index;
        for (; i < msgInBytes.length; i++) {
            if (msgInBytes[i] == -1) {
                break;
            }
        }
        return i;
    }
    private static String getValueInRange(byte[] msgInBytes, int start, int end) {
        return new String(Arrays.copyOfRange(msgInBytes, start, end), Charset.forName("ISO-8859-1"));
    }
    private String formatDate(String rawDateString, String[] possibleFormats) {
        if (rawDateString.equals("")) {
            return "";
        }
        SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        ParseException parseException = null;
        for (String fromFormatPattern : possibleFormats) {
            try {
                SimpleDateFormat fromFormat = new SimpleDateFormat(fromFormatPattern);
                date = fromFormat.parse(rawDateString);
                break;
            } catch (ParseException e) {
                parseException = e;
            }
        }
        if (date != null) {
            return toFormat.format(date);
        } else if (parseException != null) {
            System.err.println("Expected dob to be in dd/mm/yyyy or yyyy-mm-dd format, got " + rawDateString);
            return rawDateString;
        } else {
            throw new AssertionError("This code is unreachable");
        }
    }
}
