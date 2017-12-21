package com.carlisle.ben.whereshouldieat;

import android.location.Location;

class Restaurant {

	private String name;
	private int priceLevel;
	private float rating;
	private LocationIdentifier location;
	private String website;
	static final Restaurant INVALID = new Restaurant();

	private Restaurant() {
	}

	static void setMainActivity(MainActivity mainActivity) {
		LocationIdentifier.setMainActivity(mainActivity);
	}

	Restaurant(String name, int priceLevel, float rating, double latitude, double longitude, String website) {
		this.name = name;
		this.priceLevel = priceLevel;
		this.rating = rating;
		this.location = new LocationIdentifier(latitude, longitude);
		this.website = website;
	}

	String getName() {
		return name;
	}

	int getPriceLevel() {
		return priceLevel;
	}

	float getRating() {
		return rating;
	}

	String getAddress() {
		return location.getAddress();
	}

	Location getLocation() {
		return location.getLocation();
	}

	public String getWebsite() {
		return website;
	}
}
