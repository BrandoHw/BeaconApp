package org.altbeacon.InternalMap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.WorkTracking.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PinView extends SubsamplingScaleImageView implements onPinClickListener {
    private onPinClickListener onPinClickListener = this;
    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private List<CategoryPoint> categoryPoints;
    private PointF sPin;
    private Bitmap pin;
    private View view;

    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        categoryPoints = new ArrayList<>();
        initialise();
        initTouchListener();
    }

    public void setView(View v) {
        view = v;
    }


    public void setPin(PointF sPin) {
        this.sPin = sPin;
        initialise();
        invalidate();
    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.pushpin_blue);
        float w = (density / 420f) * pin.getWidth();
        float h = (density / 420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int) w, (int) h, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }

        paint.setAntiAlias(true);

        if (categoryPoints != null)
             loadData();

            for (CategoryPoint categoryPoint : categoryPoints) {
                Bitmap pinIcon = getPinImage(categoryPoint.getImage());
                if (categoryPoint.getPointF() != null && pinIcon != null) {
                    PointF point = sourceToViewCoord(categoryPoint.getPointF());
                    float vX = point.x - (pinIcon.getWidth() / 2);
                    float vY = point.y - pinIcon.getHeight();
                    canvas.drawBitmap(pinIcon, vX, vY, paint);
                }
            }

    }


    public void addCategories(List<CategoryPoint> categoryPoints) {
        this.categoryPoints = categoryPoints;
        invalidate();
    }

    public void removeCategories(List<CategoryPoint> categoryPoints) {
        this.categoryPoints.removeAll(categoryPoints);
        invalidate();
    }

    public void removeAllCategories() {
        this.categoryPoints.clear();
        invalidate();
    }

    public void setOnPinClickListener(onPinClickListener listener) {
        onPinClickListener = listener;
    }


    private void initTouchListener() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isReady() && categoryPoints != null) {
                    PointF tappedCoordinate = new PointF(e.getX(), e.getY());
                    Log.i("TappedCoordinates", tappedCoordinate.toString());

                    //Move into for loop if using multiple pin images, change to categoryPoint.getImage();
                    Bitmap clickArea;
                    if (categoryPoints.isEmpty()){
                        clickArea = getPinImage(0);
                    }
                    else {
                        clickArea = getPinImage(categoryPoints.get(0).getImage());
                    }
                    int clickAreaWidth = clickArea.getWidth();
                    int clickAreaHeight = clickArea.getHeight();
                    for (CategoryPoint categoryPoint : categoryPoints) {
                        PointF categoryCoordinate = sourceToViewCoord(categoryPoint.getPointF());
                        int categoryX = (int) (categoryCoordinate.x);
                        int categoryY = (int) (categoryCoordinate.y - clickAreaHeight / 2);
                        if (tappedCoordinate.x >= categoryX - clickAreaWidth / 2
                                && tappedCoordinate.x <= categoryX + clickAreaWidth / 2
                                && tappedCoordinate.y >= categoryY - clickAreaHeight / 2
                                && tappedCoordinate.y <= categoryY + clickAreaHeight / 2) {
                            onPinClickListener.onPinClick(categoryPoint, categoryCoordinate);
                            return true;
                        }
                    }
                    listDialog(tappedCoordinate);
                }
                return true;
            }
        });
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // pass the events to the gesture detector
                // a return value of true means the detector is handling it
                // a return value of false means the detector didn't
                // recognize the event
                return gestureDetector.onTouchEvent(event);
            }
        });
    }


    @Override
    public void onPinClick(CategoryPoint categoryPoint, PointF tappedCoordinates) {
        Log.i("Pin Click Test", categoryPoint.getCategory());
        final PopupWindow popupWindow = new PopupWindow(this); // inflet your layout or diynamic add view

        View view;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.menu_pin, null);

        Button button1 = view.findViewById(R.id.view_button);
        Button button2 = view.findViewById(R.id.delete_button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Popupwindow", "Working");
                Intent intent = new Intent(getContext(), ViewPinActivity.class);
                intent.putExtra("Location", categoryPoint.getCategory());
                getContext().startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final SharedPreferences spPinnedBeacons = getContext().getSharedPreferences("myPinnedBeacons", 0);
                SharedPreferences.Editor editor = spPinnedBeacons.edit();
                editor.remove(categoryPoint.getUid());
                editor.commit();
                for (CategoryPoint cP:categoryPoints) {
                    if (cP.getUid().equals(categoryPoint.getUid())) {
                        categoryPoints.remove(cP);
                        saveData();
                        loadData();
                        invalidate();
                        break;
                    }

                }
            }

        });
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        popupWindow.showAtLocation(this.view, Gravity.TOP | Gravity.LEFT, (int) tappedCoordinates.x, (int) tappedCoordinates.y + 100);
    }

    public void addPin(PointF tappedCoordinate, String location, String uid) {
        CategoryPoint newPin = new CategoryPoint(location, uid, 0, tappedCoordinate);
        for (CategoryPoint categoryPoint : categoryPoints) {
            if (categoryPoint.getPointF() == newPin.getPointF()) {
                Log.i("GSON", "Duplicate Pin");
                newPin = null;
            }
        }
        if (newPin != null) {
            Log.i("GSON", "Pin Added");
            categoryPoints.add(newPin);
        }
    }

    private void saveData() {
        SharedPreferences sp = getContext().getSharedPreferences("myPins", 0);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(categoryPoints);
        Log.i("GSONS", json);
        editor.putString("pinsList", json);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences sp = getContext().getSharedPreferences("myPins", 0);
        Gson gson = new Gson();
        String json = sp.getString("pinsList", null);
        if (json != null) {
            Log.i("GSONL", json);
            Type type = new TypeToken<ArrayList<CategoryPoint>>() {
            }.getType();
            categoryPoints = gson.fromJson(json, type);

            if (categoryPoints == null) {
                categoryPoints = new ArrayList<>();
            }
        }
    }

    //This method is used in the case where multiple pin images are desired
    private Bitmap getPinImage(int pinImage) {
        switch (pinImage) {
            case 0:
                float density = getResources().getDisplayMetrics().densityDpi;
                pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.pushpin_blue);
                float w = (density / 420f) * pin.getWidth();
                float h = (density / 420f) * pin.getHeight();
                pin = Bitmap.createScaledBitmap(pin, (int) w, (int) h, true);
                return pin;
            default:
                return pin;
        }
    }

    //MyBeacons is a list of beacons with a saved location, key = instance id, value = given location
    //MyPinnedBeacons is a list of the saved beacons that have been pinned, key = instance id, value = given location
    //List of pins is stored as an ArrayList of objects called CategoryPoints, Category = given location, pointF = coordinates

    void listDialog(PointF tappedCoordinate){
        MaterialAlertDialogBuilder listDialogBuilder = new MaterialAlertDialogBuilder(getContext(), R.style.myDialog);
        listDialogBuilder.setTitle("Select a Beacon: ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
        final ArrayList<String> beaconUID = new ArrayList<>();
        SharedPreferences spBeacons = getContext().getSharedPreferences("myBeacons", 0);
        final SharedPreferences spPinnedBeacons = getContext().getSharedPreferences("myPinnedBeacons", 0);
        Map<String, ?> allBeacons = spBeacons.getAll();
        for (Map.Entry<String, ?> entry : allBeacons.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            Log.d("map values", spPinnedBeacons.getString(entry.getKey().toString(), "None"));
            if (!spPinnedBeacons.contains(entry.getKey())){
                arrayAdapter.add(entry.getValue().toString());
                beaconUID.add(entry.getKey().toString());
            }
        }

        listDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        listDialogBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("which", String.valueOf(which));
                String strName = arrayAdapter.getItem(which);
                String strKey = beaconUID.get(which);
                MaterialAlertDialogBuilder builderInner = new MaterialAlertDialogBuilder(getContext(), R.style.myDialog);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Beacon is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        SharedPreferences.Editor editor = spPinnedBeacons.edit();
                        editor.putString(strKey, strName);
                        editor.commit();
                        addPin(viewToSourceCoord(tappedCoordinate), strName, strKey);
                        saveData();
                        invalidate();
                    }
                });
                builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        listDialogBuilder.show();
    }



}