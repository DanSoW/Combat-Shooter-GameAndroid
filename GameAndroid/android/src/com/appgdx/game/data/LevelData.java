package com.appgdx.game.data;

import com.appgdx.game.screen.GameScreen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LevelData{
    private int _countLevel;
    private int _indexLevel;
    private Array<Array<Enemy>> _enemies;              //монстры
    private Array<Array<ObjectTexture>> _platforms;    //платформы
    private Rectangle[] _positionChestLow = null;      //расположение малого сундука на уровне
    private Rectangle[] _positionChestBig = null;      //расположение большого сундука на уровне
    private Rectangle[] _positionPlayer = null;        //расположение игрока на уровне
    private Rectangle[] _positionDoor = null;          //расположение двери на уровне

    public void nextLevel(){
        _enemies.removeIndex(0);
        _platforms.removeIndex(0);
        _indexLevel++;
    }

    public int getCountLevel(){
        return _countLevel;
    }

    public int getCurrentIndex(){
        return _indexLevel;
    }

    public Rectangle getCurrentPositionDoor(){
        if(_indexLevel >= _countLevel)
            return null;
        return _positionDoor[_indexLevel];
    }

    public Rectangle getCurrentPositionPlayer(){
        if(_indexLevel >= _countLevel)
            return null;
        return _positionPlayer[_indexLevel];
    }

    public Rectangle getCurrentPositionChestLow(){
        if(_indexLevel >= _countLevel)
            return null;
        return _positionChestLow[_indexLevel];
    }

    public Rectangle getCurrentPositionChestBig(){
        if(_indexLevel >= _countLevel)
            return null;
        return _positionChestBig[_indexLevel];
    }

    public Array<Enemy> getCurrentEnemies(){
        if(_indexLevel >= _countLevel)
            return null;
        return _enemies.get(0);
    }

    public Array<Enemy> getElementEnemy(int index){
        if((index < 0) || (index >= _enemies.size))
            return null;
        return _enemies.get(index);
    }

    public Array<ObjectTexture> getCurrentPlatforms(){
        if(_indexLevel >= _countLevel)
            return null;
        return _platforms.get(0);
    }

    public Array<ObjectTexture> getElementPlatform(int index){
        if((index < 0) || (index >= _platforms.size))
            return null;
        return _platforms.get(index);
    }

    public void addInitialPositionChestLow(Rectangle[] rect){
        _positionChestLow = rect.clone();
    }

    public void addInitialPositionChestBig(Rectangle[] rect){
        _positionChestBig = rect.clone();
    }

    public void addInitialPositionPlayer(Rectangle[] rect){
        _positionPlayer = rect.clone();
    }

    public void addInitialPositionDoor(Rectangle[] rect){
        _positionDoor = rect.clone();
    }

    public void addCurrentPositionChestLow(Rectangle rect){
        if(_indexLevel >= _countLevel)
            return;
        _positionChestLow[_indexLevel] = rect;
    }

    public void addCurrentPositionChestBig(Rectangle rect){
        if(_indexLevel >= _countLevel)
            return;
        _positionChestBig[_indexLevel] = rect;
    }

    public void addCurrentPositionPlayer(Rectangle rect){
        if(_indexLevel >= _countLevel)
            return;
        _positionPlayer[_indexLevel] = rect;
    }

    public void addCurrentPositionDoor(Rectangle rect){
        if(_indexLevel >= _countLevel)
            return;
        _positionDoor[_indexLevel] = rect;
    }

    public void addInitialPlatforms(ObjectTexture objects, int index) throws Exception {
        if((index < 1) || (index > _countLevel)){
            throw new Exception("Error: index shall will be in range [1; " + _countLevel + "]");
        }

        _platforms.get((index - 1)).add(objects);
    }

    public void addInitialEnemies(Enemy objects, int index) throws Exception {
        if((index < 1) || (index > _countLevel)){
            throw new Exception("Error: index shall will be in range [1; " + _countLevel + "]");
        }

        _enemies.get((index - 1)).add(objects);
    }

    public LevelData(int lvl){
        _countLevel = lvl;
        _indexLevel = 0;

        _enemies = new Array<Array<Enemy>>();
        _platforms = new Array<Array<ObjectTexture>>();

        for(int i = 0; i < _countLevel; i++){
            _enemies.add(new Array<Enemy>());
            _platforms.add(new Array<ObjectTexture>());
        }

        _positionChestLow   = new Rectangle[_countLevel];
        _positionChestBig   = new Rectangle[_countLevel];
        _positionPlayer     = new Rectangle[_countLevel];
        _positionDoor       = new Rectangle[_countLevel];
    }
}