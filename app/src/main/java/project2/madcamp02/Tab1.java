package project2.madcamp02;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by yk 12/26/2016
 */

//Our class extending fragment
public class Tab1 extends Fragment {

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<Friend> friendList = new ArrayList<>();
    private ArrayList<String> urlList = new ArrayList<>();

    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;
    ContactAdapter m_adapter;

    public final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    @Override
    public void onStart() {
        super.onStart();
        Log.d("tab1", "start");
        EmptyRecyclerView recyclerView = (EmptyRecyclerView) getView().findViewById(R.id.contacts_list_recycler_view);
        //recyclerView.setPadding(0,0,0,getSoftButtonsBarHeight());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = getView().findViewById(R.id.contacts_list_empty_view);
        recyclerView.setEmptyView(emptyView);

        contacts = new ArrayList<>(); // Fetch list of contacts from the database
        m_adapter = new ContactAdapter(contacts, getActivity());
        recyclerView.setAdapter(m_adapter);
        getView().findViewById(R.id.get_contacts).setOnClickListener(mClickListener);
        getView().findViewById(R.id.button2).setOnClickListener(m2ClickListener);


        getView().findViewById(R.id.button3).setOnClickListener(m3ClickListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 0);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            GetContactTask task = new GetContactTask();
            task.execute((Void[])null);
        }
    }

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        View rootView = inflater.inflate(R.layout.tab1, container, false);

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return rootView;
    }

    private void onFblogin()
    {
        callbackmanager = CallbackManager.Factory.create();

        // Set permissions
        //this를 getactivity로 바꾸면 계속 로그인해야함!!!!
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

    Button.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            showContacts();
        }
    };

    Button.OnClickListener m2ClickListener = new View.OnClickListener(){
        public void onClick(View v) {
            // Call private method
            onFblogin();
        }
    };

    Button.OnClickListener m3ClickListener = new View.OnClickListener(){
        public void onClick(View v){
            new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/taggable_friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response == null) {
                        } else {
                            try {
                                Log.d("response", response.toString());
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

        }
    };


//    /**
//     * Show the contacts in the ListView.
//     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            return;
//            // Android version is lesser than 6.0 or the permission is already granted.
//            List<Contact> contacts1 = getContactNames();
//            ContactAdapter adapter = new ContactAdapter(contacts1);
//            recyclerView.setAdapter(adapter);
//            //DB에 데이터 추가
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
                Toast.makeText(getActivity(), "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ContactHolder extends RecyclerView.ViewHolder {
        private Contact contact;
        private TextView titleTextView;
        private ImageView profileImageView;

        public ContactHolder(View itemView) {
            super(itemView);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
            titleTextView = (TextView) itemView.findViewById(R.id.name);
        }

        public void bindContact(Contact contact) {
            this.contact = contact;
            titleTextView.setText(contact.getTitle());
            if (contact.getImg() == null) {
                profileImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.basic, null));
            } else {
                profileImageView.setImageBitmap(contact.getImg());
            }
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {
        private Context context;
        private ArrayList<Contact> contacts;

        private int lastPosition = -1;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "아이템 클릭됨!", Toast.LENGTH_SHORT).show();
            }
        };

        public ContactAdapter(ArrayList<Contact> contacts, Context mContext) {
            this.contacts = contacts;
            context = mContext;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater =
                    LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(
                    R.layout.item_contact,
                    parent, false);
            view.setOnClickListener(mOnClickListener);
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

    public class GetContactTask extends AsyncTask<Void, Contact, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            Cursor clsCursor = getActivity().getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, arrProjection,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                    null, null
            );

            while (clsCursor.moveToNext()) {
                String strContactId = clsCursor.getString(0);
                // phone number
                Cursor clsPhoneCursor = getActivity().getContentResolver().query (
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrPhoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + strContactId,
                        null, null
                );

                while( clsPhoneCursor.moveToNext() ) {
                    // add name, number
                    Contact addedItem = new Contact(clsCursor.getString(1), clsPhoneCursor.getString(0), null, null, null);
                    publishProgress(addedItem);
                }
                clsPhoneCursor.close();

            }
            clsCursor.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Contact... contactArgs) {
            // 파일 다운로드 퍼센티지 표시 작업
            contacts.add(contactArgs[0]);
            m_adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void v) {
            // doInBackground 에서 받아온 total 값 사용 장소
            GetFriendTask task2 = new GetFriendTask();
            task2.execute((Void[]) null);
        }
    }

    public class GetFriendTask extends AsyncTask<Void, Contact, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... v) {
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
                                    Friend newFriend = new Friend(jsonTemp.getString("name"),
                                            jsonTemp.getJSONObject("picture").getJSONObject("data").getString("url"),
                                            jsonTemp.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette"));
                                    Contact newContact = new Contact(newFriend.name, null, newFriend.url, null, null);
                                    publishProgress(newContact);
                                    friendList.add(newFriend);
                                    urlList.add(newFriend.url);
                                }
                                Log.d("response", friendList.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            ).executeAsync();
            return null;
        }

        @Override
        protected void onProgressUpdate(Contact... contactArgs) {
            // 파일 다운로드 퍼센티지 표시 작업
            contacts.add(contactArgs[0]);
            m_adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void v) {
            // doInBackground 에서 받아온 total 값 사용 장소
            DownloadImageTask task3 = new DownloadImageTask();
            task3.execute(urlList);
        }
    }

    // More activity code here...
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
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
}