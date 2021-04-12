package com.lawlett.taskmanageruikit.help;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.help.recycler.HelpAdapter;
import com.lawlett.taskmanageruikit.help.recycler.HelpModel;
import com.lawlett.taskmanageruikit.main.MainActivity;
import com.lawlett.taskmanageruikit.onboard.BoardActivity;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<HelpModel> helpModelList = new ArrayList<>();
    Dialog dialog;
    HelpAdapter helpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        dialog = new Dialog(this);
        fillingList();
        initRecycler();
        initListeners();
    }

    private void fillingList() {
        helpModelList.add(new HelpModel(getString(R.string.intro), getResources().getString(R.string.intro), R.raw.change1));
        helpModelList.add(new HelpModel(getString(R.string.dialog_title), getResources().getString(R.string.helper_dialog_text), R.raw.change1));
        helpModelList.add(new HelpModel(getString(R.string.move_title), getResources().getString(R.string.move_tasks), R.raw.move1));
        helpModelList.add(new HelpModel(getString(R.string.delete_title), getResources().getString(R.string.delete_task), R.raw.delete1));
        helpModelList.add(new HelpModel(getString(R.string.timer_title), getString(R.string.timer_text), R.raw.timer1));
        helpModelList.add(new HelpModel(getString(R.string.stopwatch_title), getString(R.string.stopwatch_text), R.raw.stopwatch1));
    }

    private void initListeners() {
        helpAdapter.setOnItemClickListener(new HelpAdapter.MyOnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                if (id == 0) {
                    startActivity(new Intent(HelpActivity.this, BoardActivity.class));
                } else {
                    dialog.setContentView(R.layout.fragment_help);
                    TextView title = dialog.findViewById(R.id.fragment_help_tv);
                    Button button = dialog.findViewById(R.id.fragment_help_button);
                    title.setText(helpModelList.get(id).getDescription());

                    VideoView video = dialog.findViewById(R.id.fragment_help_gif);
                    String path = "android.resource://" + getPackageName() + "/" + helpModelList.get(id).getVideo();
                    video.setVideoURI(Uri.parse(path));
                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setLooping(true);
                        }
                    });
                    video.start();
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void initRecycler() {
        recyclerView = findViewById(R.id.helpRecyclerView);
        helpAdapter = new HelpAdapter(this, helpModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(helpAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("help", "fromHelp");
        startActivity(intent);
    }

}