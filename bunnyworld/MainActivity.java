package edu.stanford.cs108.bunnyworld;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Dialog newGameDialog;

    ArrayAdapter<String> gameSavesAdapter;
    Dialog gameSavesDialog;
    private ArrayList<String> gameSaves;
    ListView gameSavesListView;
    private boolean userIsInteracting;
    private Backend helper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = Backend.getInstance(this);
        getGameSavesFromDB();
        /*TODO: delete game saves, initialize adapter with previously created saves (db search)*/
        init();

    }

    private void getGameSavesFromDB(){
        gameSaves = new ArrayList<String>();
        Cursor cr = helper.getAllGameNamesSaved();
        System.out.println("about to get old games!");
        ArrayList<String> newShapeNames = new ArrayList<String>();
        if (cr.moveToFirst()) {
            do {
                String name = cr.getString(cr.getColumnIndex("gameTitle"));
                gameSaves.add(name);
                System.out.println("ADDDING: " + name);
            } while (cr.moveToNext());
            cr.close();
        }
    }

    private void init(){
        newGameDialog = new Dialog(MainActivity.this);
        newGameDialog.setContentView(R.layout.create_new_game_dialog);
        newGameDialog.setTitle("Create a New Game Save");

        gameSavesDialog = new Dialog(MainActivity.this);
        gameSavesDialog.setContentView(R.layout.game_saves_dialog);
        gameSavesDialog.setTitle("Games Available");

        gameSavesAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, gameSaves);
        gameSavesListView = (ListView) gameSavesDialog.findViewById(R.id.gameSavesList);
        gameSavesListView.setAdapter(gameSavesAdapter);
    }

    /*
    https://stackoverflow.com/questions/13397933/android-spinner-avoid-onitemselected-calls-during-initialization
    */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    /* onClick handler for Delete Game button in Main Activity*/
    public void deleteGame(View view){
        if(!userIsInteracting) return;
        if (gameSaves.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You have no games to delete",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        gameSavesDialog.show();
        gameSavesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String gameToDelete = gameSaves.get(i);
                System.out.println("game to delete in MainActivity is " + gameToDelete);
                gameSavesDialog.dismiss();
                gameSaves.remove(gameToDelete);
                helper.deleteGame(gameToDelete);
                Toast.makeText(getApplicationContext(), "Game " + gameToDelete + " successfully deleted",
                        Toast.LENGTH_SHORT).show();
                // delete from database
            }
        });
    }


    public void goToEditor(View view) {
        if (gameSaves.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must create and name a game save first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println("goToEditor");
        printGameSaves();
        gameSavesDialog.show();
        gameSavesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String gameToOpen = gameSaves.get(i);
                System.out.println("gameToOpen in MainActivity is " + gameToOpen);
                gameSavesDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.putExtra("gameSave", gameToOpen);
                startActivity(intent);
            }
        });
    }

    public void goToPlayGame(View view) {
        if (gameSaves.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must create and name a game save first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println("goToPlayGame");
        printGameSaves();
        gameSavesDialog.show();
        gameSavesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String gameToOpen = gameSaves.get(i);
                gameSavesDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, PlayGame.class);
                intent.putExtra("gameSave", gameToOpen);
                startActivity(intent);
            }
        });
    }

    public void createNewGame(View view) {
        newGameDialog.show();
        System.out.println("createNewGame");
        printGameSaves();
        Button okButton = (Button) newGameDialog.findViewById(R.id.gameSaveOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) newGameDialog.findViewById(R.id.gameSaveEditText);
                String gameSaveName = editText.getText().toString();
                if(gameSaveName.equals("")){
                    Toast.makeText(getApplicationContext(), "Please enter a name to create a new game.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (gameSaves.contains(gameSaveName)) {
                    Toast.makeText(getApplicationContext(), "Game save with name " + gameSaveName + " already exists.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                gameSaves.add(gameSaveName);
                helper.addNewGame(gameSaveName);
                gameSavesAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Created new game with name " + gameSaveName,
                        Toast.LENGTH_SHORT).show();
                editText.setText("");
                newGameDialog.dismiss();
            }

        });
    }

    public void printGameSaves(){
        for(String game:gameSaves){
            System.out.println("game saved: " +game);
        }
    }


    public void goToTutorial(View view) {
        Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
        startActivity(intent);
    }
}
