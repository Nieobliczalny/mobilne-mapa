package pl.lodz.p.dmcs.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class AddUnitActivity extends AppCompatActivity {
    protected String token;
    private String temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unit);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }
        Button btnSend = (Button) findViewById(R.id.btnSend);
        if (btnSend != null) btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText name = (EditText) findViewById(R.id.newName);
                final EditText un = (EditText) findViewById(R.id.unoficial_name);
                final EditText number = (EditText) findViewById(R.id.number);
                temp = un.getText().toString();
                String[] temp2 = temp.split(",");
                StringBuilder finalny = new StringBuilder();
                int index = 0;
                for (String t: temp2) {
                    finalny.append(t.trim());
                    if(index < temp2.length-1) finalny.append(" ; ");
                    index ++;
                }
                if (name.getText().toString().trim().length() > 0) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("action", "addUnit");
                        data.put("name", name.getText().toString().trim());
                        data.put("unofficial_name", finalny.toString());
                        data.put("number", number.getText().toString().trim());

                        data.put("token", token);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SendPostTask task = new SendPostTask();
                    task.setActivity(AddUnitActivity.this);
                    task.setResponseListener(new JsonResponseListener() {
                        @Override
                        public void onResponse(final JSONObject obj) {
                            name.setText("");
                            un.setText("");
                            number.setText("");
                            Toast t = Toast.makeText(AddUnitActivity.this, "Dane o jednostce wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                    task.execute(data);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(AddUnitActivity.this, "Jednostka nie może mieć pustej nazwy!", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }
        });
    }
}
