package com.example.templatedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Template extends AppCompatActivity {


    RelativeLayout relativeLayout ;
    StickerView stickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        relativeLayout = findViewById(R.id.parent);

        stickerView = findViewById(R.id.stickerContainer);
        String jsonString = "{\n" +
                "    \"parent\":{\n" +
                "        \"type\" : \"RelativeLayout\",\n" +
                "        \"orientation\" : \"vertical\",\n" +
                "        \"background\" : \"#E57EF1\"\n" +
                "    },\n" +
                "    \"Imageview\":[\n" +
                "        {\n" +
                "          \"layout_width\": \"600\",\n" +
                "          \"layout_height\": \"830\",\n" +
                "          \"layout_marginTop\" : \"80\",\n" +
                "          \"layout_marginStart\" : \"90\",\n" +
                "          \"background\" : \"#e6e6e6\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"layout_width\": \"600\",\n" +
                "          \"layout_height\": \"830\",\n" +
                "          \"layout_marginTop\" : \"600\",\n" +
                "          \"layout_marginStart\" : \"345\",\n" +
                "          \"background\" : \"#ffffff\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Stickerview\" :  [{\n" +
                "          \"background\" : \"#b3b3b3\"\n" +
                "        }\n]" +
                "}\n";

        try {
            convert(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void convert(String jsonString) throws JSONException {

        JSONObject jsonObject;
        jsonObject = new JSONObject(jsonString);

        JSONObject parent = jsonObject.getJSONObject("parent");

        initializeParent(parent);

        JSONArray jsonStickerArray = jsonObject.getJSONArray("Stickerview");
        ArrayList<JSONObject> stickerImageViews= new ArrayList<>();

        for(int i=0;i<jsonStickerArray.length();i++){
            stickerImageViews.add(jsonStickerArray.getJSONObject(i));
        }

        for(int i=0;i<jsonStickerArray.length();i++){
            initializeStickerViews(stickerImageViews.get(i));
        }

        JSONArray jsonArray = jsonObject.getJSONArray("Imageview");
        ArrayList<JSONObject> imageViews= new ArrayList<>();

        for(int i=0;i<jsonArray.length();i++){
            imageViews.add(jsonArray.getJSONObject(i));
        }


        for(int i=0;i<jsonArray.length();i++){
            initializeImageViews(imageViews.get(i));
        }



    }

    @SuppressLint("ResourceAsColor")
    private void initializeStickerViews(JSONObject jsonObject) {
        TextSticker sticker = new TextSticker(getApplicationContext());

        stickerView.addSticker(sticker,Sticker.Position.CENTER);
        sticker.setText("Hey Move me!");
        sticker.setTextColor(R.color.colorPrimary);
        sticker.drawFont();
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        stickerView.invalidate();
    }

    private void initializeParent(JSONObject parent) throws JSONException {

        if(parent.has("background")) {
            String colorString = parent.getString("background");
            relativeLayout.setBackgroundColor(getAttributeColor(colorString));
        }
    }

    private void initializeImageViews(JSONObject imageView) throws JSONException{
        Log.d("ImageAdded","Called");
        ImageView img = new ImageView(this);

        if(imageView.has("background")){
            String colorString = imageView.getString("background");
            img.setBackgroundColor(getAttributeColor(colorString));
        }
        if(imageView.has("layout_height")){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Integer.parseInt(imageView.getString("layout_width")), Integer.parseInt(imageView.getString("layout_height")));
            img.setLayoutParams(layoutParams);
        }
        if(imageView.has("layout_marginTop")){
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) img.getLayoutParams();
            marginParams.setMargins(Integer.parseInt(imageView.getString("layout_marginStart")), Integer.parseInt(imageView.getString("layout_marginTop")), 0, 0);
        }

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Image Added",Toast.LENGTH_SHORT).show();
            }
        });

        relativeLayout.addView(img, img.getLayoutParams());

    }


    private int getAttributeColor(String value) {
        if(value!=null && value.equals("null")){
            return Color.TRANSPARENT;
        }
        return value != null ? getColorFromString(value) : Color.TRANSPARENT;
    }

    public static int getColorFromString(String value) {
        int color = Color.TRANSPARENT;
        if (value.length() == 7 || value.length() == 9) {
            color = Color.parseColor(value);
        } else if (value.length() == 4) {
            color = Color.parseColor("#"
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(2)
                    + value.charAt(2)
                    + value.charAt(3)
                    + value.charAt(3));
        } else if (value.length() == 2) {
            color = Color.parseColor("#"
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1)
                    + value.charAt(1));
        }
        return color;
    }
}
