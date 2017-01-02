package project2.madcamp02;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static project2.madcamp02.Tab1.callbackmanager;

public class Tabs extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private ArrayList<Friend> friendList = new ArrayList<>();

    //This is our tabLayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        setContentView(R.layout.activity_tabs);

        //Adding toolbar to the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("A"));
        tabLayout.addTab(tabLayout.newTab().setText("B"));
        tabLayout.addTab(tabLayout.newTab().setText("C"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        //Change tab indicator when swipe
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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
        if (id == R.id.action_facebook_login) {
            //login
            onFacebookLogin();
            return true;
        } else if (id == R.id.action_facebook_load) {
            //load
            new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response == null) {
                            Log.d("what", "response null");
                        } else {
                            try {
                                Log.d("Response", response.toString());
                                Log.d("Access Token", AccessToken.getCurrentAccessToken().toString());
                                JSONObject object = response.getJSONObject();
                                JSONArray friendJsonArray = object.getJSONArray("data");
                                for (int i=0; i < friendJsonArray.length(); i++) {
                                    JSONObject jsonTemp = friendJsonArray.getJSONObject(i);
                                    Friend newFriend = new Friend(jsonTemp.getString("name"),
                                        jsonTemp.getJSONObject("picture").getJSONObject("data").getString("url"),
                                        jsonTemp.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette"));
                                    friendList.add(newFriend);
                                    newFriend.printSelf();
                                }
                                Log.d("response", friendList.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            ).executeAsync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    // Private method to handle Facebook login and callback
    private void onFacebookLogin()
    {
        callbackmanager = CallbackManager.Factory.create();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","user_photos","public_profile", "user_friends"));

        LoginManager.getInstance().registerCallback(callbackmanager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    System.out.println("Success");
                    GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject json, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                    System.out.println("ERROR");
                                } else {
                                    System.out.println("Success");
                                    try {
                                        String jsonresult = String.valueOf(json);
                                        System.out.println("JSON Result"+jsonresult);

                                        String str_email = json.getString("email");
                                        String str_id = json.getString("id");
                                        String str_firstname = json.getString("first_name");
                                        String str_lastname = json.getString("last_name");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        }
                    ).executeAsync();
                }

                @Override
                public void onCancel() {
                    Log.d("TAG_CANCEL","On cancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("TAG_ERROR",error.toString());
                }
            }
        );
    }
}
