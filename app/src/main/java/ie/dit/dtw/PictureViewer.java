package ie.dit.dtw;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;

/*
  Created by Karl on 2 Nov 2016.
 */

public class PictureViewer extends FragmentActivity {
    MapFragment map;
    Bundle coordinates;
    MyDBManager db;
    Cursor c;
    float latitude;
    float longitude;
    String picture;
    ImageView img;
    Button del;
    Button upDate;
    int id;

    View customView;
    ScrollView scrollViewParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);

        //get id of picture that was clicked on
        id = getIntent().getIntExtra("index", 0);
        db = new MyDBManager(this);
        try {
            db.open();
            c = db.getPhoto(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //get infomation about the photo
        if (c != null) {
            c.moveToFirst();
            picture = c.getString(c.getColumnIndex("Photoname"));
            latitude = c.getFloat(c.getColumnIndex("Latitude"));
            longitude = c.getFloat(c.getColumnIndex("Longitude"));

            c.moveToNext();
        } else {
            latitude = 53.3382171f;
            longitude = -6.2712236f;
        }

        //set up ap fragment to display photo's location
        map = new MapFragment();

        coordinates = new Bundle();

        coordinates.putFloat("lat", latitude);
        coordinates.putFloat("long", longitude);
        map.setArguments(coordinates);

        // Add the fragment to the FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.map, map).commit();


        //button to delete current photo
        del = (Button) findViewById(R.id.delete);

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deletePhoto(id);
                db.close();
                finish();
            }
        });

        //button to update photos date
        upDate = (Button) findViewById(R.id.update);

        upDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                db.updatePhotoDate(id, cal.toString());
                db.close();
            }
        });


        //get image view
        img = (ImageView) findViewById(R.id.picture);
        img.setImageResource(R.mipmap.error);

        try {
            //get file from internal storage and display it
            File file = new File(PictureViewer.this.getFilesDir(), picture);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));

            //rotate it because of the saving in landscape
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
            img.setImageBitmap(bm);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Reference: The following code is from
        //http://stackoverflow.com/questions/39204048/map-inside-scrollview-not-scrollable

        //code to stop scrollview from scrolling when navigating the map
        scrollViewParent = (ScrollView) findViewById(R.id.scrollView);
        customView = findViewById(R.id.customView);

        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        //end reference


    }
}