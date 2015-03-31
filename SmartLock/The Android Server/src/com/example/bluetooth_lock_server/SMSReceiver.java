package com.example.bluetooth_lock_server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Bundle bundle=arg1.getExtras();
		SmsMessage[] messages=null;
		String str="";
		if(bundle!=null)
		{
			Object[] pdus =(Object[])bundle.get("pdus");
			messages = new SmsMessage[pdus.length];
			for(int i=0;i<messages.length;i++)
			{
				messages[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
				str += messages[i].getMessageBody().toString();
			
			}
			Toast.makeText(arg0, str, Toast.LENGTH_LONG).show();
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction("SMS_RECEIVED_ACTION");
			broadcastIntent.putExtra("sms", str);
			arg0.sendBroadcast(broadcastIntent);
			
		}
	}

}
