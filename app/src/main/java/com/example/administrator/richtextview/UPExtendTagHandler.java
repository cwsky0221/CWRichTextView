package com.example.administrator.richtextview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;

import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 自定义标签解析类，可以处理i，u，font 中的style标签
 */
public class UPExtendTagHandler implements UPHtmlTagHandler.TagHandler {

    private int startIndex = 0;
    private int endIndex = 0;
    private Context context;
    private ColorStateList mOriginColors;

    private Stack<Map<String, String>> mStack = new Stack();

    public UPExtendTagHandler(Context context, ColorStateList originColors) {
        this.context = context;
        startIndex = 0;
        endIndex = 0;
        mOriginColors = originColors;
    }

    @Override
    public void handleTag(boolean open, String tag, Editable output, Attributes attrs) {

        try {
            if (open) {
                //开标签，output是空（sax还没读到），attrs有值
                if (tag.toLowerCase().equals("span")
                        || tag.toLowerCase().equals("strike")
                        || tag.toLowerCase().equals("i")
                        || tag.toLowerCase().equals("u")
                        || tag.toLowerCase().equals("font")) {
                    parseStyle(attrs);
                }
                startIndex = output.length();
            } else {
                //闭标签，output有值了，attrs没值
                endIndex = output.length();
                if (tag.toLowerCase().equals("span")) {
                    Map<String, String> attrMap = mStack.peek();
                    setForegroundColor(output,attrMap);
                    mStack.pop();
                } else if (tag.toLowerCase().equals("strike")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    output.setSpan(new StrikethroughSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mStack.pop();
                } else if (tag.toLowerCase().equals("i")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                } else if (tag.toLowerCase().equals("u")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                } else if (tag.toLowerCase().equals("a")) {
                    output.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    reductionFontColor(startIndex,endIndex,output);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("font")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseStyle(Attributes attrs){
        Map<String, String> attrMap = new HashMap<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("style")) {
                String style = attrs.getValue(i);
                String[] attrArray = style.split(";");
                if (null != attrArray) {
                    for (String attr : attrArray) {
                        String[] keyValueArray = attr.split(":");
                        if (null != keyValueArray && keyValueArray.length == 2) {
                            // 记住要去除前后空格
                            attrMap.put(keyValueArray[0].trim(), keyValueArray[1].trim());
                        }
                    }
                }
            }
        }
        mStack.push(attrMap);
    }

    private void setForegroundColor(Editable output, Map<String, String> attrMap){
        String color = attrMap.get("color");
        String fontSize = attrMap.get("font-size");
        String underline = attrMap.get("text-decoration");
        if (!TextUtils.isEmpty(color)) {
            if (color.startsWith("@")) {
                Resources res = Resources.getSystem();
                String name = color.substring(1);
                int colorRes = res.getIdentifier(name, "color", "android");
                if (colorRes != 0) {
                    output.setSpan(new ForegroundColorSpan(colorRes), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                try {
                    output.setSpan(new ForegroundColorSpan(Color.parseColor(color)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                    e.printStackTrace();
                    reductionFontColor(startIndex,endIndex,output);
                }
            }
        }

        setBgColor(output,attrMap);

        if (!TextUtils.isEmpty(underline)) {
            output.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }


    private void setBgColor(Editable output, Map<String, String> attrMap){
        if(attrMap!=null){
            String background = attrMap.get("background-color");
            if(background!=null){
                String color1 = background.substring("rgb(".length(),background.indexOf(","));
                background = background.substring(background.indexOf(color1)+color1.length()+1);
                String color2 = background.substring(0,background.indexOf(","));
                background = background.substring(background.indexOf(color2)+color2.length()+1);
                String color3 = background.substring(0,background.indexOf(")"));
                output.setSpan(new BackgroundColorSpan(Color.rgb(Integer.parseInt(color1.trim()), Integer.parseInt(color2.trim()), Integer.parseInt(color3.trim()))), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * 还原为原来的颜色
     * @param startIndex
     * @param stopIndex
     * @param editable
     */
    private void reductionFontColor(int startIndex,int stopIndex,Editable editable){
        if (null != mOriginColors){
            editable.setSpan(new TextAppearanceSpan(null, 0, 0, mOriginColors, null),
                    startIndex, stopIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            editable.setSpan(new ForegroundColorSpan(0xff2b2b2b), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}