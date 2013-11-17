package com.exampleandroidbook.btdt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

public class QuizGameActivity extends QuizActivity
{
	public static final String XML_TAG_QUESTION_BLOCK="questions";
	public static final String XML_TAG_QUESTION="question";
	public static final String XML_TAG_QUESTION_ATTRIBUTE_NUMBER="number";
	public static final String XML_TAG_QUESTION_ATTRIBUTE_TEXT="text";
	public static final String XML_TAG_QUESTION_ATTRIBUTE_IMAGEURL="imageUrl";
	public static final int QUESTION_BATCH_SIZE=15; //定义默认XML读取的数量
	private Hashtable mQuestions;					//用于分析xml后获得的Question类
	private ImageSwitcher mQuestionImage;
	private TextSwitcher mQuestionText;
	private SharedPreferences mGameSettings;
	
	private QuizTask downloader;
	ProgressDialog pleaseWaitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.game);
		
		mQuestionImage=(ImageSwitcher)findViewById(R.id.ImageSwitcher_QuestionImage);
		mQuestionImage.setFactory(new MyImageSwitcherFactory());
		
		mQuestionText=(TextSwitcher)findViewById(R.id.TextSwitcher_QuestionText);
		mQuestionText.setFactory(new MyTextSwitcherFactory());
		
		mQuestions=new Hashtable<Integer,Question>(QUESTION_BATCH_SIZE);
	
		mGameSettings=getSharedPreferences(GAME_PREFERENCES,Context.MODE_PRIVATE);
		int staringQuestionNumber=mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 0);
		if(staringQuestionNumber==0)
		{
			Editor editor=mGameSettings.edit();
			editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, 1);
			editor.commit();
			staringQuestionNumber=1;
		}
		downloader=new QuizTask();
		downloader.execute(TRIVIA_SERVER_QUESTIONS,staringQuestionNumber);
	}
	private void handleAnswerAndShowNextQuestion(boolean bAnswer)
	{
		int CurScore=mGameSettings.getInt(GAME_PREFERENCES_SCORE, 0);
		int nextQuestionNumber=mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 1)+1;
		Editor editor=mGameSettings.edit();
		editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, nextQuestionNumber);
		if(bAnswer)
			editor.putInt(GAME_PREFERENCES_SCORE, CurScore+1);
		editor.commit();
		if((mQuestions.containsKey(nextQuestionNumber))==false)
		{
			try
			{
				downloader=new QuizTask();
				downloader.execute(TRIVIA_SERVER_QUESTIONS,nextQuestionNumber);
			}catch(Exception e) {Log.e(DEBUG_TAG, "Loading initial question batch failed", e);}
		}
		if(mQuestions.containsKey(nextQuestionNumber))
		{
			mQuestionText.setCurrentText(this.getQuestionText(nextQuestionNumber));
			mQuestionImage.setImageDrawable(this.getQuestionImageDrawable(nextQuestionNumber));
		}
		else
		{
			handNoQuestion();
		}
		
	}
	/**
	 * 否按钮
	 */
	public void onNoButton(View view)
	{
		handleAnswerAndShowNextQuestion(false);
	}
	/**
	 * 是按钮
	 */
	public void onYesButton(View view)
	{
		handleAnswerAndShowNextQuestion(true);
	}
	/**
	 * 当没有问题时
	 */
	private void handNoQuestion() 
	{
		mQuestionText.setText(getResources().getText(R.string.no_questions));
		mQuestionImage.setImageResource(R.drawable.noquestion);

        Button yesButton = (Button) findViewById(R.id.Button_Yes);
        yesButton.setEnabled(false);

        Button noButton = (Button) findViewById(R.id.Button_No);
        noButton.setEnabled(false);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate(R.menu.gameoptions, menu);
		menu.findItem(R.id.help_menu_item).setIntent(new Intent(QuizGameActivity.this,QuizSettingsActivity.class));
		
		menu.findItem(R.id.settings_menu_item).setIntent(new Intent(QuizGameActivity.this,QuizSettingsActivity.class));
		return true;
	}
	class MyImageSwitcherFactory implements ViewFactory
	{
		@Override
		public View makeView()
		{
			LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView imageView=(ImageView) layoutInflater.inflate(R.layout.image_switcher_view,mQuestionImage,false);
			return imageView;
		}		
	}
	class MyTextSwitcherFactory implements ViewFactory
	{
		@Override
		public View makeView() 
		{
			LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView textView=(TextView) layoutInflater.inflate(R.layout.text_switcher_view, mQuestionText, false);
			return textView;
		}		
	}
	@Override
	protected void onPause() 
	{
		if(downloader!=null)
		{
			pleaseWaitDialog.dismiss();
			downloader.cancel(true);
		}
		super.onPause();
	}
	/**
	 * 获得一批问题
	 */
	private String getQuestionText(int number)
	{
		Question question=(Question)mQuestions.get(number);
		if(question!=null)
		{
			return question.getText();
		}
		return null;
	}
	private String getQuestionImageUrl(int number)
	{
		Question question=(Question)mQuestions.get(number);
		if(question!=null)
		{
			return question.getImageUrl();
		}
		return null;
	}
	private Drawable getQuestionImageDrawable(int number)
	{
		Drawable image;
		URL imageUrl;
		try
		{
			
			imageUrl=new URL(getQuestionImageUrl(number));
			InputStream input=imageUrl.openStream();
			Bitmap bitmap=BitmapFactory.decodeStream(input);
			input.close();
			image=new BitmapDrawable(getResources(),bitmap);
		}
		catch(Exception e) 
		{
			Log.e(DEBUG_TAG,"dECODING BITMAP STREAM failed");
			image=getResources().getDrawable(R.drawable.noquestion);
		}
		return image;
	}
	private void displayCurrentQuestion(int staringQuestionNumber) 
	{
		if(mQuestions.containsKey(staringQuestionNumber))
		{
			mQuestionText.setCurrentText(this.getQuestionText(staringQuestionNumber));
			mQuestionImage.setImageDrawable(this.getQuestionImageDrawable(staringQuestionNumber));
		}
		else
		{
			handNoQuestion();
		}	
	}
	/**
	 * 定义一个封装存储问题数据的内部类
	 */
	private class Question
	{
		private int mNumber;
		private String mText;
		private String mImageUrl;
		public Question(int nNumber,String mText,String mImageUrl)
		{
			this.mNumber=nNumber;
			this.mText=mText;
			this.mImageUrl=mImageUrl;
		}
		public int getNum()
		{
			return this.mNumber;
		}
		public String getText()
		{
			return this.mText;
		}
		public String getImageUrl()
		{
			return this.mImageUrl;
		}
		
	}
	
	/**
	 * 用于 下载问题的AsyncTask子类
	 */
	private class QuizTask extends AsyncTask<Object,String,Boolean>
	{
		private static final String DEBUG_TAG="QuizGameActivity$QuizTask";
		int startingNumber;
		@Override
		protected Boolean doInBackground(Object... params)
		{
			boolean result=false;
			startingNumber=(Integer)params[1];
			String pathToQuestions=params[0]+"?max="+QUESTION_BATCH_SIZE+"&start="+startingNumber;
			
			SharedPreferences settings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE);
            Integer playerId = settings.getInt(GAME_PREFERENCES_PLAYER_ID, -1);
            if (playerId != -1)
            {
                Log.d(DEBUG_TAG, "Updating score");
                Integer score = settings.getInt(GAME_PREFERENCES_SCORE, -1);
                if (score != -1) 
                {
                    pathToQuestions += "&updateScore=yes&updateId=" + playerId + "&score=" + score;
                }
            }
					
			Log.d(DEBUG_TAG, "path: " + pathToQuestions + " -- Num: "+ startingNumber);

			try
			{
				result=loadQuestionBatch(startingNumber,pathToQuestions);
			} catch (Exception e) {Log.d(DEBUG_TAG, "doInBackground-loadQuestionBatch");}
			return result;
		}
		/**
		 * 创建进度对话框
		 */
		@Override
		protected void onPreExecute()
		{
			pleaseWaitDialog=ProgressDialog.show(QuizGameActivity.this, "下载问题", "正在下载问题",true,true);
			pleaseWaitDialog.setOnCancelListener(new OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialog) 
				{
					QuizTask.this.cancel(true);
				}
				
			});
		}
		/**
		 * 关闭进度对话框
		 */
		@Override
		protected void onPostExecute(Boolean result)
		{
			Log.d(DEBUG_TAG," Domload task complete");
			if(result)
			{
				displayCurrentQuestion(startingNumber);
			}
			else
			{
				handNoQuestion();
			}
			pleaseWaitDialog.dismiss();
		}
		@Override
		protected void onCancelled()
		{
			Log.i(DEBUG_TAG, "onCancelled");
			handNoQuestion();
			pleaseWaitDialog.dismiss();
		}
		private boolean loadQuestionBatch(int startNumber, String pathToQuestions)
				throws Exception
				{
					boolean result=false;
					mQuestions.clear();
					int eventType=-1;
					
					XmlPullParser game=null;
					URL xmlUrl=new URL(pathToQuestions);
					game=XmlPullParserFactory.newInstance().newPullParser();
					game.setInput(xmlUrl.openStream(),null);
					
					while(eventType!=XmlResourceParser.END_DOCUMENT)
					{
						if(eventType==XmlResourceParser.START_TAG)
						{
							String name=game.getName();
							if(name.equals(XML_TAG_QUESTION))
							{
								String strNumber=game.getAttributeValue(null,XML_TAG_QUESTION_ATTRIBUTE_NUMBER);
								Integer number=new Integer(strNumber);
								String text=game.getAttributeValue(null,XML_TAG_QUESTION_ATTRIBUTE_TEXT);
								String imageUrl=game.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_IMAGEURL);
								Question question=new Question(number,text,imageUrl);
								mQuestions.put(number, question);
							}
						}
						eventType=game.next();
						result=true;
					}
					return result;
				}
	}
	
}


























