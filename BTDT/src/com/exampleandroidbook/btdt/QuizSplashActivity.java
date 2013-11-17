package com.exampleandroidbook.btdt;


import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class QuizSplashActivity extends QuizActivity
{	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.splash);
		
		TextView textViewTopTitle=(TextView)findViewById(R.id.TextViewTopTitle);
		Animation animation=AnimationUtils.loadAnimation(this, R.anim.fade_in);
		textViewTopTitle.startAnimation(animation);
		
		TextView textViewBottomTitle=(TextView)findViewById(R.id.TextViewBottomTitle);
		Animation animation1=AnimationUtils.loadAnimation(this, R.anim.fade_in2);
		textViewBottomTitle.startAnimation(animation1);
		animation1.setAnimationListener(new AnimationListener() 
		{

			@Override
			public void onAnimationEnd(Animation animation) 
			{
				Intent intent=new Intent();
				intent.setClass(QuizSplashActivity.this, QuizMenuActivity.class);
				startActivity(intent);	
				finish();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
		
		});
		
		Animation animation2=AnimationUtils.loadAnimation(this, R.anim.custom_anim);
		LayoutAnimationController controller=new LayoutAnimationController(animation2);
		controller.setOrder(LayoutAnimationController.ORDER_RANDOM);
		TableLayout tl=(TableLayout)findViewById(R.id.TableLayout01);
		for(int i=0;i<tl.getChildCount();i++)
		{
			TableRow tr=(TableRow) tl.getChildAt(i);
			tr.setLayoutAnimation(controller);
		}
		
		this.setNotification();
		this.createLauncher();
	}

	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		TextView textViewTopTitle=(TextView)findViewById(R.id.TextViewTopTitle);
		textViewTopTitle.clearAnimation();
		
		TextView textViewBottomTitle=(TextView)findViewById(R.id.TextViewBottomTitle);
		textViewBottomTitle.clearAnimation();
		
		TableLayout tl=(TableLayout)findViewById(R.id.TableLayout01);
		for(int i=0;i<tl.getChildCount();i++)
		{
			TableRow tr=(TableRow) tl.getChildAt(i);
			tr.clearAnimation();
		}
	}

	/**
	 * 添加通知栏
	 */
	private void setNotification()
	{
		Intent intent=new Intent(QuizSplashActivity.this,QuizMenuActivity.class);
		PendingIntent pend=PendingIntent.getActivity(QuizSplashActivity.this, 0, intent, 0);
		
		NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification=new Notification();
		notification.defaults=Notification.DEFAULT_SOUND;
		notification.icon=R.drawable.icon;
		notification.setLatestEventInfo(QuizSplashActivity.this, "GuanMac", "启动游戏",pend);
		notificationManager.notify(0, notification);		
	}
	

	/**
	 * 创建快捷方式
	 */
	protected void createLauncher()
	{
		Intent addIntent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		
		String title=getResources().getString(R.string.app_name);
		Parcelable icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
		Intent intent=new Intent(this,QuizSplashActivity.class);
		
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		addIntent.putExtra("duplicate", false);
		sendBroadcast(addIntent);
	}
}









