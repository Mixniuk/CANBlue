package by.bsuir.canblue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class SendPackageActivity extends AppCompatActivity {

	EditText id, dlc, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8;
  TextView info;
	private StringBuilder sb = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_package);
		info = (TextView) findViewById(R.id.info);
		id = (EditText) findViewById(R.id.id);
		dlc = (EditText) findViewById(R.id.dlc);
		byte1 = (EditText) findViewById(R.id.byte1);
		byte2 = (EditText) findViewById(R.id.byte2);
		byte3 = (EditText) findViewById(R.id.byte3);
		byte4 = (EditText) findViewById(R.id.byte4);
		byte5 = (EditText) findViewById(R.id.byte5);
		byte6 = (EditText) findViewById(R.id.byte6);
		byte7 = (EditText) findViewById(R.id.byte7);
		byte8 = (EditText) findViewById(R.id.byte8);
	}

	public void sendPackage(View view){

		try {
			int data = Integer.parseInt(id.getText().toString(),16);
			String s = byte1.getText().toString();
			info.setText(s);

			int[] bytes = {
				Integer.parseInt(addZeros(byte1.getText().toString()), 16),
				Integer.parseInt(addZeros(byte2.getText().toString()), 16),
				Integer.parseInt(addZeros(byte3.getText().toString()), 16),
				Integer.parseInt(addZeros(byte4.getText().toString()), 16),
				Integer.parseInt(addZeros(byte5.getText().toString()), 16),
				Integer.parseInt(addZeros(byte6.getText().toString()), 16),
				Integer.parseInt(addZeros(byte7.getText().toString()), 16),
				Integer.parseInt(addZeros(byte8.getText().toString()), 16)
			};

			data <<= 5;
			data |= Integer.parseInt(dlc.getText().toString());
			int d1 = (data & 65280) >> 8;
			int d2 = data & 255;
			int amount = data & 15;
			int[] dataPackage = new int[amount + 2];
			dataPackage[0] = d1;
			dataPackage[1] = d2;
			System.arraycopy(bytes, 0, dataPackage, 2, amount);

			Intent intent = new Intent(this, CANMenu.class);
			intent.putExtra("dataPackage", dataPackage);
			setResult(RESULT_OK, intent);
			finish();

		}catch (Exception e){
			info.setText(info.getText() + "Fatal Error: " + e.getMessage() + ".");
		}
	}

	private String addZeros(String str){
		if(str.length() < 2){
			sb.delete(0, sb.length());
			sb.append(str);
			while(sb.length() < 2){
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
