package com.exampleandroidbook.btdt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class QuizScoresActivity extends QuizActivity
{	
	public static int mProgressCounter=0;
	ScoreDownloaderTask allScoresDownloader;
	ScoreDownloaderTask friendScoresDownloader;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		//����Ļ������ʹ�ò�ȷ��������������setContentViewǰ����
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.scores);
		
		//��ʼ��TabHost
		TabHost tabHost=(TabHost)findViewById(R.id.TabHost1);
		tabHost.setup();
		//����ѡ����������ֺ�ͼ�ָ꣬���������á�
		TabSpec allScoreTab=tabHost.newTabSpec("allTab");
		allScoreTab.setIndicator(getResources().getString(R.string.all_scores), getResources().getDrawable(android.R.drawable.star_on));
		allScoreTab.setContent(R.id.ScrollViewAllScores);
		tabHost.addTab(allScoreTab);
		
		TabSpec friendScoreTab=tabHost.newTabSpec("friensTab");
		friendScoreTab.setIndicator(getResources().getString(R.string.friends_scores), getResources().getDrawable(android.R.drawable.star_on));
		friendScoreTab.setContent(R.id.ScrollViewFriendScores);
		tabHost.addTab(friendScoreTab);
		//����Ĭ��Ҫ��ʾ��ѡ�
		tabHost.setCurrentTabByTag("allTab");
		
		TableLayout allScoresLayout=(TableLayout)findViewById(R.id.TableLayout_AllScores);	
		initializeHeaderRow(allScoresLayout);
		allScoresDownloader=new ScoreDownloaderTask();
		allScoresDownloader.execute(TRIVIA_SERVER_SCORES,allScoresLayout);
		
		TableLayout friendScoresLayout=(TableLayout)findViewById(R.id.TableLayout_FriendScores);
		initializeHeaderRow(friendScoresLayout);
		SharedPreferences sharedPreference=getSharedPreferences(GAME_PREFERENCES,Context.MODE_PRIVATE);
		Integer playerId=sharedPreference.getInt(GAME_PREFERENCES_PLAYER_ID, -1);
        if (playerId != -1) 
        {
            friendScoresDownloader = new ScoreDownloaderTask();
            friendScoresDownloader.execute(TRIVIA_SERVER_SCORES + "?playerId=" + playerId, friendScoresLayout);
        }
	}
	
	/**
	 * ȡ���첽����
	 */
	@Override
	protected void onPause() 
	{
		if(allScoresDownloader!=null &&allScoresDownloader.getStatus()!=AsyncTask.Status.FINISHED)
		{
			allScoresDownloader.cancel(true);
		}
		if(friendScoresDownloader!=null && friendScoresDownloader.getStatus()!=AsyncTask.Status.FINISHED)
		{
			friendScoresDownloader.cancel(true);
		}
		super.onPause();
	}
	
	/**
	 * ΪtableLayout��ӱ���
	 * @param tableLayout
	 */
	private void initializeHeaderRow(TableLayout tableLayout)
	{
		TableRow tableRow=new TableRow(this);
		int textColor = getResources().getColor(R.color.logo_color);
        float textSize = getResources().getDimension(R.dimen.help_text_size);
        addTextToRowWithValues(tableRow, getResources().getString(R.string.username), textColor, textSize);
        addTextToRowWithValues(tableRow, getResources().getString(R.string.score), textColor, textSize);
        addTextToRowWithValues(tableRow, getResources().getString(R.string.rank), textColor, textSize);
        tableLayout.addView(tableRow);
	}
	
	/**
	 * ��������ӵ�tableLayout��tableRow��
	 * @param tableRow
	 * @param text
	 * @param textColor
	 * @param textSize
	 */
	private void addTextToRowWithValues(final TableRow tableRow,String text,int textColor,float textSize)
	{
		TextView textView=new TextView(this);
		textView.setText(text);
		textView.setTextColor(textColor);
		textView.setTextSize(textSize);
		tableRow.addView(textView);
	}
	
	
	/**
	 * ����AsycTask���������ص÷�
	 * @author Administrator
	 *
	 */
	private class ScoreDownloaderTask extends AsyncTask<Object,String,Boolean>
	{
		private static final String DEBUG_TAG="ScoreDownloaderTasker";
		TableLayout table;
		
		/**
		 * ���к�̨����
		 */
		@Override
		protected Boolean doInBackground(Object... params) 
		{
			Log.i(DEBUG_TAG, "doInBackgroud");
			boolean result=false;
			String pathToScores=(String)params[0];
			table=(TableLayout)params[1];
			URL xmlUrl=null;
			XmlPullParser scores=null;
			try 
			{	
				xmlUrl=new URL(pathToScores);
				scores=XmlPullParserFactory.newInstance().newPullParser();
				scores.setInput(xmlUrl.openStream(),null);
				if(scores!=null)
				{
					processScores(scores);
				}
			}
			catch (XmlPullParserException e) {Log.i(DEBUG_TAG, "doInBackground-XmlPullParser");} 
			catch (MalformedURLException e) {Log.i(DEBUG_TAG, "doInBackground-URL");} 
			catch (IOException e) {Log.i(DEBUG_TAG,"doInBackground-setInput");}
			return result;
		}
		
		/**
		 * ��ʾ����ָʾ��
		 */
		@Override
		protected void onPreExecute()
		{
			mProgressCounter++;
			QuizScoresActivity.this.setProgressBarIndeterminateVisibility(true);
		}
		
		/**
		 * ���ؽ�����ʾ��
		 */
		@Override
		 protected void onPostExecute(Boolean result)
		{
			Log.i(DEBUG_TAG, "onPostExecute");
			mProgressCounter--;
			if(mProgressCounter<=0)
			{
				mProgressCounter=0;
				QuizScoresActivity.this.setProgressBarIndeterminateVisibility(false);
			}
		}
		
		/**
		 * ����ȡ�����������ؽ�����ʾ��
		 */
		@Override
		protected void onCancelled()
		{
			Log.i(DEBUG_TAG, "onCancelled");
			mProgressCounter--;
			if(mProgressCounter<=0)
			{
				mProgressCounter=0;
				QuizScoresActivity.this.setProgressBarIndeterminateVisibility(false);
			}
		}
		
		/**
		 * �������
		 */
		@Override
		protected void onProgressUpdate(String...values)
		{
			if(values.length==3)
			{
				String score=values[0];
				String rank=values[1];
				String userName=values[2];
				insertScoreRow(table,score,rank,userName);
			}
			else
			{
				final TableRow newRow=new TableRow(QuizScoresActivity.this);
				TextView noResults=new TextView(QuizScoresActivity.this);
				noResults.setText(getResources().getString(R.string.no_scores));
				newRow.addView(noResults);
				table.addView(newRow);
			}
		}
		
		/**
		 * ʹ��xmlResourceParser����xml�ļ�
		 * @param tableLayout
		 * @param scores
		 * @throws XmlPullParserException
		 * @throws IOException
		 */
		private void processScores(XmlPullParser scores)
		throws XmlPullParserException,IOException
		{
			int eventType=-1;
			boolean bFoundScores=false;
			while(eventType!=XmlResourceParser.END_DOCUMENT)
			{
				if(eventType==XmlResourceParser.START_TAG)
				{
					String strName=scores.getName();
					if(strName.equals("score"))
					{
						bFoundScores=true;
						String userName=scores.getAttributeValue(null,"username");
						String score=scores.getAttributeValue(null,"score");
						String ranking=scores.getAttributeValue(null,"rank");
						publishProgress(score,ranking,userName);			
					}
				}
				eventType=scores.next();
			}
			if(!bFoundScores)
			{
				publishProgress();
			}
		}
		
		/**
		 * ��������ӵ�Table��
		 * @param tableLayout
		 * @param username
		 * @param score
		 * @param ranking
		 */
		private void insertScoreRow(final TableLayout tableLayout,String score,String ranking,String username)
		{
			final TableRow tableRow=new TableRow(QuizScoresActivity.this);
			int textColor=getResources().getColor(R.color.title_color);
			float textSize=getResources().getDimension(R.dimen.help_text_size);
			addTextToRowWithValues(tableRow,username,textColor,textSize);
			addTextToRowWithValues(tableRow,score,textColor,textSize);
			addTextToRowWithValues(tableRow,ranking,textColor,textSize);
			tableLayout.addView(tableRow);		
		}
	}
}




















