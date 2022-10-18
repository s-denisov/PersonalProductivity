package com.example.personalproductivity.ui;

import android.os.Bundle;
import android.text.InputType;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.personalproductivity.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TARGET_WORK_KEY = "PersonalProductivity pref_target_work";
    public static final String SESSION_LENGTH_KEY = "PersonalProductivity pref_session_length";
    public static final String SHORT_BREAK_LENGTH_KEY = "PersonalProductivity pref_short_break_length";
    public static final String LONG_BREAK_LENGTH_KEY = "PersonalProductivity pref_long_break_length";
    public static final String MAX_SESSION_LENGTH_KEY = "PersonalProductivity pref_max_session_length";
    public static final String MAX_BLOCK_LENGTH_KEY = "PersonalProductivity pref_max_block_length";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        String[] keys = {TARGET_WORK_KEY, SESSION_LENGTH_KEY, SHORT_BREAK_LENGTH_KEY, LONG_BREAK_LENGTH_KEY, MAX_SESSION_LENGTH_KEY, MAX_BLOCK_LENGTH_KEY};
        for (String key : keys) {
            EditTextPreference targetWork = findPreference(key);
            targetWork.setOnBindEditTextListener(e -> e.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
    }
}
