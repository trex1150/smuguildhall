package edu.stanford.cs108.bunnyworld;

import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class TutorialActivity extends AppCompatActivity {

    private String spinnerSelection;
    private String spinnerSelection2;
    private boolean imageSelected;
    private MediaPlayer evilLaughmp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);


        init();
    }


    private void init() {
        //spinners
        imageSelected = false;
        Spinner spinner = (Spinner) findViewById(R.id.imageNamesSpinnerTutorial);
        Spinner spinner2 = (Spinner) findViewById(R.id.imageNamesSpinnerTutorial2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.img_drop_down_menu, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelection = (String) adapterView.getItemAtPosition(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerSelection = null;
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelection2 = (String) adapterView.getItemAtPosition(i);
             }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerSelection2 = null;
            }
        });

        //first user-interaction
        final ImageView duckImg = (ImageView) findViewById(R.id.duckImgTutorial);
        duckImg.setVisibility(View.GONE);

        duckImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDuck(duckImg);
            }
        });

        TextView clickSpaceDuck = (TextView) findViewById(R.id.duckClickSpaceTutorial);
        clickSpaceDuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDuck(duckImg);
            }
        });

        //second user-interaction
        final ImageView mysticImg = (ImageView) findViewById(R.id.mysticTutorial);
        mysticImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton selectButton = (RadioButton) findViewById(R.id.selectTutorial);
                if (!selectButton.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Make sure you check \"Select\" before trying to " +
                            "select the shape!", Toast.LENGTH_SHORT).show();
                } else {
                    mysticImg.setImageResource(R.drawable.mystic_selected_tut);
                    imageSelected = true;
                }
            }
        });

    }

    private void popupDuck(final ImageView duck) {
        RadioButton shapeRB = findViewById(R.id.shapeRBTutorial);
        RadioButton addToPageRB = findViewById(R.id.addToPageRBTutorial);

        if (!shapeRB.isChecked()) {
            Toast.makeText(getApplicationContext(), "Make sure you check the \"Shape\" bubble first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!addToPageRB.isChecked()) {
            Toast.makeText(getApplicationContext(), "Make sure you check the \"Add To Page\" bubble first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerSelection.equals("duck")) {
            duck.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Great job!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Make sure you select the duck from the drop down menu first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateButton(View view) {
        ImageView mystic = (ImageView) findViewById(R.id.mysticTutorial);
        if (mystic.getResources().equals(R.drawable.carrot)) return;
        if (!imageSelected) {
            Toast.makeText(getApplicationContext(), "Make sure you select the shape first!", Toast.LENGTH_SHORT).show();
        } else if (!spinnerSelection2.equals("carrot")) {
            Toast.makeText(getApplicationContext(), "Make sure you select the carrot from the drop down menu first!", Toast.LENGTH_SHORT).show();
        } else {
            mystic.setImageResource(R.drawable.carrot);
            Toast.makeText(getApplicationContext(), "Poof! Wow, great job!", Toast.LENGTH_SHORT).show();
        }
    }

    public void playSoundEvilLaugh(View view) {
        if (evilLaughmp == null) evilLaughmp = MediaPlayer.create(this, R.raw.evillaugh);
        evilLaughmp.setVolume(1.0f, 1.0f);
        evilLaughmp.start();
    }
}
