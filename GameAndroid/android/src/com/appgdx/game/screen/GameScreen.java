package com.appgdx.game.screen;

import com.appgdx.game.AndroidLauncher;
import com.appgdx.game.data.Bullet;
import com.appgdx.game.data.Enemy;
import com.appgdx.game.data.LevelData;
import com.appgdx.game.data.ObjectTexture;
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
    private Array<ObjectTexture> _platforms;    //платформы
    private ObjectTexture _attack;              //управляющая кнопка выстрела
    private ObjectTexture _pause;               //управляющая кнопка для паузы
    private ObjectTexture _chestBig;            //большой сундук (+500 очков)
    private ObjectTexture _chestLow;            //малый сундук (+200 очков)
    private ObjectTexture _door;                //дверь

    private volatile Array<Enemy> _enemies;     //монстры
    private volatile Player _player;            //игрок
    private volatile Bullet _bullet;            //пуля

    private LevelData _level;                   //уровень

    //текстуры для платформ (данные текстуры загружаются 1 раз и затем используются повсеместно
    private Texture _stage = null;
    private Texture _lavaStage = null;
    private Texture _holdStage = null;

    //музыкальное сопровождение
    private Sound _bulletAttack;
    private Sound _monsterAttack;
    private Sound _monsterKilled;
    private Sound _bonus;
    private Sound _nextLevel;
    private Sound _gameOver;
    private Sound _gameWin;
    private Music _background;

    private SpriteBatch _batch;
    private OrthographicCamera _camera;
    private Rectangle man;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    public static volatile boolean running = true;
    private volatile int _pauseCount = 0;
    private volatile boolean _pauseFlag = false;
    private Thread threadMove = null;

    //текстуры монстров:
    Array<Texture> _rightEnemy = new Array<Texture>();
    Array<Texture> _leftEnemy = new Array<Texture>();

    //ссылка на главное меню
    public static MenuScreen menuScreen;

    public GameScreen() throws Exception {

        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, AndroidLauncher.WIDTH, AndroidLauncher.HEIGHT); //установка размеров камеры
        _batch = new SpriteBatch();  //конструктор спрайтов

        _level = new LevelData(5); //инициализация объекта, реализующий логику уровней
        running = true;
        //инициализация текстур:

        //текстуры платформ
        _stage = new Texture(Gdx.files.internal("platform/stage.png"));
        _lavaStage = new Texture(Gdx.files.internal("platform/lava_platform.jpg"));
        _holdStage = new Texture(Gdx.files.internal("platform/hold_platform.png"));

        //текстуры игрока
        Array<Texture> rightPlayer = new Array<>();
        Array<Texture> leftPlayer = new Array<>();
        Array<Texture> rightPlayerJump = new Array<>();
        Array<Texture> leftPlayerJump = new Array<>();

        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight1.png")));
        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight2.png")));
        rightPlayer.add(new Texture(Gdx.files.internal("man\\manRight\\manRight3.png")));

        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft1.png")));
        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft2.png")));
        leftPlayer.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeft3.png")));

        rightPlayerJump.add(new Texture(Gdx.files.internal("man\\manRight\\manRightJumpDown.png")));
        rightPlayerJump.add(new Texture(Gdx.files.internal("man\\manRight\\manRightJumpUp.png")));

        leftPlayerJump.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeftJumpDown.png")));
        leftPlayerJump.add(new Texture(Gdx.files.internal("man\\manLeft\\manLeftJumpUp.png")));

        //текстуры монстра
        _rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight1.png")));
        _rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight2.png")));
        _rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight3.png")));
        _rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight4.png")));
        _rightEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyRight\\enemyRight5.png")));

        _leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft1.png")));
        _leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft2.png")));
        _leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft3.png")));
        _leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft4.png")));
        _leftEnemy.add(new Texture(Gdx.files.internal("enemy\\enemyLeft\\enemyLeft5.png")));

        //элементы управления для кнопки атаки и кнопки паузы
        _attack = new ObjectTexture(new Texture(Gdx.files.internal("management/ui/attack.png")), new Rectangle(
                AndroidLauncher.WIDTH - 150, 0,
                100, 100
        ));

        _pause = new ObjectTexture(new Texture(Gdx.files.internal("management/ui/pause.png")), new Rectangle(
                AndroidLauncher.WIDTH - 100, AndroidLauncher.HEIGHT - 100,
                80, 80
        ));

        //установка местоположения главной платформы
        Rectangle rectangleMainStage;
        rectangleMainStage = new Rectangle();
        rectangleMainStage.x = 0;
        rectangleMainStage.y = 0;
        rectangleMainStage.width = AndroidLauncher.WIDTH;
        rectangleMainStage.height = AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT - 100);

        _level.addInitialPlatforms(new ObjectTexture(_stage, rectangleMainStage), 1);
        _level.addInitialPlatforms(new ObjectTexture(_stage, new Rectangle(0, 450, 250, 30)), 1);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(0, 260, 450, 30)), 1);
        _level.addInitialPlatforms(new ObjectTexture(_stage, new Rectangle(800, 200, 200, 30)), 1);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(1050, 400, 350, 30)), 1);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(500, 350, 150, 30)), 1);

        _level.addInitialPlatforms(new ObjectTexture(_stage, rectangleMainStage), 2);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(0, 450, 300, 30)), 2);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(200, 250, 200, 30)), 2);
        _level.addInitialPlatforms(new ObjectTexture(_stage, new Rectangle((AndroidLauncher.WIDTH / 2 - 80), (AndroidLauncher.HEIGHT - 220), 250, 30)), 2);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(1100, 380, 300, 30)), 2);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(400, 300, 400, 30)), 2);
        _level.addInitialPlatforms(new ObjectTexture(_stage, new Rectangle(900, 200, 120, 30)), 2);

        _level.addInitialPlatforms(new ObjectTexture(_stage, rectangleMainStage), 3);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(70, 260, 250, 30)), 3);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(800, 200, 500, 30)), 3);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(1050, 400, 400, 30)), 3);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(480, 350, 150, 30)), 3);

        _level.addInitialPlatforms(new ObjectTexture(_stage, rectangleMainStage), 4);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(0, 350, 200, 30)), 4);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(880, 230, 400, 30)), 4);
        _level.addInitialPlatforms(new ObjectTexture(_holdStage, new Rectangle(500, 200, 350, 30)), 4);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(1050, 410, 350, 30)), 4);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(240, 300, 150, 30)), 4);

        _level.addInitialPlatforms(new ObjectTexture(_stage, rectangleMainStage), 5);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(300, 200, 180, 30)), 5);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(0, 260, 100, 30)), 5);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(800, 200, 300, 30)), 5);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(1050, 400, 390, 30)), 5);
        _level.addInitialPlatforms(new ObjectTexture(_lavaStage, new Rectangle(400, 300, 300, 30)), 5);

        _platforms = _level.getCurrentPlatforms();

        //создание монстров
        _level.addInitialEnemies(new Enemy(
                new Rectangle(_platforms.get(2).rect.x, _platforms.get(2).rect.y + _platforms.get(2).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _platforms.get(2).rect,
                5
        ), 1);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_platforms.get(4).rect.x, _platforms.get(4).rect.y + _platforms.get(4).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _platforms.get(4).rect,
                10
        ), 1);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(1).get(1).rect.x,
                        _level.getElementPlatform(1).get(1).rect.y + _level.getElementPlatform(1).get(1).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(1).get(1).rect,
                3
        ), 2);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(1).get(5).rect.x,
                        _level.getElementPlatform(1).get(5).rect.y + _level.getElementPlatform(1).get(5).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(1).get(5).rect,
                10
        ), 2);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(1).get(4).rect.x,
                        _level.getElementPlatform(1).get(4).rect.y + _level.getElementPlatform(1).get(4).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(1).get(4).rect,
                15
        ), 2);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(2).get(1).rect.x,
                        _level.getElementPlatform(2).get(1).rect.y + _level.getElementPlatform(2).get(1).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(2).get(1).rect,
                5
        ), 3);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(2).get(2).rect.x,
                        _level.getElementPlatform(2).get(2).rect.y + _level.getElementPlatform(2).get(2).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(2).get(2).rect,
                20
        ), 3);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(2).get(3).rect.x,
                        _level.getElementPlatform(2).get(3).rect.y + _level.getElementPlatform(2).get(3).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(2).get(3).rect,
                15
        ), 3);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(3).get(2).rect.x,
                        _level.getElementPlatform(3).get(2).rect.y + _level.getElementPlatform(3).get(2).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(3).get(2).rect,
                15
        ), 4);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(3).get(4).rect.x,
                        _level.getElementPlatform(3).get(4).rect.y + _level.getElementPlatform(3).get(4).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(3).get(4).rect,
                20
        ), 4);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(3).get(3).rect.x,
                        _level.getElementPlatform(3).get(3).rect.y + _level.getElementPlatform(3).get(3).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(3).get(3).rect,
                10
        ), 4);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(4).get(3).rect.x,
                        _level.getElementPlatform(4).get(3).rect.y + _level.getElementPlatform(4).get(3).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(4).get(3).rect,
                25
        ), 5);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(4).get(5).rect.x,
                        _level.getElementPlatform(4).get(5).rect.y + _level.getElementPlatform(4).get(5).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(4).get(5).rect,
                20
        ), 5);

        _level.addInitialEnemies(new Enemy(
                new Rectangle(_level.getElementPlatform(4).get(4).rect.x,
                        _level.getElementPlatform(4).get(4).rect.y + _level.getElementPlatform(4).get(4).rect.getHeight(), 150, 150),
                _rightEnemy,
                _leftEnemy,
                _level.getElementPlatform(4).get(4).rect,
                25
        ), 5);

        _enemies = _level.getCurrentEnemies();

        //создание малого сундука
        _level.addInitialPositionChestLow(new Rectangle[]{
                new Rectangle(_platforms.get(2).rect.x + 10, _platforms.get(2).rect.y + _platforms.get(2).rect.getHeight(),
                        80, 80),
                new Rectangle(_level.getElementPlatform(1).get(1).rect.x + 10,
                        _level.getElementPlatform(1).get(1).rect.y + _level.getElementPlatform(1).get(1).rect.getHeight(),
                        80, 80),
                new Rectangle(_level.getElementPlatform(2).get(4).rect.x,
                        _level.getElementPlatform(2).get(4).rect.y + _level.getElementPlatform(2).get(4).rect.getHeight(),
                        80, 80),
                new Rectangle(_level.getElementPlatform(3).get(2).rect.x + 10,
                        _level.getElementPlatform(3).get(2).rect.y + _level.getElementPlatform(3).get(2).rect.getHeight(),
                        80, 80),
                new Rectangle(_level.getElementPlatform(4).get(2).rect.x + 10,
                        _level.getElementPlatform(4).get(2).rect.y + _level.getElementPlatform(4).get(2).rect.getHeight(),
                        80, 80)
        });
        _chestLow = new ObjectTexture(new Texture(Gdx.files.internal("chest/chestLow.png")),
                _level.getCurrentPositionChestLow());

        //создание большого сундука
        _level.addInitialPositionChestBig(new Rectangle[]{
                new Rectangle((_platforms.get(4).rect.x + _platforms.get(4).rect.width - 80),
                        _platforms.get(4).rect.y + _platforms.get(4).rect.getHeight(), 80, 80),
                new Rectangle((_level.getElementPlatform(1).get(4).rect.x + _level.getElementPlatform(1).get(4).rect.width - 80),
                        _level.getElementPlatform(1).get(4).rect.y + _level.getElementPlatform(1).get(4).rect.getHeight(), 80, 80),
                new Rectangle((_level.getElementPlatform(2).get(0).rect.x + _level.getElementPlatform(2).get(0).rect.width - 100),
                        _level.getElementPlatform(2).get(0).rect.y + _level.getElementPlatform(2).get(0).rect.getHeight(), 80, 80),
                new Rectangle((_level.getElementPlatform(3).get(4).rect.x + _level.getElementPlatform(3).get(4).rect.width - 80),
                        _level.getElementPlatform(3).get(4).rect.y + _level.getElementPlatform(3).get(4).rect.getHeight(), 80, 80),
                new Rectangle((_level.getElementPlatform(4).get(4).rect.x + _level.getElementPlatform(4).get(4).rect.width - 80),
                        _level.getElementPlatform(4).get(4).rect.y + _level.getElementPlatform(4).get(4).rect.getHeight(), 80, 80)
        });
        _chestBig = new ObjectTexture(new Texture(Gdx.files.internal("chest/chestBig.png")),
                _level.getCurrentPositionChestBig());

        //создание двери
        _level.addInitialPositionDoor(new Rectangle[]{
                new Rectangle(0, 475, 100, 150),
                new Rectangle((AndroidLauncher.WIDTH / 2), (AndroidLauncher.HEIGHT - 200), 100, 150),
                new Rectangle((AndroidLauncher.WIDTH - 100), 420, 100, 150),
                new Rectangle(0, 370, 100, 150),
                new Rectangle(AndroidLauncher.WIDTH - 100, 100, 100, 150)
        });
        _door = new ObjectTexture(new Texture(Gdx.files.internal("door/door_finish.png")), _level.getCurrentPositionDoor());

        //создание позиции игрока
        _level.addInitialPositionPlayer(new Rectangle[]{
                new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150),
                new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150),
                new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150),
                new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150),
                new Rectangle(0, AndroidLauncher.HEIGHT - (AndroidLauncher.HEIGHT  - 100), 100, 150)
        });
        _player = new Player(_level.getCurrentPositionPlayer(), rightPlayer, leftPlayer, rightPlayerJump, leftPlayerJump, 20,
                30, 0, AndroidLauncher.HEIGHT , 0, AndroidLauncher.WIDTH);
        _player.setCountBullet(3);
        _player.setScore(MenuScreen.getRatingPlayer());

        _bulletAttack = Gdx.audio.newSound(Gdx.files.internal("bullet_attack.mp3"));
        _monsterAttack = Gdx.audio.newSound(Gdx.files.internal("monster.mp3"));
        _monsterKilled = Gdx.audio.newSound(Gdx.files.internal("monster_killed.mp3"));
        _bonus = Gdx.audio.newSound(Gdx.files.internal("bonus.mp3"));
        _nextLevel = Gdx.audio.newSound(Gdx.files.internal("next_level.mp3"));
        _gameOver = Gdx.audio.newSound(Gdx.files.internal("game_over.mp3"));
        _gameWin = Gdx.audio.newSound(Gdx.files.internal("game_win.mp3"));
        _background = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));

        _background.setLooping(true);
        _background.play();
        _background.setVolume(0.2f);

        threadMove = new Thread(){
            @Override
            public void run(){
                //функция потока, реализующая логику перемещения объектов по пространству
                while(running){
                    if(_pauseFlag)
                        continue;
                    if((_player == null) || (_player.isDispose()))
                        break;

                    if(_player.getHp() <= 0){
                        _background.stop();
                        _gameOver.play();
                        running = false;
                        dispose();
                        MenuScreen.menu.setScreen(menuScreen);
                        try {
                            MenuScreen.updateRating(_player.getScore());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    if(_player.getRectangle().overlaps(_door.rect)){
                        _level.nextLevel();
                        if((_level.getCurrentIndex() + 1) > _level.getCountLevel()){
                            _background.stop();
                            _gameWin.play();
                            running = false;
                            dispose();
                            MenuScreen.menu.setScreen(menuScreen);
                            try {
                                MenuScreen.updateRating(_player.getScore());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        _nextLevel.play();
                        _platforms = _level.getCurrentPlatforms();
                        _enemies = _level.getCurrentEnemies();
                        _player.setPosition(_level.getCurrentPositionPlayer());
                        _chestBig.rect = _level.getCurrentPositionChestBig();
                        _chestLow.rect = _level.getCurrentPositionChestLow();
                        _door.rect = _level.getCurrentPositionDoor();
                        _player.setCountBullet(3);
                        _player.setDirectionMove(true);
                        continue;
                    }

                    _player.nextTexture();

                    if((_chestLow.rect != null) && (_player.getRectangle().overlaps(_chestLow.rect))){
                        _player.setScore(_player.getScore() + 200);
                        _chestLow.rect = null;
                        _player.setCountBullet(_player.getCountBullet() + 2);
                        _bonus.play();
                    }

                    if((_chestBig.rect != null) && (_player.getRectangle().overlaps(_chestBig.rect))){
                        _player.setScore(_player.getScore() + 1000);
                        _chestBig.rect = null;
                        _player.setCountBullet(_player.getCountBullet() + 4);
                        _bonus.play();
                    }

                    if(_player.getDirectionMove()){
                        if(collisionEnemy(_player.getRectangle())){
                            _player.setHp(_player.getHp() - 20);
                            _player.setDirectionMove(false);
                            if(!collisionPlatforms(_player.getRectangle(), true)){
                                _player.moveLeft();
                                _player.moveLeft();
                            }
                            _monsterAttack.play();
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
                            _monsterAttack.play();
                        }else if(!collisionPlatformsLeft(_player.getRectangle())){
                            _player.moveLeft();
                        }
                    }

                    for(int i = 0; (_enemies != null) && (i < _enemies.size); i++){
                        if((_bullet != null) && (_bullet.getActive()) && _bullet.getRectangle().overlaps(_enemies.get(i).getRectangle())){
                            _enemies.get(i).setHp(0);
                            _bullet.dispose();
                            _bullet = null;
                            _player.setScore(_player.getScore() + 100);
                            _monsterKilled.play();
                        }else{
                            _enemies.get(i).nextTexture();
                            _enemies.get(i).move();
                        }
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

    private int getZeroEnemyIndex(){
        if(_enemies.size == 0)
            return (-1);

        for(int i = 0; i < _enemies.size; i++){
            if(_enemies.get(i).getHp() <= 0){
                return i;
            }
        }

        return (-1);
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

        if(_chestLow.rect != null){
            _batch.draw(_chestLow.texture, _chestLow.rect.x, _chestLow.rect.y, _chestLow.rect.width, _chestLow.rect.height);
        }

        if(_chestBig.rect != null){
            _batch.draw(_chestBig.texture, _chestBig.rect.x, _chestBig.rect.y, _chestBig.rect.width, _chestBig.rect.height);
        }

        if((_enemies != null) && (_enemies.size > 0)){
            for(int i = 0; i < this._enemies.size; i++){
                if(_enemies.get(i).getHp() > 0){
                    _batch.draw(_enemies.get(i).getCurrentTexture(), _enemies.get(i).getRectangle().x, _enemies.get(i).getRectangle().y,
                            _enemies.get(i).getRectangle().width, _enemies.get(i).getRectangle().height);
                }
            }

            int index = (-1);
            while((index = getZeroEnemyIndex()) >= 0){
                _enemies.removeIndex(index);
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
        CharSequence information = "LEVEL: " + (_level.getCurrentIndex() + 1) + "\nHP: " + _player.getHp() + "\nSCORE: " + _player.getScore()
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
                    _bulletAttack.play();
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
            }else{
                _player.setJumpDown(false);
            }
        }
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
        _background.dispose();
        _gameWin.dispose();
        _gameOver.dispose();
        _nextLevel.dispose();
        _monsterAttack.dispose();
        _bulletAttack.dispose();
        _monsterKilled.dispose();
        _bonus.dispose();
        _stage.dispose();
        _lavaStage.dispose();
        _holdStage.dispose();

        for(int i = 0; i < _rightEnemy.size; i++){
            _rightEnemy.get(i).dispose();
            _leftEnemy.get(i).dispose();
        }

        if((_bullet != null) && (!_bullet.getActive())){
            _bullet.dispose();
            _bullet = null;
        }

        _attack.texture.dispose();

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
