package com.example.project.editpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.project.R;
import com.example.project.editpage.adapters.FilesAdapters;
import com.example.project.editpage.common.Common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListFilesActivity extends AppCompatActivity {

    List<File> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        initToolbar();

        initViews();
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_files);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        fileList = loadFile();
        FilesAdapters filesAdapters = new FilesAdapters(this, fileList);
        recyclerView.setAdapter(filesAdapters);

    }

    private List<File> loadFile() {

        ArrayList<File> inFiles = new ArrayList<>();
        File parentDir;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parentDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + getString(R.string.app_name));
        }else{
            parentDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + getString(R.string.app_name));
        }
        File[] files = parentDir.listFiles();

        for(File file : files){
            if(file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")){
                inFiles.add(file);
            }
        }

        TextView textView = findViewById(R.id.status_empty);
        if(files.length > 0){
            textView.setVisibility(View.GONE);
        }else{
            textView.setVisibility(View.VISIBLE);
        }

        return inFiles;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pictures");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }

        return true;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getTitle().equals(Common.DELETE)){
            deleteFile(item.getOrder());
            initViews();
        }

        return true;
    }

    private void deleteFile(int order) {
        fileList.get(order).delete();
        fileList.remove(order);
    }
}