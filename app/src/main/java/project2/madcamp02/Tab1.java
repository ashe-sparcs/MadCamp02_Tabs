package project2.madcamp02;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by yk 12/26/2016
 */

//Our class extending fragment
public class Tab1 extends Fragment {

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<String> contactJsonList = new ArrayList<>();

    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;
    ContactAdapter m_adapter;

    public final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity().getIntent());
        contactJsonList = intent.getStringArrayListExtra("facebook result");
        Log.d("tab1", "start");

        EmptyRecyclerView recyclerView = (EmptyRecyclerView) getView().findViewById(R.id.contacts_list_recycler_view);
        //recyclerView.setPadding(0,0,0,getSoftButtonsBarHeight());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = getView().findViewById(R.id.contacts_list_recycler_view);
        recyclerView.setEmptyView(emptyView);

        contacts = new ArrayList<>(); // Fetch list of contacts from the database
        for (int i = 0; i < contactJsonList.size(); i++) {
            try {
                JSONObject jsonObject = new JSONObject(contactJsonList.get(i));
                contacts.add(new Contact(
                        jsonObject.getString("name"),
                        jsonObject.has("phone") ? jsonObject.getString("phone") : null,
                        jsonObject.has("url") ? jsonObject.getString("url") : null,
                        jsonObject.has("email") ? jsonObject.getString("email") : null,
                        jsonObject.has("image") ? jsonObject.getString("image") : null
                        )
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        m_adapter = new ContactAdapter(contacts, getActivity());
        recyclerView.setAdapter(m_adapter);
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
            if (contact.getImage() == null) {
                profileImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.basic, null));
            } else {
                profileImageView.setImageBitmap(decodeBase64(contact.getImage()));
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

    // More activity code here...
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }

    //이미지 디코딩
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}