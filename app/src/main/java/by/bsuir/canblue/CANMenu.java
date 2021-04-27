package by.bsuir.canblue;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class CANMenu extends AppCompatActivity {

	TextView viewInfo, receiveInfo, errorView, addressInfo, connectInfo, settingsInfo, packsView;
	Handler h;
	Button buttonClear, buttonSend, buttonSettings, buttonConnect, buttonDisconnect, buttonExit;

	static final int BASE_MESSAGE = 0;
//	static final int SPEED_MESSAGE = 1;
	static final int CAN_SETTINGS_MESSAGE = 2;        // Статус для Handler
	static final int SEND_MESSAGE = 3;

	private int speed = 250;
	private String idH = "aaaa";
	private String idL = "aaaa";
	private String maskH = "0000";
	private String maskL = "0000";

	private int mode = 4;
	private int countPacks = 0;
	private String address = null;
	private final ArrayList<Integer> data = new ArrayList<>();
	private BluetoothAdapter btAdapter = null;
	private final StringBuilder sb = new StringBuilder();
	private final StringBuilder testsb = new StringBuilder();
	private ConnectedThread mConnectedThread;
	private ReceiverThread mReceiverThread;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	static class CANMenuHandler extends Handler {

		WeakReference<CANMenu> wrActivity;

		public CANMenuHandler(CANMenu activity) {
			wrActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			CANMenu activity = wrActivity.get();
			if (activity != null){
				switch (msg.what) {
					case BASE_MESSAGE:
						activity.baseMode();
						break;
					case CAN_SETTINGS_MESSAGE:
						activity.goSetCANSettings();
						break;
					case SEND_MESSAGE:
						activity.goSendPackage();
						break;
				}
			}
		}
	}


	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_canmenu);

		buttonClear = (Button) findViewById(R.id.buttonClear);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonSettings = (Button) findViewById(R.id.buttonSettings);
		buttonConnect = (Button) findViewById(R.id.buttonConnect);
		buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
		buttonExit = (Button) findViewById(R.id.buttonExit);

		errorView = (TextView) findViewById(R.id.errorView);
		packsView = (TextView) findViewById(R.id.packsView);
	  	viewInfo = (TextView) findViewById(R.id.viewInfo);
		settingsInfo = (TextView) findViewById(R.id.settingsInfo);
		receiveInfo = (TextView) findViewById(R.id.receiveInfo);
		addressInfo = (TextView) findViewById(R.id.addressInfo);
		connectInfo = (TextView) findViewById(R.id.connectInfo);
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		Bundle arguments = getIntent().getExtras();
		assert arguments != null;
		address = arguments.getString("address");
		addressInfo.setText("address: " + address);

		h = new CANMenuHandler(this);

		buttonSend.setEnabled(false);
		buttonSettings.setEnabled(false);
//		buttonDisconnect.setEnabled(false);
	}

	@Override
	protected void onResume(){
		super.onResume();
		mode = 0;
	}

	@Override
	protected void onPause(){
		super.onPause();
		pauseReceiver();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==SEND_MESSAGE){
			if(resultCode==RESULT_OK){
				mConnectedThread.write(193);
				sendData(data.getIntArrayExtra("dataPackage"));
			}
			else{
				viewInfo.setText("Error onActivityResult 1");
			}
			mode = 0;

		}else if(requestCode==CAN_SETTINGS_MESSAGE){
			if(resultCode==RESULT_OK){
				mConnectedThread.write(196);
				int[] dat = data.getIntArrayExtra("dataCAN");
				sendData(dat);
				switch(dat[0]){
					case 4: speed = 1000; break;
					case 8: speed = 500; break;
					case 16: speed = 250; break;
					case 32: speed = 125; break;
					default:
				}

				idH = addZeros(Integer.toHexString(dat[1])) + addZeros(Integer.toHexString(dat[2]));
				maskH = addZeros(Integer.toHexString(dat[3])) + addZeros(Integer.toHexString(dat[4]));
				idL = addZeros(Integer.toHexString(dat[5])) + addZeros(Integer.toHexString(dat[6]));
				maskL = addZeros(Integer.toHexString(dat[7])) + addZeros(Integer.toHexString(dat[8]));

				settingsInfo.setText("baud = " + speed + "; Filter: " + idH + " " + maskH + " " + idL + " " + maskL);
			}
			else if(resultCode==RESULT_CANCELED) {
				viewInfo.setText("Операция отменена");
			}else {
				viewInfo.setText("Error onActivityResult 2");
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
			intent.putExtra("speed", speed);
			intent.putExtra("idH", idH);
			intent.putExtra("maskH", maskH);
			intent.putExtra("idL", idL);
			intent.putExtra("maskL", maskL);
			startActivityForResult(intent, CAN_SETTINGS_MESSAGE);
		}catch(Exception e){
			errorMessage("goSetCANSettings", e);
		}

	}

	private void goSendPackage(){
		pauseReceiver();
		Intent intent = new Intent(this, SendPackageActivity.class);
		startActivityForResult(intent, SEND_MESSAGE);
	}

	private void sendData(int[] data){
		try {
			for (int datum : data) {
				mConnectedThread.write(datum);
			}
		}catch (Exception e){
			sb.append("Fatal Error: ").append(e.getMessage()).append(".\n");
			viewInfo.setText(sb.toString());
		}
		sb.delete(0, sb.length());
	}

	private void pauseReceiver(){mode = 4;}

	private void resumeReceiver(){mode = 0;}

	public void clearText(View view){
		viewInfo.setText("");
		receiveInfo.setText("");
		packsView.setText("Количество принятых пакетов: 0");
		countPacks = 0;
	}

	public void setCANSettings(View view){
		mode = 2;
	}

	public void sendPackageOn(View view){
		mode = 3;
	}

	public void exit(View view){
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		mConnectedThread.cancel();
		mConnectedThread.interrupt();
		mReceiverThread.interrupt();
		startActivity(intent);
		finish();
	}

	@SuppressLint("SetTextI18n")
	public void disconnectHC(View view){
		pauseReceiver();
		mConnectedThread.cancel();
		mConnectedThread.interrupt();
		mReceiverThread.interrupt();

		buttonSend.setEnabled(false);
		buttonSettings.setEnabled(false);
//		buttonDisconnect.setEnabled(false);
//		buttonConnect.setEnabled((true));
		connectInfo.setText("Нет подключения");
	}

	@SuppressLint("SetTextI18n")
	public void connectToHC(View view){
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		BluetoothSocket btSocket;
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			errorMessage("connectToHC 1", e);
			return;
		}
		btAdapter.cancelDiscovery();

		try {
			btSocket.connect();
		} catch (IOException e) {
			errorMessage("connectToHC 2", e);
			viewInfo.setText("Не удалось подключиться, попробуйте еще раз.");
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorMessage("connectToHC 3", e2);
			}
			return;
		}

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();
		Toast.makeText(getBaseContext(), "connected start", Toast.LENGTH_LONG).show();


		mReceiverThread = new ReceiverThread();
		mReceiverThread.start();

		buttonSend.setEnabled(true);
		buttonSettings.setEnabled(true);
//		buttonDisconnect.setEnabled(true);
//		buttonConnect.setEnabled((false));
		connectInfo.setText("Подключено");

		resumeReceiver();
	}

	@SuppressLint("SetTextI18n")
	private void ReceiveBytes(int am_b){
		mConnectedThread.write(195);

		long elapsedTime;
		long startTime = System.currentTimeMillis();
		viewInfo.setText("Ожидание приема. Осталось " + am_b + " пакетов.");
		while (data.size() < 2) {
			elapsedTime = (new Date()).getTime() - startTime;
			if(elapsedTime < 5000) viewInfo.setText("Превышено ожидание приема. 1");
		}

		int byte1 = data.remove(0);
		int byte2 = data.remove(0);
		int length = byte2&15;

		startTime = System.currentTimeMillis();
		while (data.size() < length) {
			elapsedTime = (new Date()).getTime() - startTime;
			if(elapsedTime < 5000) viewInfo.setText("Превышено ожидание приема. 2");
		}

		short[] bytes = new short[8];
		for(int i = 0; i < length; i++){
			bytes[i] = data.remove(0).shortValue();
		}
		byte1 <<=3;
		byte1 |= byte2>>5;
		sb.append("----- Начало пакета-----\n");
		sb.append("Id = ").append(Integer.toHexString(byte1)).append(" ").append("DLC = ").append(length).append("\n");
		for(int i = 0; i < length; i++) sb.append(Integer.toHexString(bytes[i])).append(" ");
		sb.append("\n\n");
		viewInfo.setText("Прием " + am_b + "-го пакета окончен.");
		sb.append(receiveInfo.getText());
		receiveInfo.setText(sb.toString());
		sb.delete(0, sb.length());

		SystemClock.sleep(1);
		packsView.setText("Количество принятых пакетов: " + countPacks++);

		mode = 0;
	}

	@SuppressLint("SetTextI18n")
	private void baseMode(){
		int amount_bytes = getAmountByte();

		if(amount_bytes > 0){
			viewInfo.setText("Пакетов: " + amount_bytes + "; Начат прием.");
			pauseReceiver();
			try{
				ReceiveBytes(amount_bytes);
			}catch (Exception e){
				errorMessage("baseMode", e);
			}

		} else if(amount_bytes == 0){
			viewInfo.setText("Нет пакетов");
		} else {
			viewInfo.setText("Нет ответа устройства");
		}
		mode = 0;
	}

	@SuppressLint("SetTextI18n")
	private int getAmountByte(){
		mConnectedThread.write(194);
		pauseReceiver();

		Integer rem;

		try {
			long startTime = System.currentTimeMillis();
			long elapsedTime = 0;

			while (elapsedTime < 5000) {
				elapsedTime = (new Date()).getTime() - startTime;
				if (data.size() > 0) {
					rem = data.remove(0);
					if (rem != null) return rem;
					else mConnectedThread.write(194);
				}
			}
			return  -1;
		}catch (Exception e){
			errorMessage("getAmountByte", e);
			return -1;
		}
	}

	@SuppressLint("SetTextI18n")
	private void errorMessage(String Label, Exception e){
		testsb.append("Error: ").append(Label).append(": ").append(e.getMessage()).append(".\n");
		errorView.setText(testsb.toString());
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

	private class ReceiverThread extends Thread{

		public void run() {
			try{
				do {
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
				} while (mode != 5);
			}catch (Exception e){
				errorMessage("run", e);
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
			} catch (IOException e) { errorMessage("ConnectedThread", e);}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			while (true) {
				try {
					while(mmInStream.available()>0){
						data.add(mmInStream.read());
						SystemClock.sleep(1);
					}
				} catch (IOException e) {
					break;
				}
			}
		}

		public void write(int message) {
			try {
				mmOutStream.write(message);
			} catch (IOException e) {
				errorMessage("write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { errorMessage("cancel", e); }
		}

	}

}
