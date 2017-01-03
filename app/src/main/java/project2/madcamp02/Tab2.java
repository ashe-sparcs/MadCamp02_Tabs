package project2.madcamp02;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by koush on 9/4/13.
 */
public class Tab2 extends Fragment {
    private MyAdapter mAdapter;

    // Adapter to populate and imageview from an url contained in the array adapter
    private class MyAdapter extends ArrayAdapter<String> {
        public MyAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // see if we need to load more to get 40, otherwise populate the adapter
            if (position > getCount() - 4)
                loadMore();

            if (convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.grid_item, null);

            // find the image view
            final ImageView iv = (ImageView) convertView.findViewById(R.id.picture);

            // select the image view
            Ion.with(iv)
                    .centerCrop()
                    .placeholder(R.drawable.basic)
                    .load(getItem(position));

            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2, container, false);
        Ion.getDefault(getActivity()).configure().setLogging("ion-sample", Log.DEBUG);

        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi * 2;
        GridView view = (GridView) rootView.findViewById(R.id.results);
        view.setNumColumns(cols);
        mAdapter = new MyAdapter(getActivity());
        view.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMore();
        //doUpload();
    }

    Cursor mediaCursor;
    public void loadMore() {
        if (mediaCursor == null) {
            mediaCursor = getActivity().getContentResolver().query(MediaStore.Files.getContentUri("external"), null, null, null, null);
        }

        int loaded = 0;
        while (mediaCursor.moveToNext() && loaded < 10) {
            // get the media type. ion can show images for both regular images AND video.
            int mediaType = mediaCursor.getInt(mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
            if (mediaType != MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    && mediaType != MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                continue;
            }

            loaded++;

            String uri = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            File file = new File(uri);
            // turn this into a file uri if necessary/possible
            if (file.exists())
                mAdapter.add(file.toURI().toString());
            else
                mAdapter.add(uri);
        }
    }

    /*
    void doUpload() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            File f = new File(mAdapter.getItem(i));

            // this is a 180MB zip file to test with
            Future uploading = Ion.with(Tab2.this)
                    .load("http://143.248.234.144:8080/images/upload")
                    .setMultipartFile("image", f)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            try {
                                JSONObject jobj = new JSONObject(result.getResult());
                                Toast.makeText(
                                        getActivity(), jobj.getString("responses"),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
        }

    }
    */
}
