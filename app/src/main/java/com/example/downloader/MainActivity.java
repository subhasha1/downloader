package com.example.downloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.braindigit.downloader.DownloadListener;
import com.braindigit.downloader.DownloadRequest;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.Downloader;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button buttonStart, buttonPause;
    ProgressBar progressBar;
    DownloadRequest downloadRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        buttonStart = (Button) findViewById(R.id.btnStart);
        buttonPause = (Button) findViewById(R.id.btnPause);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStart.setEnabled(false);
                buttonPause.setEnabled(true);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.setVisibility(View.VISIBLE);
                downloadRequest = Downloader.from("https://upload.wikimedia.org/wikipedia/commons/e/e0/Large_Scaled_Forest_Lizard.jpg")
                        .fileName("image.jpg")
                        .listener(new DownloadListener() {
                            @Override
                            public void onProgress(DownloadStatus status) {
                                textView.setText(formatSize(status.getTotalSize()) + "/" + formatSize(status.getDownloadSize()));
                                int currentProgress = (int) ((status.getDownloadSize() * 100) / status.getTotalSize());
                                progressBar.setProgress(currentProgress);
                            }

                            @Override
                            public void onComplete() {
                                progressBar.setVisibility(View.GONE);
                                textView.setText("Complete");
                            }

                            @Override
                            public void onError(Exception e) {
                                progressBar.setVisibility(View.GONE);
                                textView.setText(e.toString());
                            }
                        }).download();
            }
        });
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloadRequest != null) {
                    downloadRequest.cancel();
                    downloadRequest = null;
                    buttonStart.setEnabled(true);
                    buttonPause.setEnabled(false);
                    textView.setText("Paused");
                }
            }
        });
    }

    static String formatSize(long size) {
        String hrSize;
        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }
        return hrSize;
    }
}
