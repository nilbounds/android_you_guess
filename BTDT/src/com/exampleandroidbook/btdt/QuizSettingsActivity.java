package com.exampleandroidbook.btdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class QuizSettingsActivity extends QuizActivity
{
	public static final String GAME_PREFERENCES_NICKNAME="Nickname";
	public static final String GAME_PREFERENCES_EMAIL="Email";
	public static final String GAME_PREFERENCES_PASSWORD="Password";
	public static final String GAME_PREFERENCES_DOB="DOB";
	public static final String GAME_PREFERENCES_GENDER="Gender";
	public static final String GAME_PREFERENCES_AVATAR="Avatar";
	
	private static final int TAKE_AVATAR_CAMERA_REQUEST=1;
	private static final int TAKE_AVATAR_GALLERY_REQUEST=2;
	private static final int FRIEND_EMAIL_DIALOG_ID=4;
	private static final int PLACE_DIALOG_ID=3;
	private  SharedPreferences sharedPreferences;
	private Editor editor;
	private Spinner spinner;
	private ImageButton avatarButton;
	public static final int DATE_DIALOG_ID=0;
	public static final int PASSWORD_DIALOG_ID=1;
	GPSCoords mFavPlaceCoords;
	FriendRequestTask friendRequest;
	protected void onCreate(Bundle savedInstanceState)
	{
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.settings);
		sharedPreferences=this.getSharedPreferences(GAME_PREFERENCES, MODE_PRIVATE);
		editor=sharedPreferences.edit();
		spinner=(Spinner)findViewById(R.id.Spinner_Gender);
		spinner.setSelection(1);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
				int strGender=spinner.getSelectedItemPosition();
				editor.putInt(GAME_PREFERENCES_GENDER, strGender);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
			}
			
		});
		avatarButton=(ImageButton)findViewById(R.id.ImageButton_Avatar);
		avatarButton.setOnLongClickListener(new LongClick());
		this.init();
	}
	class LongClick implements OnLongClickListener
	{
		@Override
		public boolean onLongClick(View v)
		{
			Intent pickPhoto=new Intent(Intent.ACTION_PICK);
			pickPhoto.setType("image/*");
			Intent.createChooser(pickPhoto, "请选择应用");
			startActivityForResult(pickPhoto, TAKE_AVATAR_GALLERY_REQUEST);
			return true;
		}
		
	}
	private void init()
	{
		final EditText editTextNickName=(EditText)findViewById(R.id.EditText_NickName);
		if(sharedPreferences.contains(GAME_PREFERENCES_NICKNAME))
		{
			editTextNickName.setText(sharedPreferences.getString(GAME_PREFERENCES_NICKNAME, ""));
		}
		final EditText editTextEmail=(EditText)findViewById(R.id.EditText_Email);
		if(sharedPreferences.contains(GAME_PREFERENCES_EMAIL))
		{
			editTextEmail.setText(sharedPreferences.getString(GAME_PREFERENCES_EMAIL, ""));
		}
		if(sharedPreferences.contains(GAME_PREFERENCES_GENDER))
		{
			spinner.setSelection(sharedPreferences.getInt(GAME_PREFERENCES_GENDER, 0));
		}
		TextView dobInfo = (TextView) findViewById(R.id.textView_DOB);
		if(sharedPreferences.contains(GAME_PREFERENCES_DOB))
		{
			dobInfo.setText(DateFormat.format("MMMM dd, yyyy",sharedPreferences.getLong(GAME_PREFERENCES_DOB, 0)));
		}
		else
		{
			dobInfo.setText(R.string.dob_not_set);
		}
		
		TextView passwordInfo=(TextView)findViewById(R.id.TextView_Password_Info);
		if(sharedPreferences.contains(GAME_PREFERENCES_PASSWORD))
		{
			passwordInfo.setText(R.string.pwd_set);
		}
		else
		{
			passwordInfo.setText(R.string.pwd_not_set);
		}
		if(sharedPreferences.contains(GAME_PREFERENCES_AVATAR))
		{
			String strUri=sharedPreferences.getString(GAME_PREFERENCES_AVATAR, "android.resource://com.androidbook.peakbagger/drawable/avatar");
			Uri uri=Uri.parse(strUri);
			avatarButton.setImageURI(uri);
		}
		else
		{
			avatarButton.setImageResource(R.drawable.avatar);
		}
		initFavortePlacePicker();
	}

	public void onAddFriendButtonClick(View view)
	{
		showDialog(FRIEND_EMAIL_DIALOG_ID);
	}
	
	private void doFriendRequest(String friendEmail)
	{
		if(friendRequest==null || friendRequest.getStatus()==AsyncTask.Status.FINISHED || friendRequest.isCancelled())
		{
			friendRequest=new FriendRequestTask();
			friendRequest.execute(friendEmail);
		}
		else
		{
			Log.e(DEBUG_TAG, "doFriendRequest");
		}
	}
	
	public void onSetPasswordButtonClick(View view)
	{
		showDialog(PASSWORD_DIALOG_ID);
	}
	
	public void onPickDateButtonClick(View view)
	{
		showDialog(DATE_DIALOG_ID);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		final EditText editTextNickName=(EditText)findViewById(R.id.EditText_NickName);
		String strNickName=editTextNickName.getText().toString();
		final EditText editTextEmail=(EditText)findViewById(R.id.EditText_Email);
		String strEmail=editTextEmail.getText().toString();	
		
		editor.putString(GAME_PREFERENCES_NICKNAME,strNickName);
		editor.putString(GAME_PREFERENCES_EMAIL,strEmail);
		editor.commit();
		if(friendRequest !=null)
			friendRequest.cancel(true);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Intent intent=new Intent(getApplicationContext(),UploaderService.class);
		startService(intent);
		Log.d(DEBUG_TAG,"SHARED PREFERENCES");
		Log.d(DEBUG_TAG,"NickName is "+sharedPreferences.getString(GAME_PREFERENCES_NICKNAME, "Not Set"));
		Log.d(DEBUG_TAG,"Email is "+sharedPreferences.getString(GAME_PREFERENCES_EMAIL, "Not Set"));
		Log.d(DEBUG_TAG, "Gender is "+sharedPreferences.getInt(GAME_PREFERENCES_GENDER, 0));
		Log.d(DEBUG_TAG,"Password is "+sharedPreferences.getString(GAME_PREFERENCES_PASSWORD, "Not Set"));
		Log.d(DEBUG_TAG,"DOB IS "+sharedPreferences.getLong(GAME_PREFERENCES_DOB, 0));
		Log.d(DEBUG_TAG,"Avatar is: "+ sharedPreferences.getString(GAME_PREFERENCES_AVATAR,"Not set"));
		Log.d(DEBUG_TAG,"Fav Place Name is: "+ sharedPreferences.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "Not set"));
		Log.d(DEBUG_TAG,"Fav Place GPS Lat is: "+sharedPreferences.getFloat(GAME_PREFERENCES_FAV_PLACE_LAT, 0));
		Log.d(DEBUG_TAG,"Fav Place GPS Lon is: "+ sharedPreferences.getFloat(GAME_PREFERENCES_FAV_PLACE_LONG, 0));
	}

	@Override
	protected Dialog onCreateDialog(int id) 
	{
		switch(id)
		{
		case DATE_DIALOG_ID:
			final TextView dob=(TextView)findViewById(R.id.textView_DOB);
			Calendar now=Calendar.getInstance();
			DatePickerDialog dataPickDialog=
					new DatePickerDialog(this,
										new DatePickerDialog.OnDateSetListener() 
										{
											@Override
											public void onDateSet(DatePicker datePicker, int year, int month,int day) 
											{	
												Time dateOfBirth=new Time();
												dateOfBirth.set(day, month, year);
												long dtDob=dateOfBirth.toMillis(true);
												
												dob.setText(DateFormat.format("MMMM dd yyyy",dtDob));
												editor.putLong(GAME_PREFERENCES_DOB, dtDob);
												editor.commit();
											}
										}
										,now.get(Calendar.YEAR)
										,now.get(Calendar.MONTH)
										,now.get(Calendar.DAY_OF_MONTH));
			return dataPickDialog;
			
		case PASSWORD_DIALOG_ID:
			 LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            final View layout = inflater.inflate(R.layout.password_dialog, (ViewGroup) findViewById(R.id.root));
	            final EditText p1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);
	            final EditText p2 = (EditText) layout.findViewById(R.id.EditText_Pwd2);
	            final TextView error = (TextView) layout.findViewById(R.id.TextView_PwdProblem);
	            p2.addTextChangedListener(new TextWatcher() 
	            {
	                @Override
	                public void afterTextChanged(Editable s) 
	                {
	                    String strPass1 = p1.getText().toString();
	                    String strPass2 = p2.getText().toString();
	                    if (strPass1.equals(strPass2)) 
	                    {
	                        error.setText(R.string.pwd_equal);
	                    } 
	                    else 
	                    {
	                        error.setText(R.string.pwd_not_equal);
	                    }
	                }
	                @Override
	                public void beforeTextChanged(CharSequence s, int start, int count, int after) 
	                {
	                }

	                @Override
	                public void onTextChanged(CharSequence s, int start, int before, int count) {
	                }
	            });
	            
	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            builder.setView(layout);
	            builder.setTitle(R.string.button_pwd);
	            
	            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() 
	            {
	                public void onClick(DialogInterface dialog, int whichButton)
	                {
	                    QuizSettingsActivity.this.removeDialog(PASSWORD_DIALOG_ID);
	                }
	            });
	            
	            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
	            {
	                public void onClick(DialogInterface dialog, int which) 
	                {
	                    TextView passwordInfo = (TextView) findViewById(R.id.TextView_Password_Info);
	                    String strPassword1 = p1.getText().toString();
	                    String strPassword2 = p2.getText().toString();
	                    if (strPassword1.equals(strPassword2)) 
	                    {
	                        editor.putString(GAME_PREFERENCES_PASSWORD, strPassword1);
	                        editor.commit();
	                        passwordInfo.setText(R.string.pwd_set);
	                    } 
	                    else
	                    {
	                        Log.d(DEBUG_TAG, "Passwords do not match. Not saving. Keeping old password (if set).");
	                    }
	                    QuizSettingsActivity.this.removeDialog(PASSWORD_DIALOG_ID);
	                }
	            });
	            AlertDialog passwordDialog = builder.create();
	            return passwordDialog;
	            
		case PLACE_DIALOG_ID:
			LayoutInflater inflate=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View dialogLayout=inflate.inflate(R.layout.fav_place_dialog, (ViewGroup)findViewById(R.id.root1));
			
			final EditText placeName=(EditText)dialogLayout.findViewById(R.id.EditText_FavPlaceName);
			final TextView placeCoordinates=(TextView)dialogLayout.findViewById(R.id.TextView_FavPlaceCoords_Info);
			placeName.setOnKeyListener(new OnKeyListener() 
			{
				@Override
				public boolean onKey(View view, int keyCode, KeyEvent event)
				{
					if((event.getAction()==KeyEvent.ACTION_DOWN)&&event.getAction()==KeyEvent.KEYCODE_ENTER)
					{
						String locName=placeName.getText().toString();
						resolveLocation(locName);
						editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME,locName);
						editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT,mFavPlaceCoords.mLat);
						editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.mLon);
						editor.commit();
						placeCoordinates.setText(formatCoordinates(mFavPlaceCoords.mLat,mFavPlaceCoords.mLon));
						return true;
					}
					return false;
				}			
			});
			
			final Button mapButton=(Button)dialogLayout.findViewById(R.id.Button_MapIt);
			mapButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
	                   String placeToFind = placeName.getText().toString();
	                    resolveLocation(placeToFind);
	                    editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME, placeToFind);
	                    editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.mLon);
	                    editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT, mFavPlaceCoords.mLat);
	                    editor.commit();
	                    placeCoordinates.setText(formatCoordinates(mFavPlaceCoords.mLat, mFavPlaceCoords.mLon));
	                    //设置URI字符串格式
	                    String geoURI=String.format("geo:%f,%f?z=10",mFavPlaceCoords.mLat,mFavPlaceCoords.mLon);
	                    Uri geo=Uri.parse(geoURI);
	                    Intent geoMap=new Intent(Intent.ACTION_VIEW,geo);
	                    startActivity(geoMap);
	            }
				
			});
			AlertDialog.Builder builder1=new AlertDialog.Builder(this);
			builder1.setTitle(getResources().getString(R.string.settings_favoriteplace));
			builder1.setView(dialogLayout);
			builder1.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{			
	                TextView placeInfo = (TextView) findViewById(R.id.TextView_FavoritePlace_Info);
                    String strPlaceName = placeName.getText().toString();

                    if ((strPlaceName != null) && (strPlaceName.length() > 0)) {
                        editor.putString(GAME_PREFERENCES_FAV_PLACE_NAME, strPlaceName);
                        editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LONG, mFavPlaceCoords.mLon);
                        editor.putFloat(GAME_PREFERENCES_FAV_PLACE_LAT, mFavPlaceCoords.mLat);
                        editor.commit();
                        placeInfo.setText(strPlaceName);
                    }
                    QuizSettingsActivity.this.removeDialog(PLACE_DIALOG_ID);
				}
			});
			builder1.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
			{				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					removeDialog(PLACE_DIALOG_ID);
				}
			});
			AlertDialog placeDialog=builder1.create();
			return placeDialog;
			
		case FRIEND_EMAIL_DIALOG_ID:
			LayoutInflater infl=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View friendDialogLayout=infl.inflate(R.layout.friend_dialog, (ViewGroup)findViewById(R.id.root3));
			final TextView emailText=(TextView)findViewById(R.id.EditText_Email);
			AlertDialog.Builder friendDialogBuilder=new AlertDialog.Builder(this);
			friendDialogBuilder.setView(friendDialogLayout);
			friendDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					String friendEmail=emailText.getText().toString();
					if(friendEmail!=null && friendEmail.length()>0)
					doFriendRequest(friendEmail);
				}
			});
			return friendDialogBuilder.create();
		}
		return null;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) 
	{
		super.onPrepareDialog(id, dialog);
		switch(id)
		{
		case DATE_DIALOG_ID:
			DatePickerDialog datePickerDialog=(DatePickerDialog)dialog;
			int iDay,iMonth,iYear;
			
			if(sharedPreferences.contains(GAME_PREFERENCES_DOB))
			{
				long msBirthDate=sharedPreferences.getLong(GAME_PREFERENCES_DOB, 0);
				
				Time dateOfBirth=new Time();
				dateOfBirth.set(msBirthDate);
				iDay=dateOfBirth.monthDay;
				iMonth=dateOfBirth.month;
				iYear=dateOfBirth.year;
			}
			else
			{
				Calendar calender=Calendar.getInstance();
				iDay=calender.get(Calendar.DAY_OF_MONTH);
				iMonth=calender.get(Calendar.MONTH);
				iYear=calender.get(Calendar.YEAR);
			}
			datePickerDialog.updateDate(iYear, iMonth, iDay);
			return;
		case PLACE_DIALOG_ID:
			AlertDialog placeDialog=(AlertDialog)dialog;
			String strFavPlaceName;
			if(sharedPreferences.contains(GAME_PREFERENCES_FAV_PLACE_NAME))
			{
				strFavPlaceName=sharedPreferences.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "");
				mFavPlaceCoords=new GPSCoords(sharedPreferences.getFloat(GAME_PREFERENCES_FAV_PLACE_LAT, 0),sharedPreferences.getFloat(GAME_PREFERENCES_FAV_PLACE_LONG, 0));			
			}
			else
			{
				strFavPlaceName = getResources().getString(R.string.settings_favoriteplace_currentlocation);
				calculateCurrentCoordinates();
			}
			final EditText placeName = (EditText) placeDialog.findViewById(R.id.EditText_FavPlaceName);
			placeName.setText(strFavPlaceName);
	    
			final TextView placeCoordinates = (TextView) placeDialog.findViewById(R.id.TextView_FavPlaceCoords_Info);
			placeCoordinates.setText(formatCoordinates(mFavPlaceCoords.mLat, mFavPlaceCoords.mLon));
			return;
		}
	}
	public void onLaunchCamera(View view)
	{
		Intent pictureIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		Intent.createChooser(pictureIntent, "请选择应用");
		this.startActivityForResult(pictureIntent, TAKE_AVATAR_CAMERA_REQUEST);
	}
	
	
	private void saveAvatar(Bitmap avatar)
	{
		try {
		//Save the Bitmap as a local file called avatar.jpg
			String strAvatarFilename="avatar.jpg";							
			avatar.compress(CompressFormat.JPEG, 100, openFileOutput(strAvatarFilename, MODE_PRIVATE));		
		//Determine the Uri to the local avatar.jsp file
			Uri imgeUri =Uri.fromFile(new File(QuizSettingsActivity.this.getFilesDir(),strAvatarFilename));			
		//Save the Uri path as a String preference
			editor.putString(GAME_PREFERENCES_AVATAR, imgeUri.getPath());	
			editor.commit();
		//Update the ImageButton with the new image
			String uri=sharedPreferences.getString(GAME_PREFERENCES_AVATAR, "android.resource://com.exampleandroidbook.btdt/drawable/avatar");
			Uri imageURI=Uri.parse(uri);
			avatarButton.setImageURI(null);
			avatarButton.setImageURI(imageURI);			
		} catch (FileNotFoundException e) {Log.e(DEBUG_TAG, "saveAvatar");}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch(requestCode)
		{
		case TAKE_AVATAR_CAMERA_REQUEST:
			if(resultCode==Activity.RESULT_CANCELED)
			{
				
			}
			else if(resultCode==Activity.RESULT_OK)
			{
				Bitmap bitmap=(Bitmap)data.getExtras().get("data");
				if(bitmap!=null)
				{
					this.saveAvatar(bitmap);
				}
				
				
			}
			break;
		case TAKE_AVATAR_GALLERY_REQUEST:
			if(resultCode==Activity.RESULT_CANCELED)
			{
				
			}
			else if(resultCode==Activity.RESULT_OK)
			{
				try
				{
					Uri photoUri=data.getData();
					if(photoUri!=null)
					{
						int maxSize=150;
						Bitmap galleryPic=Media.getBitmap(getContentResolver(),photoUri);
						Bitmap scaledGalleryPic=this.createScaleBitmapKeepingAspectRatio(galleryPic, maxSize);
						this.saveAvatar(scaledGalleryPic);
					}					
				}catch(Exception e) {Log.e(DEBUG_TAG, "TAKE_GALLERY");}
				
			}
			break;
		}
	}
	private Bitmap createScaleBitmapKeepingAspectRatio(Bitmap bitmap,int maxSize)
	{
		int orgHeight=bitmap.getHeight();
		int orgWidth=bitmap.getWidth();
		
		int scaleWidth=(orgWidth>=maxSize)?maxSize:(int)((float)maxSize*((float)orgWidth / (float)orgHeight));
		int scaleHeight=(orgHeight>=maxSize)?maxSize:(int)((float)maxSize * ((float)orgHeight/(float)orgWidth));
		Bitmap scaleGalleryPic =Bitmap.createScaledBitmap(bitmap, scaleWidth, orgHeight,true);
		return scaleGalleryPic;
	}
	
	
	
	public void onPickPlaceButtonClick(View view) {
		showDialog(PLACE_DIALOG_ID);
	}
	public void initFavortePlacePicker()
	{
		final TextView favPlace=(TextView)findViewById(R.id.TextView_FavoritePlace_Info);
		if(sharedPreferences.contains(GAME_PREFERENCES_FAV_PLACE_NAME))
		{
			favPlace.setText(sharedPreferences.getString(GAME_PREFERENCES_FAV_PLACE_NAME, ""));
		}
	}
	/**
	 * 计算检测到的最后位置
	 */
	public void calculateCurrentCoordinates() 
	{
		float lon=0;
		float lat=0;
		try
		{
			 LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
	         Location recentLoc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	         lat = (float) recentLoc.getLatitude();
	         lon = (float) recentLoc.getLongitude();
		}catch(Exception e) {Log.e(DEBUG_TAG, "calcelateCurrentCoodinates() fail");} 
		mFavPlaceCoords=new GPSCoords(lat,lon);

	}
	/**
	 * 用于在屏幕上显示坐标
	 * @param lat
	 * @param lan
	 * @return
	 */
	public String formatCoordinates(float lat,float lan)
	{
		StringBuilder builder=new StringBuilder();
		builder.append(lat).append(",").append(lan);
		return builder.toString();
	}
	/**
	 * 将地址转换为坐标
	 * @author Administrator
	 *
	 */
	private boolean lookupLocationByName(String strLocation)
	{
		 final Geocoder coder = new Geocoder(getApplicationContext());
	     boolean bResolvedAddress = false;
	        try 
	        {

	            List<Address> geocodeResults = coder.getFromLocationName(strLocation, 1);
	            Iterator<Address> locations = geocodeResults.iterator();

	            while (locations.hasNext()) 
	            {
	                Address loc = locations.next();
	                mFavPlaceCoords = new GPSCoords((float) loc.getLatitude(), (float) loc.getLongitude());
	                bResolvedAddress = true;
	            }
	        }
	        catch (Exception e)
	        {
	            Log.e(DEBUG_TAG, "Failed to geocode location", e);
	        }
	        return bResolvedAddress;
	}
	/**
	 * 如果有这个地址取这个地址，如果没有这个地址就计算当前地址
	 * @param name
	 */
	private void resolveLocation(String name)
	{
		boolean bResolvedAddress = false;
		if(name.equalsIgnoreCase(getResources().getString(R.string.settings_favoriteplace_currentlocation))==false)
		{
			bResolvedAddress=lookupLocationByName(name);
		}
		if(bResolvedAddress==false)
		{
			calculateCurrentCoordinates();
		}
	}
    private class GPSCoords
    {
        float mLat, mLon;
        GPSCoords(float lat, float lon)
        {
            mLat = lat;
            mLon = lon;
        }
    }
    
    /**
     * 后台提交信息的类
     * @author Administrator
     *
     */
    public  static class UploaderService extends Service
    {
    	private SharedPreferences sharedPreferences;
    	private Editor editor;
    	private static final String DEBUG_TAG="QuizSettigActivity$UploaderService";
    	private UploadTask uploader;
    	
    	@Override
    	public int onStartCommand(Intent intent,int flags,int startId)
    	{
    		uploader=new UploadTask();
    		uploader.execute();
    		Log.e(DEBUG_TAG, "settings and image upload requested");
    		return START_REDELIVER_INTENT;
    	}
		@Override
		public IBinder onBind(Intent intent)
		{
			return null;
		}
    	@Override
    	public void onDestroy()
    	{
    		Log.e(DEBUG_TAG, "Service finish");
    	}
		private class UploadTask extends AsyncTask<Object,String,Boolean>
		{
			@Override
			protected void onPreExecute()
			{
				sharedPreferences=getSharedPreferences(GAME_PREFERENCES,Context.MODE_PRIVATE);
				editor=sharedPreferences.edit();
			}
			@Override
			protected Boolean doInBackground(Object... params)
			{
				boolean result=postSettingToService();
				if(result && !isCancelled())
				{
					result=postAvatarToServer();
				}
				return null;
			}
			@Override
			public void onPostExecute(Boolean result)
			{
				Log.e(DEBUG_TAG, "onPostExecute");
			}
			private boolean postSettingToService() 
			{
				boolean succeeded=false;
				
				String uniqueId=sharedPreferences.getString(GAME_PREFERENCES_UNIQUE_ID,null);
				Integer playerId=sharedPreferences.getInt(GAME_PREFERENCES_PLAYER_ID,-1);
				String nickname=sharedPreferences.getString(GAME_PREFERENCES_NICKNAME, "");
				String email=sharedPreferences.getString(GAME_PREFERENCES_EMAIL, "");
				Integer score=sharedPreferences.getInt(GAME_PREFERENCES_SCORE, -1);
				Integer gender=sharedPreferences.getInt(GAME_PREFERENCES_GENDER, -1);
				Long birthdate=sharedPreferences.getLong(GAME_PREFERENCES_DOB, 0);
				String password=sharedPreferences.getString(GAME_PREFERENCES_PASSWORD, "");
				String favePlaceName=sharedPreferences.getString(GAME_PREFERENCES_FAV_PLACE_NAME, "");
				
				Vector<NameValuePair> vars=new Vector<NameValuePair>();
				if(uniqueId==null)
				{
					uniqueId=UUID.randomUUID().toString();
					Log.d(DEBUG_TAG, "uniqueId is "+uniqueId);
					editor.putString(GAME_PREFERENCES_UNIQUE_ID, uniqueId);
					editor.commit();
				}
				vars.add(new BasicNameValuePair("uniqueId",uniqueId));
				vars.add(new BasicNameValuePair("nickname",nickname));
				vars.add(new BasicNameValuePair("email",email));
				vars.add(new BasicNameValuePair("password",password));
				vars.add(new BasicNameValuePair("gender",gender.toString()));
				vars.add(new BasicNameValuePair("faveplace",favePlaceName));
				vars.add(new BasicNameValuePair("dob",birthdate.toString()));
				
				String url=TRIVIA_SERVER_ACCOUNT_EDIT+"?"+URLEncodedUtils.format(vars, null);
				
				HttpGet request=new HttpGet(url);
				
				try
				{
					ResponseHandler<String> responseHandler=new BasicResponseHandler();
					HttpClient client=new DefaultHttpClient();
					String responseBody=client.execute(request,responseHandler);
					
					if(responseBody!=null&&responseBody.length()>0)
					{
						Integer resultId=Integer.parseInt(responseBody);
						editor.putInt(GAME_PREFERENCES_PLAYER_ID, resultId);
						editor.commit();
					}
					succeeded=true;				
				}
				catch(ClientProtocolException e){Log.e(DEBUG_TAG, "Failed to get playerId(protocol:",e);}
				catch(IOException e){Log.e(DEBUG_TAG, "Failed to getPlayerId(io): ",e);}
				
				return succeeded;
			}

			private boolean postAvatarToServer()
			{
				boolean succeeded=true;
				
				String avatar=sharedPreferences.getString(GAME_PREFERENCES_AVATAR, "");
				Integer playerId=sharedPreferences.getInt(GAME_PREFERENCES_PLAYER_ID, -1);
				
				MultipartEntity entiry=new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				File file=new File(avatar);
				if(file.exists())
				{
					FileBody encFile=new FileBody(file);
					
					entiry.addPart("avatar", encFile);
					
					try
					{
						entiry.addPart("updateId",new StringBody(playerId.toString()));
					}catch(UnsupportedEncodingException e) {Log.e(DEBUG_TAG, "failed to add form field.",e);}
					
					HttpPost request=request=new HttpPost(TRIVIA_SERVER_ACCOUNT_EDIT);
					request.setEntity(entiry);
					HttpClient client=new DefaultHttpClient();
					try
					{
						ResponseHandler<String> responseHandler=new BasicResponseHandler();
						String responseBody=client.execute(request,responseHandler);
						
						if(responseBody!=null &&responseBody.length()>0)
						{
							Log.w(DEBUG_TAG, "Unexpected response from avatar upload:"+responseBody);
						}
						succeeded=true;
					}
					catch(ClientProtocolException e) {Log.e(DEBUG_TAG, "Unexpected ClientProtocolException",e);}
					catch(IOException e) {Log.e(DEBUG_TAG,"Unexpected IOException",e);}
				}
				else
				{
					Log.d(DEBUG_TAG, "no avatar to upload");
					succeeded=true;
				}
				return false;
			}
		}
    }
    /**
     * 处理加为好友请求的异步类
     */
    private class FriendRequestTask extends AsyncTask<String,Object,Boolean>
    {

    	@Override
    	protected void onPreExecute()
    	{
    		QuizSettingsActivity.this.setProgressBarIndeterminateVisibility(true);
    	}
    	@Override
    	protected void onPostExecute(Boolean result)
    	{
    		QuizSettingsActivity.this.setProgressBarIndeterminateVisibility(false);
    	}
		@Override
		protected Boolean doInBackground(String... arg0) 
		{
			Boolean succeeded=false;
			SharedPreferences prefs=(SharedPreferences)getSharedPreferences(GAME_PREFERENCES,Context.MODE_PRIVATE);
			Integer playerId=prefs.getInt(GAME_PREFERENCES_PLAYER_ID, -1);
			String friendEmail=arg0[0];
			
			Vector<NameValuePair> vars=new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair("command","add"));
			vars.add(new BasicNameValuePair("playerId",playerId.toString()));
			vars.add(new BasicNameValuePair("friend",friendEmail));
			
			HttpClient client=new DefaultHttpClient();
			HttpPost request=new HttpPost(TRIVIA_SERVER_FRIEND_ADD);
			String responseBody=null;
			try 
			{
				request.setEntity(new UrlEncodedFormEntity(vars));
				ResponseHandler<String> responseHandler=new BasicResponseHandler();
				responseBody=client.execute(request,responseHandler);
			} 
			catch (UnsupportedEncodingException e) {Log.e(DEBUG_TAG, "doInBackground");} 
			catch (ClientProtocolException e) {Log.e(DEBUG_TAG, "ClientProtocolException");} 
			catch (IOException e){Log.e(DEBUG_TAG, "IOException");}
			if(responseBody!=null)
				succeeded=true;
			return succeeded;
		}
    	
    }
}




















