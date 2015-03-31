package com.example.bluetooth_lock_server;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;



import android.R.id;
import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	PendingIntent mPermissionIntent;
	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	public String customerPhoneNumber;
	public String messageReceived;
	public EditText reMsg;
	public Button SendBtn;
	public EditText Text_to_send;
	IntentFilter intentFilter;
	// SMS RECEIVER
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent arg1) {
			TextView inText = (TextView) findViewById(R.id.ReMsg);
			String Str = arg1.getExtras().getString("sms");
			if (Str == "1" || Str == "2" || Str == "3") {
				if (Str == "1") {
					inText.setText("Send command to Rashad to Open the 1st door");
					writeToUSB(Str);
					showInfo();
				} else if (Str == "2") {
					inText.setText("Send command to Rashad to Open the Garage door");
					writeToUSB(Str);
					showInfo();
				} else if (Str == "3") {
					inText.setText("Send command to Rashad to close the Garage door");
					writeToUSB(Str);
					showInfo();
				}
			} else {
				inText.setText(Str);
				writeToUSB(Str);
				showInfo();
			}
		}
	};

	// USB SERIAL
	public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							// call method to set up device communication
							Toast.makeText(getApplicationContext(),
									"Permission Granted", Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"permission denied for device " + device,
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {

			public void onCallStateChanged(int state, String incomingNumber) {
				if (TelephonyManager.CALL_STATE_RINGING == state) {
					new Thread(new Runnable() {
						public void run() {
								writeToUSB("7");
						}
					}).start();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		//WIFI
		socText=(TextView)findViewById(R.id.textView1);

	}

	public void messageReceviedSetter(String STR) {
		messageReceived = STR;
	}

	protected void sendSMS(String Num, String Txt) {
		String SENT = "Message Sent";
		String DELIVERED = "Message Delivered";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(MainActivity.this, "SMS Sent",
							Toast.LENGTH_LONG).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(MainActivity.this,
							"Generic Failure When trying to Send an SMS",
							Toast.LENGTH_LONG).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(MainActivity.this, "No GSM Service",
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(MainActivity.this, "SMS Delivered",
							Toast.LENGTH_LONG).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(MainActivity.this, "SMS not Delivered",
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(Num, null, Txt, sentPI, deliveredPI);

	}

	@Override
	protected void onResume() {
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(intentReceiver);
		super.onPause();
	}

	public void writeToUSB(String commandToSerial) {
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		try {
			while (deviceIterator.hasNext()) {
				UsbDevice device1 = deviceIterator.next();
				byte[] bytes = commandToSerial.getBytes();
				int TIMEOUT = 0;
				boolean forceClaim = true;
				UsbInterface intf = device1.getInterface(0);
				UsbEndpoint endpoint = intf.getEndpoint(1);
				UsbDeviceConnection connection = manager.openDevice(device1);
				connection.claimInterface(intf, forceClaim);
				connection.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);// baudrate
																				// 9600
				
				//connection.controlTransfer(0x00000080, 0x03, 0x4138, 0, null, 0, 0);//for input
				
				// connection.controlTransfer(requestType, request, value,
				// index, buffer, length, timeout)
				connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
				connection.releaseInterface(device1.getInterface(0));
			}
		} catch (Exception e) {
			// ((TextView)findViewById(R.id.txtView1)).setText(e.getMessage());
			Toast.makeText(getApplicationContext(),
					"EXCEPTION" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void writeToUSBfrombtn(View v) {
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		// UsbDevice device = deviceList.get("deviceName");
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

		// It appears that it cannot detect the USB port of my PC.

		try {
			while (deviceIterator.hasNext()) {
				UsbDevice device1 = deviceIterator.next();

				// your code
				// manager.requestPermission(device1, mPermissionIntent);
				// byte[] bytes ={65,10,20,30,40,55};
				byte[] bytes = ((EditText) findViewById(R.id.editText1))
						.getText().toString().getBytes();
				int TIMEOUT = 0;
				boolean forceClaim = true;

				UsbInterface intf = device1.getInterface(0);
				UsbEndpoint endpoint = intf.getEndpoint(1);
				UsbDeviceConnection connection = manager.openDevice(device1);
				connection.claimInterface(intf, forceClaim);
				connection.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);// baudrate
																				// 9600
				// connection.controlTransfer(requestType, request, value,
				// index, buffer, length, timeout)
				connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
				connection.releaseInterface(device1.getInterface(0));

			}
		} catch (Exception e) {
			// ((TextView)findViewById(R.id.txtView1)).setText(e.getMessage());
			Toast.makeText(getApplicationContext(),
					"EXCEPTION" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	public void showInfofrombtn(View view) {
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		// UsbDevice device = deviceList.get("deviceName");
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

		// It appears that it cannot detect the USB port of my PC.

		try {
			while (deviceIterator.hasNext()) {
				UsbDevice device1 = deviceIterator.next();
				Toast.makeText(getApplicationContext(),
						device1.getDeviceName(), Toast.LENGTH_SHORT).show();
				Toast.makeText(
						getApplicationContext(),
						Integer.toString(device1.getInterfaceCount())
								+ " Interfaces", Toast.LENGTH_SHORT).show();
				Toast.makeText(
						getApplicationContext(),
						Integer.toString(device1.getInterface(0)
								.getEndpointCount()) + " End-Points",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// ((TextView)findViewById(R.id.txtView1)).setText(e.getMessage());
			Toast.makeText(getApplicationContext(),
					"EXCEPTION" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showInfo() {
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		try {
			while (deviceIterator.hasNext()) {
				UsbDevice device1 = deviceIterator.next();
				Toast.makeText(getApplicationContext(),
						device1.getDeviceName(), Toast.LENGTH_SHORT).show();
				Toast.makeText(
						getApplicationContext(),
						Integer.toString(device1.getInterfaceCount())
								+ " Interfaces", Toast.LENGTH_SHORT).show();
				Toast.makeText(
						getApplicationContext(),
						Integer.toString(device1.getInterface(0)
								.getEndpointCount()) + " End-Points",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"EXCEPTION" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	
	}
	//WIFI socket
	public TextView socText; 
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			messageDisplay(msg.obj.toString());
		}
	};
	public void messageDisplay(String servermessage)
	{
		socText.setText(""+servermessage);
		writeToUSB(servermessage);
	}
	public void StartSocket(View view)
	{
		Thread m_objThread;
		DataDisplay m_dataDisplay;
		Object m_connected;
		Toast.makeText(this, "Android Socket Started", Toast.LENGTH_LONG);
		
		m_objThread=new Thread(new Runnable() {
			 public void run()
		       {
					 try {		 
						 ServerSocket m_server;
						 String m_strMessage;
						 m_server=new ServerSocket(2001);
					     Socket connectedSocket =m_server.accept();
						 Message clientmessage=Message.obtain();					 
					     ObjectInputStream ois =new ObjectInputStream(connectedSocket.getInputStream());
					     String strMessage=(String)ois.readObject();
					     clientmessage.obj=strMessage;  
					     mHandler.sendMessage(clientmessage);
					     ObjectOutputStream oos =new ObjectOutputStream(connectedSocket.getOutputStream());
					     oos.writeObject("Done!");
					     ois.close();
					     oos.close();
					     m_server.close();
					     run();
					      } 
					 catch (Exception e) 
					 {
						 Message msg3= Message.obtain();
						 msg3.obj=e.getMessage();
						 //Toast.makeText(this, msg3, Toast.LENGTH_SHORT); 
					}
		         }
				});
			 
			 m_objThread.start();
	}
	public void StartWinSocket(View view)
	{
		Thread W_objThread;
		DataDisplay m_dataDisplay;
		Object m_connected;
		Toast.makeText(this, ".Net Socket Started", Toast.LENGTH_LONG);
		
		W_objThread=new Thread(new Runnable() {
			 public void run()
		       {
					 try {		 
						 ServerSocket m_server;
						 String m_strMessage;
						 m_server=new ServerSocket(2002,10);
					     Socket connectedSocket =m_server.accept();
					     InputStream is = connectedSocket.getInputStream();
					     
					     byte[] lenBytes = new byte[4];
					        is.read(lenBytes, 0, 4);
					        int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
					                  ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
					        byte[] receivedBytes = new byte[len];
					        is.read(receivedBytes, 0, len);
					        String received = new String(receivedBytes, 0, len);
					        Message clientmessage=Message.obtain();
					      clientmessage.obj=received;
					        mHandler.sendMessage(clientmessage);

					     
					     
						 //Message clientmessage=Message.obtain();					 
					     //ObjectInputStream ois =new ObjectInputStream(connectedSocket.getInputStream());
					     //String strMessage=(String)ois.readObject();
					     //clientmessage.obj=strMessage;  
					     //mHandler.sendMessage(clientmessage);
					     //ObjectOutputStream oos =new ObjectOutputStream(connectedSocket.getOutputStream());
					     //oos.writeObject("Done!");
					     //ois.close();
					     //oos.close();
					     m_server.close();
					     run();
					      } 
					 catch (Exception e) 
					 {
						 Message msg3= Message.obtain();
						 msg3.obj=e.getMessage();
						 //Toast.makeText(this, msg3, Toast.LENGTH_SHORT); 
					}
		         }
				});
			 
			 W_objThread.start();
	}
	
	
	
	
	
	
	
	
	
	
}
