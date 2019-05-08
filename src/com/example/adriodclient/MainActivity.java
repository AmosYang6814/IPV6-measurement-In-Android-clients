package com.example.adriodclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Util.BAseStation;
import Util.store;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;

import com.example.net.R;
import com.mysql.jdbc.Messages;

import Core.Core;
import Util.Const;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends InstrumentedActivity {
	public static boolean isForeground = false;
	private String IMEI="";
	private ToggleButton switc;
	
	public TextView log;
	private EditText input;
	private SimpleAdapter adapter;
	private List<HashMap<String, String>> list=new ArrayList<HashMap<String,String>>();
	
	public Handler h=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch (msg.what) {
			case 108:
				log.setText("正在测试");
				break;

			default:
				log.setText("测试完成：时间为："+Util.store.time);
				break;
			}
			
			
			
			
			
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
		setContentView(R.layout.activity_main);
		Util.Const.context=MainActivity.this;
		IMEI=ExampleUtil.getImei(getApplicationContext(),"");
		Util.Const.IMEI=IMEI;    //置入常量中
		Util.Const.getCore().setMAianactivityHandler(h);
		log=(TextView) findViewById(R.id.log);
		switc=(ToggleButton) findViewById(R.id.tglSound);
		switc.setOnCheckedChangeListener(listener);
		JPushInterface.stopPush(MainActivity.this);
    }
    
    private OnCheckedChangeListener listener=new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if(isChecked){
				if(!Util.NetWorkUtils.isNetworkConnected(MainActivity.this)){
					Toast.makeText(MainActivity.this, "请打开网络连接", Toast.LENGTH_LONG).show();
					buttonView.setChecked(false);
					return;
				}
				
				if(!Util.NetWorkUtils.isGPSEnabled(MainActivity.this)){
					Toast.makeText(MainActivity.this, "请打开定位设置", Toast.LENGTH_LONG).show();
					buttonView.setChecked(false);
					return;
				}
				
				log.setText("开始测试");
				Message m=new Message();
				m.what=107;
				//开始接收并处理信号
				 Util.Const.getCore().getReceiveHandler().sendMessage(m);
				 
				 //打开推送设置，允许进行推送
				 JPushInterface.setDebugMode(true);
				 JPushInterface.resumePush(MainActivity.this);
			     JPushInterface.init(getApplicationContext());
			     JPushInterface.setAlias(MainActivity.this, "123456",null);
			   
				
			}
			else{
				log.setText("关闭测试");
				JPushInterface.setDebugMode(false);
				 JPushInterface.stopPush(MainActivity.this);
			}
			
		}
	};
    
    
    
  //for receive customer msg from jpush server
  	private MyReceiver mMessageReceiver;
  	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
  	public static final String KEY_TITLE = "title";
  	public static final String KEY_MESSAGE = "message";
  	public static final String KEY_EXTRAS = "extras";
  	
  	

  	
  	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
	private static final int PRIVATE_CODE = 1315;//开启GPS权限

	public void showGPSContacts() {
		LocationManager lm = (LocationManager) Const.context.getSystemService(this.LOCATION_SERVICE);
		boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (ok) {//开了定位服务
			if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限。
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							BAIDU_READ_PHONE_STATE);
				} else {
				}
			} else {
				Toast.makeText(this,"请打开定位权限",Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "系统检测到未开启GPS定位服务,请开启", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, PRIVATE_CODE);
		}
	}


	/**
	 * Android6.0申请权限的回调方法
	 */
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			// requestCode即所声明的权限获取码，在checkSelfPermission时传入
			case BAIDU_READ_PHONE_STATE:
				//如果用户取消，permissions可能为null.
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) {  //有权限
					// 获取到权限，作相应处理
				} else {
					showGPSContacts();
				}
				break;
			default:
				break;
		}
	}
  
  }

