package com.example.mapapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import android.util.Log;
import android.view.KeyEvent;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.SearchRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.mapping.MapMarker;


public class MainActivity extends AppCompatActivity {

    private Map map = null;
    private MapFragment mapFragment = null;
    private EditText editText = null;
    private ArrayList<MapObject> markers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        editText = (EditText) findViewById(R.id.query);

        markers = new ArrayList<MapObject>();

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search(editText.getText().toString());
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    map = mapFragment.getMap();
                    map.setCenter(new GeoCoordinate(18.9219, 72.8330, 0.0), Map.Animation.NONE);
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                    MapMarker defaultMarker = new MapMarker();
                    defaultMarker.setCoordinate(new GeoCoordinate(18.9219, 72.8330, 0.0));
                    map.addMapObject(defaultMarker);

                }
            }
        });
    }


    public void search(String query) {
        if(!markers.isEmpty()) {
            map.removeMapObjects(markers);
            markers.clear();
        }
        try {
            GeoCoordinate tracy = new GeoCoordinate(18.9219, 72.8330);
            DiscoveryRequest request = new SearchRequest(query).setSearchCenter(tracy);
            request.setCollectionSize(5);
            ErrorCode error = request.execute(new ResultListener<DiscoveryResultPage>() {
                @Override
                public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode error) {
                    if (error != ErrorCode.NONE) {
                        Log.e("HERE", error.toString());
                    } else {
                        for(DiscoveryResult discoveryResult : discoveryResultPage.getItems()) {
                            if(discoveryResult.getResultType() == DiscoveryResult.ResultType.PLACE) {
                                PlaceLink placeLink = (PlaceLink) discoveryResult;
                                MapMarker marker = new MapMarker();
                                marker.setCoordinate(placeLink.getPosition());
                                markers.add(marker);
                            }
                        }
                        map.addMapObjects(markers);
                    }
                }
            });
            if( error != ErrorCode.NONE ) {
                Log.e("HERE", error.toString());
            }
        } catch (IllegalArgumentException ex) {
            Log.e("HERE", ex.getMessage());
        }
    }



}
