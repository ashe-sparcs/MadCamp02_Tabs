package project2.madcamp02;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Login extends AppCompatActivity {

    private ArrayList<Friend> friendList = new ArrayList<>();

    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;

    public final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private String loginResultJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)

        setContentView(R.layout.activity_login);


        findViewById(R.id.facebook_login).setOnClickListener(m2ClickListener);

        findViewById(R.id.no_facebook).setOnClickListener(m3ClickListener);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // facebook에서 불러오고, 주소록에서 불러오고, 그것들을 서버에 보내고 어레이에 저장.

    }

    // Private method to handle Facebook login and callback
    private void onFblogin()
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
                                        loginResultJson = String.valueOf(json);
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
                            }).executeAsync();
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }

    Button.OnClickListener m2ClickListener = new View.OnClickListener(){
        public void onClick(View v) {
            // Call private method
            onFblogin();

        }
    };

    Button.OnClickListener m3ClickListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent intent = new Intent(Login.this, Tabs.class);
            intent.putExtra("login result", loginResultJson);
            startActivity(intent);
            finish();
            /*
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
            */

        }
    };

    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
//            List<String> contacts = getContactNames();
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
//            lstNames.setAdapter(adapter);
            //DB에 데이터 추가
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
        // Get the ContentResolver
        ContentResolver cr = getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            do {
                // Get the contacts name
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contacts.add(name);
            } while (cursor.moveToNext());
        }
        // Close the curosor
        cursor.close();

        return contacts;
    }


    private class ContactHolder extends RecyclerView.ViewHolder {
        private Contact contacts;
        private TextView titleTextView;

        public ContactHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView)itemView;
        }

        public void bindContact(Contact contact) {
            contact = contact;
            titleTextView.setText(contact.getTitle());
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {
        private List<Contact> contacts;

        public ContactAdapter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            LayoutInflater layoutInflater =
                    LayoutInflater.from(Login.this);
            View view = layoutInflater.inflate(
                    android.R.layout.simple_list_item_1,
                    parent, false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = contacts.get(position);
            holder.bindContact(contact);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }
    }

    // More activity code here...
}