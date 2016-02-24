package com.wwylele.hatechnion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    public String ticket, username, real;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        ticket = extras.getString("ticket");
        username = extras.getString("username");
        real = extras.getString("real");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(real + " (" + username + ")");
        setSupportActionBar(toolbar);

    }

}
