package com.exampleandroidbook.btdt;

import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QuizMenuActivity extends QuizActivity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.menu);
		
		Animation spinin=AnimationUtils.loadAnimation(this, R.anim.fade_in);
		LayoutAnimationController controller=new LayoutAnimationController(spinin);
		RelativeLayout layout=(RelativeLayout)findViewById(R.id.relativeLayout);
		layout.setLayoutAnimation(controller);
		
		//定义变量
		ListView menuList=(ListView)findViewById(R.id.ListView_Menu);
		String[] items= {this.getResources().getString(R.string.menu_item_play)
				,this.getResources().getString(R.string.menu_item_scores)
				,this.getResources().getString(R.string.menu_item_settings)
				,this.getResources().getString(R.string.menu_item_help)
		};
		//填充控件
		ArrayAdapter<String> adapt=new ArrayAdapter<String>(this,R.layout.menu_item,items);
		menuList.setAdapter(adapt);
		//添加监听器
		menuList.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View itemClicked, int position,long id) 
			{
				TextView textView=(TextView)itemClicked;
				String strText=textView.getText().toString();
				
				if(strText.equalsIgnoreCase(getResources().getString(R.string.menu_item_play)))
				{
					startActivity(new Intent(QuizMenuActivity.this,QuizGameActivity.class));
				}
				else if(strText.equalsIgnoreCase(getResources().getString(R.string.menu_item_scores)))
				{
					startActivity(new Intent(QuizMenuActivity.this,QuizScoresActivity.class));
				}
				else if(strText.equalsIgnoreCase(getResources().getString(R.string.menu_item_settings)))
				{
					startActivity(new Intent(QuizMenuActivity.this,QuizSettingsActivity.class));
				}
				else if(strText.equalsIgnoreCase(getResources().getString(R.string.menu_item_help)))
				{
					startActivity(new Intent(QuizMenuActivity.this,QuizHelpActivity.class));
				}
			}
			
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		RelativeLayout layout=(RelativeLayout)findViewById(R.id.relativeLayout);
		layout.clearAnimation();
	}
	@Override 
	protected void onStop()
	{
		NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
		super.onStop();
	}
}


















