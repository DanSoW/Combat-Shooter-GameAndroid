package com.appgdx.game.screen;

import com.appgdx.game.AndroidLauncher;
import com.appgdx.game.data.Bullet;
import com.appgdx.game.data.Enemy;
import com.appgdx.game.data.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
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
    private ObjectTexture _pause;               //управляющая кнопка для паузы
    private ObjectTexture _chestBig;            //большой сундук (+500 очков)
    private ObjectTexture _chestLow;            //малый сундук (+200 очков)

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
    private volatile int _pauseCount = 0;
    private volatile boolean _pauseFlag = false;
    private Thread threadMove = null;

    public GameScreen(){

        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, AndroidLauncher.WIDTH, AndroidLauncher.HEIGHT); //установка размеров камеры
        _batch = new SpriteBatch();  //конструктор спрайтов

        //установка местоположения главной платформы
        Rectangle rectangleMainStage;
        rectangleMainStage = new Rectangle();
        rectangleMainStage.x = 0;
        rectangleMainStage.y = 0;
        rectangleMainStage.width = AndroidLauncher.WIDTH;
        rectangleMainStage.height = AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT - 100);

        _attack = new ObjectTexture(new Texture(Gdx.files.internal("management/ui/attack.png")), new Rectangle(
                AndroidLauncher.WIDTH - 150, 0,
                100, 100
        ));

        _pause = new ObjectTexture(new Texture(Gdx.files.internal("management/ui/pause.png")), new Rectangle(
                AndroidLauncher.WIDTH - 100, AndroidLauncher.HEIGHT - 100,
                80, 80
        ));

        //установка местоположения не главных платформ
        _platforms = new Array<>();
        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), rectangleMainStage));
        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), new Rectangle()));
        _platforms.get(1).rect.x = 0;
        _platforms.get(1).rect.y = 450;
        _platforms.get(1).rect.height = 30;
        _platforms.get(1).rect.width = 250;

        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/lava_platform.jpg")), new Rectangle()));
        _platforms.get(2).rect.x = 0;
        _platforms.get(2).rect.y = 260;
        _platforms.get(2).rect.height = 30;
        _platforms.get(2).rect.width = 400;

        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), new Rectangle()));
        _platforms.get(3).rect.x = 900;
        _platforms.get(3).rect.y = 200;
        _platforms.get(3).rect.width = 200;
        _platforms.get(3).rect.height = 30;

        _platforms.add(new ObjectTexture(new Texture(Gdx.files.internal("platform/stage.png")), new Rectangle()));
        _platforms.get(4).rect.x = 1050;
        _platforms.get(4).rect.y = 400;
        _platforms.get(4).rect.width = 350;
        _platforms.get(4).rect.height = 30;

        //создание малого сундука
        _chestLow = new ObjectTexture(new Texture(Gdx.files.internal("chest/chestLow.png")),
                new Rectangle(_platforms.get(2).rect.x, _platforms.get(2).rect.y + _platforms.get(2).rect.getHeight(), 80, 80));

        _door = new ObjectTexture(new Texture(Gdx.files.internal("door/door_finish.png")), new Rectangle());
        _door.rect.x = 0;
        _door.rect.y = 475;
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

        _player = new Player(new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150), rightPlayer, leftPlayer, rightPlayerJump, leftPlayerJump, 20,
                30, 0, AndroidLauncher.HEIGHT , 0, AndroidLauncher.WIDTH);

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

        /*Thread threadPause = new Thread(){
            @Override
            public void run(){
                while(running){
                    if(_pauseCount > 0){
                        _pauseFlag = !_pauseFlag;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        _pauseCount = 0;
                    }
                }
            }
        };

        threadPause.start();*/

        threadMove = new Thread(){
            @Override
            public void run(){
                //функция потока, реализующая логику перемещения объектов по пространству
                while(running){
                    if(_pauseFlag)
                        continue;
                    if((_player == null) || (_player.isDispose()))
                        break;

                    if((_player.getRectangle().overlaps(_door.rect)) || (_player.getHp() <= 0)){
                        running = false;
                        break;
                    }

                    _player.nextTexture();

                    if((_chestLow != null) && (_player.getRectangle().overlaps(_chestLow.rect))){
                        _player.setScore(_player.getScore() + 200);
                        _chestLow.texture.dispose();
                        _chestLow = null;
                    }

                    if(_player.getDirectionMove()){
                        if(collisionEnemy(_player.getRectangle())){
                            _player.setHp(_player.getHp() - 20);
                            _player.setDirectionMove(false);
                            if(!collisionPlatforms(_player.getRectangle(), true)){
                                _player.moveLeft();
                                _player.moveLeft();
                            }
                        }else if(!collisionPlatformsRight(_player.getRectangle())){
                            _player.moveRight();
                        }
                    }else{
                        if(collisionEnemy(_player.getRectangle())){
                            _player.setHp(_player.getHp() - 20);
                            _player.setDirectionMove(true);
                            if(!collisionPlatforms(_player.getRectangle(), true)){
                                _player.moveRight();
                                _player.moveRight();
                            }
                        }else if(!collisionPlatformsLeft(_player.getRectangle())){
                            _player.moveLeft();
                        }
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

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        threadMove.start(); //запуск потока, который обрабатывает движение игрока
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
        if((_enemies == null) || (_enemies.size == 0))
            return false;

        for(int i = 0; i < this._enemies.size; i++){
            if((_enemies.get(i) != null) && (_enemies.get(i).getRectangle() != null)
                    && rect.overlaps(this._enemies.get(i).getRectangle()))
                return true;
        }

        return false;
    }

    private boolean collisionPlatformsUp(Rectangle rect){
        if((_platforms == null) || (_platforms.size == 0))
            return false;

        for(int i = 0; i < _platforms.size; i++){
            if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                    && (rect.y < _platforms.get(i).rect.y)
                    && (_platforms.get(i).rect.overlaps(_player.getRectangle()))
                    && (
                    ((rect.x > _platforms.get(i).rect.x) && ((rect.x + rect.width) < (_platforms.get(i).rect.x + _platforms.get(i).rect.width)))
                            || ((rect.x < _platforms.get(i).rect.x) && ((rect.x + rect.width) > (_platforms.get(i).rect.x)))
                            || ((rect.x > _platforms.get(i).rect.x) && ((rect.x + rect.width) > (_platforms.get(i).rect.x + _platforms.get(i).rect.width))))
            ){
                return true;
            }
        }

        return false;
    }

    private boolean collisionPlatformsRight(Rectangle rect){
        if((_platforms == null) || (_platforms.size == 0))
            return false;

        for(int i = 0; i < _platforms.size; i++){
            if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                    && (rect.x < _platforms.get(i).rect.x)
                    && ((rect.x + rect.width) >= _platforms.get(i).rect.x)
                    && (
                            ((rect.y >= _platforms.get(i).rect.y) && ((rect.y + rect.height) <= (_platforms.get(i).rect.y + _platforms.get(i).rect.height)))
                            || ((rect.y < _platforms.get(i).rect.y) && ((rect.y + rect.height) >= (_platforms.get(i).rect.y)))
                            || ((rect.y >= _platforms.get(i).rect.y) && (rect.y < (_platforms.get(i).rect.y + _platforms.get(i).rect.height - 10))
                                    && ((rect.y + rect.height) > (_platforms.get(i).rect.y + _platforms.get(i).rect.height))))
            ){
                return true;
            }
        }

        return false;
    }

    private boolean collisionPlatformsLeft(Rectangle rect){
        if((_platforms == null) || (_platforms.size == 0))
            return false;

        for(int i = 0; i < _platforms.size; i++){
            if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                    && (rect.x <= (_platforms.get(i).rect.x + _platforms.get(i).rect.width))
                    && ((rect.x + rect.width) > (_platforms.get(i).rect.x + _platforms.get(i).rect.width))
                    && (
                    ((rect.y >= _platforms.get(i).rect.y) && ((rect.y + rect.height) <= (_platforms.get(i).rect.y + _platforms.get(i).rect.height)))
                            || ((rect.y < _platforms.get(i).rect.y) && ((rect.y + rect.height) >= (_platforms.get(i).rect.y)))
                            || ((rect.y >= _platforms.get(i).rect.y) && (rect.y < (_platforms.get(i).rect.y + _platforms.get(i).rect.height - 10))
                            && ((rect.y + rect.height) > (_platforms.get(i).rect.y + _platforms.get(i).rect.height))))
            ){
                return true;
            }
        }
        return false;
    }

    private boolean collisionPlatformsDown(Rectangle rect){
        if((_platforms == null) || (_platforms.size == 0))
            return false;

        for(int i = 0; i < _platforms.size; i++){
            if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                    && ((rect.y + rect.height) >= (_platforms.get(i).rect.y + _platforms.get(i).rect.height))
                    && (rect.y > _platforms.get(i).rect.y)
                    && (_platforms.get(i).rect.overlaps(_player.getRectangle()))
                    && (
                            ((rect.x >= _platforms.get(i).rect.x) && ((rect.x + rect.width) <= (_platforms.get(i).rect.x + _platforms.get(i).rect.width)))
                            || ((rect.x < _platforms.get(i).rect.x) && ((rect.x + rect.width) >= (_platforms.get(i).rect.x)))
                            || ((rect.x >= _platforms.get(i).rect.x) && ((rect.x + rect.width) > (_platforms.get(i).rect.x + _platforms.get(i).rect.width))))
            ){
                return true;
            }
        }

        return false;
    }

    private boolean collisionPlatforms(Rectangle rect, boolean flag){
        if((_platforms == null) || (_platforms.size == 0))
            return false;

        if(!flag){
            for(int i = 0; i < _platforms.size; i++){
                if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                        && (rect.overlaps(_platforms.get(i).rect))){
                    return true;
                }
            }
        }else{
            int index = (-1);
            for(int i = 0; i < this._platforms.size; i++){
                if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                        && ((rect.y + rect.height) >= (_platforms.get(i).rect.y + _platforms.get(i).rect.height))
                        && (rect.y > _platforms.get(i).rect.y)
                        && (_platforms.get(i).rect.overlaps(_player.getRectangle()))
                        && (
                        ((rect.x >= _platforms.get(i).rect.x) && ((rect.x + rect.width) <= (_platforms.get(i).rect.x + _platforms.get(i).rect.width)))
                                || ((rect.x < _platforms.get(i).rect.x) && ((rect.x + rect.width) >= (_platforms.get(i).rect.x)))
                                || ((rect.x >= _platforms.get(i).rect.x) && ((rect.x + rect.width) > (_platforms.get(i).rect.x + _platforms.get(i).rect.width))))
                ){
                    index = i;
                    break;
                }
            }

            if(index < 0)
                return false;

            for(int i = 0; i < this._platforms.size; i++){
                if(i == index)
                    continue;
                if((_platforms.get(i) != null) && (_platforms.get(i).rect != null)
                        && _platforms.get(i).rect.overlaps(rect)){
                    return true;
                }
            }
        }
        return false;
    }

    private float posy = 0;
    @Override
    public void render(float delta){
        ScreenUtils.clear(0, 0, 0.2f, 1);
        _camera.update();

        //------------------------------------------------------------------------------------------
        //Блок отрисовки текстур
        _batch.setProjectionMatrix(_camera.combined);
        _batch.begin();
        _batch.draw(_player.getCurrentTexture(), _player.getRectangle().x, _player.getRectangle().y,
                _player.getRectangle().width, _player.getRectangle().height);

        if(_platforms != null){
            for(int i = 0; i < this._platforms.size; i++){
                if((_platforms.get(i) != null) && (_platforms.get(i).rect != null) && (_platforms.get(i).texture != null)){
                    _batch.draw(this._platforms.get(i).texture, this._platforms.get(i).rect.x, this._platforms.get(i).rect.y,
                            this._platforms.get(i).rect.width, this._platforms.get(i).rect.height);
                }
            }
        }


        _batch.draw(_door.texture, _door.rect.x, _door.rect.y, _door.rect.width, _door.rect.height);
        _batch.draw(_attack.texture, _attack.rect.x, _attack.rect.y, _attack.rect.width, _attack.rect.height);
        _batch.draw(_pause.texture, _pause.rect.x, _pause.rect.y, _pause.rect.width, _pause.rect.height);

        if(_chestLow != null){
            _batch.draw(_chestLow.texture, _chestLow.rect.x, _chestLow.rect.y, _chestLow.rect.width, _chestLow.rect.height);
        }

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

        //Отрисовка статистики пользователя
        BitmapFont font;
        CharSequence information = "HP: " + _player.getHp() + "\nSCORE: " + _player.getScore()
                + "\nBULLETS: " + _player.getCountBullet();
        font = new BitmapFont();

        font.draw(_batch, information, 10, AndroidLauncher.HEIGHT - 10);

        if(_pauseFlag && running){
            font.draw(_batch, "PAUSE", AndroidLauncher.WIDTH / 2, AndroidLauncher.HEIGHT / 2);
        }else if(!running){
            font.draw(_batch, "GAME OVER!", AndroidLauncher.WIDTH / 2, AndroidLauncher.HEIGHT / 2);
        }

        _batch.end();
        //------------------------------------------------------------------------------------------

        if(!running){
            return;
        }

        //обработка нажатия на экран
        if(Gdx.input.justTouched()){
            Vector3 touchPos = new Vector3();                          //позиция, куда пользователь нажал
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            _camera.unproject(touchPos);
            //обработка нажатия на кнопку паузы
            if(_pause.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, _pause.rect.width,  _pause.rect.height))){
                this._pauseFlag = !this._pauseFlag;
                return;
            }

            //логика, функционирующая без паузы
            if(!_pauseFlag){
                if(_attack.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, 100, 100)) && (_bullet == null)
                        && (_player.getCountBullet() > 0)){
                    if(_player.getDirectionMove()){
                        _bullet = new Bullet(new Rectangle((_player.getRectangle().x + _player.getRectangle().getWidth() + 20), (_player.getRectangle().getY() + 50),
                                50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), true, 40,
                                0, AndroidLauncher.WIDTH);
                    }else{
                        _bullet = new Bullet(new Rectangle((_player.getRectangle().x - 20), (_player.getRectangle().getY() + 50),
                                50, 50), new Texture(Gdx.files.internal("bullet\\bullet.png")), false, 40,
                                0, AndroidLauncher.WIDTH);
                    }
                    _player.setCountBullet(_player.getCountBullet() - 1);
                }

                if((!_attack.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, 100, 100)))
                 && (!_pause.rect.overlaps(new Rectangle(touchPos.x, touchPos.y, 80, 80)))){
                    if(touchPos.x >= _player.getRectangle().x)
                        _player.setDirectionMove(true);
                    else
                        _player.setDirectionMove(false);
                }

                if((touchPos.y > (_player.getRectangle().y + _player.getRectangle().height + 50)) && (!_player.getJumpUp())
                && (collisionPlatformsDown(_player.getRectangle()))){
                    _player.setJumpUp(true);
                    posy = touchPos.y;
                }
            }
        }

        //в случае паузы - выход из рендеринга (при этом, сохраняется состояние игрока)
        if(_pauseFlag)
            return;

        if(_player.getJumpUp()){
            if(!collisionPlatformsUp(_player.getRectangle())){
                _player.moveUp(10);
            }else{
                _player.setJumpUp(false);
            }
        }else{
            _player.setJumpUp(false);
            _player.setJumpDown(true);

            if((_player.getJumpDown()) && (!collisionPlatformsDown(_player.getRectangle()))){
                _player.moveDown(10);
            }
            _player.setJumpDown(false);
        }

        //обработка движения вверх
        /*if(_player.getJumpUp() && (collisionPlatforms(_player.getRectangle(), 1, true) < 0))
            _player.moveUp(10);
        else{
            _player.setJumpUp(false);
        }*/

        //обработка падения вниз
        /*if((collisionPlatformsDown(_player.getRectangle(), 1) < 0)
                || ((!_player.getJumpUp()) && (!collisionPlatforms(_player.getRectangle(), false)))){
            _player.setJumpDown(true);
        }else{
            _player.setJumpDown(false);
        }

        if(_player.getJumpDown() && (!collisionPlatforms(_player.getRectangle(), false)))
            _player.moveDown(5);
        else if(collisionPlatforms(_player.getRectangle(), true)){
            _player.moveDown(5);
        }

        //изменение метки прыжка (если после прыжка игрок врезается в стенку, то будет падение вниз)
        if(collisionPlatforms(_player.getRectangle(), false))
            _player.setJumpDown(false);*/


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
    public void show() {

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
/*        _manImage.dispose();
        _currentMan.dispose();
        dropSound.dispose();
        rainMusic.dispose();*/
        _batch.dispose();
        _player.dispose();
        if(_chestLow != null){
            _chestLow.texture.dispose();
        }

        if(_chestBig != null){
            _chestBig.texture.dispose();
        }
    }
}
