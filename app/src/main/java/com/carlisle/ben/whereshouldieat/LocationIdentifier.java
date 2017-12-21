package com.carlisle.ben.whereshouldieat;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

class LocationIdentifier {
	private final double latitude;
	private final double longitude;
	private String address;
	private boolean addressFound = true;
	private static MainActivity mainActivity;
	private final Location location = new Location("");

	LocationIdentifier(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		try {
			createStreetAddress();
		} catch (IOException e) {
			addressFound = false;
			e.printStackTrace();
		}
	}

	static void setMainActivity(MainActivity mA) {
		mainActivity = mA;
	}

	private void createStreetAddress() throws IOException {
		Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
		List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
		address = addresses.get(0).getAddressLine(0);
	}

	String getAddress() {
		if (addressFound)
			return address;
		return "Address not found";
	}

	Location getLocation() {
		return location;
	}
}
