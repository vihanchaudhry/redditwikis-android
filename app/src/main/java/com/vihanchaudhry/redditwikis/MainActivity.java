package com.vihanchaudhry.redditwikis;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.managers.WikiManager;
import net.dean.jraw.models.WikiPage;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_CONTENT = "com.vihanchaudhry.redditwikis.CONTENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get clientId and deviceId
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        String clientId = "eVztidCUydteCQ";

        // Execute AsyncTask
        new AuthenticateClient().execute(clientId, deviceId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final class AuthenticateClient extends AsyncTask<String, Void, String> {

        private RedditClient reddit;
        private Credentials creds;
        private OAuthData authData;
        private WikiManager wikiManager;
        private WikiPage wikiPage;
        private String content;

        @Override
        protected String doInBackground(String... params) {
            // Authenticate client and return wiki page
            reddit = new RedditClient(UserAgent.of("android", "com.vihanchaudhry.redditwikis", "1.0", "reloadxox"));
            creds = Credentials.userlessApp(params[0], UUID.fromString(params[1]));
            try {
                authData = reddit.getOAuthHelper().easyAuth(creds);
            } catch (OAuthException e) {
                e.printStackTrace();
            }
            reddit.authenticate(authData);
            wikiManager = new WikiManager(reddit);
            wikiPage = wikiManager.get("learnprogramming", "index");
            content = wikiPage.getContent();
            return content;
        }

        @Override
        protected void onPostExecute(String content) {
            Toast.makeText(getApplicationContext(), "User authenticated", Toast.LENGTH_LONG).show();
            // Send the contents of the wiki page to the next activity
            Intent intent = new Intent(getApplicationContext(), DisplayWikiPage.class);
            intent.putExtra(EXTRA_CONTENT, content);
            startActivity(intent);
        }
    }
}
