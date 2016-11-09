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
                new DownloadTask().execute();
            }
        });
    }

    class DownloadTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Downloader.from("https://raw.githubusercontent.com/googlesamples/android-architecture/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/ApplicationModule.java").
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
            return null;
        }
    }
}
