package com.appgdx.game;

import android.os.Bundle;

import com.appgdx.game.screen.GameScreen;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	public static final int WIDTH = 1440;
	public static final int HEIGHT = 720;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		initialize(new MenuGame(), config);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GameScreen.running = false;
	}
}
