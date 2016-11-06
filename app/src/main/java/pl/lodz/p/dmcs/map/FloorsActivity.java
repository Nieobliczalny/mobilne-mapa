package pl.lodz.p.dmcs.map;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class FloorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floors);
        setUpListeners();
    }

    private void setUpListeners(){
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView img=(ImageView)findViewById(R.id.widget45);
                img.setBackgroundResource(R.drawable.images);
            }
        });
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView img=(ImageView)findViewById(R.id.widget45);
                img.setBackgroundResource(R.drawable.cat);
            }
        });
        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView img=(ImageView)findViewById(R.id.widget45);
                img.setBackgroundResource(R.drawable.hamster);
            }
        });
        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView img=(ImageView)findViewById(R.id.widget45);
                img.setBackgroundResource(R.drawable.pet);
            }
        });
    }
}
