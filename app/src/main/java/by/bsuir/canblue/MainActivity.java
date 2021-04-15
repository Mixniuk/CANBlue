package by.bsuir.canblue;

import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private BluetoothAdapter btAdapter = null;
	private static final int REQUEST_ENABLE_BT = 1;
	private String address = null;
	TextView viewInfo;
	Button buttonFind;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewInfo = (TextView) findViewById(R.id.viewInfo);
		btAdapter = BluetoothAdapter.getDefaultAdapter();       // получаем локальный Bluetooth адаптер

		buttonFind = (Button) findViewById(R.id.buttonFind);

		checkBTState();
	}

	@Override
	public void onResume() {
		super.onResume();
		findDevices();
	}

	public void connectToHC(View view){
		if(address != null){
			Intent intent = new Intent(this, CANMenu.class);
			intent.putExtra("address", address);
			startActivity(intent);
		} else {
			viewInfo.setText("Выбрите адресс!");
		}

	}

	public void findDev(View view){
		findDevices();
	}

	private void findDevices(){
		buttonFind.setEnabled(false);

		ListView deviceList = (ListView)findViewById(R.id.deviceList);
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(
			this,android.R.layout.simple_list_item_1);
		final Set<BluetoothDevice> pairedDevices= btAdapter.getBondedDevices();
		if(pairedDevices.size()>0){
			for(BluetoothDevice device: pairedDevices){
				mArrayAdapter.add(device.getName()+"\n"+ device.getAddress());
			}
			deviceList.setAdapter(mArrayAdapter);
		}

		deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@SuppressLint("SetTextI18n")
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				try {
					address = pairedDevices.toArray()[position].toString();
					viewInfo.setText("Выбран адресс: " + address);
				}catch (Exception e){
					viewInfo.setText("Fatal Error: In onResume() and socket create failed: " + e.getMessage() + ".");
				}
			}
		});

		buttonFind.setEnabled(true);
	}

	@SuppressLint("SetTextI18n")
	private void checkBTState() {
		if(btAdapter==null) {
			viewInfo.setText("Fatal Error: Bluetooth не поддерживается");
		} else {
			if (btAdapter.isEnabled()) {
				viewInfo.setText("Bluetooth включен.");
			} else {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public void exit(View view){
		finish();
	}

}