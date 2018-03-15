package com.abicetta.italianpastaquiz;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
     * This method is called when the submit button is clicked.
     */
    public void submitChoice(View view) {
        RadioButton left_button, center_button, right_button;
        int totalPoint = 0;
        EditText nameText = (EditText) findViewById(R.id.name_input);
        String namesubject = nameText.getText().toString();

       /*
        * this code checks each radiogroup and calls method addScore to calulate the score
        */
        left_button = (RadioButton) findViewById(R.id.left_button01);
        center_button = (RadioButton) findViewById(R.id.center_button01);
        right_button = (RadioButton) findViewById(R.id.right_button01);
        totalPoint = addScore(1, totalPoint, left_button, center_button, right_button);

        left_button = (RadioButton) findViewById(R.id.left_button02);
        center_button = (RadioButton) findViewById(R.id.center_button02);
        right_button = (RadioButton) findViewById(R.id.right_button02);
        totalPoint = addScore(2, totalPoint, left_button, center_button, right_button);

        left_button = (RadioButton) findViewById(R.id.left_button03);
        center_button = (RadioButton) findViewById(R.id.center_button03);
        right_button = (RadioButton) findViewById(R.id.right_button03);
        totalPoint = addScore(3, totalPoint, left_button, center_button, right_button);

        left_button = (RadioButton) findViewById(R.id.left_button04);
        center_button = (RadioButton) findViewById(R.id.center_button04);
        right_button = (RadioButton) findViewById(R.id.right_button04);
        totalPoint = addScore(4, totalPoint, left_button, center_button, right_button);

        left_button = (RadioButton) findViewById(R.id.left_button05);
        center_button = (RadioButton) findViewById(R.id.center_button05);
        right_button = (RadioButton) findViewById(R.id.right_button05);
        totalPoint = addScore(5, totalPoint, left_button, center_button, right_button);

        left_button = (RadioButton) findViewById(R.id.left_button06);
        center_button = (RadioButton) findViewById(R.id.center_button06);
        right_button = (RadioButton) findViewById(R.id.right_button06);
        totalPoint = addScore(6, totalPoint, left_button, center_button, right_button);

        /*
        * This code checks only the correct checkboxes and does not include the incorrect answers in the count.
        */
        CheckBox box1 = (CheckBox) findViewById(R.id.pastanames1);
        if (box1.isChecked()) {
            totalPoint+=10;
        }
        CheckBox box2 = (CheckBox) findViewById(R.id.pastanames2);
        if (box2.isChecked()) {
            totalPoint+=10;
        }
        CheckBox box3 = (CheckBox) findViewById(R.id.pastanames3);
        if (box3.isChecked()) {
            totalPoint+=10;
        }
        int perc = totalPoint * 100 / 90;

        /*
         * Create a message of the final score as a toast
         */
        String finalMessage;
        finalMessage = getString(R.string.hi) + " " + namesubject + "!";
        if (perc < 50) {
            finalMessage += "\n" + getString(R.string.badRes);
        } else {
            finalMessage += "\n" + getString(R.string.goodRes);
        }
        finalMessage += "\n" + getString(R.string.you) + " " + perc + "% " + getString(R.string.ofThe);
        finalMessage += " " + totalPoint + " " + getString(R.string.points);
        final Toast tag = Toast.makeText(this, finalMessage, Toast.LENGTH_SHORT);
        tag.show();
        new CountDownTimer(9000, 1000)
        {
            public void onTick(long millisUntilFinished) {tag.show();}
            public void onFinish() {tag.show();}
        }.start();

        /*
         * Open web page with information about many type of pasta.
         * */
        CheckBox seeSite = (CheckBox) findViewById(R.id.emailMe);
        boolean hasSite = seeSite.isChecked();
        if (hasSite) {
            String url = "https://en.wikipedia.org/wiki/List_of_pasta";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    /*
     * This method add the score of a choice of the quiz.
     */
    public int addScore(int quizNumber, int totalPoint, RadioButton left_button, RadioButton center_button, RadioButton right_button) {
        if (left_button.isChecked()) {
            if (quizNumber == 2 || quizNumber == 4 || quizNumber == 5) {
                totalPoint += 10;
            }
        } else if (center_button.isChecked()) {
            if (quizNumber == 1 || quizNumber == 6) {
                totalPoint += 10;
            }
        } else if (right_button.isChecked()) {
            if (quizNumber == 3) {
                totalPoint += 10;
            }
        }
        return totalPoint;
    }
}