package org.dodgybits.shuffle.android.preference.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import org.apache.http.HttpStatus;
import org.dodgybits.android.shuffle.R;
import org.dodgybits.shuffle.android.core.activity.flurry.FlurryEnabledActivity;
import org.dodgybits.shuffle.android.preference.model.Preferences;
import org.dodgybits.shuffle.android.synchronisation.tracks.ApiException;
import org.dodgybits.shuffle.android.synchronisation.tracks.WebClient;
import org.dodgybits.shuffle.android.synchronisation.tracks.WebResult;
import roboguice.inject.InjectView;

import java.net.URI;
import java.net.URISyntaxException;

import static org.dodgybits.shuffle.android.preference.model.Preferences.*;

/**
 * Activity that changes the options set for synchronization
 */
public class PreferencesTracksSynchronizationActivity extends FlurryEnabledActivity {
    @InjectView(R.id.url) EditText mUrlTextbox;
    @InjectView(R.id.user) EditText mUserTextbox;
    @InjectView(R.id.pass) EditText mPassTextbox;
    @InjectView(R.id.checkSettings) Button mCheckSettings;
    @InjectView(R.id.sync_interval) Spinner mInterval;
    @InjectView(R.id.tracks_self_signed_cert) CheckBox mSelfSignedCertCheckBox;

    private AsyncTask<Void, Void, Boolean> mCheckTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.synchronize_settings);

        String[] options = new String[] {
                getText(R.string.sync_interval_none).toString(),
                getText(R.string.sync_interval_30min).toString(),
                getText(R.string.sync_interval_1h).toString(),
                getText(R.string.sync_interval_2h).toString(),
                getText(R.string.sync_interval_3h).toString() };

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_list_item_1, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInterval.setAdapter(adapter);
        mInterval.setSelection(Preferences.getTracksInterval(this));

        String tracksUrl = Preferences.getTracksUrl(this);
        mUrlTextbox.setText(tracksUrl);
        // select server portion of URL
        int startIndex = 0;
        int index = tracksUrl.indexOf("://");
        if (index > 0) {
            startIndex = index + 3;
        }
        mUrlTextbox.setSelection(startIndex, tracksUrl.length());
        
        mUserTextbox.setText(Preferences.getTracksUser(this));
        mPassTextbox.setText(Preferences.getTracksPassword(this));
        mSelfSignedCertCheckBox.setChecked(Preferences.isTracksSelfSignedCert(this));

        CompoundButton.OnClickListener checkSettings = new CompoundButton.OnClickListener() {
			
			@Override
			public void onClick(View view) {
                mCheckTask = new CheckSettings().execute();
			}
		};
        
        CompoundButton.OnClickListener saveClick = new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                savePrefs();
                finish();
            }
        };

        CompoundButton.OnClickListener cancelClick = new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        };
        final int color = mUrlTextbox.getCurrentTextColor();
        verifyUrl(color);
        // Setup the bottom buttons
        View view = findViewById(R.id.action_done);
        view.setOnClickListener(saveClick);
        view = findViewById(R.id.action_cancel);
        view.setOnClickListener(cancelClick);

        view = findViewById(R.id.checkSettings);
        view.setOnClickListener(checkSettings);
        
        View url = findViewById(R.id.url);
        url.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                verifyUrl(color);

                return false;
            }
        });
    }

    private boolean verifyUrl(int color) {
        try {
            new URI(mUrlTextbox.getText().toString());
            mUrlTextbox.setTextColor(color);
            return true;
        } catch (URISyntaxException e) {
            mUrlTextbox.setTextColor(Color.RED);
            return false;
        }
    }

    private boolean savePrefs() {

        SharedPreferences.Editor ed = Preferences.getEditor(this);
        URI uri = null;
        try {
            uri = new URI(mUrlTextbox.getText().toString());
        } catch (URISyntaxException ignored) {

        }

            ed.putString(TRACKS_URL, uri.toString());

            ed.putInt(TRACKS_INTERVAL, mInterval.getSelectedItemPosition());
            ed.putString(TRACKS_USER, mUserTextbox.getText().toString());
            ed.putString(TRACKS_PASSWORD, mPassTextbox.getText().toString());
            ed.putBoolean(TRACKS_SELF_SIGNED_CERT, mSelfSignedCertCheckBox.isChecked());            
            ed.commit();
            return true;
    }
    
    private class CheckSettings extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            
            URI uri = null;
            try {
                uri = new URI(mUrlTextbox.getText().toString());
            } catch (URISyntaxException ignored) {

            }

            try {
                WebClient client = new WebClient(PreferencesTracksSynchronizationActivity.this, 
                        mUserTextbox.getText().toString(), 
                        mPassTextbox.getText().toString(), 
                        mSelfSignedCertCheckBox.isChecked());

                if (uri != null && uri.isAbsolute()) {
                    WebResult result = client.getUrlContent(uri.toString() + "/contexts.xml");
                    if(result.getStatus().getStatusCode() != HttpStatus.SC_OK)
                        return false;
                }
            } catch (ApiException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isGood) {
            int messageId = isGood ? R.string.tracks_settings_valid : R.string.tracks_failed_to_check_url;
            Toast toast = Toast.makeText(getApplicationContext(),
                    messageId, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}