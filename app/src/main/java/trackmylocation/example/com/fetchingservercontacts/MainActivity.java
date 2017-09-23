package trackmylocation.example.com.fetchingservercontacts;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    public static final int MY_PERMISSION_REQUEST_READ_CONTACTS = 1;
    public static final int MY_PERMISSION_REQUEST_WRITE_CONTACTS = 1;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    String lat[] = new String[6], lon[]=new String [6], name[] = new String[6];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        Button addbutton = (Button) findViewById(R.id.add_contacts);
        addbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                view.setClickable(true);
                AccessContacts contacts = new AccessContacts();
                contacts.start();
                try {
                    contacts.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
                mapFragment.getMapAsync(MainActivity.this);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap mMap) {

    }


    private class AccessContacts extends Thread {
        String str = "";
        int i=0;
        public void run(){
            try
            {
                URL url = new URL("http://www.cs.columbia.edu/~coms6998-8/assignments/homework2/contacts/contacts.txt");

                BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));

                while((str = input.readLine())!=null){
                    Log.d(TAG,"doInBackground: "+str);
                    String array[] = str.trim().split("\\s+");

                    Log.d(TAG,"doInBackground : Name:" + array[0]);
                    Log.d(TAG,"doInBackground : Email:" + array[1]);
                    Log.d(TAG,"doInBackground : Mobile:" + array[2]);
                    Log.d(TAG,"doInBackground : Phone:" + array[3]);

                    lat[i] = array[2];
                    lon[i] = array[3];
                    name[i] = array[0];
                    i++;

                    ArrayList<ContentProviderOperation> arrayList = new ArrayList<>();
                    arrayList.add(ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());

                    //For Mobile Number
                    if (array[2] != null){
                        arrayList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,array[2]).withValue(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

                    }
                    //for names
                    if (array[0]!=null){
                        arrayList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,array[0]).build());
                    }
                    //for email
                    if (array[1] != null){
                        arrayList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0).withValue(ContactsContract.CommonDataKinds.Email.DATA,array[1]).withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK).build());

                    }
                    //for home numbers
                    if(array[3]!=null){
                        arrayList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,array[3]).withValue(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_HOME).build());

                    }
                    try{
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY,arrayList);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    private void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, READ_CONTACTS, WRITE_CONTACTS},MY_PERMISSION_REQUEST_WRITE_CONTACTS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_REQUEST_WRITE_CONTACTS:
                if (grantResults.length > 0){
                    boolean StoragePermission=grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPermission=grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean WritePermission=grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && ReadPermission && WritePermission){
                        Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
                    }



                }
                break;
        }
    }
}
