package project2.madcamp02;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by yk 12/26/2016
 */

//Our class extending fragment
public class Tab1 extends Fragment {

    private ArrayList<Friend> friendList = new ArrayList<>();

    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;

    public final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    @Override
    public void onStart() {
        super.onStart();
        EmptyRecyclerView recyclerView = (EmptyRecyclerView) getView().findViewById(R.id.contacts_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = getView().findViewById(R.id.contacts_list_empty_view);
        recyclerView.setEmptyView(emptyView);

        List<Contact> contacts = new ArrayList<>(); // Fetch list of contacts from the database
//        Contact.add(new Contact("잠"));
//        Contact.add(new Contact("잠"));
//        Contact.add(new Contact("잠"));
        ContactAdapter dataAdapter = new ContactAdapter(contacts);
        recyclerView.setAdapter(dataAdapter);
        getView().findViewById(R.id.get_contacts).setOnClickListener(mClickListener);
        getView().findViewById(R.id.button2).setOnClickListener(m2ClickListener);
        getView().findViewById(R.id.button3).setOnClickListener(m3ClickListener);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
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

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
    private List<Contact> getContactNames() {
        List<Contact> contacts = new ArrayList<>();
        // Get the ContentResolver
        ContentResolver cr = getActivity().getContentResolver();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        // Get the Cursor of all the contacts
//        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Cursor cursor = cr.query(uri, projection, null, null, null);
        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            do {
                // Get the contacts name
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contacts.add(new Contact(name,number));
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
                    LayoutInflater.from(getActivity());
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }
}