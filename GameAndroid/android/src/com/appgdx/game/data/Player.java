package com.appgdx.game.data;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Rectangle _plRect;
    private Array<Texture> _plStatesRight;      //текстуры движения вправо
    private Array<Texture> _plStatesLeft;       //текстуры движения влево
    private Array<Texture> _plStatesJumpRight;  //текстуры прыжка вверх
    private Array<Texture> _plStatesJumpLeft;   //текстуры прыжка влево

    private int _hp = 100;                      //количество жизней персонажа
    private int _score = 0;                     //количество очков у игрока
    private int _countBullet = 5;               //количество пуль для стрельбы

    private boolean _directionMove = true;      //направление: true - вправо, false - влево
    private boolean _directionJump = false;     //направление прыжка: true - вверх, false - вниз
    private int _currentMoveState = 0;          //текущая текстура движения вправо/влево
    private int _currentJumpState = 0;          //текущая текстура прыжка
    private boolean _jumpUp   = false;          //прыжок вверх
    private boolean _jumpDown = false;           //падение вниз
    private int _minY = 0;
    private int _maxY = 0;
    private int _minX = 0;
    private int _maxX = 0;
    private float _countMove = 20;                 //количество "шагов", которые игрок проходит по игровому пространству
    private float _countJump = 20;                 //число прыжков
    private float _currentCountJump = 0;

    public Player(Rectangle pos, Array<Texture> right, Array<Texture> left,
                  Array<Texture> rightJump, Array<Texture> leftJump, float move, float countJump,
                  int minY, int maxY, int minX, int maxX){
        this._maxY = maxY;
        this._minY = minY;
        this._maxX = maxX;
        this._minX = minX;

        this._plRect = pos;
        this._countMove = move;
        this._countJump = countJump;
        this._currentCountJump = countJump;
        _plStatesRight = new Array<>();
        _plStatesLeft = new Array<>();
        _plStatesJumpRight = new Array<>();
        _plStatesJumpLeft = new Array<>();

        for(int i = 0; i < right.size; i++){
            _plStatesRight.add(right.get(i));
        }

        for(int i = 0; i < left.size; i++){
            _plStatesLeft.add(left.get(i));
        }

        for(int i = 0; i < rightJump.size; i++){
            _plStatesJumpRight.add(rightJump.get(i));
        }

        for(int i = 0; i < leftJump.size; i++){
            _plStatesJumpLeft.add(leftJump.get(i));
        }
    }

    public int getCountBullet(){
        return this._countBullet;
    }

    public void setCountBullet(int countBullet){
        this._countBullet = countBullet;
    }

    public int getHp(){
        return this._hp;
    }

    public void setHp(int hp){
        this._hp = hp;
    }

    public int getScore(){
        return this._score;
    }

    public void setScore(int score){
        this._score = score;
    }

    public float getCountMove() {
        return _countMove;
    }
    public void setCountMove(float countMove) {
        this._countMove = countMove;
    }


    public void nextTexture(){
        _currentMoveState++;
        if(_currentMoveState < _plStatesRight.size)
            return;
        else if(_currentMoveState >= _plStatesRight.size)
            _currentMoveState = 0;
    }

    public boolean getDirectionMove(){
        return _directionMove;
    }

    public void setDirectionMove(boolean dir){
        _directionMove = dir;
    }

    public void moveRight(){
        if((_plRect.x  + _plRect.width + this._countMove) > this._maxX)
            return;
        _plRect.x = _plRect.x + this._countMove;
    }

    public void moveLeft(){
        if((_plRect.x - this._countMove) < this._minX)
            return;
        _plRect.x = _plRect.x - this._countMove;
    }

    public void setJumpUp(boolean jump){
        _jumpUp = jump;
        if(_jumpUp == false){
            this._currentCountJump = this._countJump;
        }
    }

    public boolean getJumpUp(){
        return _jumpUp;
    }

    public void setJumpDown(boolean jump){
        _jumpDown = jump;
    }

    public boolean getJumpDown(){
        return _jumpDown;
    }

    public void moveUp(float value){
        if(_jumpUp == false)
            return;
        if(((_plRect.y + _plRect.getHeight() + value) > _maxY) || (this._currentCountJump <= 0)){
            _jumpUp = false;
            this._currentCountJump = this._countJump;
            return;
        }
        _plRect.y = _plRect.y + value;
        this._currentCountJump -= 1;
    }

    public void moveDown(float value){
        if(_jumpDown == false)
            return;
        if((_plRect.y - value) < this._minY)
            return;
        _plRect.y = _plRect.y - value;
    }

    public Texture getCurrentTexture(){
        if(_jumpUp){
            if(_directionMove)
                return _plStatesJumpRight.get(1);
            else
                return _plStatesJumpLeft.get(1);
        }else if(_jumpDown){
            if(_directionMove)
                return _plStatesJumpRight.get(0);
            else
                return _plStatesJumpLeft.get(0);
        }

        if(_directionMove)
            return _plStatesRight.get((_currentMoveState < _plStatesRight.size)? _currentMoveState : 0);
        else
            return _plStatesLeft.get((_currentMoveState < _plStatesRight.size)? _currentMoveState : 0);
    }

    public Rectangle getRectangle(){
        return _plRect;
    }

    public boolean isDispose(){
        return ((_plStatesRight == null) || (_plStatesRight.size == 0)
        || (_plStatesLeft == null) || (_plStatesLeft.size == 0));
    }

    public void dispose(){
        for(int i = 0; i < _plStatesRight.size; i++) {
            _plStatesRight.get(i).dispose();
            _plStatesLeft.get(i).dispose();
        }

        for(int i = 0; i < _plStatesJumpRight.size; i++){
            _plStatesJumpRight.get(i).dispose();
            _plStatesJumpLeft.get(i).dispose();
        }
    }
}
