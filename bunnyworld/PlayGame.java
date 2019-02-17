package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class PlayGame extends AppCompatActivity {

    private Backend helper;
    private Page gamepage;
    private Page possession;
    private int currPage;
    private String gameSave;
    private static final String ON_CLICK = "onClick";
    private static final String ON_ENTER = "onEnter";
    private static final String ON_DROP = "onDrop";
    private static final String GO_TO = "goToPage";
    private static final String PLAY_SOUND = "playSound";
    private static final String HIDE_SHAPE = "hideShape";
    private static final String SHOW_SHAPE = "showShape";

    private static final String EVIL_LAUGH = "evilLaugh";
    private static final String CARROT = "Carrot";
    private static final String FIRE = "Fire";
    private static final String HOORAY = "Hooray";
    private static final String MUNCH = "Munch";
    private static final String MUNCHING = "Munching";
    private static final String WOOF = "Woof";

    private static final String NO_COLOR = "No Color";
    private static final String YELLOW = "Yellow";
    private static final String GREEN = "Green";
    private static final String BLUE = "Blue";
    private static final String PURPLE = "Purple";


    private static final String OUTLINE = "OUTLINE";     // used for onDraw green outline for onDrop


    private MediaPlayer evilLaughmp = null;
    private MediaPlayer carrotmp = null;
    private MediaPlayer firemp = null;
    private MediaPlayer hooraymp = null;
    private MediaPlayer munchmp = null;
    private MediaPlayer munchingmp = null;
    private MediaPlayer woofmp = null;

    private float last_x;
    private float last_y;

    private ArrayList<Shape> shapesToHighlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        Intent intent = getIntent();
        gameSave = intent.getStringExtra("gameSave");

        gamepage = (Page) findViewById(R.id.gamePage);
        possession = (Page) findViewById(R.id.possessionPage);
        shapesToHighlight = new ArrayList<Shape>();
        last_x = 0;
        last_y = 0;
        init();
        getGameFromBackend();

        possession.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float x = e.getX();
                        float y = e.getY();
                        possession.selectShape(new PointF(x, y));
                        if (possession.selectedShape == null) break;
                        //parseScripts();
                    case MotionEvent.ACTION_UP:
                        float move_x = e.getX();
                        float move_y = e.getY();
                        if(possession.selectedShape == null || !possession.selectedShape.isMovable()) break;
                        highlightPotentialDropSites(possession);
                        /* move possession to game page */
                        if(move_y < 0){
                            Shape s = possession.selectedShape;
                            gamepage.addPossession(s, move_x, move_y);
                            gamepage.invalidate();
                            possession.deleteShape();
                        }else {
                            possession.selectedShape.setCoordinates(move_x,move_y);
                            possession.flexPossession = true;
                            possession.invalidate();
                        }
                        break;
                }
                return true;
            }
        });

        gamepage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float x = e.getX();
                        float y = e.getY();
                        gamepage.selectShape(new PointF(x, y));
                        if (gamepage.selectedShape == null) break;
                        /* set last coordinates in case target fails and need to jump back to last known coords */
                        last_x = gamepage.selectedShape.getCoordinates().x;
                        last_y = gamepage.selectedShape.getCoordinates().y;
                        parseScripts();

                    case MotionEvent.ACTION_MOVE:
                        float move_x = e.getX();
                        float move_y = e.getY();
                        if(gamepage.selectedShape == null) break;
                        if(!gamepage.selectedShape.isMovable()) break;
                        highlightPotentialDropSites(gamepage);
                        float MAX_X = view.getRight();
                        float MAX_Y = view.getBottom();
                        PointF moveCoords = formatShapesPosition(move_x, move_y, MAX_X, MAX_Y);
                        gamepage.selectedShape.setCoordinates(moveCoords.x, moveCoords.y);
                        gamepage.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        float x_up = e.getX();
                        float y_up = e.getY();

                        /* check if onDrop targets for the shape being dragged */
                        if(shapesToHighlight.size() > 0){
                            boolean targetFound = shapeDropTarget(x_up, y_up);
                            /* if no target found ( and there were potentials ), snap shape back to prev coords */
                            if(!targetFound) gamepage.selectedShape.setCoordinates(last_x,last_y);
                        }
                        removeHighlights();
                        shapesToHighlight.clear();
                        break;
                }
                return true;
            }
        });
    }

    private void removeHighlights() {
        for (Shape s: gamepage.shapes) {
            if (s.getShapeName().equals(OUTLINE)) {
                gamepage.shapes.remove(s);
            }
        }
        gamepage.invalidate();
    }

    private boolean shapeDropTarget(float p_x, float p_y){
        Shape target = findDropTarget(p_x, p_y);
        if(target == null) return false;
        // do action clause of target's on drop!!!
        String shapeNameSoughtWithPage = gamepage.selectedShape.getShapeName();
        StringTokenizer pageTokSought = new StringTokenizer(shapeNameSoughtWithPage.trim(),"(");
        if(pageTokSought.hasMoreTokens()){
            String shapeNameSought = pageTokSought.nextToken();
            String onDrop = target.getOnDrop();
            StringTokenizer tokenizer = new StringTokenizer(onDrop.trim()," ");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.equals(ON_DROP)) {
                    if (tokenizer.hasMoreTokens()) {
                        String shapeNameTokenWithPg = tokenizer.nextToken();
                        StringTokenizer pageTokenizer = new StringTokenizer(shapeNameTokenWithPg.trim(), "(");
                        if (pageTokenizer.hasMoreTokens()) {
                            String shapeNameToken = pageTokenizer.nextToken();
                            StringTokenizer pageTok = new StringTokenizer(shapeNameToken.trim(), "(");
                            if (pageTok.hasMoreTokens()) {
                                String shapeWithoutPageName = pageTok.nextToken();
                                if (shapeWithoutPageName.equals(shapeNameSought)) {
                                    if (tokenizer.hasMoreTokens()) {
                                        String action = tokenizer.nextToken();
                                        if (tokenizer.hasMoreTokens()) {
                                            String object = tokenizer.nextToken();
                                            doAction(action, object);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private Shape findDropTarget(float p_x, float p_y){
        Shape target = null;
        for (Shape s : shapesToHighlight) {
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
            if (p_y >= y_1 && p_y <= y_2) {
                if (p_x >= x_1 && p_x <= x_2) {
                    return s;
                }
            }
        }
        return target;
    }


    private boolean checkOnDropForShape(Shape s, String shapeNameSought){
        String onDrop = s.getOnDrop();
        StringTokenizer tokenizer = new StringTokenizer(onDrop.trim()," ");
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if(token.equals(ON_DROP)){
                if(tokenizer.hasMoreTokens()){
                    String shapeNameToken = tokenizer.nextToken();
                    StringTokenizer pageTokSought = new StringTokenizer(shapeNameToken.trim(),"(");
                    if(pageTokSought.hasMoreTokens()){
                        String shapeNameWithoutPage = pageTokSought.nextToken();
                        if(shapeNameWithoutPage.equals(shapeNameSought)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void highlightPotentialDropSites(Page pg){
        if(shapesToHighlight.size() > 0) return;
        String shapeName = pg.selectedShape.getShapeName();
        StringTokenizer pageTokSought = new StringTokenizer(shapeName.trim(),"(");
        if(!pageTokSought.hasMoreTokens()) return;
        String shapeNameF = pageTokSought.nextToken();
        System.out.println("SHAPENAME SOUGHT IN HIGHLIGHT POTE: " + shapeNameF);
        for(Shape s:pg.shapes){
            if(checkOnDropForShape(s, shapeNameF)){
                shapesToHighlight.add(s);
            }
        }
        for(Shape s:shapesToHighlight) {
            pg.highlightInGreen(s);
        }
    }

    public PointF formatShapesPosition(float x, float y, float MAX_X, float MAX_Y){
        boolean isText = gamepage.selectedShape.getTextField().length() > 0;
        if(isText){
            float width = gamepage.selectedShape.getTextDimensions().x;                 // width for text
            float height = gamepage.selectedShape.getTextDimensions().y;                // height for text
            if(x + width >= MAX_X) x = MAX_X - width;
            if(y + height >= MAX_Y) y = MAX_Y;
            if(y - height <= 0) y = height;
        }else{
            if(x + 120 >= MAX_X) x = MAX_X - 120;
            if(y + 120 >= MAX_Y) y = MAX_Y - 120;
            if(y - 120 <= 0) y = 0;
        }
        return new PointF(x,y);
    }

    public void init(){
        possession.setPageNum(0);
        currPage = 1;
        helper = Backend.getInstance(this, gameSave, false);
        View pageLayout = (View) findViewById(R.id.gamePage);
        pageLayout.setBackgroundColor(Color.WHITE);
        View possessionLayout = (View) findViewById(R.id.possessionPage);
        possessionLayout.setBackgroundColor(Color.GRAY);
    }

    public void getGameFromBackend(){
        getPageFromDB(0, possession);
        getPageFromDB(1, gamepage);
        gamepage.setPageNum(1);
        executeOnEnters();
    }

    public void executeOnEnters() {
        for (Shape s: gamepage.shapes) {
            String onEnterTrim = s.getOnEnter().trim();
            if(!onEnterTrim.isEmpty())parseAction(onEnterTrim);
        }
        gamepage.invalidate();
    }

    public void parseScripts() {
        String onClickAction = gamepage.selectedShape.getOnClick();
        if (!onClickAction.isEmpty()) parseAction(onClickAction);
    }

    public void parseAction(String scriptAction){
        StringTokenizer tokenizer = new StringTokenizer(scriptAction.trim(), " ");
        while(tokenizer.hasMoreTokens()){
            String trigger = tokenizer.nextToken().trim();
            if(tokenizer.hasMoreTokens()) {
                String action = tokenizer.nextToken().trim();
                if(tokenizer.hasMoreTokens()){
                    String object = tokenizer.nextToken().trim();
                    doAction(action, object);
                }
            }
        }
    }

    public void doAction(String action, String object){
        if(action.equals(GO_TO)){
            goToPage(object);
        }else if(action.equals(HIDE_SHAPE)){
            hideThisShape(object);
        }else if(action.equals(SHOW_SHAPE)){
            showThisShape(object);
        }else if(action.equals(PLAY_SOUND)){
            playSound(object);
        }
    }

    public void hideThisShape(String shapeName){
        StringTokenizer tok = new StringTokenizer(shapeName,"(");
        if(tok.hasMoreTokens()) shapeName = tok.nextToken();
        for(Shape s:gamepage.shapes) {
            if (s.getShapeName().equals(shapeName)){
                s.setHidden(true);
            }
        }
    }

    public void showThisShape(String shapeName){
        StringTokenizer tok = new StringTokenizer(shapeName,"(");
        if(tok.hasMoreTokens()) shapeName = tok.nextToken();
        for(Shape s:gamepage.shapes)
            if(s.getShapeName().equals(shapeName)) s.setHidden(false);
    }

    public int getColor(String pageColor){
        switch(pageColor){
            case NO_COLOR: return R.color.WHITE;
            case YELLOW: return R.color.YELLOW;
            case BLUE: return R.color.BLUE;
            case GREEN: return R.color.YELLOWGREEN;
            case PURPLE: return R.color.MEDPURPLE;
            default: return R.color.WHITE;
        }
    }

    public void getColorForPageInDB(int pagenum) {
        Cursor cr = helper.getPageColor(pagenum);
        String pageColor = NO_COLOR;
        if (cr.moveToFirst()) {
            do {
                //"page INTEGER, pageName STRING, color STRING"
                int pgnum = cr.getInt(cr.getColumnIndex("page"));
                String pageName = cr.getString(cr.getColumnIndex("pageName"));
                pageColor = cr.getString(cr.getColumnIndex("color"));
            } while (cr.moveToNext());
            cr.close();
        }
        Page gamePageLayout = (Page) findViewById(R.id.gamePage);
        gamePageLayout.setBackgroundResource(getColor(pageColor));
    }

    public void getPageFromDB(int pageNum, Page thisPage) {
        getColorForPageInDB(pageNum);
        Cursor cr = helper.lookupPage(pageNum);
        if (cr.moveToFirst()) {
            do {
                int pgNum = cr.getInt(cr.getColumnIndex("page"));
                String name = cr.getString(cr.getColumnIndex("name"));
                String bounds = cr.getString(cr.getColumnIndex("bounds"));
                String image = cr.getString(cr.getColumnIndex("image"));
                String text = cr.getString(cr.getColumnIndex("text"));
                int hidden = cr.getInt(cr.getColumnIndex("hidden"));
                int movable = cr.getInt(cr.getColumnIndex("movable"));
                String script = cr.getString(cr.getColumnIndex("script"));
                String font = cr.getString(cr.getColumnIndex("font"));
                thisPage.remakeShape(pgNum,name,bounds,image, text, hidden,movable,script, font);
                thisPage.incrNumShapes();
            } while (cr.moveToNext());
            cr.close();
        }
        thisPage.invalidate();
    }

    public void wipeScreen(){
        gamepage.shapes.clear();
        gamepage.invalidate();
        gamepage.resetNumShapes();
    }

    public void sendShapesToBackend(){
        helper.clearPage(currPage);
        if (gamepage.getNumShapes() != 0) {
            String dataStr = gamepage.generateInsertString(gameSave, false);
            helper.addPage(dataStr);
        }
    }

    public void goToPage(String pageName){
        int newPage = helper.getPageNumForPageName(pageName);
        if(newPage == -1) System.out.println("FUCKED");
//        int newPage = Integer.parseInt(pageNum);
        sendShapesToBackend();
        wipeScreen();
        gamepage.setSelectedShapeNull();
        currPage = newPage;
        gamepage.setPageNum(newPage);
        getPageFromDB(newPage, gamepage);
        executeOnEnters();
    }

    public void playSound(String mp3){
        switch(mp3) {
            case "Evil":
                if (evilLaughmp == null) evilLaughmp = MediaPlayer.create(this, R.raw.evillaugh);
                evilLaughmp.setVolume(1.0f, 1.0f);
                evilLaughmp.start();
                break;
            case EVIL_LAUGH:
                if (evilLaughmp == null) evilLaughmp = MediaPlayer.create(this, R.raw.evillaugh);
                evilLaughmp.setVolume(1.0f, 1.0f);
                evilLaughmp.start();
                break;
            case CARROT:
                if (carrotmp == null) carrotmp = MediaPlayer.create(this, R.raw.carrotcarrotcarrot);
                carrotmp.setVolume(1.0f, 1.0f);
                carrotmp.start();
                break;
            case FIRE:
                if (firemp == null) firemp = MediaPlayer.create(this, R.raw.fire);
                firemp.setVolume(1.0f, 1.0f);
                firemp.start();
                break;
            case HOORAY:
                if (hooraymp == null) hooraymp = MediaPlayer.create(this, R.raw.hooray);
                hooraymp.setVolume(1.0f, 1.0f);
                hooraymp.start();
                break;
            case MUNCH:
                if (munchmp == null) munchmp = MediaPlayer.create(this, R.raw.munch);
                munchmp.setVolume(1.0f, 1.0f);
                munchmp.start();
                break;
            case MUNCHING:
                if (munchingmp == null) munchingmp = MediaPlayer.create(this, R.raw.munching);
                munchingmp.setVolume(1.0f, 1.0f);
                munchingmp.start();
                break;
            case WOOF:
                if (woofmp == null) woofmp = MediaPlayer.create(this, R.raw.woof);
                woofmp.setVolume(1.0f, 1.0f);
                woofmp.start();
                break;
        }
    }
}