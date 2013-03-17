package com.example.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket mBluetoothSocket = null;
	private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private final int REQUEST_ENABLE_BT = 1;
	private LinkedHashSet<String> bluetoothDevicesName;
	private Set<BluetoothDevice> bluetoothDevices;
	private ListView mBluetoothList;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		checkBluetooth();

		// 取得目前已經配對過的裝置
		bluetoothDevices = mBluetoothAdapter.getBondedDevices();
		bluetoothDevicesName = new LinkedHashSet<String>();
		// 如果已經有配對過的裝置
		if (bluetoothDevices.size() > 0) {
			// 把裝置名稱以及MAC Address印出來
			for (BluetoothDevice device : bluetoothDevices) {
				bluetoothDevicesName.add("[已配對]" + "\n" + device.getName() + "\n" + device.getAddress());
			}
		}

		// 註冊一個BroadcastReceiver，等等會用來接收搜尋到裝置的消息
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery(); // 開始搜尋裝置

		mBluetoothList.setAdapter(new BtAdapter(this, bluetoothDevicesName));
		mBluetoothList.setOnItemClickListener(listListener);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// 當收尋到裝置時
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				// 取得藍芽裝置這個物件
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				bluetoothDevicesName.add("[搜尋到]" + device.getName() + "\n" + device.getAddress());
			}
			mBluetoothList.setAdapter(new BtAdapter(MainActivity.this, bluetoothDevicesName));
		}
	};

	private void checkBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 裝置不支援藍芽
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "本裝置不支援藍芽", Toast.LENGTH_SHORT).show();
			return;
		}

		// 藍芽沒有開啟
		if (!mBluetoothAdapter.isEnabled()) {
			Intent mIntentOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(mIntentOpenBT, REQUEST_ENABLE_BT);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (!(resultCode == RESULT_OK)) {
				Toast.makeText(this, "不開啟藍芽無法使用，因此關閉程式", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	private void findViews() {
		mBluetoothList = (ListView) findViewById(R.id.Devices_list);
	}

	OnItemClickListener listListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// 註冊一個BroadcastReceiver，等等會用來接收搜尋到裝置的消息
			mBluetoothAdapter.cancelDiscovery();
			Object[] deviceses = bluetoothDevices.toArray();
			BluetoothDevice mBluetoothDevice = (BluetoothDevice) deviceses[0];
			Toast.makeText(MainActivity.this, mBluetoothDevice.getName(), 0).show();
			try {
				mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
				InputStream input;
				mBluetoothSocket.connect();
				input = mBluetoothSocket.getInputStream();
				byte[] b = new byte[1024];
				int tmp = input.read(b);
				Toast.makeText(MainActivity.this, new String(b, 0, tmp - 1), 0).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

}
