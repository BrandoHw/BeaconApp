package org.altbeacon.InternalMap;

import android.graphics.PointF;

public class CategoryPoint {
    private String category;
    private String uid;
    private int image;
    private PointF pointF;


    public CategoryPoint(String category, String uid, int image, PointF pointF) {
        this.category = category;
        this.uid = uid;
        this.image = image;
        this.pointF = pointF;
    }

    public String getUid() {
        return uid;
    }

    public PointF getPointF(){
        return pointF;
    }

    public String getCategory(){
        return category;
    }
    public int getImage(){
        return image;
    }
}
