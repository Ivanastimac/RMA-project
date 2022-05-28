package com.example.project.picturebook;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;

public class PagesViewModel extends AndroidViewModel {

    private static LiveData<ArrayList<Uri>> filePaths;

    public PagesViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ArrayList<Uri>> getFilePaths() {
        return filePaths;
    }

    public static void insert(ArrayList<Uri> uris) {
        //filePaths.setValue(uris);
    }
}
