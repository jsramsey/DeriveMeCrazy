package edu.fandm.jramsey.derivemecrazy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Intent i = getIntent();
        ArrayList<String> list =i.getStringArrayListExtra("list");
        if(savedInstanceState == null) {
            list.add(0, "Completed Operations:");
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listView.setAdapter(adapter);
    }
}
