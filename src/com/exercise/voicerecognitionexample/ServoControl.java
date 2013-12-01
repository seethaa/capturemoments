package com.exercise.voicerecognitionexample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ServoControl extends Activity {

	private static final String TAG = "CM";

	private TextView result; 
	private String dataToSend;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;

	private OutputStream outStream = null;
	private static String address = "00:06:66:03:AA:46";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final int REQUEST_ENABLE_BT = 1;

	private InputStream inStream = null;
	Handler handler = new Handler(); 
	byte delimiter = 10;
	boolean stopWorker = false;
	int readBufferPosition = 0;
	byte[] readBuffer = new byte[1024]; 

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.servo_control_layout);

		result = (TextView) findViewById(R.id.msgJonduino);


		//Check if bluetooth is available
		CheckBt();

		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		Log.e("BT address", device.toString());

		//pair the bluetooth
		connect();

		//start service to detect laughter --currently "haha"
		//startHahaService();

		startFaceDetectionService();


	}

	private void startFaceDetectionService() {

//		if(btSocket.isConnected()){	
			toggleServo("continue");
//		}
		Intent intent = new Intent(this, AndroidCamera.class);
//		startActivity(intent);
		startActivityForResult(intent, 1);

	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if(resultCode == RESULT_OK){      
				String result=data.getStringExtra("result");  
				toggleServo(result);
				//start service again..?
//				startFaceDetectionService();
			}
			if (resultCode == RESULT_CANCELED) {    
				//Write your code if there's no result
			}
		}
	}//onActivityResult

	public void toggleServo(String data) {
		if(btSocket.isConnected()){	

			if (data.equalsIgnoreCase("pause")){
				dataToSend = "0";
				writeData(dataToSend);
			}
			else if (data.equalsIgnoreCase("continue")){
				dataToSend = "1";
				writeData(dataToSend);
			}
			//			if (OnOff.isChecked()) {
			//				dataToSend = "0";
			//				writeData(dataToSend);
			//			} else if (!OnOff.isChecked()) {
			//				dataToSend = "1";
			//				writeData(dataToSend);
			//			}
		}

	}



	private void disconnect() {
		try {
			if(btSocket!= null)
				btSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "";
		if(btSocket!= null && btSocket.isConnected()){
			str = "Connected";
		}
		else{
			str = "DisConnected";
		}

		Toast.makeText(getApplicationContext(), "Bluetooth Status " + str,
				Toast.LENGTH_SHORT).show();

	}

	public void connect() {

		Log.d(TAG, address);

		for (int i = 0; i < 3; i++) {
			if(btSocket != null && btSocket.isConnected() )
				return;
			CheckBt();

			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			Log.d(TAG, "Connecting to ... " + device);
			mBluetoothAdapter.cancelDiscovery();
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
				btSocket.connect();
				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Connection made.");
				beginListenForData();
				break;
			} catch (IOException e) {
				try {
					btSocket.close();
					Toast.makeText(getApplicationContext(), "Could Not connect ... Trying Again",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e2) {
					Log.d(TAG, "Unable to end the connection");
				}
				Log.d(TAG, "Socket creation failed");
			}

		}

	}

	private void writeData(String data) {
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d(TAG, "Bug BEFORE Sending stuff", e);
		}

		String message = data;
		byte[] msgBuffer = message.getBytes();

		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			Log.d(TAG, "Bug while sending stuff", e);
		}
	}

	private void CheckBt() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
					Toast.LENGTH_SHORT).show();
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth null !", Toast.LENGTH_SHORT)
					.show();
		}
	}


	public void beginListenForData()   {
		try {
			inStream = btSocket.getInputStream();
		} catch (IOException e) {
		}

		Thread workerThread = new Thread(new Runnable()
		{
			public void run()
			{                
				while(!Thread.currentThread().isInterrupted() && !stopWorker)
				{
					try 
					{
						int bytesAvailable = inStream.available();                        
						if(bytesAvailable > 0)
						{
							byte[] packetBytes = new byte[bytesAvailable];
							inStream.read(packetBytes);
							for(int i=0;i<bytesAvailable;i++)
							{
								byte b = packetBytes[i];
								if(b == delimiter)
								{
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
									final String data = new String(encodedBytes, "US-ASCII");
									readBufferPosition = 0;
									handler.post(new Runnable()
									{
										public void run()
										{

											if(result.getText().toString().equals("..")) {
												result.setText(data);
											} else {
												result.append("\n"+data);
											}

											/* You also can use Result.setText(data); it won't display multilines
											 */

										}
									});
								}
								else
								{
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} 
					catch (IOException ex) 
					{
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}

}
