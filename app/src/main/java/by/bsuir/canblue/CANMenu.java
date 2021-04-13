package by.bsuir.canblue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CANMenu extends AppCompatActivity {

	TextView viewInfo, receiveInfo, testView;
	Handler h;

	final int BASE_MESSAGE = 0;
//	final int SPEED_MESSAGE = 1;
	final int CAN_SETTINGS_MESSAGE = 2;        // Статус для Handler
	final int SEND_MESSAGE = 3;

	private int mode;
	private ArrayList<Integer> data = new ArrayList<>();
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private StringBuilder sb = new StringBuilder();
	private StringBuilder testsb = new StringBuilder();
	private ConnectedThread mConnectedThread;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_canmenu);

		testView = (TextView) findViewById(R.id.testView);
	  	viewInfo = (TextView) findViewById(R.id.viewInfo);
		receiveInfo = (TextView) findViewById(R.id.receiveInfo);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
					case BASE_MESSAGE:
						baseMode();
						break;
					case CAN_SETTINGS_MESSAGE:
						goSetCANSettings();
						break;
					case SEND_MESSAGE:
						goSendPackage();
						break;
				}
			};
		};
		connectToHC();
		new ReceiverThread().start();
	}

	@Override
	protected void onResume(){
		super.onResume();
		mode = 0;
	}

	@Override
	protected void onPause(){
		super.onPause();
		mode = 4;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==SEND_MESSAGE){
			if(resultCode==RESULT_OK){
				mConnectedThread.write(193);
				sendData(data.getIntArrayExtra("dataPackage"));
			}
			else{
				viewInfo.setText("Ошибка onActivityResult 1");
			}
			mode = 0;

		}else if(requestCode==CAN_SETTINGS_MESSAGE){
			if(resultCode==RESULT_OK){
				mConnectedThread.write(196);
				sendData(data.getIntArrayExtra("dataCAN"));
			}
			else{
				viewInfo.setText("Ошибка onActivityResult 2");
			}
			mode = 0;
		}else{
			super.onActivityResult(requestCode, resultCode , data);
		}
	}

	private void goSetCANSettings(){
		try{
			pauseReceiver();
			Intent intent = new Intent(this, CANSettingsActivity.class);
			startActivityForResult(intent, CAN_SETTINGS_MESSAGE);
		}catch(Exception e){
			testsb.append(e.getMessage() + "\n");
			testView.setText(testsb.toString());
		}

	}

	private void goSendPackage(){
		pauseReceiver();
		Intent intent = new Intent(this, SendPackageActivity.class);
		startActivityForResult(intent, SEND_MESSAGE);
	}

	private void sendData(int[] data){
		sb.append(viewInfo.getText());
		try {
			for(int i = 0; i < data.length; i++){
				mConnectedThread.write(data[i]);
				sb.append(data[i]).append("\n");
			}
			viewInfo.setText(sb.toString());
		}catch (Exception e){
			sb.append("Fatal Error: ").append(e.getMessage()).append(".\n");
			viewInfo.setText(sb.toString());
		}
		sb.delete(0, sb.length());
	}

	private void pauseReceiver(){
		mode = 4;
	}

	public void clearText(View view){
		viewInfo.setText("");
		receiveInfo.setText("");
	}

	public void setCANSettings(View view){
		mode = 2;
	}

	public void sendPackageOn(View view){
		mode = 3;
	}

	public void disconnect(View view){
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		mConnectedThread.cancel();
		startActivity(intent);
		finish();
	}

	public void connectToHC(){
		Bundle arguments = getIntent().getExtras();
		String address = arguments.getString("address");
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			viewInfo.setText("Error: connectToHC 1: " + e.getMessage() + ".");
		}
		btAdapter.cancelDiscovery();

		try {
			btSocket.connect();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				viewInfo.setText("Error: connectToHC 2: " + e.getMessage() + ".");
			}
		}
		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();
		Toast.makeText(getBaseContext(), "connected start", Toast.LENGTH_LONG).show();
	}

	private void ReceiveBytes(int am_b){
		int cur_b = 0;
		while(cur_b < am_b){

			mConnectedThread.write(195);

			while(data.size() < 2)viewInfo.setText("Ожидание приема. Осталось " + am_b + " пакетов.");
			int byte1 = data.remove(0);
			int byte2 = data.remove(0);
			int length = byte2&15;

			short[] bytes = new short[8];
			while(data.size() < length)viewInfo.setText("Ожидание приема. Осталось " + am_b + " пакетов.");
			for(int i = 0; i < length; i++){
				bytes[i] = data.remove(0).shortValue();
			}
			byte1 <<=3;
			byte1 |= byte2>>5;
			sb.append("Id = ").append(Integer.toHexString(byte1)).append("\n");
			sb.append("DLC = ").append(length).append("\n");
			for(int i = 0; i<length;i++){
				sb.append(i).append(". ").append(Integer.toHexString(bytes[i])).append("\n");
			}
			viewInfo.setText("Прием " + am_b + "-го пакета окончен.");
			sb.append("Прием окончен\n\n");
			sb.append(receiveInfo.getText()).append("\n");
			receiveInfo.setText(sb.toString());
			sb.delete(0, sb.length());

			cur_b++;
		}



		mode = 0;

	}

	private void baseMode(){
		int amount_bytes = getAmountByte();

		if(amount_bytes > 0){
			viewInfo.setText("Пакетов: " + amount_bytes + "; Начат прием.");
			pauseReceiver();
			try{
				ReceiveBytes(amount_bytes);
			}catch (Exception e){
				testView.setText(e.getMessage().toString());
			}

		} else if(amount_bytes == 0){
			viewInfo.setText("Нет пакетов");
		} else {
			viewInfo.setText("Нет ответа устройства");
		}
		mode = 0;
	}

	private int getAmountByte(){
		mConnectedThread.write(194);
		mode = 4;

		Integer rem;

		try {
			long startTime = System.currentTimeMillis();
			long elapsedTime = 0;

			while (elapsedTime < 1000) {
				elapsedTime = (new Date()).getTime() - startTime;
				if (data.size() > 0) {
					rem = data.remove(0);
					if (rem == null) return 0;
					else return rem;
				}
			}
			return  -1;
		}catch (Exception e){
			testView.setText("Error: getAmountByte 1: " + e.getMessage() + ".");
			return -1;
		}
	}

	private void errorMessage(Exception e){
		viewInfo.setText("Error: errorMessage 1: " + e.getMessage() + ".");
	}

	private class ReceiverThread extends Thread{

		public void run() {
			try{
				while(true){
					switch (mode) {
						case 0:
							h.obtainMessage(BASE_MESSAGE).sendToTarget();
							break;
						case 1:

							break;
						case 2:
							h.obtainMessage(CAN_SETTINGS_MESSAGE).sendToTarget();
							break;
						case 3:
							h.obtainMessage(SEND_MESSAGE).sendToTarget();
							break;
						default:
					}
					SystemClock.sleep(1000); //pause;
					if(mode == 5){
						break;
					}
				}
			}catch (Exception e){
				errorMessage(e);
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
//			byte[] buffer = new byte[256];
//			int bytes;
			while (true) {
				try {
					while(mmInStream.available()>0){
//						bytes = mmInStream.read();
//						if(bytes != 160) testsb.append(bytes + "\n");
						data.add(mmInStream.read());
						SystemClock.sleep(1);
					}
//					bytes = mmInStream.read(buffer);
//					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		public void write(int message) {
			try {
				mmOutStream.write(message);
			} catch (IOException e) {
				errorMessage(e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}

	}

}
