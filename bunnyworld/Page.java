package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.*;
import android.widget.RadioButton;
import android.content.Context;
import android.graphics.*;
import android.widget.*;
import java.util.*;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.round;

/**
 * Created by shelbymarcus on 2/25/18.
 */

public class Page extends View {
    // instance variables
    private int numShapes;
    private int pageNumber;
    private float x, y;
    private float move_x, move_y;
    private boolean moving;
    private BitmapDrawable bmapCarrot;
    private BitmapDrawable bmapCarrot2;
    private BitmapDrawable bmapDeath;
    private BitmapDrawable bmapDuck;
    private BitmapDrawable bmapFire;
    private BitmapDrawable bmapMystic;
    public ArrayList<Shape> shapes;
    private String spinnerSelection;
    private String fontSpinnerSelection;        //shelby
    private Paint paintText;
    private Paint lightGreyOutlinePaint;
    private Paint blueOutlinePaint;
    private Paint blueOutlinePaintText;
    private Paint greenOutlinePaint;
    private Paint redFillPaint;
    private Paint greenFillPaint;
    private int WIDTH = 120;
    private int HEIGHT = 120;
    public Shape selectedShape;
    /*added by CRYSTAL wed night*/
    private Shape backupShape;
    private Shape newShape;
    /*CRYSTAL THURSDAY NIGHT*/
    private ArrayList<String> shapeNames;
    private int screenWidth;
    private boolean reshaping = false;
    private float RESHAPING_X = 0;
    private float RESHAPING_Y = 0;
    private float up_x = 0;
    private float up_y = 0;

    private int corner = 0;

    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;

    private static String NO_IMAGE = "no image";
    private static final int TEXT_BUFFER = 10;
    private static final int OVAL_SIZE = 30;
    public boolean flexPossession = false;
    private String pageName;
    private static final String DEFAULT = "Default";
    private static final String DEFAULT_BOLD = "Default Bold";
    private static final String MONOSPACE = "Monospace";
    private static final String SANS_SERIF = "Sans Serif";
    private static final String SERIF = "Serif";
    private static final String WHITE = "White";
    private static final String YELLOW = "Yellow";
    private static final String GREEN = "Green";
    private static final String BLUE = "Blue";
    private static final String PURPLE = "Purple";



    private String backgroundColor;

    private static final ArrayList<String> FONT_STRINGS = new ArrayList<String>() {{
        add(DEFAULT);
        add(DEFAULT_BOLD);
        add(MONOSPACE);
        add(SANS_SERIF);
        add(SERIF);
    }};

    private static final ArrayList<String> IMAGE_STRINGS = new ArrayList<String>() {{
        add(NO_IMAGE);
        add("carrot");
        add("carrot2");
        add("death");
        add("duck");
        add("fire");
        add("mystic");
    }};

    private static final ArrayList<String> BACKGROUND_STRINGS = new ArrayList<String>(){{
        add(WHITE);
        add(YELLOW);
        add(GREEN);
        add(BLUE);
        add(PURPLE);
    }};

    public Page(Context ctx, AttributeSet attr) {
        super(ctx, attr);
        init();
    }

    private void init() {
        backgroundColor = "White";
        resetNumShapes();                                   //        this.numShapes = 0;
        shapes = new ArrayList<Shape>();
        /*CRYSTAL THURSDAY NIGHT*/
        shapeNames = new ArrayList<String>();
        selectedShape = null;
        pageNumber = 1;
        pageName = "page1";// SHELBY
        initialize_bmaps();
        initialize_paints();
        fontSpinnerSelection = DEFAULT;
    }

    private void initialize_paints() {
        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(48);

        greenOutlinePaint = new Paint();
        greenOutlinePaint.setColor(Color.GREEN);
        greenOutlinePaint.setStyle(Paint.Style.STROKE);
        greenOutlinePaint.setStrokeWidth(10.0f);


        lightGreyOutlinePaint = new Paint();
        lightGreyOutlinePaint.setColor(Color.LTGRAY);
        lightGreyOutlinePaint.setStyle(Paint.Style.STROKE);
        lightGreyOutlinePaint.setStrokeWidth(10.0f);

        blueOutlinePaint = new Paint();
        blueOutlinePaint.setColor(Color.BLUE);
        blueOutlinePaint.setStyle(Paint.Style.STROKE);
        blueOutlinePaint.setStrokeWidth(10.0f);

        blueOutlinePaintText = new Paint();
        blueOutlinePaintText.setColor(Color.BLUE);
        blueOutlinePaintText.setStyle(Paint.Style.STROKE);
        blueOutlinePaintText.setStrokeWidth(5.0f);

        redFillPaint = new Paint();
        redFillPaint.setColor(Color.RED);
        redFillPaint.setStyle(Paint.Style.FILL);

        greenFillPaint = new Paint();
        greenFillPaint.setColor(Color.GREEN);
        greenFillPaint.setStyle(Paint.Style.FILL);
    }

    private void initialize_bmaps() {
        bmapCarrot = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot);
        bmapCarrot2 = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot2);
        bmapDeath = (BitmapDrawable) getResources().getDrawable(R.drawable.death);
        bmapDuck = (BitmapDrawable) getResources().getDrawable(R.drawable.duck);
        bmapFire = (BitmapDrawable) getResources().getDrawable(R.drawable.fire);
        bmapMystic = (BitmapDrawable) getResources().getDrawable(R.drawable.mystic);
    }

    public String generateInsertString(String tableName, boolean editor) {
        StringBuilder sb = new StringBuilder();
        String table_name = tableName + "PageTable";
        if (!editor) table_name += "Copy";
        String beginning = "INSERT INTO " + table_name + " VALUES";
        sb.append(beginning);
        for (Shape s : shapes) {
            int hidden = 0;
            int movable = 0;
            if (s.isHidden()) hidden = 1;
            if (s.isMovable()) movable = 1;
            String script = s.getOnClick() + "," + s.getOnEnter() + "," + s.getOnDrop();
            String currShape =
                    "(" + pageNumber
                            + ",'"
                            + pageName
                            + "','"
                            + s.getShapeName()
                            + "','"
                            + s.getBoundaries()
                            + "','"
                            + s.getImageName()
                            + "','"
                            + s.getTextField()
                            + "',"
                            + hidden
                            + ","
                            + movable
                            + ",'"
                            + script
                            + "','"
                            + s.getFont()
                            + "',"
                            + "NULL),";
            System.out.println(currShape);
            sb.append(currShape);
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");
        return sb.toString();
    }

    public String generatePageColorInsertString(String tableName){
        if(pageName.equals("")) pageName = "page" + pageNumber;
        String query = "INSERT INTO " + tableName + "PageColorTable VALUES(" + pageNumber + ",'" + pageName + "','" + backgroundColor + "',NULL)";
        System.out.println(query);
        return query;
    }

    public void setPageColor(String color){
        backgroundColor = color;
        Spinner spinner = (Spinner) ((Activity) getContext()).findViewById(R.id.backgroundColor);
        spinner.setSelection(BACKGROUND_STRINGS.indexOf(backgroundColor));

    }

    public String getPageColor(){
        return backgroundColor;
    }

    public void remakeShape(int pageNumber, String shapeName, String bounds, String imageName, String shapeText, int hidden, int movable, String script, String font) {
        /*CRYSTAL THURSDAY NIGHT*/
//        shapeNames.add(shapeName);
        StringTokenizer tk = new StringTokenizer(bounds, ",");
        // should error check but YOLOOO -shelby
        float x1 = (Float.parseFloat(tk.nextToken()));
        float y1 = (Float.parseFloat(tk.nextToken()));
        float w = (Float.parseFloat(tk.nextToken()));
        float h = (Float.parseFloat(tk.nextToken()));

        boolean hiddenBool = false;
        if (hidden == 1) hiddenBool = true;

        boolean movableBool = false;
        if (movable == 1) movableBool = true;

        Shape shape = new Shape(x1, y1, w, h, shapeName, imageName, shapeText, hiddenBool, movableBool, font);

        /* parse script from script saved in DB for this shape*/
        StringTokenizer scriptTokenizer = new StringTokenizer(script, ",");
        if (scriptTokenizer.hasMoreTokens()) {
            String onClick = scriptTokenizer.nextToken();
            shape.setOnClickAction(onClick);
            if (scriptTokenizer.hasMoreTokens()) {
                String onEnter = scriptTokenizer.nextToken();
                shape.setOnEnterAction(onEnter);
                if (scriptTokenizer.hasMoreTokens()) {
                    String onDrop = scriptTokenizer.nextToken();
                    shape.setOnDropAction(onDrop);
                }
            }
        }
        shapes.add(shape);
    }


    /* CRYSTAL THURSDAY */
    private boolean checkNameDuplicate() {
        EditText name = (EditText) ((Activity) getContext()).findViewById(R.id.name);
        if(name == null) return false;
        String shapeName = name.getText().toString();
        if (shapeNames.contains(shapeName)) {
            return true;
        }
        return false;
    }


    private void addShape(float xcoord, float ycoord, boolean updateShape) {
        if(!updateShape){
            System.out.println("HEREERE");
            if(checkNameDuplicate()) {
                Toast.makeText(getContext(), "Shape name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        numShapes++;
        EditText name = (EditText) ((Activity) getContext()).findViewById(R.id.name);
        String shapeName = name.getText().toString();
        if (shapeName.contains("(") || shapeName.contains(")")) {
            Toast.makeText(getContext(), "Shape names cannot contain \'(\' or \')\'", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shapeName.equals("")) shapeName = "shape" + numShapes;

        EditText text = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);

        if (spinnerSelection == null) spinnerSelection = "";

        CheckBox hidden = (CheckBox) ((Activity) getContext()).findViewById(R.id.hidden);
        CheckBox movable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        EditText x2 = (EditText) ((Activity) getContext()).findViewById(R.id.x2);
        EditText y2 = (EditText) ((Activity) getContext()).findViewById(R.id.y2);

        float width = WIDTH;
        if(x2.getText().toString().length()>0 && updateShape){
            float x2f = Float.valueOf(x2.getText().toString());
            width = abs(xcoord-x2f);
        }

        float height = HEIGHT;

        if(y2.getText().toString().length()>0 && updateShape){
            float y2f = Float.valueOf(y2.getText().toString());
            height = abs(ycoord-y2f);
        }

        Shape shape = new Shape(xcoord, ycoord, width, height, shapeName, spinnerSelection, text.getText().toString(), hidden.isChecked(), movable.isChecked(), fontSpinnerSelection);

        if(updateShape){
            EditText onClickEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onClickEditText);
            if(onClickEditText != null){
                shape.setOnClickAction(onClickEditText.getText().toString());
            }
            EditText onEnterEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onEnterEditText);
            if(onEnterEditText != null){
                shape.setOnEnterAction(onEnterEditText.getText().toString());
            }
            EditText onDropEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onDropEditText);
            if(onDropEditText != null){
                shape.setOnDropAction(onDropEditText.getText().toString());
            }
        }
        shapeNames.add(shapeName);      // COULD BREAK EVERYTHING
        shapes.add(shape);
    }

    public void incrNumShapes() {
        this.numShapes++;
    }

    public void resetNumShapes() {
        this.numShapes = 0;
    }

    /* CRYSTAL THURSDAY NIGHT */
    public void resetShapeNames(){
        this.shapeNames.clear();
    }

    public int getNumShapes() {
        return numShapes;
    }

    public void setPageNum(int pageNum) {
        this.pageNumber = pageNum;
    }

    public BitmapDrawable getBitMap(String imgName) {
        switch (imgName) {
            case ("carrot"):
                return bmapCarrot;
            case ("carrot2"):
                return bmapCarrot2;
            case ("death"):
                return bmapDeath;
            case ("duck"):
                return bmapDuck;
            case ("fire"):
                return bmapFire;
            case ("mystic"):
                return bmapMystic;
            default:
                return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        SCREEN_WIDTH = canvas.getWidth();
        int numShapesToDisplay = SCREEN_WIDTH / (WIDTH + TEXT_BUFFER);
        SCREEN_HEIGHT = canvas.getHeight();
        int shape_i = 0;
        RadioButton selectButton = (RadioButton) ((Activity) getContext()).findViewById(R.id.select);
        boolean gameMode = selectButton == null;                    // know if in game mode if no selectButton

        /* get all shapes from database and draw them */
        for (Shape s : shapes) {
            if(s.isHidden() && gameMode) continue;                              // SHELBY adding, shape is hidden in game mode
            float s_width = s.getWidth();
            float s_height = s.getHeight();
            PointF pt = s.getCoordinates();
            float x_coord = pt.x;
            float y_coord = pt.y;
            boolean isImage = false;
            boolean isRect = false;
            boolean isText = false;
            boolean isPossessions = false;
            System.out.println("IMAGE: " + s.getImageName());
            if (pageNumber == 0 & !flexPossession) isPossessions = true;        // added for possessions interface moving shapes
            if (!s.getTextField().isEmpty()) isText = true;
            else if (!s.getImageName().equals(NO_IMAGE)) isImage = true;
            else isRect = true;

            /* update coordinates if in possessions page */
            if (isPossessions) {
                x_coord = shape_i * s_width + TEXT_BUFFER;
                if(isText) y_coord = SCREEN_HEIGHT - TEXT_BUFFER;               // y value is lower left for text
                else y_coord = SCREEN_HEIGHT - TEXT_BUFFER - s_height;            // y value is upper left for image & rect
            }
            s.setCoordinates(x_coord, y_coord);

            if(s.getShapeName().equals("OUTLINE")){
                drawRectangle(canvas, s, x_coord, y_coord, s_width, s_height, greenOutlinePaint, gameMode);
                continue;
            }
            if(isText) drawText(canvas, s, x_coord, y_coord, gameMode);
            else if(isImage) drawImage(canvas, s, x_coord, y_coord, s_width, s_height, gameMode);
            else drawRectangle(canvas, s, x_coord, y_coord, s_width, s_height, lightGreyOutlinePaint, gameMode);
            shape_i++;
        }
    }


    //TODO: TREVOR: in progress... the goal with the commented out code is to make it so that when the user moves
    //around a selected shape, it doesn't snap the shape to the top left corner of the cursor
    private void drawRectangle(Canvas canvas, Shape s, float x_coord, float y_coord, float width, float height, Paint outlinePaint, boolean gameMode){
        //change x-coord based on difference between move_x and x?
        if (s.equals(selectedShape) && !gameMode) {
            float diff_x = 0;
            float diff_y = 0;
            if (moving) {
                diff_x = abs(x - x_coord);
                diff_y = abs(y - y_coord);
            }
            canvas.drawRect(x_coord, y_coord, x_coord + width, y_coord + height, blueOutlinePaint);
            drawResizeBoundaries(canvas, x_coord, y_coord);
        } else {
            canvas.drawRect(x_coord, y_coord, x_coord + width, y_coord + height, outlinePaint);
        }
    }

    private void drawImage(Canvas canvas, Shape s, float x_coord, float y_coord, float width, float height, boolean gameMode){
        BitmapDrawable bm = getBitMap(s.getImageName());
        Bitmap bmap = bm.getBitmap();
        bmap = Bitmap.createScaledBitmap(bmap, round(width), round(height), false);
        canvas.drawBitmap(bmap, x_coord, y_coord, null);
        if (s.equals(selectedShape) && !gameMode) {
            canvas.drawRect(x_coord, y_coord, width + x_coord, height + y_coord, blueOutlinePaint);
            drawResizeBoundaries(canvas, x_coord, y_coord);
        }
    }

    private void drawText(Canvas canvas, Shape s, float x_coord, float y_coord, boolean gameMode){
        paintText.setTypeface(getFontType(s.getFont()));
        canvas.drawText(s.getTextField(), x_coord, y_coord, paintText);
        Rect result = new Rect();
        paintText.getTextBounds(s.getTextField(), 0, s.getTextField().length(), result);

        float text_width = result.width();
        float text_height = result.height();
        s.setTextDimensions(new PointF(text_width, text_height));
        if (s.equals(selectedShape) && !gameMode){
            canvas.drawRect(x_coord - TEXT_BUFFER, y_coord - text_height - (float) 0.5 * TEXT_BUFFER, x_coord + text_width + TEXT_BUFFER,y_coord + TEXT_BUFFER, blueOutlinePaintText);
        }
    }


    /*Trevor's resize code*/
    private void drawResizeBoundaries(Canvas canvas, float x_coord, float y_coord) {
        /* TODO: change width and height to selectedShape.width and selectedShape.height*/
        makeTopLeftOval(canvas, x_coord, y_coord);
        makeTopRightOval(canvas, x_coord, y_coord);
        makeBottomLeftOval(canvas, x_coord, y_coord);
        makeBottomRightOval(canvas, x_coord, y_coord);
    }

    private void makeTopLeftOval(Canvas canvas, float x_coord, float y_coord){
        Paint paint = redFillPaint;
        if(corner==1) paint = greenFillPaint;
        RectF topLeftOval= new RectF(x_coord, y_coord,
                x_coord + OVAL_SIZE, y_coord + OVAL_SIZE);

        canvas.drawOval(topLeftOval, paint);
    }

    private void makeTopRightOval(Canvas canvas, float x_coord, float y_coord){
        Paint paint = redFillPaint;
        if(corner==2) paint = greenFillPaint;
        RectF topRightOval = new RectF(x_coord + selectedShape.getWidth() - OVAL_SIZE, y_coord,
                x_coord + selectedShape.getWidth(), y_coord + OVAL_SIZE);
        canvas.drawOval(topRightOval, paint);
    }

    private void makeBottomLeftOval(Canvas canvas, float x_coord, float y_coord){
        Paint paint = redFillPaint;
        if(corner==3) paint = greenFillPaint;
        RectF bottomLeftOval= new RectF(x_coord, y_coord +  selectedShape.getHeight() - OVAL_SIZE,
                x_coord + OVAL_SIZE, y_coord +  selectedShape.getHeight());
        canvas.drawOval(bottomLeftOval, paint);
    }

    private void makeBottomRightOval(Canvas canvas, float x_coord, float y_coord){
        Paint paint = redFillPaint;
        if(corner==4) paint = greenFillPaint;
        RectF bottomRightOval = new RectF(x_coord + selectedShape.getWidth() - OVAL_SIZE, y_coord  + selectedShape.getHeight() - OVAL_SIZE,
                x_coord + selectedShape.getWidth(), y_coord + selectedShape.getHeight());
        canvas.drawOval(bottomRightOval, redFillPaint);
    }


    private Typeface getFontType(String fontChoice){
        switch(fontChoice){
            case DEFAULT:
                return Typeface.DEFAULT;
            case DEFAULT_BOLD:
                return Typeface.DEFAULT_BOLD;
            case MONOSPACE:
                return Typeface.MONOSPACE;
            case SANS_SERIF:
                return Typeface.SANS_SERIF;
            case SERIF:
                return Typeface.SERIF;
            default:
                return Typeface.DEFAULT;
        }
    }

    public void selectShape(PointF p) {
        selectedShape = null;
        for (Shape s : shapes) {
            float x_1 = s.getCoordinates().x;
            float y_1 = s.getCoordinates().y;

            float w = s.getWidth();
            float h = s.getHeight();
            float x_2 = x_1 + w;
            float y_2 = y_1 + h;

            /* if is a text box, want to set coordinates based on length of text */
            boolean textExists = false;
            if (!s.getTextField().equals("")) textExists = true;
            if(textExists){
                w = s.getTextDimensions().x;
                h = s.getTextDimensions().y;
                x_2 = x_1 + w;
                y_2 = y_1;        // switch y_1 and y_2 b/c of upper left/lower left diff with text
                y_1 = y_1 - h;
            }
            if (p.y >= y_1 && p.y <= y_2) {
                if (p.x >= x_1 && p.x <= x_2) {
                    selectedShape = s;
                }
            }
        }
        RadioButton selectButton = (RadioButton) ((Activity) getContext()).findViewById(R.id.select);
        /* if in game mode */
        if(selectButton == null){
            invalidate();
            return;                     // don't want to finish rest of method if game mode
        }
        /* if in editor mode */
        if (selectedShape != null) {
            displayShapeInfo();
            invalidate();
        } else {
            clearInputs();
        }
    }

    public void setSelectedShapeNull() {
        selectedShape = null;
    }


    /* CRYSTAL THURSDAY NIGHT*/
    public void updateShape() {
        for (Shape s : shapes) {
            if (s.equals(selectedShape)) {

                //check name
                /*added by CRYSTAL wed night*/
                backupShape = s;
                PointF point = s.getCoordinates();

                EditText name = (EditText) ((Activity) getContext()).findViewById(R.id.name);

                shapes.remove(s);

                if (name != null) {
                    shapeNames.remove(s.getShapeName());

                    /*CRYSTAL THURSDAY*/
                    if (checkNameDuplicate()) {
                        Toast.makeText(getContext(), "Shape name already exists", Toast.LENGTH_SHORT).show();
                        if (name != null) name.setText(s.getShapeName());
                    }
                    shapeNames.add(name.getText().toString());

                    String nameString = name.getText().toString();
                    if (nameString.contains("(") || nameString.contains(")")) {
                        Toast.makeText(getContext(), "Shape names cannot contain \'(\' or \')\'", Toast.LENGTH_SHORT).show();
                        name.setText(s.getShapeName());
                    }
                }

                //if(checkNameDuplicate())
                numShapes--;                  //SHELBY WED AFTERNOON
                // add shape at text coords
                EditText x1 = (EditText) ((Activity) getContext()).findViewById(R.id.x1);
                EditText y1 = (EditText) ((Activity) getContext()).findViewById(R.id.y1);
                if (x1.getText().toString().equals("") || y1.getText().toString().equals("")) {
                    addShape(point.x, point.y, false);
                } else {
                    float x1f = Float.valueOf(x1.getText().toString());
                    float y1f = Float.valueOf(y1.getText().toString());
                    addShape(x1f, y1f, true);
                }

                selectedShape = shapes.get(shapes.size() - 1);
                /*added by CRYSTAL wed night*/
                newShape = selectedShape;
                displayShapeInfo();
                invalidate();
                return;
            }

        }
    }

    // public void remakeShape(int pageNumber, String shapeName, String bounds, String imageName,
    // String shapeText, int hidden, int movable, String script, String font)
    /*CRYSTAL THURSDAY NIGHT*/
    private int numPastes = 0;
    public void pasteShape(Shape shape){
        PointF p = shape.getCoordinates();
        int hidden = 0;
        if (shape.isHidden()) hidden = 1;

        int movable = 0;
        if (shape.isMovable()) movable = 1;
        String script = shape.getOnClick() + "," + shape.getOnEnter() + "," + shape.getOnDrop();


        String shapeName = shape.getShapeName();
        if (shapeNames.contains(shape.getShapeName())) {
            numPastes++;
            shapeName += numPastes;
        }

        remakeShape(pageNumber, shapeName, shape.getBoundaries(), shape.getImageName(),
                shape.getTextField(), hidden, movable, script, shape.getFont());
        invalidate();
    }

    /*added by CRYSTAL wed night*/
    public int undoShapeUpdate(){
        if(newShape == null && backupShape == null || selectedShape == null) return 0;
        for(Shape s : shapes){
            if(s.equals(newShape) && selectedShape.equals(newShape)){
                //remove the new shape
                shapes.remove(s);
                numShapes--;
                /* CRYSTAL THURSDAY NIGHT */
                shapeNames.remove(s.getShapeName());

                //add the old shape back into shapes
                selectedShape = backupShape;
                displayShapeInfo();
                PointF point = selectedShape.getCoordinates();
                addShape(point.x,point.y,false);
                /*CRYSTAL THURSDAY NIGHT */
                shapeNames.add(selectedShape.getShapeName());

                //unclear if needed
                selectedShape = shapes.get(shapes.size()-1);

                //reset backupShape and newShape
                newShape = null;
                backupShape = null;

                invalidate();
                return 1;
            }
        }
        return 2;
    }

    public void deleteShape() {
        for (Shape s : shapes) {
            if (s.equals(selectedShape)) {
                /* CRYSTAL THURSDAY NIGHT */
                shapeNames.remove(s.getShapeName());
                shapes.remove(s);
                numShapes--;
                selectedShape = null;
                invalidate();
                break;
            }
        }
    }

    public void clearInputs() {
        spinnerSelection = NO_IMAGE;
        Spinner spinner = (Spinner) ((Activity) getContext()).findViewById(R.id.imageNamesSpinner);
        spinner.setSelection(IMAGE_STRINGS.indexOf(NO_IMAGE));

        fontSpinnerSelection = DEFAULT;
        Spinner fontSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontSpinner);
        fontSpinner.setSelection(FONT_STRINGS.indexOf(DEFAULT));

        EditText text = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);
        text.setText("");

        EditText name = (EditText) ((Activity) getContext()).findViewById(R.id.name);
        name.setText("");

        CheckBox hidden = (CheckBox) ((Activity) getContext()).findViewById(R.id.hidden);
        hidden.setChecked(false);
        CheckBox movable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        movable.setChecked(true);

        EditText x1 = (EditText) ((Activity) getContext()).findViewById(R.id.x1);
        x1.setText("");

        EditText y1 = (EditText) ((Activity) getContext()).findViewById(R.id.y1);
        y1.setText("");

        EditText x2 = (EditText) ((Activity) getContext()).findViewById(R.id.x2);
        x2.setText("");

        EditText y2 = (EditText) ((Activity) getContext()).findViewById(R.id.y2);
        y2.setText("");


        EditText onClickEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onClickEditText);
        onClickEditText.setText("");

        EditText onEnterEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onEnterEditText);
        onEnterEditText.setText("");

        EditText onDropEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onDropEditText);
        onDropEditText.setText("");



        invalidate();
    }

    public void displayShapeInfo() {
        if (selectedShape == null) return;
        /* reflect selection of image spinner for this shape */
        spinnerSelection = selectedShape.getImageName();
        Spinner spinner = (Spinner) ((Activity) getContext()).findViewById(R.id.imageNamesSpinner);
        spinner.setSelection(IMAGE_STRINGS.indexOf(spinnerSelection));

        /* reflect selection of font spinner for this shape */
        fontSpinnerSelection = selectedShape.getFont();
        Spinner font_spinner = (Spinner) ((Activity) getContext()).findViewById(R.id.fontSpinner);
        font_spinner.setSelection(FONT_STRINGS.indexOf(fontSpinnerSelection));


        /* display text input for shape */
        EditText text = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);
        text.setText(selectedShape.getTextField());
        /* display name input for shape */
        EditText name = (EditText) ((Activity) getContext()).findViewById(R.id.name);
        name.setText(selectedShape.getShapeName());
        /* display checked/unchecked states of hidden & movable checkboxes for this shape */
        CheckBox hidden = (CheckBox) ((Activity) getContext()).findViewById(R.id.hidden);
        hidden.setChecked(selectedShape.isHidden());
        CheckBox movable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        movable.setChecked(selectedShape.isMovable());

        EditText x1 = (EditText) ((Activity) getContext()).findViewById(R.id.x1);
        x1.setText(String.valueOf(selectedShape.getCoordinates().x));
        EditText y1 = (EditText) ((Activity) getContext()).findViewById(R.id.y1);
        y1.setText(String.valueOf(selectedShape.getCoordinates().y));

        if(!reshaping) {
            EditText x2 = (EditText) ((Activity) getContext()).findViewById(R.id.x2);
            x2.setText(String.valueOf(selectedShape.getCoordinates().x + selectedShape.getWidth()));
            EditText y2 = (EditText) ((Activity) getContext()).findViewById(R.id.y2);
            y2.setText(String.valueOf(selectedShape.getCoordinates().y + selectedShape.getHeight()));
        }

        EditText onClickEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onClickEditText);
        if (!selectedShape.getOnClick().equals(" ")) onClickEditText.setText(selectedShape.getOnClick());
        else onClickEditText.setText("");

        EditText onEnterEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onEnterEditText);
        if (!selectedShape.getOnEnter().equals(" ")) onEnterEditText.setText(selectedShape.getOnEnter());
        else onEnterEditText.setText("");

        EditText onDropEditText = (EditText) ((Activity) getContext()).findViewById(R.id.onDropEditText);
        if (!selectedShape.getOnDrop().equals(" ")) onDropEditText.setText(selectedShape.getOnDrop());
        else onDropEditText.setText("");


    }

    public void setPageName(String name){this.pageName= name;}      //CRYSTAL
    public String getPageName(){return this.pageName;}      //CRYSTAL

    public void setFontSpinnerSelection(String selection) {
        fontSpinnerSelection = selection;
    }
    public String getFontSpinnerSelection() {
        return fontSpinnerSelection;
    }


    public void setSpinnerSelection(String selection) {
        spinnerSelection = selection;
    }
    public String getSpinnerSelection() {
        return spinnerSelection;
    }

    /*Trevor's helper for shape resize. Need to be able to differentiate between this and
      just moving the shape around
     */
    private boolean clickingResizeArea() {
        float selected_x = selectedShape.getCoordinates().x;
        float selected_y = selectedShape.getCoordinates().y;

        //The click area is too small if it's OVAL_SIZE/2, OVAL_SIZE gives user buffer room
        float w = selectedShape.getWidth();
        float h = selectedShape.getHeight();
//        float selected_x2 = selected_x + w;
//        float selected_y2 = selected_y + h;


        float bottom_min = selected_y + h - OVAL_SIZE;
        float bottom_max = selected_y + h + OVAL_SIZE;
        float top_min = selected_y - OVAL_SIZE;
        float top_max = selected_y + OVAL_SIZE;
        float left_min = selected_x;
        float left_max = selected_x + OVAL_SIZE;
        float right_min = selected_x + w - OVAL_SIZE;
        float right_max = selected_x + w + OVAL_SIZE;

        if(x >= left_min && x <= left_max && y >= top_min && y <= top_max) {
//            Toast.makeText(getContext(), "topLeft", Toast.LENGTH_SHORT).show();
            corner = 1;
            return true;
        }

        if(x >= right_min && x <= right_max && y >= top_min && y <= top_max) {
//            Toast.makeText(getContext(), "topRight", Toast.LENGTH_SHORT).show();
            corner = 2;
            return true;
        }
        if(x >= left_min && x <= left_max && y >= bottom_min && y <= bottom_max) {
//            Toast.makeText(getContext(), "bottomLeft", Toast.LENGTH_SHORT).show();
            corner = 3;
            return true;
        }
        if(x >= right_min && x <= right_max && y >= bottom_min && y <= bottom_max) {
//            Toast.makeText(getContext(), "bottomRight", Toast.LENGTH_SHORT).show();
            corner = 4;
            return true;
        }
        return false;
    }

    public PointF formatShapesPosition(float x, float y, float MAX_X, float MAX_Y){
        EditText text = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);
        boolean isText = text.getText().toString().length() > 0;
        if(isText){
            float width = text.getWidth();                 // width for text
            float height = text.getHeight();                // height for text
            if(x + width >= MAX_X) x = MAX_X - width;
            if(y + height >= MAX_Y) y = MAX_Y;
            if(y - height <= 0) y = height;
        }else{
            if(x + WIDTH >= MAX_X) x = MAX_X - WIDTH;
            if(y + HEIGHT >= MAX_Y) y = MAX_Y - HEIGHT;
            if(y - WIDTH <= 0) y = 0;
        }
        return new PointF(x,y);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        RadioButton addToButton = (RadioButton) ((Activity) getContext()).findViewById(R.id.addToPage);
        RadioButton selectButton = (RadioButton) ((Activity) getContext()).findViewById(R.id.select);
        RadioButton pageRB = (RadioButton) ((Activity) getContext()).findViewById(R.id.pageRB);
        if(pageRB.isChecked()) return true;

        boolean gameMode = selectButton == null;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("ACTION DOWN");
                if(reshaping){
                    resetReshapingVariables();
                    invalidate();
                }
                x = e.getX();
                y = e.getY();
                if (!gameMode) {
                    /* react to touch based on which radiobutton is checked */
                    if (selectButton.isChecked()) {
                        // display variables for shape at x,y
                        selectShape(new PointF(x, y));
                        if (selectedShape != null) {
                            if (clickingResizeArea()) {
                                initializeReshaping();
                            }
                        }
                    } else if (addToButton.isChecked()) {
                        // add shape at this x,y
                        addShape(x, y, false);
                        invalidate();
                    }
                    if (selectedShape != null)
                        break;
                }
            case MotionEvent.ACTION_UP:
                System.out.println("ACTION UP");
                up_x = e.getX();
                up_y = e.getY();
                if(reshaping) reshapeShape();
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("ACTION MOVE");
                if(reshaping)break;
                move_x = e.getX();
                move_y = e.getY();
                if (!gameMode) {
                    if (selectedShape != null && selectButton.isChecked()) {
                        selectedShape.setCoordinates(move_x, move_y);
                        displayShapeInfo();
                        invalidate();
                    }
                }
        }
        return true;
    }

    private void initializeReshaping(){
        reshaping = true;
        /* upper left corner being dragged */
        if(corner == 1) {
            RESHAPING_X = x + selectedShape.getWidth();
            RESHAPING_Y = y + selectedShape.getHeight();
        }
        /* upper right corner being dragged */
        if(corner == 2){
            RESHAPING_X = x - selectedShape.getWidth();     // opp corner of one being dragged
            RESHAPING_Y = y + selectedShape.getHeight();
        }
        /* bottom left corner being dragged */
        if(corner == 3) {
            RESHAPING_X = x + selectedShape.getWidth();
            RESHAPING_Y = y - selectedShape.getHeight();
        }
        /* bottom right corner being dragged */
        if(corner == 4) {
            RESHAPING_X = x - selectedShape.getWidth();
            RESHAPING_Y = y - selectedShape.getHeight();
        }

    }

    private void resetReshapingVariables(){
        updateShape();

        displayShapeInfo();
        selectedShape = null;
        reshaping = false;
        RESHAPING_Y = 0;
        RESHAPING_X = 0;
        corner = 0;
    }

    private void reshapeShape() {
        EditText x1 = (EditText) ((Activity) getContext()).findViewById(R.id.x1);
        EditText y1 = (EditText) ((Activity) getContext()).findViewById(R.id.y1);
        EditText x2 = (EditText) ((Activity) getContext()).findViewById(R.id.x2);
        EditText y2 = (EditText) ((Activity) getContext()).findViewById(R.id.y2);

        /* upper left corner being dragged */
        if (corner == 1) {
            x1.setText(String.valueOf(up_x));
            y1.setText(String.valueOf(up_y));
            x2.setText(String.valueOf(RESHAPING_X));
            y2.setText(String.valueOf(RESHAPING_Y));
            resetReshapingVariables();
        }
        /* upper right corner being dragged */
        if (corner == 2) {
            x1.setText(String.valueOf(RESHAPING_X));
            y1.setText(String.valueOf(up_y));
            x2.setText(String.valueOf(up_x));
            y2.setText(String.valueOf(RESHAPING_Y));
            resetReshapingVariables();
        }
        /* bottom left corner being dragged */
        if (corner == 3) {
            x1.setText(String.valueOf(up_x));
            y1.setText(String.valueOf(RESHAPING_Y));
            x2.setText(String.valueOf(RESHAPING_X));
            y2.setText(String.valueOf(up_y));
            resetReshapingVariables();
        }
        /* bottom right corner being dragged */
        if (corner == 4) {
            x1.setText(String.valueOf(RESHAPING_X));
            y1.setText(String.valueOf(RESHAPING_Y));
            x2.setText(String.valueOf(up_x));
            y2.setText(String.valueOf(up_y));
            resetReshapingVariables();
        }
    }

    public void addPossession(Shape s, float xcoord, float orig_y){
        String shapeName = s.getShapeName();
        String text = s.getTextField();
        String imgName = s.getImageName();
        boolean isText = text.length() > 0;
        float ht = getHeight();
        float wd = getWidth();
        float ycoord = ht + orig_y;
        if(isText){
            ycoord = ht + orig_y + s.getTextDimensions().y;
        }
        boolean movable = s.isMovable();
        boolean hidden = s.isHidden();

        Shape shape = new Shape(xcoord, ycoord, s.getWidth(), s.getHeight(), shapeName, imgName, text, hidden, movable, fontSpinnerSelection);
        shapes.add(shape);
        selectedShape = shape;
    }

    public void highlightInGreen(Shape s){
        float x = s.getCoordinates().x;
        float y = s.getCoordinates().y;
        Shape greenOutlineRect = new Shape(x, y, s.getWidth(), s.getHeight(),"OUTLINE", "", "", false, false, fontSpinnerSelection);
        shapes.add(greenOutlineRect);
        invalidate();
    }
}