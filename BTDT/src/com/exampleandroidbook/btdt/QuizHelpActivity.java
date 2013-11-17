package com.exampleandroidbook.btdt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class QuizHelpActivity extends QuizActivity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.help);
		InputStream input=getResources().openRawResource(R.raw.quizhelp);
		try
		{
			String text=inputStreamToString(input);
		
			TextView textView=(TextView)findViewById(R.id.TextView_HelpText);
			textView.setText(text);
		}catch(IOException e) {  Log.e(ACTIVITY_SERVICE, "InputStreamToString failure", e);}
		
	}
	public String inputStreamToString(InputStream in) throws IOException
	{
		StringBuffer strBuffer=new StringBuffer();
		DataInputStream data=new DataInputStream(in);
		String strLine=null;
		while((strLine=data.readLine())!=null)
		{
			strBuffer.append(strLine+"\n");
		}
		data.close();
		in.close();
		return strBuffer.toString();
	}
	
}
