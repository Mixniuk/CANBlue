package by.bsuir.canblue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

public class SpeedActivity extends AppCompatActivity {

	TextView info;
	int speed = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed);
		info = (TextView) findViewById(R.id.info);
	}

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
			case R.id.moderate:
				if (checked){
					speed = 16;
				}
			case R.id.low:
				if (checked){
					speed = 32;
				}
				break;
		}
	}

	public void sendSpeed(View view){
		int[] dataSpeed = {speed};
		Intent intent = new Intent(this, CANMenu.class);
		intent.putExtra("dataSpeed", dataSpeed);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void goBack(View view){
		setResult(RESULT_CANCELED);
		finish();
	}

}
