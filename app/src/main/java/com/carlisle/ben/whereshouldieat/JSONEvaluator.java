package com.carlisle.ben.whereshouldieat;

import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

class JSONEvaluator implements Runnable {

	private static final String key = "AIzaSyD5w507o2kUqBSCBMdGE55egk1Q4l6zipA";
	private static final String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	private static final String detailsLink = "https://maps.googleapis.com/maps/api/place/details/json?";
	private Restaurant restaurant;
	private int radius = 2000;
	private int maxPrice = 5;
	private int minPrice = 0;
	private Location location;
	private boolean radiusLowered = false;

	void setLocation(Location location) {
		this.location = location;
	}

	public void run() {
		try {
			for (int i=0; i < 2; i++) {
				Log.w("url", buildURL(location));
				URL url = new URL(buildURL(location));
				Scanner scanner = new Scanner(url.openConnection().getInputStream());
				ArrayList<Restaurant> restaurants = new ArrayList<>();
				double longitude;
				double latitude;
				String name;
				int priceLevel;
				float rating;
				String website = "Website Not Found :(";
				String currentLine;
				int startPos;
				int endPos;
				int pos = 0;
				while (scanner.hasNext()) {
					currentLine = scanner.nextLine();
					if (currentLine.contains("geometry")) {
						Log.w("pos", String.valueOf(pos++));
						longitude = -1;
						latitude = -1;
						name = null;
						priceLevel = -1;
						rating = -1;
						while (scanner.hasNext()) {
							currentLine = scanner.nextLine();
							if (currentLine.contains("\"lat\"")) {
								startPos = currentLine.indexOf(":") + 2;
								endPos = currentLine.indexOf(",");
								latitude = Double.parseDouble(currentLine.substring(startPos, endPos));
							} else if (currentLine.contains("\"lng\"")) {
								startPos = currentLine.indexOf(":") + 2;
								longitude = Double.parseDouble(currentLine.substring(startPos));
							} else if (currentLine.contains("\"name\"")) {
								startPos = currentLine.indexOf(":") + 3;
								endPos = currentLine.indexOf(",") - 1;
								name = currentLine.substring(startPos, endPos);
//							} else if (currentLine.contains("photo_reference")) {
//								startPos = currentLine.indexOf(":") + 3;
//								endPos = currentLine.indexOf(",")-1;
//								String photoReference = currentLine.substring(startPos, endPos);
//								photoLocation = getPhotoLocation(photoReference);
							} else if (currentLine.contains("\"price_level\"")) {
								startPos = currentLine.indexOf(":") + 2;
								endPos = currentLine.indexOf(",");
								priceLevel = Integer.parseInt(currentLine.substring(startPos, endPos));
							} else if (currentLine.contains("\"rating\"")) {
								startPos = currentLine.indexOf(":") + 2;
								endPos = currentLine.indexOf(",");
								rating = Float.parseFloat(currentLine.substring(startPos, endPos));
							} else if (currentLine.contains("\"reference\"")) {
								startPos = currentLine.indexOf(":") + 3;
								endPos = currentLine.indexOf(",")-1;
								String reference = currentLine.substring(startPos, endPos);
								Log.w("re",reference);
								website = getWebsite(reference);
								break;
							}
						}
						Log.w("name", name);
						Log.w("priceLevel", String.valueOf(priceLevel));
						Log.w("rating", String.valueOf(rating));
						Log.w("latitude", String.valueOf(latitude));
						Log.w("longitude", String.valueOf(longitude));

						if (name != null && priceLevel != -1 && rating != -1 && latitude != -1 && longitude != -1)
							restaurants.add(new Restaurant(name, priceLevel, rating, latitude, longitude, website));
					}
				}
				Log.v("Size", String.valueOf(restaurants.size()));
				if (restaurants.size() == 0) {
					if (radiusLowered) {
						radiusLowered = false;
						radius *= 2;
						continue;
					}
					restaurant = Restaurant.INVALID;
					return;
				}
				int randomNumber = (int) (Math.random() * restaurants.size());
				restaurant = restaurants.get(randomNumber);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			restaurant = Restaurant.INVALID;
		}
	}

	private String buildURL(Location location) {
		return link + "key=" + key
				+ "&radius=" + radius
				+ "&maxprice=" + maxPrice
				+ "&minprice=" + minPrice
				+ "&type=restaurant"
				+ "&openNow=true&location="
				+ location.getLatitude()
				+ "," + location.getLongitude();
	}

	void resetAll() {
		radius = 1000;
		minPrice = 0;
		maxPrice = 5;
		location = null;
		restaurant = null;
	}

	Restaurant getRestaurant() {
		return restaurant;
	}

	void raisePrice() {
		minPrice= Math.min(4,++minPrice);
		maxPrice= Math.min(4,++maxPrice);
	}

	void lowerPrice() {
		minPrice= Math.max(1,--minPrice);
		maxPrice= Math.max(1,--maxPrice);
	}

	void lowerRadius() {
		radius /= 2;
		radiusLowered = true;
	}

	private String getWebsite(String reference) {
		try {
			Log.w("is",reference);
			String url = detailsLink + "reference=" + reference + "&key="+key;
			Scanner scanner = new Scanner(new URL(url).openConnection().getInputStream());
			String currentLine;
			while (scanner.hasNext()) {
				currentLine = scanner.nextLine();
				if (currentLine.contains("\"website\"")) {
					int startPos = currentLine.indexOf(":") + 3;
					int endPos = currentLine.indexOf("\"", startPos);
					return currentLine.substring(startPos, endPos);
				}
			}
		} catch (IOException e) {
			Log.w("we","notfound");
			e.printStackTrace();
		}
		return "Website Not Found :(";
	}
}
