package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import static java.lang.Math.abs;

/**
 * Created by shelbymarcus on 2/25/18.
 */

public class Shape{
    // instance variables
    private float upperLeftx, upperLefty;
    private float width, height;
    private Paint lightGreyOutlinePaint;
    private String textField;
    private String imgName;
    private String shapeName;
    private boolean hidden;
    private boolean movable;
    private float textWidth;
    private float textHeight;
    private String onEnter;
    private String onClick;
    private String onDrop;
    private String font;


    public Shape(float x1, float y1, float w, float h, String shapeName, String imageName, String text, boolean hidden, boolean movable, String fontName){
        /* set boundaries of shape */
        this.upperLeftx = x1;
        this.upperLefty = y1;
        this.width = w;
        this.height = h;

        this.hidden = hidden;
        this.movable = movable;
        this.font = fontName;
        if(text!=null) this.textField = text;
        else this.textField = "";

        if(imageName!=null)this.imgName = imageName;
        else this.imgName = "";

        this.shapeName = shapeName;             // create default name if null

        init();
    }

    private void init() {
        textWidth = 0;
        textHeight = 0;

        lightGreyOutlinePaint = new Paint();
        lightGreyOutlinePaint.setColor(Color.LTGRAY);
        lightGreyOutlinePaint.setStyle(Paint.Style.STROKE);
        lightGreyOutlinePaint.setStrokeWidth(10.0f);

        onEnter = " ";                   // need to be init to " " else tokenizing on "," will fail
        onClick = " ";
        onDrop = " ";

    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        // standard equals() technique 2 (null will be false)
        if (!(obj instanceof Shape)) return false;
        Shape other = (Shape)obj;
        return other.getBoundaries().equals(this.getBoundaries())
                && other.getTextField().equals(this.getTextField())
                && other.getImageName().equals(this.getImageName())
                && other.getShapeName().equals(this.getShapeName());
    }

    public void setTextDimensions(PointF dimensions){
        this.textWidth = dimensions.x;
        this.textHeight = dimensions.y;
    }

    public PointF getTextDimensions(){
        return new PointF(textWidth,textHeight);
    }

    public boolean isHidden(){
        return hidden;
    }

    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public boolean isMovable(){
        return movable;
    }

    public void setMovable(boolean movable) { this.movable = movable; }

    public String getTextField(){
        return textField;
    }

    public String getShapeName(){
        return this.shapeName;
    }

    //used for database
    public String getBoundaries(){
        String boundaries = "";
        boundaries += upperLeftx + "," + upperLefty + "," + width + "," + height;
        return boundaries;
    }


    public PointF getCoordinates(){
        PointF point = new PointF(this.upperLeftx,this.upperLefty);
        return point;
    }

    public void setCoordinates(float x, float y) {
        this.upperLeftx = x;
        this.upperLefty = y;
    }


    public void setWidth(float width){this.width = width;}

    public float getWidth() {return width;}

    public void setHeight(float height){this.height = height;}

    public float getHeight() {return height;}

    public void setImageName(String name){
        this.imgName = name;
    }

    public String getImageName(){
        return imgName;
    }


    public void setFont(String fontName){
        this.font = fontName;
    }

    public String getFont(){
        return font;
    }

    public void setScriptAction(String action, String object, int triggerIndex, String onDropShapeName){
        if(triggerIndex == 0){
            addOnClickAction(action, object);
        }else if(triggerIndex == 1){
            addOnEnterAction(action, object);
        }else if (triggerIndex == 2){
            addOnDropAction(onDropShapeName, action, object);
        }
    }

    private void addOnClickAction(String action, String object) {
        onClick += " onClick " + action.trim() + " " + object.trim();
    }

    private void addOnEnterAction(String action, String object) {
        onEnter += " onEnter " + action.trim() + " " + object.trim();
    }

    private void addOnDropAction(String onDropShapeName, String action, String object) {
        onDrop += " onDrop " + onDropShapeName.trim() + " " + action.trim() + " " + object.trim();
    }

    public void setOnClickAction(String onClickActions){onClick = onClickActions;}

    public void setOnEnterAction(String onEnterActions){onEnter = onEnterActions;}

    public void setOnDropAction(String onDropActions){onDrop = onDropActions;}

    public String getOnClick() {
        return onClick;
    }

    public String getOnEnter() {
        return onEnter;
    }

    public String getOnDrop() {
        return onDrop;
    }
}