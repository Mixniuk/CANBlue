package by.bsuir.canblue;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private BluetoothAdapter btAdapter = null;
	private static final int REQUEST_ENABLE_BT = 1;
	private String address = "98:D3:61:F5:BE:B4";
	TextView viewInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewInfo = (TextView) findViewById(R.id.viewInfo);
		btAdapter = BluetoothAdapter.getDefaultAdapter();       // получаем локальный Bluetooth адаптер
		checkBTState();
	}

	@Override
	public void onResume() {
		super.onResume();
		findDevices();
	}

	public void connectToHC(View view){
		Intent intent = new Intent(this, CANMenu.class);
		intent.putExtra("address", address);
		startActivity(intent);
	}

	private void findDevices(){
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

	}

	private void checkBTState() {
		if(btAdapter==null) {
			viewInfo.setText("Fatal Error: Bluetooth не поддерживается");
		} else {
			if (btAdapter.isEnabled()) {
				viewInfo.setText("Bluetooth включен.");
			} else {
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public void exit(View view){
		finish();
	}

}