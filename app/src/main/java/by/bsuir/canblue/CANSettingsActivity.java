package by.bsuir.canblue;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;


public class CANSettingsActivity extends AppCompatActivity {

    TextView info;
    int speed = 16;
    EditText idH, idL, maskH, maskL;
    private final StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_settings);

        info = (TextView) findViewById(R.id.info);
        idH = (EditText) findViewById(R.id.idH);
        idL = (EditText) findViewById(R.id.idL);
        maskH = (EditText) findViewById(R.id.maskH);
        maskL = (EditText) findViewById(R.id.maskL);
    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.veryFast:
                if (checked){
                    speed = 4;
                }
                break;
            case R.id.fast:
                if (checked){
                    speed = 8;
                }
                break;
            case R.id.moderate:
                if (checked){
                    speed = 16;
                }
                break;
            case R.id.low:
                if (checked){
                    speed = 32;
                }
                break;
        }

        sb.append(speed).append(" ");
        info.setText(sb.toString());
    }

    public void sendSettings(View view){
        String idHigh = addZeros(idH.getText().toString());
        String idLow = addZeros(idL.getText().toString());
        String maskHigh = addZeros(maskH.getText().toString());
        String maskLow = addZeros(maskL.getText().toString());
        int[] dataCAN = {
                speed,
                Integer.parseInt(idHigh.substring(0, 2), 16),
                Integer.parseInt(idHigh.substring(2, 4), 16),
                Integer.parseInt(maskHigh.substring(0, 2), 16),
                Integer.parseInt(maskHigh.substring(2, 4), 16),
                Integer.parseInt(idLow.substring(0, 2), 16),
                Integer.parseInt(idLow.substring(2, 4), 16),
                Integer.parseInt(maskLow.substring(0, 2), 16),
                Integer.parseInt(maskLow.substring(2, 4), 16)
        };

        Intent intent = new Intent(this, CANMenu.class);
        intent.putExtra("dataCAN", dataCAN);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void goBack(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    private String addZeros(String str){
        if(str.length() < 4){
            sb.delete(0, sb.length());
            sb.append(str);
            while(sb.length() < 4){
                sb.insert(0,"0");
            }
            return sb.toString();
        } else {
            return str;
        }
    }

}
