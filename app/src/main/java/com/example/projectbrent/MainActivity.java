package com.example.projectbrent;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;


// Imports for speech
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.ArrayList;
import java.util.Locale;

import android.widget.TextView;
import android.speech.tts.TextToSpeech;

import android.os.AsyncTask;


// Includes for sphinx
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, RecognitionListener {

    TextToSpeech tts;

    ArrayList<State> stateMachine;
    State activeState;

    int partialResultCounter = 0;
    int fullResultCounter = 0;

    // Element references

    /* We only need the keyphrase to start recognition, one menu with list of choices,
       and one word that is required for method switchSearch - it will bring recognizer
       back to listening for the keyphrase*/
    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    /* Keyword we are looking for to activate recognition */
    private static final String KEYPHRASE = "namara";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);


        tts = new TextToSpeech(this, this);
        setupStates();
        setupElementReferences();

        setupDingService();
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    showMessage(result.getMessage());
                } else {
                    recognizer.startListening(KWS_SEARCH);
                }
            }
        }.execute();
    }

    protected void setupDingService()
    {
        Intent serviceIntent = new Intent(this, DingService.class);
        startService(serviceIntent);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                //.setRawLogDir(assetsDir)

                .getRecognizer();
        recognizer.addListener(this);
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (recognizer != null) {
//            recognizer.cancel();
//            recognizer.shutdown();
//        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
        {
            setPartialResultText("NULL - " +  partialResultCounter++);
            return;
        }

        setPartialResultText(hypothesis.getHypstr() + " - " + partialResultCounter++);

//        String text = hypothesis.getHypstr();
//        if (text.equals(KEYPHRASE))
//            switchSearch(MENU_SEARCH);
//        else {
//            //showMessage(hypothesis.getHypstr());
//        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            setFullResultText(hypothesis.getHypstr() + " - " + fullResultCounter++);
            //showMessage(hypothesis.getHypstr());
        } else
        {
            setFullResultText("NULL - " + fullResultCounter++);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
//        if (!recognizer.getSearchName().equals(KWS_SEARCH))
//            switchSearch(KWS_SEARCH);
    }


    private void switchSearch(String searchName) {
//        recognizer.stop();
//        if (searchName.equals(KWS_SEARCH))
//            recognizer.startListening(searchName);
//        else
//            recognizer.startListening(searchName, 10000);
    }

    @Override
    public void onError(Exception error) {
        showMessage(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    protected void setupElementReferences()
    {
        TextView fullResultText = (TextView) findViewById(R.id.FullResult);
        TextView partialResultText = (TextView) findViewById(R.id.PartialResult);
    }

    @Override
    public void onInit(int i) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupSpeech()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        startActivityForResult(intent, 10);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK && data == null) {
            showMessage("bad code");
            return;
        }

        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        if (results == null)
        {
            showMessage("null results");
            return;
        }

        // We are looking for 3 key words here. The first one is complete. The next two
        // are numbers representing pounds and reps respectively.

        boolean complete = false;
        int reps = -1;
        int weight = -1;

        String phrase = "";
        String result = "";
        if (results.size() > 0)
        {
            result = results.get(0);
        }


        String [] wordsArray = result.split(" ");

        for (String word : wordsArray) {
            phrase = phrase + " " + word;

            if (word.equalsIgnoreCase("complete") ||
                    word.equalsIgnoreCase("completed") ||
                    word.equalsIgnoreCase("finish") ||
                    word.equalsIgnoreCase("finished")) {
                complete = true;
            }

            try {
                int num = Integer.parseInt(word);

                if (reps < 0) {
                    reps = num;
                } else if (weight < 0) {
                    // If we've gotten to the second number, we can just exit early.
                    weight = num;
                    break;
                }


            } catch (NumberFormatException e) {
                continue;
            }
        }





        if (complete)
        {
            completeStateAndAdvance(reps, weight);
        }


        // Also set the text for debugging
        showMessage(phrase);
    }

    protected void showMessage(String message)
    {
        TextView spokenText = (TextView) findViewById(R.id.FullResult);
        spokenText.setText(message);
    }

    protected void setFullResultText(String text)
    {
        TextView fullResultText = (TextView) findViewById(R.id.FullResult);
        fullResultText.setText(text);
    }

    protected void setPartialResultText(String text)
    {
        TextView partialResultText = (TextView) findViewById(R.id.PartialResult);
        partialResultText.setText(text);
    }

    protected void speak(String message)
    {
        tts.speak(message, TextToSpeech.QUEUE_ADD, null);
    }

    protected void setupStates()
    {
        stateMachine = new ArrayList<State>();

        State bench = new State("Bench Press", "Starting bench press bro");
        State pulldown = new State("Lat pull downs", "Starting lat pulldowns bro");

        stateMachine.add(bench);
        stateMachine.add(pulldown);

        activeState = stateMachine.get(0);
        introduceState(activeState);
    }

    protected void introduceState(State state)
    {
        speak(state.introMessage);
    }

    protected void completeStateAndAdvance(int reps, int weight)
    {
        activeState.complete(reps, weight);

        int activeIndex = stateMachine.indexOf(activeState);


        if (activeIndex >= stateMachine.size() - 1)
        {

            speak("Workout is complete. Great job penis face. Summary email is on the way.");
            sendEmail();
        } else
        {
            speak("You completed " + activeState.stateName + " by doing " + reps + " repetitions of " + weight + " pounds");
            activeIndex++;
            activeState = stateMachine.get(activeIndex);
            introduceState(activeState);
        }
    }

    protected void sendEmail()
    {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("plain/text");
        emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"joey.pla@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Your latest workout");

        String body = "Your latest workout summary!\n";
        for (State state : stateMachine)
        {
            body += state.stateName + "\n";
            body += state.reps + " reps\n";
            body += state.weight + " lbs\n\n";
        }

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        //startActivity(emailIntent);
    }
}
