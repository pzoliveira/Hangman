package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    // Declare variables
    TextView txtWordToBeGuessed;
    String wordToBeGuessed;
    String wordDisplayedString;
    char[] wordDisplayedCharArray;
    ArrayList<String> myListOfWords;
    EditText edtInput;
    TextView txtLettersTried;
    String lettersTried;
    final String MESSAGE_WITH_LETTERS_TRIED = "Letters tried: ";
    TextView txtTriesLeft;
    String triesLeft;
    final String WINNING_MESSAGE = "You won!";
    final String LOSING_MESSAGE = "You lost!";
    Animation rotateAnimation;
    Animation scaleAnimation;
    Animation scaleAndRotateAnimation;
    TableRow trReset;
    TableRow trTriesLeft;

    public void revealLetterInWord(char letter) {
        int indexOfLetter = wordToBeGuessed.indexOf(letter);

        // Loop if index is 0 or positive
        while(indexOfLetter >= 0) {
            wordDisplayedCharArray[indexOfLetter] = wordToBeGuessed.charAt(indexOfLetter);
            indexOfLetter = wordToBeGuessed.indexOf(letter, indexOfLetter + 1);
        }

        // Update the string as well
        wordDisplayedString = String.valueOf(wordDisplayedCharArray);
    }

    public void displayWordOnScreen() {
        String formattedWord = "";
        for(char character : wordDisplayedCharArray) {
            formattedWord += character + " ";
        }
        txtWordToBeGuessed.setText(formattedWord);
    }

    public void initializeGame() {
        // 1.WORD
        // Shuffle array list and get first element, and then remove it
        Collections.shuffle(myListOfWords);
        wordToBeGuessed = myListOfWords.get(0);
        myListOfWords.remove(0);

        // Initialize char array
        wordDisplayedCharArray = wordToBeGuessed.toCharArray();

        // Add underscores
        for(int i = 1; i < wordDisplayedCharArray.length -1; i++) {
            wordDisplayedCharArray[i] = '_';
        }

        // Reveal all occurrences of first character
        revealLetterInWord(wordDisplayedCharArray[0]);

        // Reveal all occurrences of last character
        revealLetterInWord(wordDisplayedCharArray[wordDisplayedCharArray.length - 1]);

        // Initialize a string from this char array (for search purposes)
        wordDisplayedString = String.valueOf(wordDisplayedCharArray);

        // Display word
        displayWordOnScreen();

        // 2.INPUT
        // Clear input field
        edtInput.setText("");

        // 3.LETTERS TRIED
        // Initialize string for letters tried with a space
        lettersTried = " ";

        // Display on screen
        txtLettersTried.setText(lettersTried);

        // 4.TRIES LEFT
        // Initialize string for tries left
        triesLeft = " X X X X X";
        txtTriesLeft.setText(triesLeft);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        txtWordToBeGuessed = (TextView) findViewById(R.id.txtWordToBeGuessed);
        myListOfWords = new ArrayList<String>();
        edtInput = (EditText) findViewById(R.id.edtInput);
        txtLettersTried = (TextView) findViewById(R.id.txtLettersTried);
        txtTriesLeft = (TextView) findViewById(R.id.txtTriesLeft);
        rotateAnimation = AnimationUtils.loadAnimation(this,R.anim.rotate);
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        scaleAndRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_and_rotate);
        scaleAndRotateAnimation.setFillAfter(true);
        trReset = (TableRow) findViewById(R.id.trReset);
        trTriesLeft = (TableRow) findViewById(R.id.trTriesLeft);

        // Traverse database file and populate array list
        InputStream myInputStream = null;
        Scanner in = null;
        String aWord = "";

        try {
            myInputStream = getAssets().open("database_file.txt");
            in = new Scanner(myInputStream);
            while(in.hasNext()){
                aWord = in.next();
                myListOfWords.add(aWord);
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getClass().getSimpleName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            // Close Scanner
            if(in != null){
                in.close();
            }
            // Close InputStream
            try {
                if(myInputStream != null) {
                    myInputStream.close();
                }
            }
            catch (IOException e) {
                Toast.makeText(MainActivity.this, e.getClass().getSimpleName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        initializeGame();

        // Setup the text changed listener for the edit text
        edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If there is some letter on the input field
                if(s.length() !=0) {
                    checkIfLetterIsInWord(s.charAt(0));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void checkIfLetterIsInWord(char letter) {
        // If the letter was found inside the word to be guessed
        if(wordToBeGuessed.indexOf(letter) >= 0) {
            // If the letter was not displayed yet
            if(wordDisplayedString.indexOf(letter) < 0) {
                // Animate
                txtWordToBeGuessed.startAnimation(scaleAnimation);

                // Replace dhe underscores with the letter
                revealLetterInWord(letter);
                }

            // Update the changes on screen
            displayWordOnScreen();

            // Check if the game is won
            if(!wordDisplayedString.contains("_")){
                trTriesLeft.startAnimation(scaleAndRotateAnimation);
                txtTriesLeft.setText(WINNING_MESSAGE);
            }
        }
        // The letter was NOT found
        else {
            // Decrease the number of tries left, and we'll show it on screen
            decreaseAndDisplayTriesLeft();

            // Check if the game is lost
            if(triesLeft.isEmpty()){
                trTriesLeft.startAnimation(scaleAndRotateAnimation);
                txtTriesLeft.setText(LOSING_MESSAGE);
                txtWordToBeGuessed.setText(wordToBeGuessed);
            }
        }

        // Display the letter that was tried
        if(lettersTried.indexOf(letter) < 0){
            if(lettersTried.length() == 1){
                lettersTried += letter;
            } else {
                lettersTried += ", " + letter;
            }
            String messageToBeDisplayed = MESSAGE_WITH_LETTERS_TRIED + lettersTried;
            txtLettersTried.setText(messageToBeDisplayed);
        }
    }

    public void decreaseAndDisplayTriesLeft() {
        // If there are still some tries
        if(!triesLeft.isEmpty()){
            // Animate
            txtTriesLeft.startAnimation(scaleAnimation);

            // Take out the last two characters from this string
            triesLeft = triesLeft.substring(0, triesLeft.length() -2);
            txtTriesLeft.setText(triesLeft);
        }
    }

    public void resetGame(View view) {
        // Animate
        trReset.startAnimation(rotateAnimation);

        // Clear animation on table row
        trTriesLeft.clearAnimation();

        // Setup a new game
        initializeGame();
    }
}