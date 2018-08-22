package com.example.wiser.dottracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        TextView textView = findViewById(R.id.displayText);
        String finalOutput = "";
        finalOutput+="This application was created in the summer of 2018 by Mitchell Cooke as a research project under Dr.Rong Zheng of McMaster University's Department of Computing and software. ";
        finalOutput+= "\n\nThe purpose of this application is to collect WiFi signal data paired with collection coordinates for the purpose of fingerprinting indoors. This allows one to develop a relationship between position indoors and WiFi signal readings. ";
        finalOutput+="\n\nThe openCV camera is a separate tool that implements a visual step counter along with collection and location tagging of accelerometer, magentic, and grycoscope sensors. This allows better analysis of human walking cycle";
        finalOutput+= "\n\nFor more information contact Mitchell Cooke at cookem4@mcmaster.ca";
        textView.setText(finalOutput);

    }
}
