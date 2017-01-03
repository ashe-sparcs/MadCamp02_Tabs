package project2.madcamp02;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yk 12/26/2016
 */

public class Tab2 extends Fragment {

    private GridView mGridView = null;
    private MyAdapter mGridAdapter = null;
    private List<Item> items = new ArrayList<>();
    GridView gd;

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        View view = inflater.inflate(R.layout.tab2, container, false);

        //add item
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));

        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));
        items.add(new Item("미나", R.drawable.basic));




        mGridView = (GridView) view.findViewById(R.id.gridView1);
        mGridAdapter = new MyAdapter((getActivity()));
        final ImageView imgzoom = (ImageView) view.findViewById(R.id.imageZoom);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int a = (int) mGridAdapter.getItemId(position);
//                Log.v("흔적","getItemId is");
                imgzoom.setImageResource(a);
                imgzoom.setVisibility(View.VISIBLE);
                imgzoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgzoom.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        mGridView.setAdapter(mGridAdapter);
        return view;
    }

    public class MyAdapter extends BaseAdapter {


        private LayoutInflater inflator;

        public MyAdapter(Context context) {
            // TODO Auto-generated constructor stub
            inflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return items.get(position);
        }

        public String getItemName(int position) {
            return items.get(position).name;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return items.get(position).drawableId;
        }

        public void changeItem(int position, int rid) {
            Item it = items.get(position);
            String name2 = it.name;
            Item cit = new Item(name2, rid);
            items.set(position, cit);


        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = convertView;
            ImageView img1;


            TextView txt1;
            if (v == null) {
                v = inflator.inflate(R.layout.grid_item, parent, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));


                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            img1 = (ImageView) v.findViewById(R.id.picture);


            txt1 = (TextView) v.findViewById(R.id.text);


            Item item = (Item) getItem(position);

            img1.setImageResource(item.drawableId);


            txt1.setText(item.name);


            return v;
        }

    }



    private class Item {
        final String name;
        final int drawableId;
        int chosen;

        Item(String name, int drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}