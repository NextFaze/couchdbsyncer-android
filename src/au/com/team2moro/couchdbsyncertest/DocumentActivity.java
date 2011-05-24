package au.com.team2moro.couchdbsyncertest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import au.com.team2moro.couchdbsyncer.Attachment;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.Document;

public class DocumentActivity extends Activity {
	private static final String TAG = "DocumentActivity";
	
	TestApplication application;
	Document document;
	DatabaseStore dbstore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_activity);
		
		application = (TestApplication) getApplication();
    	
		String db_name = getIntent().getStringExtra("database_name");
		dbstore = application.getDatabaseStore();
    	Database database = dbstore.getDatabase(db_name);
    	String docId = getIntent().getStringExtra("docId");
        document = dbstore.getDocument(database, docId);

        if(document != null) displayDocument();
	}
	
	void displayDocument() {
        setTitle("Document " + document.getDocId());

        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(new ImageAdapter(this));

        /*
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(DocumentActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
        */
        
        // populate view with document fields and values
        Map<String, Object> content = document.getContent();
        if(content != null) {
    		TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
    		TreeSet<String> keys = new TreeSet<String>(content.keySet());  // order keys
        	for(String key : keys) {
        		if(key.equals("_attachments")) continue;
        		Object value = content.get(key);
                TableRow row = (TableRow)getLayoutInflater().inflate(R.layout.document_row, null);
                TextView tv1 = (TextView)row.findViewById(R.id.textView1);
                TextView tv2 = (TextView)row.findViewById(R.id.textView2);
                tv1.setText(key);
                tv2.setText(value == null ? "null" : value.toString());
                tl.addView(row);
        	}
        }        
	}
	
	// adapted from http://developer.android.com/resources/tutorials/views/hello-gallery.html
	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;
	    private List<Bitmap> images;

	    public ImageAdapter(Context c) {
	        mContext = c;
	        TypedArray a = obtainStyledAttributes(R.styleable.HelloGallery);
	        mGalleryItemBackground = a.getResourceId(R.styleable.HelloGallery_android_galleryItemBackground, 0);
	        a.recycle();
	        
	        images = new ArrayList<Bitmap>();
	        for(Attachment attachment : document.getAttachments()) {
	        	if(attachment.isImage()) {
	        		// read image from database
	        		attachment = dbstore.getAttachment(attachment);
	        		byte[] content = attachment.getContent();
	        		Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
	        		Log.d(TAG, "got bitmap: " + bitmap);
	        		images.add(bitmap);
	        	}
	        }
	    }

	    public int getCount() {
	        return images.size();
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView i = new ImageView(mContext);

	        i.setImageBitmap(images.get(position));
	        i.setLayoutParams(new Gallery.LayoutParams(150, 150));
	        i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	        i.setBackgroundResource(mGalleryItemBackground);

	        return i;
	    }
	}
}
