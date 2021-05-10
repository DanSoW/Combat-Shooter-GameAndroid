package com.appgdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameLogic extends ApplicationAdapter {
    private class ObjectTexture{                         //объект с текстурой
        public Texture texture;
        public Rectangle rect;

        public ObjectTexture(Texture texture, Rectangle rect) {
            this.texture = texture;
            this.rect = rect;
        }
    }

    private Array<ObjectTexture> _platforms;    //платформы
    private ObjectTexture _door;                //дверь
    private volatile Player _player;            //игрок
    private volatile Array<Enemy> _enemies;     //монстры
    private volatile Bullet _bullet;            //пуля
    private ObjectTexture _attack;              //управляющая кнопка выстрела

    private volatile Array<Texture> _mans;
    private volatile Texture _currentMan;
    private Texture _manImage;
    private Sound dropSound;
    private Music rainMusic;
    private SpriteBatch _batch;
    private OrthographicCamera _camera;
    private Rectangle man;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    public static volatile boolean running = true;


    @Override
    public void create(){

        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); //установка размеров камеры
        _batch = new SpriteBatch();  //конструктор спрайтов

        //установка местоположения главной платформы
        Rectangle rectangleMainStage;
        rectangleMainStage = new Rectangle();
        rectangleMainStage.x = 0;
        rectangleMainStage.y = 0;
        rectangleMainStage.width = Gdx.graphics.getWidth();
        rectangleMainStage.height = Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() - 100);

        _attack = new ObjectTexture(new Texture(Gdx.files.internal("attack/attack.png")), new Rectangle(
                Gdx.graphics.getWidth() - 150, 0,
                100, 100
        ));

        //установка местоположения не главных платформ
        _platforms = new Array<>();
        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), rectangleMainStage));
        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), new Rectangle()));
        _platforms.get(1).rect.x = 0;
        _platforms.get(1).rect.y = 250;
        _platforms.get(1).rect.height = 30;
        _platforms.get(1).rect.width = 400;

        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/lava_platform.jpg")), new Rectangle()));
        _platforms.get(2).rect.x = 600;
        _platforms.get(2).rect.y = 400;
        _platforms.get(2).rect.height = 30;
        _platforms.get(2).rect.width = 400;

        _door = new ObjectTexture(new Texture(Gdx.files.internal("door/door_finish.png")), new Rectangle());
        _door.rect.x = 0;
        _door.rect.y = 280;
        _door.rect.height = 150;
        _door.rect.width = 100;

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

        _player = new Player(new Rectangle(0, Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() - 100) + 50, 100, 150), rightPlayer, leftPlayer, rightPlayerJump, leftPlayerJump, 20,
                40, 0, Gdx.graphics.getHeight(), 0, Gdx.graphics.getWidth());

        _enemies = new Array<Enemy>();
        Array<Texture> rightEnemy = new Array<Texture>();
        rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight1.png")));
        rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight2.png")));
        rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight3.png")));
        rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight4.png")));
        rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight5.png")));

        Array<Texture> leftEnemy = new Array<Texture>();
        leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft1.png")));
        leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft2.png")));
        leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft3.png")));
        leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft4.png")));
        leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft5.png")));

        _enemies.add(new Enemy(
                new Rectangle(_platforms.get(2).rect.x, _platforms.get(2).rect.y + _platforms.get(2).rect.getHeight(), 150, 150),
                rightEnemy,
                leftEnemy,
                _platforms.get(2).rect,
                10
        ));

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
                //функция потока, реализующая логику перемещения по пространству
                while(running){
                    if((_player == null) || (_player.isDispose()))
                        break;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(_player.getRectangle().overlaps(_door.rect)){
                        running = false;
                        break;
                    }

                    _player.nextTexture();

                    if(_player.getDirectionMove()){
                        if(collisionEnemy(_player.getRectangle())){
                            _player.setHp(_player.getHp() - 20);
                            _player.setDirectionMove(false);
                            if(!collisionPlatforms(_player.getRectangle(), true))
                                _player.moveLeft();
                        }else if(!collisionPlatforms(_player.getRectangle(), true))
                            _player.moveRight();
                    }else{
                        if(collisionEnemy(_player.getRectangle())){
                            _player.setHp(_player.getHp() - 20);
                            _player.setDirectionMove(true);
                            if(!collisionPlatforms(_player.getRectangle(), true))
                                _player.moveLeft();
                        }else if(!collisionPlatforms(_player.getRectangle(), true))
                            _player.moveLeft();
                    }

                    for(int i = 0; i < _enemies.size; i++){
                        if((_bullet != null) && (_bullet.getActive()) && _bullet.getRectangle().overlaps(_enemies.get(i).getRectangle())){
                            _enemies.get(i).setHp(0);
                            _bullet.dispose();
                            _bullet = null;
                            _player.setScore(_player.getScore() + 100);
                        }
                        _enemies.get(i).nextTexture();
                        _enemies.get(i).move();
                    }

                    if((_bullet != null) && (_bullet.getActive())){
                        _bullet.move();
                    }
                }
            }
        };
        thread.start(); //запуск потока, который обрабатывает движение игрока
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

    private boolean collisionEnemy(Rectangle rect){
        if(_enemies.size == 0)
            return false;
        for(Enemy i : this._enemies){
            if(rect.overlaps(i.getRectangle()))
                return true;
        }

        return false;
    }

    private boolean collisionPlatforms(Rectangle rect, boolean flag){
        if(_platforms.size == 0)
            return false;

        if(flag == false){
            for(ObjectTexture i : this._platforms){
                if(rect.overlaps(i.rect))
                    return true;
            }
        }else{
            int index = (-1);
            for(int i = 0; i < this._platforms.size; i++){
                if((rect.y + 10 >= (this._platforms.get(i).rect.getY() + this._platforms.get(i).rect.getHeight()))
                        && (rect.y - 10 <= (this._platforms.get(i).rect.getY() + this._platforms.get(i).rect.getHeight()))
                        && (((rect.x >= this._platforms.get(i).rect.getX()) && (rect.x <= (this._platforms.get(i).rect.getX() + this._platforms.get(i).rect.getWidth())))
                        || (rect.x + rect.getWidth() >= this._platforms.get(i).rect.getX()))){
                    index = i;
                    break;
                }
            }

            for(int i = 0; i < this._platforms.size; i++){
                if(i == index)
                    continue;
                if(this._platforms.get(i).rect.overlaps(_player.getRectangle()))
                    return true;
            }
        }
        return false;
    }

    private float posy = 0;
    @Override
    public void render(){
        ScreenUtils.clear(0, 0, 0.2f, 1);
        _camera.update();

        //отрисовка текстур
        _batch.setProjectionMatrix(_camera.combined);
        _batch.begin();
        _batch.draw(_player.getCurrentTexture(), _player.getRectangle().x, _player.getRectangle().y,
                _player.getRectangle().width, _player.getRectangle().height);

        for(int i = 0; i < this._platforms.size; i++){
            _batch.draw(this._platforms.get(i).texture, this._platforms.get(i).rect.x, this._platforms.get(i).rect.y,
                    this._platforms.get(i).rect.width, this._platforms.get(i).rect.height);
        }

        _batch.draw(_door.texture, _door.rect.x, _door.rect.y, _door.rect.width, _door.rect.height);
        _batch.draw(_attack.texture, _attack.rect.x, _attack.rect.y, _attack.rect.width, _attack.rect.height);

        if((_enemies != null) && (_enemies.size > 0)){
            Array<Integer> del = new Array<>();
            for(int i = 0; i < this._enemies.size; i++){
                if(_enemies.get(i).getHp() > 0){
                    _batch.draw(_enemies.get(i).getCurrentTexture(), _enemies.get(i).getRectangle().x, _enemies.get(i).getRectangle().y,
                            _enemies.get(i).getRectangle().width, _enemies.get(i).getRectangle().height);
                }else{
                    del.add(i);
                }
            }

            for(Integer i : del){
                _enemies.get(i).dispose();
                _enemies.removeIndex(i);
            }
        }

        if((_bullet != null) && (_bullet.getActive())){
            _batch.draw(_bullet.getTexture(), _bullet.getRectangle().x, _bullet.getRectangle().y,
                    _bullet.getRectangle().width, _bullet.getRectangle().height);
        }else if((_bullet != null) && (!_bullet.getActive())){
            _bullet.dispose();
            _bullet = null;
        }

        BitmapFont font;
        CharSequence information = "HP: " + _player.getHp() + "\nSCORE: " + _player.getScore();
        font = new BitmapFont();

        font.draw(_batch, information, 10, Gdx.graphics.getHeight() - 10);

        _batch.end();

        if(Gdx.input.isTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            _camera.unproject(touchPos);
            /*if((touchPos.x >= (_player.getRectangle().x + _player.getRectangle().getWidth() + 200)) && (_bullet == null)){
                _bullet = new Bullet(new Rectangle((_player.getRectangle().x + _player.getRectangle().getWidth() + 20), (_player.getRectangle().getY() + 50),
                        50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), true, 40,
                        0, Gdx.graphics.getWidth());
            }else if((touchPos.x < (_player.getRectangle().x - 200)) && (_bullet == null)){
                _bullet = new Bullet(new Rectangle((_player.getRectangle().x - 20), (_player.getRectangle().getY() + 50),
                        50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), false, 40,
                        0, Gdx.graphics.getWidth());
            }*/

            if(_attack.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, 100, 100)) && (_bullet == null)){
                if(_player.getDirectionMove()){
                    _bullet = new Bullet(new Rectangle((_player.getRectangle().x + _player.getRectangle().getWidth() + 20), (_player.getRectangle().getY() + 50),
                            50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), true, 40,
                            0, Gdx.graphics.getWidth());
                }else{
                    _bullet = new Bullet(new Rectangle((_player.getRectangle().x - 20), (_player.getRectangle().getY() + 50),
                            50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), false, 40,
                            0, Gdx.graphics.getWidth());
                }
            }

            if(!_attack.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, 100, 100))){
                if(touchPos.x >= _player.getRectangle().x)
                    _player.setDirectionMove(true);
                else
                    _player.setDirectionMove(false);
            }

            //player.moveRigth(touchPos.x - 64 / 2);
            //man.y = touchPos.y;
            if(((touchPos.y > (_player.getRectangle().y + _player.getRectangle().height + 50)) && (!_player.getJumpUp()))
                    && (collisionPlatforms(_player.getRectangle(), false))
            ){
                _player.setJumpUp(true);
                posy = touchPos.y;
            }
        }

        if(_player.getJumpUp() && (!collisionPlatforms(_player.getRectangle(), true)))
            _player.moveUp((posy + 100) * Gdx.graphics.getDeltaTime());
        else
            _player.setJumpUp(false);

        /*if((!collisionPlatforms(player.getRectangle(), false)
        || ((collisionPlatforms(player.getRectangle(), true)))) && (!player.getJump()))
            player.moveDown(100 * Gdx.graphics.getDeltaTime());*/

        if(_player.getJumpDown() && (!collisionPlatforms(_player.getRectangle(), false)))
            _player.moveDown(200 * Gdx.graphics.getDeltaTime());
        else if(collisionPlatforms(_player.getRectangle(), true))
            _player.moveDown(200 * Gdx.graphics.getDeltaTime());

        if(collisionPlatforms(_player.getRectangle(), false))
            _player.setJumpDown(false);

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
        //освобождение ресурсов

        for(int i = 0; i < _platforms.size; i++){
            _platforms.get(i).texture.dispose();
        }

        for(int i = 0; i < _enemies.size; i++){
            _enemies.get(i).dispose();
        }

        if((_bullet != null) && (!_bullet.getActive())){
            _bullet.dispose();
            _bullet = null;
        }

        _attack.texture.dispose();
        _manImage.dispose();
        _currentMan.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        _batch.dispose();
        _player.dispose();
    }
}
