package com.xiaopo.flying.sticker.richpath;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.xiaopo.flying.sticker.richpath.model.Group;
import com.xiaopo.flying.sticker.richpath.model.Vector;
import com.xiaopo.flying.sticker.richpath.util.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

public class RichPathParser
{
    int counter =0;

    public  RichPathDrawable  retrunRichpathDrawableFromServer(Context context, String url)
    {
        try {
            Log.v("APP", "Downloading File");
            StringBuffer output = new StringBuffer("");
            InputStream stream = null;
            URLConnection conn = new URL(url).openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            if(httpURLConnection.getResponseCode()==HttpURLConnection.HTTP_OK){
                stream = httpURLConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }
            String inputString = output.toString();
            StringBuilder sample = new StringBuilder();
            // Use the URL passed in the AysncClass and return an InputStream to be used in onPostExecute
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

            Vector vector = new Vector();

            myParser.setInput(inputStream,"UTF-8");

            Stack<Group> groupStack = new Stack<>();

            int event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT)  {
                String tagName= myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals(Vector.TAG_NAME)){
                            float width = getAttributeDimen(myParser.getAttributeValue(null,"android:width"),480.0f);
                            float height = getAttributeDimen(myParser.getAttributeValue(null,"android:height"),480.0f);
                            float viewportWidth= getAttributeFloat(myParser.getAttributeValue(null,"android:viewportWidth"),480.0f);
                            float viewportHeight= getAttributeFloat(myParser.getAttributeValue(null,"android:viewportHeight"),480.0f);
                            vector.inflate(height,width,viewportHeight,viewportWidth);
                        }
                        if(tagName.equals(Group.TAG_NAME)){
                            String name = getAttributeName(myParser.getAttributeValue(null,"android:name"));
                            float rotation = Float.parseFloat(myParser.getAttributeValue(null,"android:rotation"));
                            float pivotX = Float.parseFloat(myParser.getAttributeValue(null,"android:pivotX"));
                            float pivotY = Float.parseFloat(myParser.getAttributeValue(null,"android:pivotY"));
                            float scaleX = Float.parseFloat(myParser.getAttributeValue(null,"android:scaleX"));
                            float scaleY = Float.parseFloat(myParser.getAttributeValue(null,"android:scaleY"));
                            float translateX = Float.parseFloat(myParser.getAttributeValue(null,"android:translateX"));
                            float translateY = Float.parseFloat(myParser.getAttributeValue(null,"android:translateY"));
                            Group group = new Group(name,rotation,pivotX,pivotY,scaleX,scaleY,translateX,translateY);
                            if (!groupStack.empty()) {
                                group.scale(groupStack.peek().matrix());
                            }
                            groupStack.push(group);
                        }
                        if(tagName.equals(RichPath.TAG_NAME)){

                            String pathData = myParser.getAttributeValue(null,"android:pathData");
                            RichPath path = new RichPath(pathData);
                            String name = myParser.getAttributeValue(null,"android:name");
                            float fillAlpha = getAttributeFloat(myParser.getAttributeValue(null,"android:fillAlpha"),1.0f);
                            int fillColor = getAttributeColor(myParser.getAttributeValue(null, "android:fillColor"));
                            float strokeAlpha =  getAttributeFloat(myParser.getAttributeValue(null,"android:strokeAlpha"),1.0f);
                            int strokeColor = getAttributeColor(myParser.getAttributeValue(null,"android:strokeColor"));
                            Paint.Cap strokeLineCap =Paint.Cap.BUTT;
                            Paint.Join strokeLineJoin = Paint.Join.MITER;
                            float strokeMiterLimit = getAttributeFloat(myParser.getAttributeValue(null,"android:strokeMeterLimit"),4);
                            float strokeWidth =  getAttributeFloat(myParser.getAttributeValue(null,"android:strokeWidth"),0);
                            float trimPathStart =  getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathStart"),0);
                            float trimPathEnd =  getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathEnd"),1);
                            float trimPathOffset = getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathOffset"),0);
                            Path.FillType fillType = getAttributePathFillType(myParser.getAttributeValue(null,"android:fillType"), Path.FillType.WINDING);
                            path.inflate(pathData,name,fillAlpha,fillColor,strokeAlpha,strokeColor,strokeLineCap,strokeLineJoin,strokeMiterLimit,strokeWidth,trimPathStart,trimPathEnd,trimPathOffset, fillType);

                            if (!groupStack.empty()) {
                                path.applyGroup(groupStack.peek());
                            }
                            vector.paths.add(path);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (Group.TAG_NAME.equals(tagName)) {
                            if (!groupStack.empty()) {
                                groupStack.pop();
                            }
                        }
                        break;
                }
                event = myParser.next();
            }
            final RichPathDrawable alienRichPathDrawable;
            ImageView.ScaleType alienScaleType = ImageView.ScaleType.CENTER;
            alienRichPathDrawable = new RichPathDrawable(vector, alienScaleType);
            inputStream.close();
            return alienRichPathDrawable;
        } catch (IOException | XmlPullParserException e) {
            Log.e("localS","es"+e.getMessage());
            return null;
        }

    }


    public RichPathDrawable getRichPathDrawableFromUri(Context  context, Uri uri)
    {
        try {
            // Use the URL passed in the AysncClass and return an InputStream to be used in onPostExecute
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            Vector vector = new Vector();

            myParser.setInput(inputStream,"UTF-8");

            Stack<Group> groupStack = new Stack<>();

            int event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT)  {
                String tagName= myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals(Vector.TAG_NAME)){
                            float width = getAttributeDimen(myParser.getAttributeValue(null,"android:width"),480.0f);
                            float height = getAttributeDimen(myParser.getAttributeValue(null,"android:height"),480.0f);
                            float viewportWidth= getAttributeFloat(myParser.getAttributeValue(null,"android:viewportWidth"),480.0f);
                            float viewportHeight= getAttributeFloat(myParser.getAttributeValue(null,"android:viewportHeight"),480.0f);
                            vector.inflate(height,width,viewportHeight,viewportWidth);
                        }
                        if(tagName.equals(Group.TAG_NAME)){
                            String name = getAttributeName(myParser.getAttributeValue(null,"android:name"));
                            float rotation = Float.parseFloat(myParser.getAttributeValue(null,"android:rotation"));
                            float pivotX = Float.parseFloat(myParser.getAttributeValue(null,"android:pivotX"));
                            float pivotY = Float.parseFloat(myParser.getAttributeValue(null,"android:pivotY"));
                            float scaleX = Float.parseFloat(myParser.getAttributeValue(null,"android:scaleX"));
                            float scaleY = Float.parseFloat(myParser.getAttributeValue(null,"android:scaleY"));
                            float translateX = Float.parseFloat(myParser.getAttributeValue(null,"android:translateX"));
                            float translateY = Float.parseFloat(myParser.getAttributeValue(null,"android:translateY"));
                            Group group = new Group(name,rotation,pivotX,pivotY,scaleX,scaleY,translateX,translateY);
                            if (!groupStack.empty()) {
                                group.scale(groupStack.peek().matrix());
                            }
                            groupStack.push(group);
                        }
                        if(tagName.equals(RichPath.TAG_NAME)){

                            String pathData = myParser.getAttributeValue(null,"android:pathData");
                            RichPath path = new RichPath(pathData);
                            String name = myParser.getAttributeValue(null,"android:name");
                            float fillAlpha = getAttributeFloat(myParser.getAttributeValue(null,"android:fillAlpha"),1.0f);
                            int fillColor = getAttributeColor(myParser.getAttributeValue(null, "android:fillColor"));
                            float strokeAlpha =  getAttributeFloat(myParser.getAttributeValue(null,"android:strokeAlpha"),1.0f);
                            int strokeColor = getAttributeColor(myParser.getAttributeValue(null,"android:strokeColor"));
                            Paint.Cap strokeLineCap =Paint.Cap.BUTT;
                            Paint.Join strokeLineJoin = Paint.Join.MITER;
                            float strokeMiterLimit = getAttributeFloat(myParser.getAttributeValue(null,"android:strokeMeterLimit"),4);
                            float strokeWidth =  getAttributeFloat(myParser.getAttributeValue(null,"android:strokeWidth"),0);
                            float trimPathStart =  getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathStart"),0);
                            float trimPathEnd =  getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathEnd"),1);
                            float trimPathOffset = getAttributeFloat(myParser.getAttributeValue(null,"android:trimPathOffset"),0);
                            Path.FillType fillType = getAttributePathFillType(myParser.getAttributeValue(null,"android:fillType"), Path.FillType.WINDING);
                            path.inflate(pathData,name,fillAlpha,fillColor,strokeAlpha,strokeColor,strokeLineCap,strokeLineJoin,strokeMiterLimit,strokeWidth,trimPathStart,trimPathEnd,trimPathOffset, fillType);

                            if (!groupStack.empty()) {
                                path.applyGroup(groupStack.peek());
                            }
                            vector.paths.add(path);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (Group.TAG_NAME.equals(tagName)) {
                            if (!groupStack.empty()) {
                                groupStack.pop();
                            }
                        }
                        break;
                }
                event = myParser.next();
            }
            final RichPathDrawable alienRichPathDrawable;
            ImageView.ScaleType alienScaleType = ImageView.ScaleType.CENTER;
            alienRichPathDrawable = new RichPathDrawable(vector, alienScaleType);
            inputStream.close();
            return alienRichPathDrawable;
        } catch (IOException | XmlPullParserException e) {
            return null;
        }
    }

    private Path.FillType getAttributePathFillType(String value,Path.FillType defValue) {
        return value != null ? getPathFillType(Integer.parseInt(value), defValue) : defValue;
    }
    private Path.FillType getPathFillType(int id, Path.FillType defValue) {
        switch (id) {
            case 0:
                return Path.FillType.WINDING;
            case 1:
                return Path.FillType.EVEN_ODD;
            case 2:
                return Path.FillType.INVERSE_WINDING;
            case 3:
                return Path.FillType.INVERSE_EVEN_ODD;
            default:
                return defValue;
        }
    }
    private float getAttributeDimen(String value, float defValue) {

        float dp = Utils.dpToPx( Utils.getDimenFromString(value));
        return value != null ? dp : defValue;
    }
    private float getAttributeFloat(String value, float defValue) { ;
        return value != null ? Float.parseFloat(value) : defValue;
    }
    private int getAttributeColor(String value) {
        if(value!=null && value.equals("null")){
            return Color.TRANSPARENT;
        }
        return value != null ? Utils.getColorFromString(value) : Color.TRANSPARENT;
    }
    private String getAttributeName(String value) {
        counter+=1;
        return value != null ? value: String.valueOf(counter);
    }
}
