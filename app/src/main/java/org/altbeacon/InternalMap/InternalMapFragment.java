package org.altbeacon.InternalMap;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.altbeacon.InternalMap.CategoryPoint;
import org.altbeacon.InternalMap.PinView;
import org.altbeacon.beaconreference.MapsFragment;
import org.altbeacon.beaconreference.MonitoringActivity;
import org.altbeacon.beaconreference.R;
import org.altbeacon.beaconreference.TabPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class InternalMapFragment extends Fragment {

    private List<CategoryPoint> categoryPoints = new ArrayList<>();
    private Bitmap pin;
    FloatingActionButton fab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.internal_map_fragment, container, false);

        PinView imageView = view.findViewById(R.id.internalMap);
        imageView.setImage(ImageSource.resource(R.drawable.sample_building_plan));
        imageView.setView(view);

        fab = view.findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);

        MonitoringActivity activity = (MonitoringActivity) getActivity();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.GONE);
                MapsFragment mapFrag = activity.returnMapsFragment();
                mapFrag.setFabVisible();
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Lifecycle", "On Resume Called");
        fab.setVisibility(View.VISIBLE);;
    }
}
