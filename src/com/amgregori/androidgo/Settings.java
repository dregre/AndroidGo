package com.amgregori.androidgo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Settings extends SherlockPreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}

	
//	@Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//	    // Set summary to be the user-description for the selected value
//	    int koRule = sharedPreferences.getString("ko","") == "0" ? Game.SITUATIONAL : Game.POSITIONAL;
//	    boolean suicideRule = sharedPreferences.getString("suicide","") == "1" ? true : false;
//	}
}
