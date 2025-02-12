package com.example.musicduniya;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;

public class Playsong extends AppCompatActivity {


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            updateseek.interrupt();
            mediaPlayer = null;
        }
    }

    TextView textView;
    ImageView previous;
    ImageView play;
    ImageView next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar2;
    Thread updateseek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playsong);

        textView = findViewById(R.id.textView);
        previous = findViewById(R.id.previous);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        seekBar2 = findViewById(R.id.seekBar2);

        Intent intent = getIntent();
        ArrayList<String> songPaths = intent.getStringArrayListExtra("songlist");
        textContent = intent.getStringExtra("currentsong");
        position = intent.getIntExtra("position", 0);

        if (songPaths != null) {
            songs = new ArrayList<>();
            for (String path : songPaths) {
                songs.add(new File(path)); // Convert string path back to File
            }

            textView.setText(textContent);
            textView.setSelected(true);
            Uri uri = Uri.parse(songs.get(position).toString());
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.start();

            // Set the play button to show the "pause" icon initially since the song starts playing
            play.setImageResource(R.drawable.pause);

            seekBar2.setMax(mediaPlayer.getDuration());

            seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar2.getProgress());
                }
            });

            // Update the seek bar position in a background thread
            updateseek = new Thread() {
                @Override
                public void run() {
                    int currentPosition = 0;
                    try {
                        while (currentPosition < mediaPlayer.getDuration()) {
                            currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar2.setProgress(currentPosition);
                            sleep(800);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            updateseek.start();

            // Add the listener to automatically play the next song when the current one ends
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNextSong();
                }
            });
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    play.setImageResource(R.drawable.play); // Change to play icon
                    mediaPlayer.pause();
                } else {
                    play.setImageResource(R.drawable.pause); // Change to pause icon
                    mediaPlayer.start();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != 0) {
                    position = position - 1;
                } else {
                    position = songs.size() - 1;
                }
                playSong();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Helper method to play the next song
    private void playNextSong() {
        mediaPlayer.stop();
        mediaPlayer.release();
        if (position != songs.size() - 1) {
            position = position + 1;
        } else {
            position = 0;
        }
        playSong();
    }

    // Helper method to play a song
    private void playSong() {
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause); // Show pause icon as song starts
        seekBar2.setMax(mediaPlayer.getDuration());
        textContent = songs.get(position).getName().toString();
        textView.setText(textContent);

        // Set the listener for the next song when this one ends
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextSong();
            }
        });
    }
}