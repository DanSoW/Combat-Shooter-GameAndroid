package com.appgdx.game;

import com.appgdx.game.screen.MenuScreen;
import com.badlogic.gdx.Game;

public class MenuGame extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
