package com.appgdx.game.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Bullet {
    private Rectangle _bRect;                   //четырёхугольник в который будет вписан спрайт
    private Texture _bullet;                    //текстура пули
    private boolean _directionMove = true;      //направление: true - вправо, false - влево
    private float _speedMove = 20;              //скорость движения пули
    private boolean _active = false;            //активность пули

    private int _minX = 0;
    private int _maxX = 0;

    public boolean getActive(){
        return _active;
    }

    public Texture getTexture(){
        return this._bullet;
    }

    public Bullet(Rectangle bRect, Texture bullet, boolean dir, float speedMove,
                  int minX, int maxX) {
        this._bRect = bRect;
        this._bullet = bullet;
        this._speedMove = speedMove;
        this._maxX = maxX;
        this._minX = minX;
        this._active = true;
        this._directionMove = dir;
    }

    public boolean getDirectionMove(){
        return _directionMove;
    }

    public void setDirectionMove(boolean dir){
        _directionMove = dir;
    }

    public void move(){
        if(_directionMove)
            moveRight();
        else
            moveLeft();
    }

    private void moveRight(){
        if((_bRect.x  + _bRect.width + this._speedMove) > _maxX){
            _active = false;
            return;
        }
        _bRect.x = _bRect.x + this._speedMove;
    }

    private void moveLeft(){
        if((_bRect.x - this._speedMove) < _minX){
            _active = false;
            return;
        }
        _bRect.x = _bRect.x - this._speedMove;
    }

    public Rectangle getRectangle(){
        return _bRect;
    }

    public boolean isDispose(){
        return (_bullet == null);
    }

    public void dispose(){
        _bullet.dispose();
    }
}
