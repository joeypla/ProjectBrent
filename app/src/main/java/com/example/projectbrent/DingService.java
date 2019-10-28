package com.example.projectbrent;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.media.MediaPlayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import edu.cmu.pocketsphinx.RecognitionListener;

import android.speech.tts.TextToSpeech;

// This service is going to be handling pretty much all the speech input, as well as the acknowledgement
// output to the user. The purpose of having it in a service is so that we can talk to our phone
// while the app is in the background and/or the phone is asleep/locked.

public class DingService extends Service, implements TextToSpeech.OnInitListener, RecognitionListener {

    /* Recognition object - Interface with CMU pocket sphinx */
    private SpeechRecognizer recognizer;

    // The ding to play when acknowledging a command.
    MediaPlayer dingPlayer;

    private static final String hotWord = "Project Brent";
    private static final String SEARCH_TYPE_KEYWORD = "wakeup";
    private static final String SEARCH_TYPE_COMMAND = "command";

    // A testing timer for the ding, will be removed.
    private Timer timer = null;

    public DingService()
    {
    }

    /* TextToSpeech - OnInitListener */
    @Override
    public void onInit(int i) {} // Not really sure what to do here.

    @Override
    public void onCreate()
    {
        try {
            dingPlayer = MediaPlayer.create(this, R.raw.ding);

        } catch (IllegalStateException e)
        {

        }


        // Remove when sure
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimedDing(), 0,5000);


    }

    private void setupSpeechRecognition()
    {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(DingService.this);
                    File assetsDir = assets.syncAssets();
                    setupRecognizerParameters(assetsDir);
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
                    recognizer.startListening(SEARCH_TYPE_KEYWORD);
                }
            }
        }.execute();
    }

    public void setupRecognizerParameters(File assetsDir) throws IOException
    {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                //.setRawLogDir(assetsDir)

                .getRecognizer();
        recognizer.addListener(this);
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch("wakeup", hotWord);
        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "mymenu.gram");
        recognizer.addGrammarSearch("command", menuGrammar);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {




        return START_STICKY;
    }

    @Nullable


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /* CMU Sphinx Speech Recognition */
    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        if ()
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }


    // Play ding timer
    class TimedDing extends TimerTask
    {
        @Override
        public void run()
        {
            dingPlayer.start();
        }
    }
}
