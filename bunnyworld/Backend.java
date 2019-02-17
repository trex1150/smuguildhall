package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by shelbymarcus on 2/28/18.
 */

class Backend{
    private static SQLiteDatabase pgcolorsDB; // instance variable on Activity
    private static SQLiteDatabase pageDB; // instance variable on Activity
    private static SQLiteDatabase gameSavesDB; // instance variable used for Game Save strings from main

    private static Backend anInstance = null;
    private static Backend anInstance_gs = null;

    private static String tableName;
    boolean editor;

    public static Backend getInstance(Context context, String gameSave, boolean editor){
        anInstance = new Backend(context.getApplicationContext(), gameSave, editor);
        return anInstance;
    }

    public static Backend getInstance(Context context){
        anInstance_gs = new Backend(context.getApplicationContext());
        return anInstance_gs;
    }

    /* constructor for gameSaves names */
    private Backend(Context ctx){
        gameSavesDB = ctx.openOrCreateDatabase("gameSavesInit",Context.MODE_PRIVATE,null);
        //Cursor c = getAllGameNamesSaved();
        tableName = null;           // make sure don't attempt functionality of colorDB or pageDB
        Cursor cursorGames = gameSavesDB.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name='GameSaves';", null);
        if(cursorGames.getCount() == 0) initializGameSavesDatabase();
    }

    private static void initializGameSavesDatabase(){
        String setupStr = "CREATE TABLE GameSaves("
                + "gameTitle STRING,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        gameSavesDB.execSQL(setupStr);
    }

    public void addNewGame(String gameSaveToAdd){
        gameSavesDB.execSQL("INSERT INTO GameSaves VALUES('" + gameSaveToAdd.replaceAll(" ", "_") + "',NULL);");
    }

    public Cursor getAllGameNamesSaved(){
        Cursor c = gameSavesDB.rawQuery("SELECT * FROM GameSaves;", null);
        return c;
    }

    public void deleteGame(String gameToDelete){
        String query = "DELETE FROM GameSaves WHERE gameTitle = '" + gameToDelete.replaceAll(" ", "_") + "';";
        gameSavesDB.execSQL(query);
        pageDB.execSQL("DROP TABLE IF EXISTS '" + tableName + "PageTable'");
        pgcolorsDB.execSQL("DROP TABLE IF EXISTS '" + tableName + "PageColorTable'");
    }

    private Backend(Context ctx, String usersInputGameName, boolean editor) {
        pgcolorsDB = ctx.openOrCreateDatabase("pagesCInit",Context.MODE_PRIVATE,null);
        pageDB = ctx.openOrCreateDatabase("pagesInit",Context.MODE_PRIVATE,null);
        this.tableName = usersInputGameName.replaceAll(" ", "_");
        this.editor = editor;
        Cursor cursorColor = pgcolorsDB.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name='" + tableName + "PageColorTable';", null);
        Cursor cursor = pageDB.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name='" + tableName + "PageTable';", null);
        if(cursor.getCount()==0) initializeDatabase();
        if (!editor) initializeDatabaseGameplay();
        if(cursorColor.getCount()==0) initializeColorDatabase();
    }

    private static void initializeColorDatabase(){
        if(tableName==null)return;          // protect GameSaves db from accidentally using
        String setupStr = "CREATE TABLE "+ tableName + "PageColorTable ("
                + "page INTEGER, pageName STRING, color STRING,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        pgcolorsDB.execSQL(setupStr);
    }

    private static void initializeDatabase(){
        if(tableName==null)return;          // protect GameSaves db from accidentally using
        String setupStr = "CREATE TABLE " + tableName + "PageTable ("
                + "page INTEGER, pageName STRING, name STRING, bounds STRING, image STRING, text STRING, hidden INTEGER, movable INTEGER, script STRING, font STRING,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        pageDB.execSQL(setupStr);
    }

    public static void initializeDatabaseGameplay() {
        String resetStr = "DROP TABLE IF EXISTS " + tableName + "PageTableCopy;";
        pageDB.execSQL(resetStr);
        String setupStr = "CREATE TABLE " + tableName + "PageTableCopy ("
                + "page INTEGER, pageName STRING, name STRING, bounds STRING, image STRING, text STRING, hidden INTEGER, movable INTEGER, script STRING, font STRING,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        pageDB.execSQL(setupStr);

        String copyStr = "INSERT INTO " + tableName + "PageTableCopy SELECT * FROM " + tableName + "PageTable";
        pageDB.execSQL(copyStr);
    }

    public int getPageNumForPageName(String pageName){
        if(tableName==null)return -1;
        String sqlSearch = "SELECT * FROM " + tableName + "PageColorTable WHERE pageName = '" + pageName + "';";
        Cursor cr = pgcolorsDB.rawQuery(sqlSearch,null);
        int pgnum = -1;
        if (cr.moveToFirst()) {
            do {
                //"page INTEGER, pageName STRING, color STRING"
                pgnum = cr.getInt(cr.getColumnIndex("page"));
            } while (cr.moveToNext());
            cr.close();
        }
        return pgnum;
    }

    public void addPageColor(String query){
        pgcolorsDB.execSQL(query);
    }

    public Cursor getPageColor(int pgNum){
        if(tableName==null)return null;          // protect GameSaves db from accidentally using
        String sqlSearch = "SELECT * FROM " + tableName + "PageColorTable WHERE page = " + pgNum + ";";
        System.out.println("getPageColor SEARCH " + sqlSearch);
        Cursor cursor = pgcolorsDB.rawQuery(sqlSearch, null);
        return cursor;
    }

    public Cursor getAllPages(){
        if(tableName==null)return null;          // protect GameSaves db from accidentally using
        Cursor cursor = pgcolorsDB.rawQuery("SELECT * FROM " + tableName + "PageColorTable;", null);
        return cursor;
    }

    public void deletePage(int pageNum, int numPages){
        if(tableName==null)return;          // protect GameSaves db from accidentally using
        for(int i = numPages; i > pageNum; i--){
            int newPageNum = i-1;
            String query = "UPDATE " + tableName + "PageTable SET page = " + newPageNum + " WHERE page = " + i + ";";
            pageDB.execSQL(query);
            String query_c = "UPDATE " + tableName + "PageColorTable SET page = " + newPageNum + " WHERE page = " + i + ";";
            pgcolorsDB.execSQL(query_c);          /// added for colors
        }
    }

    public void updatePageName(int pageNum, String newPageName){
        String query = "UPDATE " + tableName + "PageTable SET pageName = '" + newPageName + "' WHERE page = " + pageNum + ";";
        pageDB.execSQL(query);
    }

    public int numPageColorsInDB(){
        if(tableName==null)return -1;          // protect GameSaves db from accidentally using
        Cursor cursor = pgcolorsDB.rawQuery("SELECT * FROM " + tableName + "PageColorTable;", null);
        return cursor.getCount();
    }

    public Cursor getAllShapes(){
        if(tableName==null)return null;          // protect GameSaves db from accidentally using
        Cursor cursor = pageDB.rawQuery("SELECT * FROM " + tableName + "PageTable;", null);
        return cursor;
    }

    public int getNumPages() {
        if(tableName==null)return -1;          // protect GameSaves db from accidentally using
        int pg = 1;
        while(true){
            Cursor c = lookupPage(pg);
            if(c.getCount() == 0) return pg - 1;
            pg++;
            if(pg > 100) return -1;
        }
    }

    public void addPage(String pageShapes){
        pageDB.execSQL(pageShapes);
    }

    /*One of the methods that PlayGame calls, include check for whether or not to use gameplayCopy*/
    public Cursor lookupPage(int pageNumber){
        if(tableName==null)return null;          // protect GameSaves db from accidentally using
        StringBuilder sqlSearch = new StringBuilder();
        String table_name = tableName + "PageTable";
        if (!editor) table_name += "Copy";
        sqlSearch.append("SELECT * FROM " + table_name + " WHERE page = " + pageNumber + ";");
        String searchSQL = sqlSearch.toString();
        Cursor cursor = pageDB.rawQuery(
                searchSQL, null);
        return cursor;
    }

    public void clearPage(int pageNum){
        if(tableName==null)return;          // protect GameSaves db from accidentally using
        String table_name = tableName + "PageTable";
        if (!editor) table_name += "Copy";
        String query = "DELETE FROM " + table_name + " WHERE page = " + pageNum + ";";
        pageDB.execSQL(query);
        table_name = tableName + "PageColorTable";
        String query_c = "DELETE FROM " + table_name + " WHERE page = " + pageNum + ";";
        pgcolorsDB.execSQL(query_c);
    }
}