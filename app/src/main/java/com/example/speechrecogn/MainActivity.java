package com.example.speechrecogn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private ImageButton imageButton;
    private Button okButton;
    private TextView viewText;
    private EditText editText;
    private SpeechRecognizer speechRecognizer;
    private int count = 0;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;



    private ArrayList<String> list = new ArrayList<>();

    public ArrayList<String> getList() {
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.button);
        okButton = findViewById(R.id.button2);
        viewText = findViewById(R.id.viewtext);
        editText = findViewById(R.id.edittext2);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent speechRecognition = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 0) {
                    imageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
                    speechRecognizer.startListening(speechRecognition);
                    count = 1;
                } else {
                    imageButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    speechRecognizer.stopListening();
                    count = 0;


                }
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                list.add(editText.getText().toString());
                new ProgressTask().execute();
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {


            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> list1 = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);

                for (String str : list1) {
                    viewText.setText(str);

                }
                editText.setText(viewText.getText(), TextView.BufferType.EDITABLE);


            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


    }

    private StringBuilder parseResult(String str) {
        String[] strComma = str.split(",");
        StringBuilder stringBuilder = new StringBuilder();
        ;
        for (int i = 0; i < strComma.length; i++) {
            String[] strColon = strComma[i].split(":");
            for (int j = 0; j < strColon.length/2; j++) {
                if (strColon.length % 2 == 0 && j<= strColon.length-1) {
                    stringBuilder.append("\""+strColon[0]+"\"" + ":");
                    stringBuilder.append("\""+strColon[1]+"\"");
                    if (i != strColon.length - 1) {
                        stringBuilder.append("," + "\n");
                    }
                }
            }
        }
        return stringBuilder;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT);
            }
        }
    }


    private class ProgressTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (getList() != null) {

                StringBuilder str = parseResult(getList().toString());

                String body = "{" + str + "}";
                System.out.println("JSON" + body);
                sendPostRequest("http://localhost:8080/test", body);
            }
            return null;
        }

        private String sendPostRequest(String path, String body) {
            String result = null;
            if (path != null && body != null) {
                System.out.println("[Debug] send to get request on: " + path);
                try {
                    URL url = new URL(path);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setReadTimeout(15000);
                    c.setConnectTimeout(15000);
                    c.setRequestProperty("Content-Type", "application/json; utf-8");
                    c.setRequestProperty("Accept", "application/json");
                    c.setDoOutput(true);

                    try (OutputStream os = c.getOutputStream()) {
                        byte[] input = body.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int code = c.getResponseCode();
                    result = Integer.toString(code);

                } catch (MalformedURLException ex) {
                    System.out.println("[ERROR] URL is not correct");
                    System.out.println(ex.getMessage());
                } catch (IOException ioException) {
                    System.out.println("[ERROR] Not connect");
                    System.out.println(ioException.getMessage());
                }
            }

            return result;
        }

    }
}