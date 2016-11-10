package com.example.downloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.braindigit.downloader.DownloadListener;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.Downloader;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button button;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        button = (Button) findViewById(R.id.download);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                Downloader.from("https://scontent-sin6-1.xx.fbcdn.net/v/t1.0-9/14980568_1153383811382004_7401565203278646031_n.jpg?oh=0aa9437f90242b2dd5b27ecd9c7525ad&oe=58CC44D6").
                        into(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                        .start(new DownloadListener() {
                            @Override
                            public void onProgress(DownloadStatus status) {

                            }

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }
        });
    }
}
