package com.carlisle.ben.whereshouldieat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DecimalFormat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements Runnable {

	private LocationManager locationManager;
	private Location lastLocation;
	private final InfoFinder infoFinder = new InfoFinder();
	private static int count = 0;
	private boolean success;
	private boolean adsOn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.print(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION));
		setContentView(R.layout.main_layout);
		AdView mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@SuppressWarnings("unused")
	public void initialClick(View v) {
		infoFinder.resetParameters();
		findRestaurant();
	}

	public void changeParameter(View v) {
		if (v instanceof TextView && ((TextView) v).getText().equals("Else"))
			count++;
		if (count == 3) {
			count = 0;
			if (promptDialog())
				return;
		}
		@SuppressWarnings("ConstantConditions") Button b = (Button) v;
		if (b.getText().equals("Closer"))
			infoFinder.lowerRadius();
		else if (b.getText().equals("Cheaper"))
			infoFinder.lowerPrice();
		else if (b.getText().equals("Classier"))
			infoFinder.raisePrice();
		findRestaurant();
	}

	private void findRestaurant() {
		lastLocation = getLastBestLocation();
		if (lastLocation == null) {
			setView(R.id.permission_layout);
			ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, 0);
			return;
		}
		setView(R.id.loading_layout);
		Restaurant.setMainActivity(this);
		infoFinder.setMainAndLocation(this, lastLocation);
		new Thread(infoFinder).start();
	}

	void setSuccess(boolean success) {
		this.success = success;
	}

	public void run() {
		if (success) {
			Restaurant restaurant = infoFinder.getRestaurant();
			setView(R.id.restaurant_layout);
			setRestaurant(restaurant);
		} else {
			setView(R.id.not_connected_layout);
		}
	}

	@Nullable
	private Location getLastBestLocation() {
		if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return null;
		}
		Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		long GPSLocationTime = 0;
		if (locationGPS != null) {
			GPSLocationTime = locationGPS.getTime();
		}

		long NetLocationTime = 0;

		if (locationNet != null) {
			NetLocationTime = locationNet.getTime();
		}

		if (NetLocationTime < GPSLocationTime) {
			return locationGPS;
		} else {
			return locationNet;
		}
	}

	private void setRestaurant(Restaurant restaurant) {
		TextView name = findViewById(R.id.name);
		name.setText(restaurant.getName());
		TextView price = findViewById(R.id.price);
		String priceText = "Price: " + restaurant.getPriceLevel() + "/4";
		price.setText(priceText);
		TextView rating = findViewById(R.id.rating);
		String ratingText = "Rating: " + restaurant.getRating() + " Stars";
		rating.setText(ratingText);
		TextView address = findViewById(R.id.address);
		address.setText(restaurant.getAddress());
		TextView distance = findViewById(R.id.distance);
		String distanceText = new DecimalFormat("#.##").format(restaurant.getLocation().distanceTo(lastLocation) / 1609) + " Miles Away";
		distance.setText(distanceText);
		TextView website = findViewById(R.id.website);
		String websiteText = restaurant.getWebsite();
		website.setText(websiteText );
	}

	private boolean promptDialog() {
		final boolean[] choice = new boolean[1];
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("You've Hit Something Else Three Times")
				.setMessage("Are you sure you want to pick shuffle again? We recommend picking whatever comes next.")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						choice[0] = true;
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						choice[0] = false;
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
		return choice[0];
	}

	private void setView(int layoutId) {
		RelativeLayout layouts = findViewById(R.id.layouts);
		for (int i=0; i < layouts.getChildCount(); i++)
			layouts.getChildAt(i).setVisibility(View.GONE);
		findViewById(layoutId).setVisibility(View.VISIBLE);
	}

	private void ads(final MenuItem i) {
		if (adsOn) {
			AlertDialog.Builder builder;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
			} else {
				builder = new AlertDialog.Builder(this);
			}
			builder.setTitle("Do you need to remove ads?")
					.setMessage("I'm trying to pay for college so please keep ads on if possible thanks.")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							adsOn = false;
							i.setTitle(R.string.on);
							findViewById(R.id.adView).setVisibility(View.INVISIBLE);
							RelativeLayout layouts = (RelativeLayout) findViewById(R.id.layouts);
							RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layouts.getLayoutParams();
							params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							layouts.setLayoutParams(params);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		} else {
			adsOn = true;
			findViewById(R.id.adView).setVisibility(View.VISIBLE);
			i.setTitle(R.string.off);
			RelativeLayout l = (RelativeLayout) findViewById(R.id.layouts);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) l.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			l.setLayoutParams(params);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.layout, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ads(item);
		return true;
	}
}
