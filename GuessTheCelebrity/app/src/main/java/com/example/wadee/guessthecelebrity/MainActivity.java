package com.example.wadee.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    static final String DEBUGSTRING = "GUESSTHE CELEB APP";
    ArrayList<String> imagesURL = new ArrayList<String>();
    ArrayList<String> actorsNames = new ArrayList<String>();
    Button[] btns;
    ImageView view;
    int correctIndex = 0;
    int rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        view = (ImageView) findViewById(R.id.celebrityImage);
        btns = new Button[4];
        btns[0] = (Button) findViewById(R.id.btn1);
        btns[1] = (Button) findViewById(R.id.btn2);
        btns[2] = (Button) findViewById(R.id.btn3);
        btns[3] = (Button) findViewById(R.id.btn4);

        loadData();
        Log.i(DEBUGSTRING, "The size of the image array is: " + imagesURL.size() + "  and the other is " + actorsNames.size());

        downloadImage();


    }//onCreate method

    public void loadData() {
        //get the data from the URL ==>//http://www.posh24.com/celebrities
        DataLoader dataLoader = new DataLoader();
        try {
            String data = dataLoader.execute("http://www.posh24.com/celebrities").get();
            //parse the data using regular expressions
            Log.i(DEBUGSTRING, data.length() + "");
            //now parse the result and get what you need
            /*
            we need to get the images for the actors only
            so we should split the code
             */
            String[] results = data.split("<div class=\"col-xs-12 col-sm-6 col-md-4\">");
            Log.i(DEBUGSTRING, "The length of the result is " + results.length + "and the wanted data length is :" + results[0].length());
            Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = pattern.matcher(results[0]);
            int count = 0;
            while (m.find()) {
                count++;
                imagesURL.add(m.group(1));

            }

            //this is to store the actors names
            pattern = Pattern.compile("alt=\"(.*?)\"");
            m = pattern.matcher(results[0]);
            count = 0;
            while (m.find()) {
                count++;
                actorsNames.add(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //a method to download an image using the URL
    public void downloadImage() {
        Random r = new Random();
        rand = r.nextInt(imagesURL.size());
        String actorName = actorsNames.get(rand);
        String imageURL = imagesURL.get(rand);
        Log.i(DEBUGSTRING, "Random :" + rand);
        //get a random image
        correctIndex = r.nextInt(4);
        ImageLoader loader = new ImageLoader();
        try {
            Bitmap map = loader.execute(imageURL).get();
            //set this image in the imageview
            view.setImageBitmap(map);
            for (int i = 0; i < 4; i++) {
                if (i == correctIndex) {
                    btns[i].setText(actorName);
                } else {
                    int incorrectIndex = r.nextInt(imagesURL.size());
                    while (incorrectIndex == correctIndex) {
                        incorrectIndex = r.nextInt(imagesURL.size());
                    }
                    btns[i].setText(actorsNames.get(incorrectIndex));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

    class DataLoader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //get the data from the URL
            try {
                Log.i(DEBUGSTRING, "Starting the async class");
                String result = "";
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current + "";
                    data = reader.read();
                    //Log.i(DEBUGSTRING , "This is the result : " +result.length());
                }
                //Log.i(DEBUGSTRING , "This is the result : " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ImageLoader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                Bitmap map = BitmapFactory.decodeStream(is);
                return map;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void nameChosen(View view) {

        //get the tag and compare it with the correct Index
        String tag = view.getTag().toString();
        Log.i(DEBUGSTRING, "Tag : " + tag + " and correctIndex is " + correctIndex);
        if (Integer.parseInt(tag) == (correctIndex+1)) {
            Toast.makeText(this, "Correct !", Toast.LENGTH_LONG).show();
            downloadImage();
        } else {
            Toast.makeText(this, "Wrong , The correct answer is : " + actorsNames.get(rand), Toast.LENGTH_LONG).show();
        }


    }
}
