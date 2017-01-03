package project2.madcamp02;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Login extends AppCompatActivity {

    private ArrayList<Friend> friendList = new ArrayList<>();
    private ArrayList<String> contactJsonList = new ArrayList<>();
    String str_id;

    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;

    public final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    private String loginResultJson;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        askContactsPermission();
        askStoragePermission();

        setContentView(R.layout.activity_login);


        findViewById(R.id.facebook_login).setOnClickListener(m2ClickListener);

        findViewById(R.id.no_facebook).setOnClickListener(m3ClickListener);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
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
                                        if (json.has("id")) {
                                            str_id = json.getString("id");
                                        }else{
                                            str_id = "none";
                                        }
//                                        if (json.has("first_name")) {
//                                            String str_email = json.getString("first_name");
//                                        }
//                                        if (json.has("last_name")) {
//                                            String str_email = json.getString("last_name");
//                                        }

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
            GetFriendTask friendTask = new GetFriendTask();
            friendTask.execute();

        }
    };

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

    public class GetFriendTask extends AsyncTask<Void, Contact, Void> {

        private ProgressDialog mProgressDialog = new ProgressDialog(Login.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(Login.this, "",
                    "페이스북 연락처를 가져오고 있습니다.", false);
        }

        @Override
        protected Void doInBackground(Void... v) {
            // db에 넣기
            // 이미 회원이면 디비에 집어넣지 않는다.
            String mongo_id = preferences.getString("mongo_id", "none");
            JSONObject restJson = new JSONObject();
            try {
                restJson.put("mongo_id", mongo_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!Objects.equals("none", str_id)) {
                try {
                    restJson.put("facebook_id", str_id);
                    restJson.put("isFacebook", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    restJson.put("isFacebook", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("restJson", restJson.toString());
            Rest userAddRest = new Rest("http://143.248.234.144:8080/users", "POST", restJson.toString());
            String restResult = null;
            try {
                restResult = userAddRest.Post();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                JSONObject restResultJson = new JSONObject(restResult);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("mongo_id", restResultJson.getString("_id"));
                edit.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                    if (response != null) {
                        try {
                            Log.d("response", response.toString());
                            JSONObject object = response.getJSONObject();
                            JSONArray friendJsonArray = object.getJSONArray("data");
                            for (int i=0; i < friendJsonArray.length(); i++) {
                                JSONObject jsonTemp = friendJsonArray.getJSONObject(i);
                                JSONObject newJson = new JSONObject();
                                newJson.put("name", jsonTemp.getString("name"));
                                newJson.put("url", jsonTemp.getJSONObject("picture").getJSONObject("data").getString("url"));
                                InputStream in = new java.net.URL(newJson.getString("url")).openStream();
                                Bitmap profileBitmap = BitmapFactory.decodeStream(in);
                                newJson.put("image", encodeToBase64(profileBitmap, Bitmap.CompressFormat.JPEG, 100));
                                contactJsonList.add(newJson.toString());
                                JSONObject restJson = new JSONObject();
                                restJson.put("whose", preferences.getString("mongo_id", "none"));
                                restJson.put("fromWhere", "facebook");
                                restJson.put("name", jsonTemp.getString("name"));
                                Log.d("restJson", restJson.toString());
                                Rest friendAddRest = new Rest("http://143.248.234.144:8080/friends/" +
                                        preferences.getString("mongo_id", "none"), "POST", restJson.toString());
                                String restResult = friendAddRest.Post();
                            }
                            Log.d("response", contactJsonList.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    }
                }
            ).executeAndWait();
            return null;
        }

        @Override
        protected void onProgressUpdate(Contact... contactArgs) {

        }

        @Override
        protected void onPostExecute(Void v) {
            // doInBackground 에서 받아온 total 값 사용 장소
            mProgressDialog.dismiss();
            Log.d("isshowing", String.valueOf(mProgressDialog.isShowing()));
            for (int i = 0; i < contactJsonList.size(); i++) {
                JSONObject jsonTemp = null;
                try {
                    jsonTemp = new JSONObject(contactJsonList.get(i));
                    Log.d("name", jsonTemp.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            GetContactTask getContactTask = new GetContactTask();
            getContactTask.execute();
            /*
            GetContactTask getContactTask = new GetContactTask();
            getContactTask.execute();
            */
        }
    }

    public class GetContactTask extends AsyncTask<Void, Contact, Void> {
        private ProgressDialog mProgressDialog = new ProgressDialog(Login.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mProgressDialog.isShowing()) {
                mProgressDialog = ProgressDialog.show(Login.this, "",
                        "핸드폰 연락처를 가져오고 있습니다.", false);
            }

        }

        @Override
        protected Void doInBackground(Void... v) {
            String[] arrProjection = {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
            };
            String[] arrPhoneProjection = {
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            // get user list
            Cursor clsCursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, arrProjection,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                    null, null
            );

            while (clsCursor.moveToNext()) {
                String strContactId = clsCursor.getString(0);
                // phone number
                Cursor clsPhoneCursor = getContentResolver().query (
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrPhoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + strContactId,
                        null, null
                );

                while( clsPhoneCursor.moveToNext() ) {
                    // add name, number
                    Contact addedItem = new Contact(clsCursor.getString(1), clsPhoneCursor.getString(0), null, null, null);
                    JSONObject newJson = new JSONObject();
                    try {
                        newJson.put("name", clsCursor.getString(1));
                        newJson.put("phone", clsPhoneCursor.getString(0));
//                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.basic);
//                        newJson.put("image", encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 10));
                        contactJsonList.add(newJson.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                clsPhoneCursor.close();
            }
            clsCursor.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Contact... contactArgs) {
            // 파일 다운로드 퍼센티지 표시 작업
            /*
            contacts.add(contactArgs[0]);
            m_adapter.notifyDataSetChanged();
            */
        }

        @Override
        protected void onPostExecute(Void v) {
            // doInBackground 에서 받아온 total 값 사용 장소
            mProgressDialog.dismiss();
            Intent intent = new Intent(Login.this, Tabs.class);
            intent.putExtra("login result", loginResultJson);
            intent.putExtra("facebook result", contactJsonList);
            startActivity(intent);
            finish();
        }
    }

    private class DownloadImageTask extends AsyncTask<ArrayList<String>, Bitmap, Void> {

        public DownloadImageTask() {
        }

        protected Void doInBackground(ArrayList<String>... urls) {
            ArrayList<String> urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                for (int i=0; i < urldisplay.size(); i++) {
                    Log.d("url", urldisplay.get(i));
                    InputStream in = new java.net.URL(urldisplay.get(i)).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    publishProgress(mIcon11);
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Bitmap... bm) {

        }

        protected void onPostExecute(Void v) {

        }
    }

    //이미지 인코딩
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    private void askContactsPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }

    private void askStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            return;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                askContactsPermission();
            } else {
                Toast.makeText(this, "연락처 권한 주세영", Toast.LENGTH_SHORT).show();
            }
        }
    }
}