package com.example.project.picturebook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.example.project.model.Picturebook;
import com.example.project.user_profile.Login;
import com.example.project.user_profile.Register;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class NewPicturebook extends AppCompatActivity {

    EditText title;
    EditText summary;
    Button savePicturebookBtn;
    Button discardPicturebookBtn;
    Button addPageBtn;
    String picturebookId;
    String titleSF;
    String summarySF;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_picturebook);

        title = findViewById(R.id.editTextTitle);
        summary = findViewById(R.id.editTextSummary);
        savePicturebookBtn = findViewById(R.id.buttonSavePicturebook);
        discardPicturebookBtn = findViewById(R.id.buttonDiscardPicturebook);
        addPageBtn = findViewById(R.id.buttonAddPage);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("/picturebooks");

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        titleSF = sharedPref.getString(getString(R.string.title), null);
        summarySF = sharedPref.getString(getString(R.string.summary), null);

        if (titleSF != null) {
            title.setText(titleSF);
        }

        if (summarySF != null) {
            summary.setText(summarySF);
        }

        addPageBtn.setOnClickListener(view -> {
            // save title and summary to shared preferences
            editor = sharedPref.edit();
            editor.putString(getString(R.string.title), title.getText().toString());
            editor.putString(getString(R.string.summary), summary.getText().toString());
            editor.apply();

            // TODO - redirect to new activity
        });

        // save title and summary
        // TODO - save pages
        savePicturebookBtn.setOnClickListener(view -> {
            if (title != null && !title.getText().toString().isEmpty() && summary != null && !summary.getText().toString().isEmpty()) {
                Picturebook picturebook = new Picturebook(loggedInUser.getUid(), title.getText().toString(), summary.getText().toString());
                database.push().setValue(picturebook);
                picturebookId = database.getKey();
                if (picturebookId != null) {
                    Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        discardPicturebookBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_picturebook_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // delete title and summary from shared preferences
                            editor.remove(getString(R.string.title));
                            editor.remove(getString(R.string.summary));
                            editor.apply();

                            Intent in = new Intent(view.getContext(), MainMenu.class);
                            view.getContext().startActivity(in);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });


    }
}