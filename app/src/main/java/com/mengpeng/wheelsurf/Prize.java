package com.mengpeng.wheelsurf;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * 作者：MengPeng
 * 时间：2018/4/10 - 下午3:48
 * 说明：
 */
public class Prize {
    private int Id;
    private String Name;
    private Bitmap Icon;
    private Bitmap BgColor;
    private Rect rect;

    public OnClickListener getListener() {
        return listener;
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    OnClickListener listener;

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Bitmap getIcon() {
        return Icon;
    }

    public void setIcon(Bitmap icon) {
        Icon = icon;
    }

    public Bitmap getBgColor() {
        return BgColor;
    }

    public void setBgColor(Bitmap bgColor) {
        BgColor = bgColor;
    }

    public interface OnClickListener {
        void click();
    }

    public void click() {
        if (null!=listener){
            listener.click();
        }
    }

    public boolean isClick(Point touchPoint, int width) {
        if (touchPoint.x < width / 3 * 2 & touchPoint.x > width / 3 & touchPoint.y < width / 3 * 2 & touchPoint.y > width / 3) {
            return true;
        } else {
            return false;
        }
    }
}
