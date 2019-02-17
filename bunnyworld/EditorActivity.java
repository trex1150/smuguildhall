package edu.stanford.cs108.bunnyworld;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener{
    private int currPage;
    private int numPages;
    private int totalNumPagesEver;
    private Backend helper;
    private Page page;
    private String gameSave;

    boolean[] triggerItemsClicked;
    boolean[] actionItemsClicked;

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

    ArrayAdapter<String> pagesAdapter;
    private ArrayList<String> pageNames;
    private ArrayList<Integer> pages;
    Dialog dialog;
    ListView pagesListView;
    private int nPagesInDB;
    private boolean userIsInteracting;
    ArrayAdapter<String> shapeNamesAdapter;
    Dialog dialog_shapeNames;
    private ArrayList<String> shapeNames;
    ListView shapeNamesListView;
    private String onDropShapeName;
    private Shape clipboardShape;

    private static final ArrayList<String> BACKGROUND_STRINGS = new ArrayList<String>(){{
        add(NO_COLOR);
        add(YELLOW);
        add(GREEN);
        add(BLUE);
        add(PURPLE);
    }};

    //https://stackoverflow.com/questions/13397933/android-spinner-avoid-onitemselected-calls-during-initialization
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        gameSave = intent.getStringExtra("gameSave");
        System.out.println("gameSave in Editor is " + gameSave);

        helper = Backend.getInstance(this, gameSave, true);
        nPagesInDB = helper.getNumPages();
        triggerItemsClicked = new boolean[] {false, false, false};
        actionItemsClicked = new boolean[] {false, false, false, false};
        RadioButton shapeRB = (RadioButton) findViewById(R.id.shapeRB);
        shapeRB.setChecked(true);
        showShapeButtons();
        hidePageButtons();
        init();

        final RadioButton addButton = (RadioButton) findViewById(R.id.addToPage);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.selectedShape = null;
                page.clearInputs();
            }
        });

        final Spinner backgroundColorSpinner = (Spinner) findViewById(R.id.backgroundColor);
        ArrayAdapter<CharSequence> adapter_background = ArrayAdapter.createFromResource(this, R.array.background_drop_down_menu, R.layout.support_simple_spinner_dropdown_item);
        adapter_background.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        backgroundColorSpinner.setAdapter(adapter_background);
        backgroundColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(userIsInteracting) {
                    String backgroundSpinnerSelection = (String) adapterView.getItemAtPosition(i);
                    if(backgroundColorSpinner.equals(NO_COLOR))return;
                    page.setPageColor(backgroundSpinnerSelection);
                    LinearLayout editorLayout = (LinearLayout) findViewById(R.id.editor_layout);
                    editorLayout.setBackgroundResource(getColor(backgroundSpinnerSelection));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String backgroundSpinnerSelection = null;
                page.setPageColor(backgroundSpinnerSelection);
            }
        });

        Spinner fontSpinner = (Spinner) findViewById(R.id.fontSpinner);
        ArrayAdapter<CharSequence> adapter_font = ArrayAdapter.createFromResource(this, R.array.font_drop_down_menu, R.layout.support_simple_spinner_dropdown_item);
        adapter_font.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        fontSpinner.setAdapter(adapter_font);
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String fontSpinnerSelection = (String) adapterView.getItemAtPosition(i);
                page.setFontSpinnerSelection(fontSpinnerSelection);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String fontSpinnerSelection = null;
                page.setFontSpinnerSelection(fontSpinnerSelection);
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.imageNamesSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.img_drop_down_menu, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String spinnerSelection = (String) adapterView.getItemAtPosition(i);
                page.setSpinnerSelection(spinnerSelection);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                String spinnerSelection = null;
                page.setSpinnerSelection(spinnerSelection);
            }
        });
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

    private String getShapeNamePageNumString(String shapeName, String pageName) {
        return shapeName + "(" + pageName + ")";
    }

    public void getShapesFromDB() {
        Cursor cr = helper.getAllShapes();
        ArrayList<String> newShapeNames = new ArrayList<String>();
        if (cr.moveToFirst()) {
            do {
                String name = cr.getString(cr.getColumnIndex("name"));
                String pageName = cr.getString(cr.getColumnIndex("pageName"));
                int pgNum = cr.getInt(cr.getColumnIndex("page"));
                String namePlusPgNum = getShapeNamePageNumString(name, pageName);
                newShapeNames.add(namePlusPgNum);
            } while (cr.moveToNext());
            cr.close();
        }
        for (String s: newShapeNames) {
            if (!shapeNames.contains(s)) shapeNames.add(s);
        }
        shapeNamesAdapter.notifyDataSetChanged();
    }

    public void init() {
        page = (Page) findViewById(R.id.page0);
        onDropShapeName = "";
        currPage = 1;                                                                                  // old currPage = 0
        numPages = 1;                                                                                   // old numPages = 1

        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setText(Integer.toString(currPage));
        pages = new ArrayList<Integer>();
        pageNames = new ArrayList<String>();
        if (nPagesInDB == 0) {
            pages.add(1);
            pageNames.add("page1");
        } else {
            checkPageNames();
        }
        dialog = new Dialog(EditorActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Pages");

        pagesAdapter = new ArrayAdapter<String>(EditorActivity.this, android.R.layout.simple_list_item_1, pageNames);
        pagesListView = (ListView) dialog.findViewById(R.id.pagesList);
        pagesListView.setAdapter(pagesAdapter);
        pagesAdapter.notifyDataSetChanged();

        shapeNames = new ArrayList<String>();
        dialog_shapeNames = new Dialog(EditorActivity.this);
        dialog_shapeNames.setContentView(R.layout.shapenames_dialog);
        dialog_shapeNames.setTitle("Shape Names");

        shapeNamesAdapter = new ArrayAdapter<String>(EditorActivity.this, android.R.layout.simple_list_item_1, shapeNames);
        shapeNamesListView = (ListView) dialog_shapeNames.findViewById(R.id.shapeNamesList);
        shapeNamesListView.setAdapter(shapeNamesAdapter);


        getPageFromDB(currPage);
    }

    private void saveCurrentData() {
        CheckBox possessionsCheck = (CheckBox) findViewById(R.id.possessionsCheckbox);
        boolean isChecked = possessionsCheck.isChecked();
        int temp = currPage;
        if (isChecked) currPage = 0;
        page.setPageNum(currPage);
        helper.clearPage(currPage);
        sendShapesToBackend(); //hi
        page.setPageNum(currPage);
        if (isChecked) currPage = temp;
    }

    @Override
    public void onStop() {
        saveCurrentData();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //ok
        saveCurrentData();
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        saveCurrentData();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        saveCurrentData();
        super.onDestroy();
    }

    /*important that we are creating a new instance of backend
    when we resume the editor activity so that backend's constructor
    is called
     */
    @Override
    public void onResume() {
        helper = Backend.getInstance(this, gameSave, true);
        super.onResume();
    }

    public void showShapeButtons(){
        EditText shapeNameButton = (EditText) findViewById(R.id.name);
        shapeNameButton.setVisibility(View.VISIBLE);

        RadioGroup rg = (RadioGroup) findViewById(R.id.selectOrAdd);
        rg.setVisibility(View.VISIBLE);

        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setVisibility(View.VISIBLE);

        TextView imageTxt = (TextView) findViewById(R.id.imageTitle);
        imageTxt.setVisibility(View.VISIBLE);

        Spinner spin = (Spinner) findViewById(R.id.imageNamesSpinner);
        spin.setVisibility(View.VISIBLE);

        TextView fontTxt = (TextView) findViewById(R.id.changeTextFont);
        fontTxt.setVisibility(View.VISIBLE);

        Spinner fontspin = (Spinner) findViewById(R.id.fontSpinner);
        fontspin.setVisibility(View.VISIBLE);

        EditText shapeText = (EditText) findViewById(R.id.shapeText);
        shapeText.setVisibility(View.VISIBLE);

        CheckBox hiddenChk = (CheckBox) findViewById(R.id.hidden);
        hiddenChk.setVisibility(View.VISIBLE);

        CheckBox movableChk = (CheckBox) findViewById(R.id.movable);
        movableChk.setVisibility(View.VISIBLE);

        Button addScripts = (Button) findViewById(R.id.addScripts);
        addScripts.setVisibility(View.VISIBLE);

        Button deleteShape = (Button) findViewById(R.id.deleteButton);
        deleteShape.setVisibility(View.VISIBLE);

        Button updateShape = (Button) findViewById(R.id.updateButton);
        updateShape.setVisibility(View.VISIBLE);

        EditText x1 = (EditText) findViewById(R.id.x1);
        x1.setVisibility(View.VISIBLE);

        TextView x1text = (TextView) findViewById(R.id.textx1);
        x1text.setVisibility(View.VISIBLE);

        EditText y1 = (EditText) findViewById(R.id.y1);
        y1.setVisibility(View.VISIBLE);

        TextView y1text = (TextView) findViewById(R.id.texty1);
        y1text.setVisibility(View.VISIBLE);

        EditText x2 = (EditText) findViewById(R.id.x2);
        x2.setVisibility(View.VISIBLE);

        EditText y2 = (EditText) findViewById(R.id.y2);
        y2.setVisibility(View.VISIBLE);

        TextView x2text = (TextView) findViewById(R.id.textx2);
        x2text.setVisibility(View.VISIBLE);

        TextView y2text = (TextView) findViewById(R.id.texty2);
        y2text.setVisibility(View.VISIBLE);

        EditText onClickEditText = (EditText) findViewById(R.id.onClickEditText);
        onClickEditText.setVisibility(View.VISIBLE);

        EditText onEnterEditText = (EditText) findViewById(R.id.onEnterEditText);
        onEnterEditText.setVisibility(View.VISIBLE);

        EditText onDropEditText = (EditText) findViewById(R.id.onDropEditText);
        onDropEditText.setVisibility(View.VISIBLE);

        Button updateScripts = (Button) findViewById(R.id.updateScripts);
        updateScripts.setVisibility(View.VISIBLE);

        Button copyB = (Button) findViewById(R.id.copy);
        copyB.setVisibility(View.VISIBLE);

        Button cutB = (Button) findViewById(R.id.cut);
        cutB.setVisibility(View.VISIBLE);

        Button pasteB = (Button) findViewById(R.id.paste);
        pasteB.setVisibility(View.VISIBLE);

        Button undo = (Button) findViewById(R.id.undoButton);
        undo.setVisibility(View.VISIBLE);


    }

    public void hideShapeButtons(){
        EditText shapeNameButton = (EditText) findViewById(R.id.name);
        shapeNameButton.setVisibility(View.GONE);

        RadioGroup rg = (RadioGroup) findViewById(R.id.selectOrAdd);
        rg.setVisibility(View.GONE);

        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setVisibility(View.GONE);

        TextView imageTxt = (TextView) findViewById(R.id.imageTitle);
        imageTxt.setVisibility(View.GONE);

        Spinner spin = (Spinner) findViewById(R.id.imageNamesSpinner);
        spin.setVisibility(View.GONE);

        TextView fontTxt = (TextView) findViewById(R.id.changeTextFont);
        fontTxt.setVisibility(View.GONE);

        Spinner fontspin = (Spinner) findViewById(R.id.fontSpinner);
        fontspin.setVisibility(View.GONE);

        EditText shapeText = (EditText) findViewById(R.id.shapeText);
        shapeText.setVisibility(View.GONE);

        CheckBox hiddenChk = (CheckBox) findViewById(R.id.hidden);
        hiddenChk.setVisibility(View.GONE);

        CheckBox movableChk = (CheckBox) findViewById(R.id.movable);
        movableChk.setVisibility(View.GONE);

        Button addScripts = (Button) findViewById(R.id.addScripts);
        addScripts.setVisibility(View.GONE);

        Button deleteShape = (Button) findViewById(R.id.deleteButton);
        deleteShape.setVisibility(View.GONE);

        Button updateShape = (Button) findViewById(R.id.updateButton);
        updateShape.setVisibility(View.GONE);

        EditText x1 = (EditText) findViewById(R.id.x1);
        x1.setVisibility(View.GONE);

        EditText y1 = (EditText) findViewById(R.id.y1);
        y1.setVisibility(View.GONE);

        TextView x1text = (TextView) findViewById(R.id.textx1);
        x1text.setVisibility(View.GONE);

        TextView y1text = (TextView) findViewById(R.id.texty1);
        y1text.setVisibility(View.GONE);

        EditText x2 = (EditText) findViewById(R.id.x2);
        x2.setVisibility(View.GONE);

        EditText y2 = (EditText) findViewById(R.id.y2);
        y2.setVisibility(View.GONE);

        TextView x2text = (TextView) findViewById(R.id.textx2);
        x2text.setVisibility(View.GONE);

        TextView y2text = (TextView) findViewById(R.id.texty2);
        y2text.setVisibility(View.GONE);


        EditText onClickEditText = (EditText) findViewById(R.id.onClickEditText);
        onClickEditText.setVisibility(View.GONE);

        EditText onEnterEditText = (EditText) findViewById(R.id.onEnterEditText);
        onEnterEditText.setVisibility(View.GONE);

        EditText onDropEditText = (EditText) findViewById(R.id.onDropEditText);
        onDropEditText.setVisibility(View.GONE);

        Button updateScripts = (Button) findViewById(R.id.updateScripts);
        updateScripts.setVisibility(View.GONE);

        /*CRYSTAL THURSDAY NIGHT*/
        Button copyB = (Button) findViewById(R.id.copy);
        copyB.setVisibility(View.GONE);

        Button cutB = (Button) findViewById(R.id.cut);
        cutB.setVisibility(View.GONE);

        Button pasteB = (Button) findViewById(R.id.paste);
        pasteB.setVisibility(View.GONE);

        Button undo = (Button) findViewById(R.id.undoButton);
        undo.setVisibility(View.GONE);
    }

    public void showPageButtons(){
        Button prevButton = (Button) findViewById(R.id.prevPageButton);
        prevButton.setVisibility(View.VISIBLE);

        Button nextButton = (Button) findViewById(R.id.nextPageButton);
        nextButton.setVisibility(View.VISIBLE);

        Button clearButton = (Button) findViewById(R.id.clearPage);
        clearButton.setVisibility(View.VISIBLE);

        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setVisibility(View.VISIBLE);

        Button updatePageNameButton = (Button) findViewById(R.id.updatePageNameButton);
        updatePageNameButton.setVisibility(View.VISIBLE);

        Button deletePageButton = (Button) findViewById(R.id.deletePageButton);
        deletePageButton.setVisibility(View.VISIBLE);

        EditText pgName = (EditText) findViewById(R.id.pgName);
        pgName.setVisibility(View.VISIBLE);

        TextView backgroundColorText = (TextView) findViewById(R.id.bckgrndColorText);
        backgroundColorText.setVisibility(View.VISIBLE);

        Spinner backgroundColorSpin = (Spinner) findViewById(R.id.backgroundColor);
        backgroundColorSpin.setVisibility(View.VISIBLE);
    }

    private void hidePageButtons(){
        Button prevButton = (Button) findViewById(R.id.prevPageButton);
        prevButton.setVisibility(View.GONE);

        Button nextButton = (Button) findViewById(R.id.nextPageButton);
        nextButton.setVisibility(View.GONE);

        Button clearButton = (Button) findViewById(R.id.clearPage);
        clearButton.setVisibility(View.GONE);

        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setVisibility(View.GONE);

        Button updatePageNameButton = (Button) findViewById(R.id.updatePageNameButton);
        updatePageNameButton.setVisibility(View.GONE);

        Button deletePageButton = (Button) findViewById(R.id.deletePageButton);
        deletePageButton.setVisibility(View.GONE);

        EditText pgName = (EditText) findViewById(R.id.pgName);
        pgName.setVisibility(View.GONE);

        TextView backgroundColorText = (TextView) findViewById(R.id.bckgrndColorText);
        backgroundColorText.setVisibility(View.GONE);

        Spinner backgroundColorSpin = (Spinner) findViewById(R.id.backgroundColor);
        backgroundColorSpin.setVisibility(View.GONE);

    }

    public void showPageLayout(View view){
        TextView backgroundColorText = (TextView) findViewById(R.id.bckgrndColorText);
        backgroundColorText.setVisibility(View.VISIBLE);
        String color = page.getPageColor();
        page.selectedShape=null;
        showPageButtons();
        hideShapeButtons();
        LinearLayout editorLayout = (LinearLayout) findViewById(R.id.editor_layout);
        editorLayout.setBackgroundResource(getColor(page.getPageColor()));
        page.setPageColor(color);
    }

    public void showShapeLayout(View view){
        page.selectedShape=null;
        hidePageButtons();
        showShapeButtons();
    }

    public void showPossessions(View view){
        CheckBox possessionsCheck = (CheckBox) findViewById(R.id.possessionsCheckbox);
        boolean isChecked = possessionsCheck.isChecked();
        if(isChecked){
            /* going to possessions*/
            LinearLayout editorLayout = (LinearLayout) findViewById(R.id.editor_layout);
            editorLayout.setBackgroundResource(R.color.POSSESSIONS);
            RadioButton rb_shape = (RadioButton) findViewById(R.id.shapeRB);
            rb_shape.setChecked(true);
            /* show shape layout */
            showShapeLayout(view);
            TextView pageNumText = (TextView) findViewById(R.id.pageNum);
            pageNumText.setVisibility(View.GONE);
            RadioButton rb = (RadioButton) findViewById(R.id.pageRB);
            rb.setVisibility(View.GONE);
            page.setPageNum(currPage);
            sendShapesToBackend();
            wipeScreen();
            int oldCurrPage = currPage;
            currPage = 0;
            getPageFromDB(currPage);                           // possessions is page zero
            currPage = oldCurrPage;
            TextView currPageNum = (TextView) findViewById(R.id.pageNum);
            currPageNum.setText("Possessions");
            page.clearInputs();
            editorLayout.setBackgroundResource(R.color.POSSESSIONS);
        }else{
            /* going back to pages */
            LinearLayout editorLayout = (LinearLayout) findViewById(R.id.editor_layout);
            editorLayout.setBackgroundResource(getColor(page.getPageColor()));
            RadioButton rb = (RadioButton) findViewById(R.id.pageRB);
            rb.setVisibility(View.VISIBLE);
            rb.setChecked(true);
            showPageLayout(view);
            TextView pageNumText = (TextView) findViewById(R.id.pageNum);
            pageNumText.setVisibility(View.VISIBLE);
            /* going back to pages */
            page.setPageNum(0);
            int olderCurrPage = currPage;
            currPage = 0;
            sendShapesToBackend();
            currPage = olderCurrPage;
            page.setPageNum(currPage);
            wipeScreen();
            getPageFromDB(currPage);
            TextView currPageNum = (TextView) findViewById(R.id.pageNum);
            currPageNum.setText(Integer.toString(currPage));
            page.clearInputs();
            showPageLayout(view);
        }
    }

    public void updatePageName(View view) {
        if(pageNames.size() == 0){
            System.out.println("ERROR: PAGE NAME ARRAY IS ZERO!");
            return;
        }
        if (page.getNumShapes() == 0) {
            Toast.makeText(getApplicationContext(), "Add shapes before editing page name", Toast.LENGTH_SHORT).show();
        } else {
            String oldPageName = page.getPageName();
            EditText pageNameView = (EditText) findViewById(R.id.pgName);
            String newName = pageNameView.getText().toString();
            page.setPageName(newName);
            sendShapesToBackend();
            for(Shape s:page.shapes){
                shapeNames.remove(getShapeNamePageNumString(s.getShapeName(),oldPageName));
            }
            page.shapes.clear();
            helper.updatePageName(currPage, newName);
            getPageFromDB(currPage);
            System.out.println("CURRPAGE: " + currPage);
            pageNames.add(currPage - 1, newName);
            pageNames.remove(currPage - 1);
            Toast.makeText(getApplicationContext(), "Page name set to " + newName, Toast.LENGTH_SHORT).show();
        }
    }

    public void deletePage(View view){
        if (currPage == 0) {
            Toast.makeText(getApplicationContext(), "Cannot delete possessions", Toast.LENGTH_SHORT).show();
        } else if (currPage == 1) {
            Toast.makeText(getApplicationContext(), "Cannot delete starter page", Toast.LENGTH_SHORT).show();
        } else {
            //clears current page
            wipeScreen();
            helper.clearPage(currPage);
            page.clearInputs();

            helper.deletePage(currPage, numPages);
            shapeNames.clear();
            getShapesFromDB();

            for (int i = 0; i < numPages; i++) {
                if (i < currPage - 1) continue;
                else if (i == currPage - 1) pageNames.remove(i);
            }
            pagesAdapter.notifyDataSetChanged();

            //reset pages array
            pages.clear();
            numPages--;
            for(int i = 0; i < numPages; i++){
                pages.add(i + 1);
            }

            //goes to previous page without saving curr to database
            currPage--;
            page.setSelectedShapeNull();
            wipeScreen();
            page.setPageNum(currPage);

            getPageFromDB(currPage);
            TextView currPageNum = (TextView) findViewById(R.id.pageNum);
            currPageNum.setText(Integer.toString(currPage));
            page.clearInputs();
        }
    }

    private void resetTriggerArray() {
        for (int i = 0; i < triggerItemsClicked.length; i++) {
            triggerItemsClicked[i] = false;
        }
    }

    public void getThisPagesShapes(){
        boolean addedShape = false;
        for(Shape s:page.shapes) {
            String shapeName = s.getShapeName();
            String namePlusPgNum = getShapeNamePageNumString(shapeName, page.getPageName());
            if (!shapeNames.contains(namePlusPgNum)) {
                shapeNames.add(namePlusPgNum);
                addedShape = true;
            }
        }
        if(addedShape) shapeNamesAdapter.notifyDataSetChanged();
    }

    /* CRYSTAL USES RECURSION BIIIIITCH FRIDAY NIGHT*/
    public boolean checkOnClick(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(ON_CLICK)) {
                if (tokenizer.hasMoreTokens()) {
                    String actionToken = tokenizer.nextToken();
                    if (actionToken.equals(GO_TO)) {
                        if(tokenizer.hasMoreTokens()){
                            String page = tokenizer.nextToken();
                            //check if page exists
                            if(pageNames.contains(page)) return checkOnClick(tokenizer);
                        }
                    } else if (actionToken.equals(PLAY_SOUND)) {
                        if(tokenizer.hasMoreTokens()){
                            String sound = tokenizer.nextToken();
                            //check if sound exists
                            if(sound.equals(EVIL_LAUGH) || sound.equals(CARROT) || sound.equals(FIRE) || sound.equals(HOORAY)
                                    || sound.equals(MUNCH) || sound.equals(MUNCHING) || sound.equals(WOOF)) return checkOnClick(tokenizer);
                        }
                    } else if (actionToken.equals(HIDE_SHAPE)) {
                        if(tokenizer.hasMoreTokens()){
                            String shape = tokenizer.nextToken();
                            //check if shape exists
                            if(shapeNames.contains(shape)) return checkOnClick(tokenizer);
                        }
                    } else if (actionToken.equals(SHOW_SHAPE)) {
                        if(tokenizer.hasMoreTokens()){
                            String shape = tokenizer.nextToken();
                            if(shapeNames.contains(shape)) return checkOnClick(tokenizer);
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
            return false;
        }
        return true;
    }

    /* CRYSTAL USES RECURSION BIIIIITCH FRIDAY NIGHT*/
    public boolean checkOnEnter(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(ON_ENTER)) {
                if (tokenizer.hasMoreTokens()) {
                    String actionToken = tokenizer.nextToken();
                    if (actionToken.equals(GO_TO)) {
                        if(tokenizer.hasMoreTokens()){
                            String page = tokenizer.nextToken();
                            //check if page exists
                            if(pageNames.contains(page)) return checkOnEnter(tokenizer);
                        }
                    } else if (actionToken.equals(PLAY_SOUND)) {
                        if(tokenizer.hasMoreTokens()){
                            String sound = tokenizer.nextToken();
                            //check if sound exists
                            if(sound.equals(EVIL_LAUGH) || sound.equals(CARROT) || sound.equals(FIRE) || sound.equals(HOORAY)
                                    || sound.equals(MUNCH) || sound.equals(MUNCHING) || sound.equals(WOOF)) return checkOnEnter(tokenizer);
                        }
                    } else if (actionToken.equals(HIDE_SHAPE)) {
                        if(tokenizer.hasMoreTokens()){
                            String shape = tokenizer.nextToken();
                            //check if shape exists
                            if(shapeNames.contains(shape)) return checkOnEnter(tokenizer);
                        }
                    } else if (actionToken.equals(SHOW_SHAPE)) {
                        if(tokenizer.hasMoreTokens()){
                            String shape = tokenizer.nextToken();
                            if(shapeNames.contains(shape)) return checkOnEnter(tokenizer);
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
            return false;
        }
        return true;
    }

    /* CRYSTAL USES RECURSION BIIIIITCH FRIDAY NIGHT*/
    public boolean checkOnDrop(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(ON_DROP)) {
                if (tokenizer.hasMoreTokens()) {
                    String dropShape = tokenizer.nextToken();
                    //if dropShape name equals current name
                    if(dropShape.equals(page.selectedShape.getShapeName())) {
                        Toast.makeText(getApplicationContext(), "Drop shape cannot equal selected shape", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (shapeNames.contains(dropShape)) {
                        if (tokenizer.hasMoreTokens()) {
                            String actionToken = tokenizer.nextToken();
                            if (actionToken.equals(GO_TO)) {
                                if (tokenizer.hasMoreTokens()) {
                                    String page = tokenizer.nextToken();
                                    //check if page exists
                                    if (pageNames.contains(page)) return checkOnDrop(tokenizer);
                                }
                            } else if (actionToken.equals(PLAY_SOUND)) {
                                if (tokenizer.hasMoreTokens()) {
                                    String sound = tokenizer.nextToken();
                                    //check if sound exists
                                    if (sound.equals(EVIL_LAUGH) || sound.equals(CARROT) || sound.equals(FIRE) || sound.equals(HOORAY)
                                            || sound.equals(MUNCH) || sound.equals(MUNCHING) || sound.equals(WOOF)) return checkOnDrop(tokenizer);
                                }
                            } else if (actionToken.equals(HIDE_SHAPE)) {
                                if (tokenizer.hasMoreTokens()) {
                                    String shape = tokenizer.nextToken();
                                    //check if shape exists
                                    if (shapeNames.contains(shape)) return checkOnDrop(tokenizer);
                                }
                            } else if (actionToken.equals(SHOW_SHAPE)) {
                                if (tokenizer.hasMoreTokens()) {
                                    String shape = tokenizer.nextToken();
                                    if (shapeNames.contains(shape)) return checkOnDrop(tokenizer);
                                }
                            } else {
                                return false;
                            }
                        }
                        return false;
                    }
                    return false;
                }
                return false;
            }
            return false;
        }
        return true;
    }

    public void updateScripts(View view) {
        if (page.selectedShape == null) {
            Toast.makeText(getApplicationContext(), "Please select a shape first.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText onClickText = (EditText) findViewById(R.id.onClickEditText);
        if(!onClickText.getText().toString().equals("")){
            StringTokenizer onClickTokenizer = new StringTokenizer(onClickText.getText().toString().trim(), " ");
            if(!checkOnClick(onClickTokenizer)){
                Toast.makeText(getApplicationContext(), "Incorrect input for OnClick", Toast.LENGTH_SHORT).show();
                onClickText.setText("");
                page.selectedShape.setOnClickAction(" ");
            } else {
                Toast.makeText(getApplicationContext(), "OnClick update successful", Toast.LENGTH_SHORT).show();
                page.selectedShape.setOnClickAction(onClickText.getText().toString());
            }
        }

        EditText onEnterText = (EditText) findViewById(R.id.onEnterEditText);
        if(!onEnterText.getText().toString().equals("")){
            StringTokenizer onEnterTokenizer = new StringTokenizer(onEnterText.getText().toString().trim(), " ");
            if(!checkOnEnter(onEnterTokenizer)){
                Toast.makeText(getApplicationContext(), "Incorrect input for OnEnter", Toast.LENGTH_SHORT).show();
                onEnterText.setText("");
                page.selectedShape.setOnEnterAction(" ");
            } else {
                Toast.makeText(getApplicationContext(), "OnEnter update successful", Toast.LENGTH_SHORT).show();
                page.selectedShape.setOnEnterAction(onEnterText.getText().toString());
            }
        }

        EditText onDropText = (EditText) findViewById(R.id.onDropEditText);
        if(!onDropText.getText().toString().equals("")){
            StringTokenizer onDropTokenizer = new StringTokenizer(onDropText.getText().toString().trim(), " ");
            if(!checkOnDrop(onDropTokenizer)){
                Toast.makeText(getApplicationContext(), "Incorrect input for OnDrop", Toast.LENGTH_SHORT).show();
                onDropText.setText("");
                page.selectedShape.setOnDropAction(" ");
            } else {
                Toast.makeText(getApplicationContext(), "OnDrop update successful", Toast.LENGTH_SHORT).show();
                page.selectedShape.setOnDropAction(onDropText.getText().toString());
            }
        }


    }


    public void addScripts(View view){
        if (page.selectedShape == null) {
            Toast.makeText(getApplicationContext(), "Please select a shape first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Button addScriptsButton = (Button) findViewById(R.id.addScripts);
        final PopupMenu popup = new PopupMenu(EditorActivity.this, addScriptsButton);
        final PopupMenu actionsPopup = new PopupMenu(EditorActivity.this, addScriptsButton);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.scripts_pop_up, popup.getMenu());
        actionsPopup.getMenuInflater().inflate(R.menu.actions_pop_up, actionsPopup.getMenu());
        //registering popup with OnMenuItemClickListener
        onDropShapeName = "";
        getShapesFromDB();
        getThisPagesShapes();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(EditorActivity.this, "You Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                resetTriggerArray();                          /// SHELBY's newer version
                String menuItem = (String) item.getTitle();
                switch (menuItem) {
                    case ON_CLICK:
                        triggerItemsClicked[0] = true;
                        actionsPopup.show();
                        break;
                    case ON_ENTER:
                        triggerItemsClicked[1] = true;
                        actionsPopup.show();
                        break;
                    case ON_DROP:
                        triggerItemsClicked[2] = true;
                        pagePopup("", actionsPopup);
                        dialog_shapeNames.show();       // initialize onDropShapeName
                        break;
                }
                return true;
            }
        });

        actionsPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String menuItem = (String) item.getTitle();
                Toast.makeText(getApplicationContext(), "you clicked " + menuItem, Toast.LENGTH_SHORT).show();
                String actionItem = "";
                boolean onDropClicked = triggerItemsClicked[2];
                switch(menuItem) {
                    case CARROT:
                        break;
                    case EVIL_LAUGH:
                        actionItem = EVIL_LAUGH;
                        break;
                    case FIRE:
                        break;
                    case HOORAY:
                        break;
                    case MUNCH:
                        break;
                    case MUNCHING:
                        break;
                    case WOOF:
                        break;
                    case "Play sound...":
                        return false;
                    case "Hide shape":
                        pagePopup(HIDE_SHAPE, actionsPopup);
                        dialog_shapeNames.show();
                        return false;
                    case "Show shape":
                        pagePopup(SHOW_SHAPE, actionsPopup);
                        dialog_shapeNames.show();
                        return false;
                    case "Go to page...":
                        pagePopup(GO_TO,actionsPopup);
                        dialog.show();
                        return false;
                }
                // for sounds ONLY
                if(actionItem.equals("")) actionItem = (String) item.getTitle();
                setShapeAction(PLAY_SOUND, actionItem);
                return false;
            }
        });
        popup.show();
    }

    private int getTriggerIndex(){
        for(int i = 0; i < triggerItemsClicked.length; i++){
            if(triggerItemsClicked[i]) return i;
        }
        return -1;
    }

    private void setShapeAction(String actionItem, String object){
        int i = getTriggerIndex();
        page.selectedShape.setScriptAction(actionItem.trim(), object.trim(), i, onDropShapeName);
        resetTriggerArray();
        page.displayShapeInfo();
    }

    int pageClicked;
    private void pagePopup(final String action, final PopupMenu actionsPopup) {
        pagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                String pageName = (String) pagesListView.getItemAtPosition(i);
                setShapeAction(GO_TO, pageName);
            }
        });

        shapeNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                dialog_shapeNames.dismiss();
                System.out.println(action);
                String shapeName = (String) shapeNamesListView.getItemAtPosition(i);
                if(action!=""){
                    // hideShape/showShape clicked most recently
                    setShapeAction(action, shapeName);
                }else{
                    // onDrop clicked most recently
                    onDropShapeName = shapeName;
                    actionsPopup.show();
                }
            }
        });
    }

    public void sendShapesToBackend(){
        helper.clearPage(currPage);
        sendColorOfPageToDB();                      // ADDING FOR COLORS
        if (page.getNumShapes() != 0) {
            String dataStr = page.generateInsertString(gameSave, true);
            helper.addPage(dataStr);
        }
    }

    public void wipeScreen(){
        page.shapes.clear();
        page.invalidate();
        page.resetNumShapes();
        /* CRYSTAL THURSDAY NIGHT */
        page.resetShapeNames();
        EditText pageNameView = (EditText) findViewById(R.id.pgName);
        pageNameView.setText("");
    }

    public void  clearPage(View view){
        wipeScreen();
        helper.clearPage(currPage);
        page.clearInputs();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                break;
            default:
                break;
        }
    }

    public void sendColorOfPageToDB(){
        String query = page.generatePageColorInsertString(gameSave);
        helper.addPageColor(query);
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
                System.out.println("GOT: pagecolor: " + pageColor + "for page: " + pagenum);
            } while (cr.moveToNext());
            cr.close();
        }
        System.out.println("COLOR CHANGED TO: " + pageColor + "searched for PAGENUM: " + pagenum);
        LinearLayout editorLayout = (LinearLayout) findViewById(R.id.editor_layout);
        editorLayout.setBackgroundResource(getColor(pageColor));
        page.setPageColor(pageColor);
        /* reflect selection of background color spinner for this shape */
        Spinner backgroundSpinner = (Spinner) findViewById(R.id.backgroundColor);
        backgroundSpinner.setSelection(BACKGROUND_STRINGS.indexOf(pageColor));
        System.out.println("numpages in db: " + helper.numPageColorsInDB());
    }

    public void checkPageNames() {
        pageNames.clear();
        Cursor cr = helper.getAllPages();
        if (cr.moveToFirst()) {
            do {
                // for each shape "page INTEGER, name STRING, bounds STRING, image STRING, hidden INTEGER, movable INTEGER,"
                int pgNum = cr.getInt(cr.getColumnIndex("page"));
                String pageName = cr.getString(cr.getColumnIndex("pageName"));
                String color = cr.getString(cr.getColumnIndex("color"));
                pageNames.add(pageName);
            } while (cr.moveToNext());
            cr.close();
        }
    }

    public void getPageFromDB(int pageNum) {
        checkPageNames();
        getColorForPageInDB(pageNum);
        Cursor cr = helper.lookupPage(pageNum);
        String pageName = "";
        int pgNum = -1;
        System.out.println("getting page from DB");
        EditText currPageName = (EditText) findViewById(R.id.pgName);
        if (cr.moveToFirst()) {
            do {
                // for each shape "page INTEGER, name STRING, bounds STRING, image STRING, hidden INTEGER, movable INTEGER,"
                pgNum = cr.getInt(cr.getColumnIndex("page"));
                pageName = cr.getString(cr.getColumnIndex("pageName"));
                String name = cr.getString(cr.getColumnIndex("name"));
                String bounds = cr.getString(cr.getColumnIndex("bounds"));
                String image = cr.getString(cr.getColumnIndex("image"));
                String text = cr.getString(cr.getColumnIndex("text"));
                int hidden = cr.getInt(cr.getColumnIndex("hidden"));
                int movable = cr.getInt(cr.getColumnIndex("movable"));
                String script = cr.getString(cr.getColumnIndex("script"));
                String font = cr.getString(cr.getColumnIndex("font"));
                System.out.println("got this page in DB, with page name" + pageName);
                page.remakeShape(pgNum,name,bounds,image, text, hidden,movable,script,font);
                page.incrNumShapes();
            } while (cr.moveToNext());
            cr.close();
        }
        if(pgNum == -1) pgNum = pageNum;
        if(!pageName.equals("")){
            currPageName.setText(pageName);
            page.setPageName(pageName);
        } else{
            currPageName.setText("page" + pgNum);
            page.setPageName("page" + pgNum);
        }
        helper.clearPage(pageNum);
        page.invalidate();
    }

    public void goToPrevious(View view) {
        if (currPage == 1) return;
        sendShapesToBackend();
        currPage--;
        page.setSelectedShapeNull();
        wipeScreen();
        page.setPageNum(currPage);
        getPageFromDB(currPage);
        TextView currPageNum = (TextView) findViewById(R.id.pageNum);
        currPageNum.setText(Integer.toString(currPage));
        page.clearInputs();
    }

    public void goToNext(View view) {
        page.setSelectedShapeNull();
        if (currPage == numPages) {                     // old: currPage == numPages - 1
            //save the old stuff
            sendShapesToBackend();
            wipeScreen();
            //create new page
            numPages++;
            totalNumPagesEver++;
            currPage++;
            if (!pages.contains(numPages)){
                pages.add(numPages);
                String pageName = "page" + totalNumPagesEver;
                pageNames.add(pageName);
                page.setPageName(pageName);
            }
            pagesAdapter.notifyDataSetChanged();
            page.setPageNum(currPage);
            TextView currPageNum = (TextView) findViewById(R.id.pageNum);
            currPageNum.setText(Integer.toString(currPage));
            getPageFromDB(currPage);
        } else {
            sendShapesToBackend();
            currPage++;
            wipeScreen();
            page.setPageNum(currPage);
            getPageFromDB(currPage);
            TextView currPageNum = (TextView) findViewById(R.id.pageNum);
            currPageNum.setText(Integer.toString(currPage));
        }
        page.clearInputs();
    }

    public void updateShape(View view) {
        if(page.selectedShape == null) return;
        String shapeName = page.selectedShape.getShapeName();
        String namePlusPgNum = getShapeNamePageNumString(shapeName, page.getPageName());
        shapeNames.remove(namePlusPgNum);
        shapeNamesAdapter.notifyDataSetChanged();
        page.updateShape();
    }


    /*added by CRYSTAL wed night*/
    public void undoUpdate(View view){
        int undoInt = page.undoShapeUpdate();
        if(undoInt == 0){
            Toast.makeText(getApplicationContext(), "No changes to undo", Toast.LENGTH_SHORT).show();
        } else if(undoInt == 1){
            Toast.makeText(getApplicationContext(), "Undo successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Select most recently changed shape", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteShape(View view){
        if(page.selectedShape == null) return;
        String shapeName = page.selectedShape.getShapeName();
        String namePlusPgNum = getShapeNamePageNumString(shapeName, page.getPageName());
        shapeNames.remove(namePlusPgNum);
        shapeNamesAdapter.notifyDataSetChanged();
        page.deleteShape();
    }

    private Shape selectedShapeDeepCopy() {
        Shape s = new Shape(page.selectedShape.getCoordinates().x, page.selectedShape.getCoordinates().y,
                page.selectedShape.getWidth(), page.selectedShape.getHeight(),
                page.selectedShape.getShapeName() + "Copy", page.selectedShape.getImageName(), page.selectedShape.getTextField(),
                page.selectedShape.isHidden(), page.selectedShape.isMovable(), page.selectedShape.getFont());

        s.setOnClickAction(page.selectedShape.getOnClick());
        s.setOnEnterAction(page.selectedShape.getOnEnter());
        s.setOnDropAction(page.selectedShape.getOnDrop());
        return s;
    }

    public void copyShape(View view){
        //save shape
        if(page.selectedShape == null) {
            Toast.makeText(getApplicationContext(), "Select a shape before copying", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboardShape = selectedShapeDeepCopy();
        String clipboardShapeName = clipboardShape.getShapeName();
        Toast.makeText(getApplicationContext(), "Shape " + page.selectedShape.getShapeName() + " copied", Toast.LENGTH_SHORT).show();
    }

    public void cutShape(View view){
        //save shape and delete off page
        if(page.selectedShape == null) return;

        clipboardShape = selectedShapeDeepCopy();
        String clipboardShapeName = clipboardShape.getShapeName();
        Toast.makeText(getApplicationContext(), "Shape " + page.selectedShape.getShapeName() + " cut", Toast.LENGTH_SHORT).show();
        deleteShape(view);
    }

    public void pasteShape(View view){
        if(clipboardShape == null){
            Toast.makeText(getApplicationContext(), "No shape copied to clipboard", Toast.LENGTH_SHORT).show();
            return;
        }
        page.pasteShape(clipboardShape);
        String clipboardShapeName = clipboardShape.getShapeName().substring(0, clipboardShape.getShapeName().length() - 4);
        Toast.makeText(getApplicationContext(), "Shape " + clipboardShapeName + " pasted", Toast.LENGTH_SHORT).show();
    }
}