package ru.chesterlife.beatsfm;

import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    boolean OnOff = false;
    String DATA_STREAM;
    String SONG_NAME;
    MediaPlayer mediaPlayer;
    AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        StreamTask stream_t = new StreamTask();
        stream_t.execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView) findViewById(R.id.playButton);
        imageView.setImageResource(R.drawable.logo_off);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        TextView mTextView = (TextView) findViewById(R.id.trackText);
        Typeface tp = Typeface.createFromAsset(getAssets(), "fonts/avenir.otf");
        mTextView.setTypeface(tp);
    }

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

    public void onClick(View view) {
        ImageView imageView = (ImageView) findViewById(R.id.playButton);
        if (OnOff) {
            OnOff = false;
            imageView.setImageResource(R.drawable.logo_off);
            releaseMP();
            stop();
        }
        else {
            OnOff = true;
            imageView.setImageResource(R.drawable.logo_on);
            releaseMP();
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(DATA_STREAM));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class StreamTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Document doc = null;
            try {
                doc = Jsoup.connect("http://www.beatsfm.ru/").get();
            } catch (IOException e) { }
            if (doc != null) {
                Elements metaElement = doc.select("audio");
                DATA_STREAM = metaElement.attr("src");
            }
            else
                DATA_STREAM = "Error";

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    class SongTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Document doc2 = null;
            try {
                doc2 = Jsoup.connect("http://www.beatsfm.ru/app/php/radiostat.php").get();
            } catch (IOException e) { }
            if (doc2 != null) {
                SONG_NAME = doc2.text().toUpperCase();
            }
            else
                SONG_NAME = "Error";

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            TextView mTextView = (TextView) findViewById(R.id.trackText);
            Typeface tp = Typeface.createFromAsset(getAssets(), "fonts/avenir.otf");
            if (mTextView.getText() != SONG_NAME){
                mTextView.setTypeface(tp);
                mTextView.setText(SONG_NAME);
            }
        }
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                //mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(OnOff) {
                start();
            }
        }
    };
    public void stop() {
        handler.removeCallbacks(runnable);
    }

    public void start() {
        SongTask song_t = new SongTask();
        song_t.execute();
        handler.postDelayed(runnable, 3000);
    }
}
