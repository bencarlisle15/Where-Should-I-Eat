package com.carlisle.ben.whereshouldieat;

import android.location.Location;

class InfoFinder implements Runnable {

	private final JSONEvaluator jsonEvaluator = new JSONEvaluator();
	private MainActivity mainActivity;
	private Location location;

	void setMainAndLocation(MainActivity mainActivity, Location location) {
		this.mainActivity = mainActivity;
		this.location = location;
	}

	@Override
	public void run() {
		jsonEvaluator.setLocation(location);
		new Thread(jsonEvaluator).start();
		long startTime = System.currentTimeMillis();
		while (jsonEvaluator.getRestaurant() == null) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (System.currentTimeMillis() - startTime >= 10000) {
				mainActivity.setSuccess(false);
				mainActivity.runOnUiThread(mainActivity);
				return;
			}
		}
		mainActivity.setSuccess(getRestaurant() != Restaurant.INVALID);
		mainActivity.runOnUiThread(mainActivity);
	}

	public void raisePrice() {
		jsonEvaluator.raisePrice();
	}

	public void lowerPrice() {
		jsonEvaluator.lowerPrice();
	}

	public void lowerRadius() {
		jsonEvaluator.lowerRadius();
	}

	Restaurant getRestaurant() {
		return jsonEvaluator.getRestaurant();
	}

	public void resetParameters() {
		jsonEvaluator.resetAll();
	}
}
