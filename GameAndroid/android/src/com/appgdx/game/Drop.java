package com.appgdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import org.w3c.dom.Text;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
    private Texture stage;                  //платформа
    private Rectangle rectangleMainStage;

    private volatile Player player;                  //игрок

    private volatile Array<Texture> mans;
    private volatile Texture currentMan;
    private Texture manImage;
    private Sound dropSound;
    private Music rainMusic;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Rectangle man;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private volatile int pos = 0;
    public static volatile boolean running = true;


    @Override
    public void create(){

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); //установка размеров камеры
        batch = new SpriteBatch();  //конструктор спрайтов

        stage = new Texture(Gdx.files.internal("stage.png"));
        rectangleMainStage = new Rectangle();
        rectangleMainStage.x = 0;
        rectangleMainStage.y = 0;
        rectangleMainStage.width = Gdx.graphics.getWidth();
        rectangleMainStage.height = 100;

        Array<Texture> rightPlayer = new Array<>();
        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight1.png")));
        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight2.png")));
        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight3.png")));

        Array<Texture> leftPlayer = new Array<>();
        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft1.png")));
        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft2.png")));
        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft3.png")));

        Array<Texture> rightPlayerJump = new Array<>();
        rightPlayerJump.add(new Texture(Gdx.files.internal("man\\manRight\\manRightJumpDown.png")));
        rightPlayerJump.add(new Texture(Gdx.files.internal("man\\manRight\\manRightJumpUp.png")));

        Array<Texture> leftPlayerJump = new Array<>();
        leftPlayerJump.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeftJumpDown.png")));
        leftPlayerJump.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeftJumpUp.png")));

        player = new Player(new Rectangle(0, 100, 100, 150), rightPlayer, leftPlayer, rightPlayerJump, leftPlayerJump, 300);

        /*mans.add(new Texture((Gdx.files.internal("man\\manRight1.png"))));
        mans.add(new Texture((Gdx.files.internal("man\\manRight2.png"))));
        mans.add(new Texture((Gdx.files.internal("man\\manRight3.png"))));*/
        //загрузка текстур
       /* dropImage = new Texture(Gdx.files.internal("drop.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));*/
        //загрузка треков
        //dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        //rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

        //начала прогрывания трека
        //rainMusic.setLooping(true);
        //rainMusic.play();

        Thread thread = new Thread(){
            @Override
            public void run(){
                while(running){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if((player == null) || (player.isDispose()))
                        break;

                    player.nextTexture();

                    if(player.getDirectionMove()){
                        if((player.getRectangle().x + player.getRectangle().width) < Gdx.graphics.getWidth())
                            player.moveRight(20);
                    }else{
                        if((player.getRectangle().x - 20) > 0)
                            player.moveLeft(20);
                    }

                }
            }
        };
        thread.start();
        //raindrops = new Array<Rectangle>();
    }

    /*private void spawnRaindrop(){
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }*/


    private float posy = 0;

    @Override
    public void render(){
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(player.getCurrentTexture(), player.getRectangle().x, player.getRectangle().y,
                player.getRectangle().width, player.getRectangle().height);
        batch.draw(stage, rectangleMainStage.x, rectangleMainStage.y, rectangleMainStage.width, rectangleMainStage.height);
        batch.end();

        if(Gdx.input.isTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if(touchPos.x >= player.getRectangle().x)
                player.setDirectionMove(true);
            else
                player.setDirectionMove(false);
            //player.moveRigth(touchPos.x - 64 / 2);
            //man.y = touchPos.y;
            if((touchPos.y > (player.getRectangle().height + 150)) && (!player.getJump())
            && (player.getRectangle().y <= rectangleMainStage.height)){
                player.setJump(true);
                posy = touchPos.y;
            }
        }

        if(player.getJump())
            player.moveUp(posy * Gdx.graphics.getDeltaTime());

        if((player.getRectangle().y > rectangleMainStage.height) && (!player.getJump()))
            player.moveDown(200 * Gdx.graphics.getDeltaTime());
        /*if(posy != 0){
            player.setDirectionJump(true);
            player.moveUp(posy * Gdx.graphics.getDeltaTime());
        }else{
            player.setDirectionJump(false);
            player.moveDown(20 * Gdx.graphics.getDeltaTime());
        }*/

        /*if(man.x < 0) man.x = 0;
        if(man.y < 0) man.y = 0;
        if(man.y > 480 - 64) man.y = 480 - 64;
        if(man.x > 800-64) man.x = 800 - 64;
        man.x += 1;
        man.y += posy* Gdx.graphics.getDeltaTime();
        if(man.y > 10)
            man.y += (-50) * Gdx.graphics.getDeltaTime();*/

        /*batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops){
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();

        if(Gdx.input.isTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
            bucket.y = touchPos.y;
        }

        if(bucket.x < 0) bucket.x = 0;
        if(bucket.y < 0) bucket.y = 0;
        if(bucket.y > 480 - 64) bucket.y = 480 - 64;
        if(bucket.x > 800-64) bucket.x = 800 - 64;

        if(TimeUtils.nanoTime() - lastDropTime > 1000000000)
            spawnRaindrop();

        for(Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext();){
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if(raindrop.y + 64 < 0)
                iter.remove();
            if(raindrop.overlaps(bucket)){
                dropSound.play();
                iter.remove();
            }
        }*/
    }

    @Override
    public void dispose(){
        stage.dispose();
        manImage.dispose();
        currentMan.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
        player.dispose();
    }
}
