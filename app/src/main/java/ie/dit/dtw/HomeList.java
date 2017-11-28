package ie.dit.dtw;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/*
  Created by Karl on 2 Nov 2016.
 */

public class HomeList extends ListActivity {

    private MyDBManager db;
    private Cursor c;
    private ImageButton imageButton;
    String sampleText;
    ArrayList<String> name;
    ArrayList<String> coords;
    ArrayList<Integer> id;
    Boolean b = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);

        //get permissions for the app
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        2);
            }
        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        3);
            }
        }


        //set up the navigate to camera button
        imageButton = (ImageButton) findViewById(R.id.openCam);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b = true;
                Intent i = new Intent(HomeList.this, Camera.class);
                startActivity(i);
            }
        });

        //setup refresh button
        ImageButton frefresh = (ImageButton) findViewById(R.id.refresh);

        frefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });


        //gett all the phtos from the data base to but them in the list
        db = new MyDBManager(this);
        try {
            db.open();
            c = db.getAllPhotos();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        name = new ArrayList<>();
        coords = new ArrayList<>();
        id = new ArrayList<>();

        if (c != null) {
            Log.d("pre", "ifed");
            c.moveToFirst();
            while (!c.isAfterLast()) {
                //store database results into arraylists
                id.add(c.getInt(c.getColumnIndex("_id")));
                name.add(c.getString(c.getColumnIndex("Photoname")));
                coords.add(c.getString(c.getColumnIndex("Latitude")) + ", " +
                        c.getString(c.getColumnIndex("Longitude")));

                c.moveToNext();
            }
            sampleText = "null";
        }

        //if there is no data in the database set the sample text
        if (id.size() < 1) {
            sampleText = "Sydney";
            id.add(-1);
            name.add(sampleText);
        }
        Integer[] nArray = id.toArray(new Integer[id.size()]);

        //set up the list
        setListAdapter(new CustomAdapter(this, R.layout.row, nArray));
        db.close();
        b = false;
    }

    @Override
    protected void onResume() {
        //refresh the activity if it's coming back from another actvity
        super.onResume();
        if (b) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        b = true;
        if (sampleText.equals(name.get(position))) {
            //start camera if you click on sample text box
            Intent i = new Intent(HomeList.this, Camera.class);
            startActivity(i);
        } else {

            //start the pictureviere activity if there is user photos present
            int clickedID = (int) getListView().getItemAtPosition(position);

            Intent i = new Intent(HomeList.this, PictureViewer.class);
            i.putExtra("index", clickedID);
            startActivity(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //refreash the activity so to get all permissions before user can use the app
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public class CustomAdapter extends ArrayAdapter {

        public CustomAdapter(Context context, int rowLayoutId, Integer[] myArrayData) {
            super(context, rowLayoutId, myArrayData);
        }

        @Override
        public
        @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            TextView coordinates;
            ImageView tick;


            if (view == null) {
                // Get a new instance of the row layout view
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.row, parent, false);
            }

            coordinates = (TextView) view.findViewById(R.id.co_ord);

            tick = (ImageView) view.findViewById(R.id.prevImage);

            // Set data the co ordinates text
            if(name.get(position).length()>8) {
                String display = name.get(position).substring(4, (name.get(position).length() - 4));
                coordinates.setText(display);
            }else{
                coordinates.setText("");
            }
            if (sampleText.equals(name.get(position))) {
                //Log.d("pre", "sample text plese ignore");
                tick.setImageResource(R.drawable.opera);
                coordinates.setText(sampleText);
            } else {

                tick.setImageResource(R.mipmap.error);
                try {
                    //set the users photo to the imageview
                    File file = new File(HomeList.this.getFilesDir(), name.get(position));
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));

                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);

                    Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    tick.setImageBitmap(bm);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            return view;
        }
    }
}

