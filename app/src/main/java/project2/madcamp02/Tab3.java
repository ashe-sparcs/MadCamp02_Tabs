package project2.madcamp02;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by yk 12/26/2016
 */

//Our class extending fragment
public class Tab3 extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Post> posts = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        View rootView = inflater.inflate(R.layout.tab3, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        GetPostTask getPostTask = new GetPostTask();
        getPostTask.execute();
    }

    class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>
    {
        private Context context;
        private ArrayList<Post> posts;

        // Allows to remember the last item shown on screen
        private int lastPosition = -1;

        public PostAdapter(ArrayList<Post> items, Context mContext)
        {
            posts = items;
            context = mContext;
        }

        // 필수로 Generate 되어야 하는 메소드 1 : 새로운 뷰 생성
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // 새로운 뷰를 만든다
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview,parent,false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        // 필수로 Generate 되어야 하는 메소드 2 : ListView의 getView 부분을 담당하는 메소드
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.imageView.setImageResource(R.drawable.basic);
            holder.titleView.setText(posts.get(position).title);
            holder.contentView.setText(posts.get(position).content);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = posts.get(position).title;
                    String content = posts.get(position).content;
                    String author = posts.get(position).author;
                    String id = posts.get(position).id;
                    SharedPreferences preferences = getActivity().getSharedPreferences("user",
                            MODE_PRIVATE);
                    Log.d("author", author);
                    Log.d("mongo_id", preferences.getString("mongo_id", "none"));
                    if (Objects.equals(preferences.getString("mongo_id", "none"), author)) {
                        Log.d("they are", "same");
                        doPutInput(title, content, id, position);
                    }
                }
            });


            setAnimation(holder.imageView, position);
        }

        // // 필수로 Generate 되어야 하는 메소드 3
        @Override
        public int getItemCount() {
            return posts.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView titleView;
            public TextView contentView;

            public ViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.image);
                titleView = (TextView) view.findViewById(R.id.title);
                contentView = (TextView) view.findViewById(R.id.content);
            }
        }

        private void setAnimation(View viewToAnimate, int position)
        {
            // 새로 보여지는 뷰라면 애니메이션을 해줍니다
            if (position > lastPosition)
            {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }
    }

    public class GetPostTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... v) {
            try {
                Rest newRest = new Rest("/posts", "GET", "");
                JSONArray getResult = null;
                try {
                    getResult = new JSONArray(newRest.Get());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < getResult.length(); i++) {
                    try {
                        JSONObject getSingleResult = getResult.getJSONObject(i);
                        Post newPost = new Post(getSingleResult.getString("title"),
                                getSingleResult.getString("content"),
                                getSingleResult.getString("author"));
                        newPost.setId(getSingleResult.getString("_id"));
                        posts.add(newPost);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new PostAdapter(posts, getActivity());
            recyclerView.setAdapter(adapter);
        }
    }

    public void doPutInput(String title, String content, final String id, final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        LayoutInflater factory = LayoutInflater.from(getActivity());

//text_entry is an Layout XML file containing two text field to display in alert dialog
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText titleBox = new EditText(getActivity());
        titleBox.setText(title);
        layout.addView(titleBox);

        final EditText descriptionBox = new EditText(getActivity());
        descriptionBox.setText(content);
        layout.addView(descriptionBox);

        alert.setView(layout);
        alert.setTitle("글쓰기").setPositiveButton("저장",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        Log.i("AlertDialog","TextEntry 1 Entered "+titleBox.getText().toString());
                        Log.i("AlertDialog","TextEntry 2 Entered "+descriptionBox.getText().toString());

                        // rest api 호출
                        DoPutTask doPutTask = new DoPutTask();
                        doPutTask.execute(titleBox.getText().toString(),
                                descriptionBox.getText().toString(), id, String.valueOf(position));
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                    }
                });
        alert.show();
    }

    public class DoPutTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... box) {
            SharedPreferences preferences = getActivity().getSharedPreferences("user", MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            try {
                String putResult = null;
                jsonObject.put("title", box[0]);
                jsonObject.put("content", box[1]);
                jsonObject.put("author", preferences.getString("mongo_id", "none"));
                jsonObject.put("_id", box[2]);
                Log.d("jsonObject", jsonObject.toString());
                Rest newRest = new Rest("/posts", "PUT", jsonObject.toString());
                putResult = newRest.Put();
                JSONObject putResultJson = new JSONObject(putResult);
                putResultJson.put("position", box[3]);
                return putResultJson.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String str) {
            JSONObject j = null;
            try {
                j = new JSONObject(str);
                Post changedPost = new Post(j.getString("title"), j.getString("content"), j.getString("author"));
                changedPost.setId(j.getString("_id"));
                posts.set(Integer.parseInt(j.getString("position")), changedPost);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}