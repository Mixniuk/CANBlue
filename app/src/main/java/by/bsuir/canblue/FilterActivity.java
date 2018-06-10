package by.bsuir.canblue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class FilterActivity extends AppCompatActivity {

	EditText idH, idL, maskH, maskL;
	TextView info;
	private StringBuilder sb = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		info = (TextView) findViewById(R.id.info);
		idH = (EditText) findViewById(R.id.idH);
		idL = (EditText) findViewById(R.id.idL);
		maskH = (EditText) findViewById(R.id.maskH);
		maskL = (EditText) findViewById(R.id.maskL);
	}

	public void setFilter(View view){

		String idHigh = addZeros(idH.getText().toString());
		String idLow = addZeros(idL.getText().toString());
		String maskHigh = addZeros(maskH.getText().toString());
		String maskLow = addZeros(maskL.getText().toString());
		int[] dataFilter = {
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
		intent.putExtra("dataFilter", dataFilter);
		intent.putExtra("address", "other");
		setResult(RESULT_OK, intent);
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

	public void goBack(View view){
		setResult(RESULT_CANCELED);
		finish();
	}

}
