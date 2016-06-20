package com.net.location.mylocationhow;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Mari on 2015-09-25.
 */
public class SettingActivity extends PreferenceActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_setting);
    }
}
