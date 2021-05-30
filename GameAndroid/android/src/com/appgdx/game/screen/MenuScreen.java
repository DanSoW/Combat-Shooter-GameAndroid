package com.appgdx.game.screen;

import com.appgdx.game.AndroidLauncher;
import com.appgdx.game.MenuGame;
import com.appgdx.game.screen.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen implements Screen {

    private SpriteBatch _batch;
    private OrthographicCamera _camera;

    private Texture _startButtonTexture;
    private Texture _ratingButtonTexture;
    private Texture _exitButtonTexture;
    private Rectangle _startButtonRect;
    private Rectangle _ratingButtonRect;
    private Rectangle _exitButtonRect;

    private MenuGame _menu;

    public MenuScreen(MenuGame menu){
        this._menu = menu;
        float height = Gdx.graphics.getHeight();
        float width = Gdx.graphics.getWidth();

        _camera = new OrthographicCamera(width, height);
        _camera.setToOrtho(false, AndroidLauncher.WIDTH, AndroidLauncher.HEIGHT);
        _batch = new SpriteBatch();

        _startButtonTexture = new Texture(Gdx.files.internal("menu\\texture_start.png"));
        _ratingButtonTexture = new Texture(Gdx.files.internal("menu\\texture_rating.png"));
        _exitButtonTexture = new Texture(Gdx.files.internal("menu\\texture_exit.png"));

        _startButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _startButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 200,
                _startButtonTexture.getWidth(), _startButtonTexture.getHeight());

        _ratingButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _ratingButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 350,
                _ratingButtonTexture.getWidth(), _ratingButtonTexture.getHeight());

        _exitButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _exitButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 500,
                _exitButtonTexture.getWidth(), _exitButtonTexture.getHeight());;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        _batch.setProjectionMatrix(_camera.combined);
        _batch.begin();
        _batch.draw(_startButtonTexture, _startButtonRect.x, _startButtonRect.y,
                _startButtonRect.width, _startButtonRect.height);
        _batch.draw(_ratingButtonTexture, _ratingButtonRect.x, _ratingButtonRect.y,
                _ratingButtonRect.width, _ratingButtonRect.height);
        _batch.draw(_exitButtonTexture, _exitButtonRect.x, _exitButtonRect.y,
                _exitButtonRect.width, _exitButtonRect.height);
        _batch.end();

        if(Gdx.input.justTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            _camera.unproject(touchPos);

            if(_startButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _startButtonRect.width, _startButtonRect.height))){
                this._menu.setScreen(new GameScreen());
            }else if(_ratingButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _ratingButtonRect.width, _ratingButtonRect.height))){
                System.out.println("RATING");
            }else if(_exitButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _exitButtonRect.width, _exitButtonRect.height))){
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        _startButtonTexture.dispose();
        _ratingButtonTexture.dispose();
        _exitButtonTexture.dispose();
        _batch.dispose();
    }
}
