package com.exampleandroidbook.btdt;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class QuizActivity extends Activity
{	
	public static final String GAME_PREFERENCES="GamePrefs";
	public static final String DEBUG_TAG ="BTDT";
	
	public static final String GAME_PREFERENCES_FAV_PLACE_NAME = "FavPlaceName"; 
    public static final String GAME_PREFERENCES_FAV_PLACE_LONG = "FavPlaceLong"; 
    public static final String GAME_PREFERENCES_FAV_PLACE_LAT = "FavPlaceLat"; 
 
	public static final String GAME_PREFERENCES_SCORE="Score";
	public static final String GAME_PREFERENCES_CURRENT_QUESTION="CurQuestion";
	
    public static final String TRIVIA_SERVER_BASE = "http://tqs.mamlambo.com/";
    public static final String TRIVIA_SERVER_SCORES = TRIVIA_SERVER_BASE + "scores.jsp";
    public static final String TRIVIA_SERVER_QUESTIONS = TRIVIA_SERVER_BASE + "questions.jsp";
    
    public static final String GAME_PREFERENCES_PLAYER_ID="ServerId";
    public static final String GAME_PREFERENCES_UNIQUE_ID="ClientId";
    public static final String TRIVIA_SERVER_ACCOUNT_EDIT=TRIVIA_SERVER_BASE+"receive";
    public static final String TRIVIA_SERVER_FRIEND_ADD=TRIVIA_SERVER_BASE+"friend";
}
