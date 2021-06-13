package com.appgdx.game.screen;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.appgdx.game.AndroidLauncher;
import com.appgdx.game.ManagerDatabase;
import com.appgdx.game.MenuGame;
import com.appgdx.game.data.RatingData;
import com.appgdx.game.data.RegisterData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Collections;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class MenuScreen implements Screen {

    private SpriteBatch _batch;
    private OrthographicCamera _camera;

    private Texture _startButtonTexture;
    private Texture _ratingButtonTexture;
    private Texture _exitButtonTexture;
    private Texture _registerButtonTexture;
    private Texture _authorizationButtonTexture;
    private Texture _backButtonTexture;

    private Rectangle _startButtonRect;
    private Rectangle _ratingButtonRect;
    private Rectangle _exitButtonRect;
    private Rectangle _registerButtonRect;
    private Rectangle _authorizationButtonRect;
    private Rectangle _backButtonRect;

    public static MenuGame menu;

    //данные о пользователе
    public static String nameUser;
    public static String passUser;

    //метка о идентификации
    public static boolean identifier;
    public static boolean work;
    private static boolean viewRating;  //просмотр рэйтинга

    public static ManagerDatabase dbManager;

    private static Array<RegisterData> registerData = new Array<>();
    private static Array<RatingData> ratingData = new Array<>();

    private Thread readData;

    private String[] errors = new String[]{
            "Error: username and password should not be empty",
            "Error: an account with this username is already present in the system!",
            "Error: wrong password!",
            "Error: this user is not registered in the system!"
    };

    private static int indexError = (-1);

    public MenuScreen(MenuGame menu) throws Exception {
        //настройка связей между окнами
        this.menu = menu;
        GameScreen.menuScreen = this;

        work = true;
        identifier = false;
        viewRating = false;

        float height = Gdx.graphics.getHeight();
        float width = Gdx.graphics.getWidth();

        _camera = new OrthographicCamera(width, height);
        _camera.setToOrtho(false, AndroidLauncher.WIDTH, AndroidLauncher.HEIGHT);
        _batch = new SpriteBatch();

        _startButtonTexture = new Texture(Gdx.files.internal("menu\\texture_start.png"));
        _ratingButtonTexture = new Texture(Gdx.files.internal("menu\\texture_rating.png"));
        _exitButtonTexture = new Texture(Gdx.files.internal("menu\\texture_exit.png"));
        _authorizationButtonTexture = new Texture(Gdx.files.internal("menu\\texture_authorization.png"));
        _registerButtonTexture = new Texture(Gdx.files.internal("menu\\texture_register.png"));
        _backButtonTexture = new Texture(Gdx.files.internal("menu\\texture_back.png"));

        _startButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _startButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 200,
                _startButtonTexture.getWidth(), _startButtonTexture.getHeight());

        _registerButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _startButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 200,
                _startButtonTexture.getWidth(), _startButtonTexture.getHeight());

        _ratingButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _ratingButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 350,
                _ratingButtonTexture.getWidth(), _ratingButtonTexture.getHeight());

        _authorizationButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _ratingButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 450,
                _ratingButtonTexture.getWidth(), _ratingButtonTexture.getHeight());

        _exitButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _exitButtonTexture.getWidth() / 2, AndroidLauncher.HEIGHT - 500,
                _exitButtonTexture.getWidth(), _exitButtonTexture.getHeight());

        _backButtonRect = new Rectangle(AndroidLauncher.WIDTH / 2 - _exitButtonTexture.getWidth() / 2, 50,
                _exitButtonTexture.getWidth(), _exitButtonTexture.getHeight());

        dbManager = new ManagerDatabase(AndroidLauncher.context);
        readData = new Thread(){
            @Override
            public void run(){
                while(work){
                    try {
                        ReadAllRegisterData();
                        ReadAllRatingData();
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        readData.start();
    }

    private static void ReadAllRegisterData() throws Exception {
        registerData.clear();
        SQLiteDatabase database = dbManager.getWritableDatabase();
        Cursor cursor = database.query(ManagerDatabase.REGISTER, null, null,
                null, null, null, null);
        while(cursor.moveToNext()){
            int loginIndex= cursor.getColumnIndex(ManagerDatabase.LOGIN),
                    passIndex = cursor.getColumnIndex(ManagerDatabase.PASSWORD);
            registerData.add(new RegisterData(cursor.getString(loginIndex), cursor.getString(passIndex)));
        }
        cursor.close();
        database.close();
    }

    private static void ReadAllRatingData() throws Exception {
        ratingData.clear();
        SQLiteDatabase database = dbManager.getWritableDatabase();
        Cursor cursor = database.query(ManagerDatabase.RATING, null, null,
                null, null, null, null);
        while(cursor.moveToNext()){
            int loginIndex= cursor.getColumnIndex(ManagerDatabase.FK_LOGIN),
                    scoreIndex = cursor.getColumnIndex(ManagerDatabase.SCORE);
            ratingData.add(new RatingData(cursor.getString(loginIndex), cursor.getInt(scoreIndex)));
        }

        cursor.close();
        database.close();
    }

    private static void addRegisterData(){
        if((passUser == null) || (passUser.length() == 0)
        || (nameUser == null) || (nameUser.length() == 0)){
            indexError = 0;
            return;
        }

        for (RegisterData data:
             registerData) {
            if(data.login.equals(nameUser)){
                indexError = 1;
                return;
            }
        }

        SQLiteDatabase database = dbManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ManagerDatabase.LOGIN, nameUser);
        contentValues.put(ManagerDatabase.PASSWORD, passUser);
        database.insert(ManagerDatabase.REGISTER, null, contentValues);
        database.close();
    }

    public static void deleteRating(String login){
        SQLiteDatabase database = dbManager.getWritableDatabase();
        database.delete(ManagerDatabase.RATING, ManagerDatabase.FK_LOGIN + " = ?",
                new String[]{
                        login
                });
        database.close();
    }

    public static void updateRating(int score) throws Exception {
        int index = (-1);

        ReadAllRatingData();
        for(int i = 0; i < ratingData.size; i++){
            if(ratingData.get(i).login.equals(nameUser)){
                index = i;
            }
        }

        if(index >= 0){
            SQLiteDatabase database = dbManager.getWritableDatabase();
            ContentValues cv = new ContentValues();
            //cv.put(ManagerDatabase.FK_LOGIN, nameUser);
            cv.put(ManagerDatabase.SCORE, score);
            database.update(ManagerDatabase.RATING, cv, ManagerDatabase.FK_LOGIN + " = ?",
                    new String[]{
                            ratingData.get(index).login
                    });
            database.close();
        }else if((index < 0) && (isExistRegister())){
            SQLiteDatabase database = dbManager.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ManagerDatabase.FK_LOGIN, nameUser);
            contentValues.put(ManagerDatabase.SCORE, score);
            database.insert(ManagerDatabase.RATING, null, contentValues);
            database.close();
        }

        ReadAllRatingData();
    }

    public static int getRatingPlayer() throws Exception {
        if(!isExistRating()){
            updateRating(0);
            return 0;
        }

        for(int i = 0; i < ratingData.size; i++){
            if(ratingData.get(i).login.equals(nameUser)){
                return ratingData.get(i).score;
            }
        }

        return 0;
    }

    private static boolean isExistRegister(){
        for(RegisterData data : registerData){
            if((data.login.equals(nameUser)) && (data.password.equals(passUser))){
                return true;
            }
        }

        return false;
    }

    private static boolean isExistRating(){
        for(RatingData data : ratingData){
            if((data.login.equals(nameUser))){
                return true;
            }
        }

        return false;
    }

    private void Authorization(){
        if((passUser == null) || (passUser.length() == 0)
                || (nameUser == null) || (nameUser.length() == 0)){
            indexError = 0;
            return;
        }

        int index = (-1);
        for(int i = 0; i < registerData.size; i++){
            if((passUser.equals(registerData.get(i).password)) && (nameUser.equals(registerData.get(i).login))){
                index = i;
                break;
            }else if((!passUser.equals(registerData.get(i).password)) && (nameUser.equals(registerData.get(i).login))){
                indexError = 3;
            }
        }

        if(index < 0){
            indexError = 2;
            return;
        }

        identifier = true;
    }

    @Override
    public void show() {

    }

    public void enterData(boolean authorization){
        Gdx.input.getTextInput(new TextInputListener() {
            @Override
            public void input(String text) {
                passUser = text;
                indexError = (-1);
                if(authorization){
                    Authorization();
                }else{
                    addRegisterData();
                    if(indexError < 0){
                        indexError = (-2);
                    }
                }
            }

            @Override
            public void canceled() {

            }
        }, "Password", "", "");

        Gdx.input.getTextInput(new TextInputListener() {
            @Override
            public void input(String text) {
                nameUser = text;
            }

            @Override
            public void canceled() {

            }
        }, "Login", "name", "");
    }

    public Array<RatingData> sortRatingData(Array<RatingData> rating){
        if(rating.size == 0)
            return null;

        for(int i = 0; i < rating.size; i++){
            System.out.println(rating.get(i).login);
        }

        boolean flag = true;
        while(flag){
            flag = false;
            for(int i = 0; i < (rating.size - 1); i++){
                if(rating.get(i).score < (rating.get(i + 1).score)){
                    int score1 = rating.get(i + 1).score, score2 = rating.get(i).score;
                    String login1 = rating.get(i + 1).login.substring(0),
                            login2 = rating.get(i).login.substring(0);

                    rating.get(i + 1).score = score2;
                    rating.get(i + 1).login = login2;

                    rating.get(i).score = score1;
                    rating.get(i).login = login1;
                    flag = true;
                }
            }
        }

        return rating;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        _batch.setProjectionMatrix(_camera.combined);
        _batch.begin();

        if((identifier) && (!viewRating)){
            _batch.draw(_startButtonTexture, _startButtonRect.x, _startButtonRect.y,
                    _startButtonRect.width, _startButtonRect.height);
            _batch.draw(_ratingButtonTexture, _ratingButtonRect.x, _ratingButtonRect.y,
                    _ratingButtonRect.width, _ratingButtonRect.height);
            _batch.draw(_exitButtonTexture, _exitButtonRect.x, _exitButtonRect.y,
                    _exitButtonRect.width, _exitButtonRect.height);
        }else if(viewRating){
            _batch.draw(_backButtonTexture, _backButtonRect.x, _backButtonRect.y,
                    _backButtonRect.width, _backButtonRect.height);

            BitmapFont font;
            CharSequence information = "Score";
            font = new BitmapFont();
            font.getData().setScale(2);
            font.draw(_batch, information, AndroidLauncher.WIDTH / 2 - information.length()/2 * 12, AndroidLauncher.HEIGHT - 50);

            Array<RatingData> rating = sortRatingData(ratingData);
            if(rating != null){
                for(int i = 0, j = (rating.size > 10)? 10 : rating.size; i < j; i++){
                    CharSequence info = String.valueOf((i + 1)) + ". " + rating.get(i).login + " - " + rating.get(i).score.toString();
                    font.draw(_batch, info, AndroidLauncher.WIDTH / 2 - 100, AndroidLauncher.HEIGHT - ((i + 1)*10 + (100 + (i * 25))));
                }
            }

        }else{
            _batch.draw(_registerButtonTexture, _registerButtonRect.x, _registerButtonRect.y,
                    _registerButtonRect.width, _registerButtonRect.height);
            _batch.draw(_authorizationButtonTexture, _authorizationButtonRect.x, _authorizationButtonRect.y,
                    _authorizationButtonRect.width, _authorizationButtonRect.height);

            if(indexError >= 0){
                BitmapFont font = new BitmapFont();
                font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                font.getData().setScale(2);
                CharSequence information = errors[indexError];
                font.draw(_batch, information, (AndroidLauncher.WIDTH/2) - (information.length()/2 * 12),  _authorizationButtonRect.y - 100);
            }else if(indexError == (-2)){
                BitmapFont font = new BitmapFont();
                font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                font.getData().setScale(2);
                CharSequence information = "The account is registered!";
                font.draw(_batch, information, (AndroidLauncher.WIDTH/2) - (information.length()/2 * 12),  _authorizationButtonRect.y - 100);
            }
        }
        _batch.end();

        if(Gdx.input.justTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            _camera.unproject(touchPos);

            if(identifier){
                if(viewRating && (_backButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _backButtonRect.width, _backButtonRect.height)))){
                    viewRating = false;
                }

                if(_startButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _startButtonRect.width, _startButtonRect.height))){
                    try {
                        this.menu.setScreen(new GameScreen());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(_ratingButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _ratingButtonRect.width, _ratingButtonRect.height))){
                    viewRating = true;
                }else if(_exitButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _exitButtonRect.width, _exitButtonRect.height))){
                    Gdx.app.exit();
                }
            }else{
                if(_registerButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _registerButtonRect.width, _registerButtonRect.height))){
                    enterData(false);
                }else if(_authorizationButtonRect.overlaps(new Rectangle(touchPos.x, touchPos.y, _authorizationButtonRect.width, _authorizationButtonRect.height))){
                    enterData(true);
                }
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
        work = false;
    }
}
