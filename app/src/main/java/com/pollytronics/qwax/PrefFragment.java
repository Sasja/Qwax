package com.pollytronics.qwax;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrefFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference enabled = findPreference("enabled"); // TODO: match with key string in preference.xml
        enabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof Boolean) {
                    Boolean enable = (Boolean) newValue;
                    Intent intent = new Intent(getActivity(), MyService.class);
                    if(enable) {
                        getActivity().startService(intent);
                    } else {
                        getActivity().stopService(intent);
                    }
                    return true;
                } else return false;
            }
        });
    }
}
