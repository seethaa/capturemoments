package com.exercise.voicerecognitionexample;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidCamera extends Activity implements SurfaceHolder.Callback {

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;

	Button buttonTakePicture;
	TextView prompt;

//	ServoControl sc;

	private static final String TAG = "CM";

	final int RESULT_SAVEIMAGE = 0;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		
		CheckBt();

		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		Log.e("BT address", device.toString());

		// pair the bluetooth
		connect();

		//start moving
		toggleServo("continue");
		
//		sc = new ServoControl();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		controlInflater = LayoutInflater.from(getBaseContext());
		View viewControl = controlInflater.inflate(R.layout.control, null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);

		buttonTakePicture = (Button) findViewById(R.id.viewPicsButton);

		buttonTakePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// camera.takePicture(myShutterCallback,
				// myPictureCallback_RAW, myPictureCallback_JPG);
		
				Toast.makeText(getApplicationContext(), "Flash!",
						Toast.LENGTH_SHORT).show();
				
				System.out.println("taking picture..");
				
				Intent intent = new Intent(AndroidCamera.this, MainActivity.class);
		        startActivity(intent);
			}
		});

		LinearLayout layoutBackground = (LinearLayout) findViewById(R.id.background);
		layoutBackground.setOnClickListener(new LinearLayout.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

//				buttonTakePicture.setEnabled(false);
				camera.autoFocus(myAutoFocusCallback);
			}
		});

		prompt = (TextView) findViewById(R.id.prompt);
	}

	FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {

			if (faces.length == 0) {
				prompt.setText(" No Face Detected! ");
				toggleServo("continue");
			} else {
				prompt.setText(String.valueOf(faces.length)
						+ " Face Detected :) ");
				// if face detected, take pic
				// camera.takePicture(myShutterCallback,
				// myPictureCallback_RAW, myPictureCallback_JPG);

				toggleServo("pause");

//				Intent returnIntent = new Intent();
//				returnIntent.putExtra("result", "pause");
//				setResult(RESULT_OK, returnIntent);
//				finish();
 
				camera.takePicture(myShutterCallback, myPictureCallback_RAW,
						myPictureCallback_JPG);
				PictureCallback rawCallback = new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						System.out.println("onPictureTaken - raw");
					}
				};

			}

		}
	};

	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			// TODO Auto-generated method stub
			buttonTakePicture.setEnabled(true);
		}
	};

	ShutterCallback myShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub

		}
	};

	PictureCallback myPictureCallback_RAW = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub

		}
	};

	PictureCallback myPictureCallback_JPG = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub
			/*
			 * Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0,
			 * arg0.length);
			 */

			Uri uriTarget = getContentResolver().insert(
					Media.EXTERNAL_CONTENT_URI, new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(arg0);
				imageFileOS.flush();
				imageFileOS.close();

				prompt.setText("Image saved: " + uriTarget.toString());

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			camera.startPreview();
			camera.startFaceDetection();
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if (previewing) {
			camera.stopFaceDetection();
			camera.stopPreview();
			previewing = false;
		}

		if (camera != null) {
			try {
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();

				prompt.setText(String.valueOf("Max Face: "
						+ camera.getParameters().getMaxNumDetectedFaces()));
				camera.startFaceDetection();
				previewing = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera = Camera.open();
		camera.setFaceDetectionListener(faceDetectionListener);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.stopFaceDetection();
		camera.stopPreview();
		camera.release();
		camera = null;
		previewing = false;
	}

	public void onPicView(final View view){
		Toast.makeText(getApplicationContext(), "Flash!",
				Toast.LENGTH_SHORT).show();
		
		System.out.println("taking picture..");
	}
	public void toggleServo(String data) {
		if (btSocket.isConnected()) {

			if (data.equalsIgnoreCase("pause")) {
				dataToSend = "0";
				writeData(dataToSend);
			} else if (data.equalsIgnoreCase("continue")) {
				dataToSend = "1";
				writeData(dataToSend);
			}
			// if (OnOff.isChecked()) {
			// dataToSend = "0";
			// writeData(dataToSend);
			// } else if (!OnOff.isChecked()) {
			// dataToSend = "1";
			// writeData(dataToSend);
			// }
		}

	}

	private void disconnect() {
		try {
			if (btSocket != null)
				btSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "";
		if (btSocket != null && btSocket.isConnected()) {
			str = "Connected";
		} else {
			str = "DisConnected";
		}

		Toast.makeText(getApplicationContext(), "Bluetooth Status " + str,
				Toast.LENGTH_SHORT).show();

	}

	public void connect() {

		Log.d(TAG, address);

		for (int i = 0; i < 3; i++) {
			if (btSocket != null && btSocket.isConnected())
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
					Toast.makeText(getApplicationContext(),
							"Could Not connect ... Trying Again",
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
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth null !",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void beginListenForData() {
		try {
			inStream = btSocket.getInputStream();
		} catch (IOException e) {
		}

		Thread workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = inStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							inStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;
									handler.post(new Runnable() {
										public void run() {

											// if(result.getText().toString().equals(".."))
											// {
											// result.setText(data);
											// } else {
											// result.append("\n"+data);
											// }

											/*
											 * You also can use
											 * Result.setText(data); it won't
											 * display multilines
											 */

										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}
}