package com.google.firebase.codelab.kit.model.Mini_Game;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.kit.model.Login_Home_help_pages.HomePage;
import com.google.firebase.codelab.kit.model.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HighscoreOutside extends AppCompatActivity {

    private static final String TAG = "HighscoreOutside";

    TextView scoring_TextView;
    Button mExitBut, mRetryBut;
    private FirebaseAuth mAuth;
    String username, email;
    TextView userLogin;

    private DatabaseReference mDatabaseRef; //get our items and fill them into a list and into the adapter
    private  String userID;
    ArrayList<String> mUserUploads;
    ArrayAdapter<String> myAdapter;
    ListView listView;
    Integer count = 0;

    public TextView UserTextView;
    int lastScore, best1, best2, best3, recentScore;
    String score;
    TextView show;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

       // scoring_TextView = findViewById(R.id.scoring_view);
        mExitBut = findViewById(R.id.exitButton);
        mRetryBut = findViewById(R.id.retryButton);
        userLogin = findViewById(R.id.userLogin);
        show = findViewById(R.id.show);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        email = user.getEmail();
        String[] str = email.split("@");
        String username = str[0];

        userID = user.getUid();
        userLogin.setText("User: " + username);


        UserTextView = findViewById(R.id.text_view_user);
        listView = findViewById(R.id.list_view_score);



        mDatabaseRef = FirebaseDatabase.getInstance().getReference("games").child(userID);
        mUserUploads = new ArrayList<>();
        myAdapter = new ArrayAdapter<String>(this,R.layout.activity_highscore_view,
                R.id.text_view_user, mUserUploads);

        listView.setAdapter(myAdapter);

        mExitBut.setOnClickListener((View v) ->{
            Intent start = new Intent(HighscoreOutside.this, HomePage.class);
            startActivity(start);
            Toast.makeText(this, "Come back soon!", Toast.LENGTH_SHORT).show();
        });

        mRetryBut.setOnClickListener((View v) ->{
            Intent start = new Intent(HighscoreOutside.this, memoryGameActivity.class);
            startActivity(start);
        });



        mDatabaseRef.orderByChild("score").limitToFirst(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                     User user = postSnapshot.getValue(User.class);

                         mUserUploads.add(user.getScore().toString() + "s");

                }
                listView.setAdapter(myAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HighscoreOutside.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


    }
}
