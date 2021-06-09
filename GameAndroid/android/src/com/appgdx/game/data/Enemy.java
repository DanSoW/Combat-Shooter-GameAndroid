package com.appgdx.game.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Enemy {
    private Rectangle _enRect;                  //четырёхугольник в который будет вписан спрайт
    private Array<Texture> _enStatesRight;      //текстуры движения вправо
    private Array<Texture> _enStatesLeft;       //текстуры движения влево
    private Rectangle _platform;                //платформа, за которой будет закреплён монстр (в пределах которой он будет двигаться)
    private int _hp = 100;                      //количество жизней монстра
    private boolean _directionMove = true;      //направление: true - вправо, false - влево
    private int _currentMoveState = 0;          //текущая текстура движения вправо/влево
    private float _countMove = 20;              //количество "шагов", которые монст проходит по пространству

    public Enemy(Rectangle enRect, Array<Texture> enStatesRight, Array<Texture> enStatesLeft,
                 Rectangle platform, float countMove) {
        this._enRect = enRect;
        this._platform = platform;
        this._countMove = countMove;

        _enStatesRight = new Array<>();
        _enStatesLeft = new Array<>();

        for(int i = 0; i < enStatesRight.size; i++){
            _enStatesRight.add(enStatesRight.get(i));
        }

        for(int i = 0; i < enStatesRight.size; i++){
            _enStatesLeft.add(enStatesLeft.get(i));
        }
    }

    public boolean getDirectionMove(){
        return _directionMove;
    }

    public void setDirectionMove(boolean dir){
        _directionMove = dir;
    }

    public int getHp(){
        return this._hp;
    }

    public void setHp(int hp){
        this._hp = hp;
    }

    public void move(){
        if(_directionMove)
            moveRight();
        else
            moveLeft();
    }

    private void moveRight(){
        if((_enRect.x  + _enRect.width + this._countMove) > (this._platform.getX() + this._platform.getWidth())){
            _directionMove = false;
            return;
        }
        _enRect.x = _enRect.x + this._countMove;
    }

    private void moveLeft(){
        if((_enRect.x - this._countMove) < this._platform.getX()){
            _directionMove = true;
            return;
        }
        _enRect.x = _enRect.x - this._countMove;
    }

    public void nextTexture(){
        _currentMoveState++;
        if(_currentMoveState < _enStatesRight.size)
            return;
        else if(_currentMoveState >= _enStatesRight.size)
            _currentMoveState = 0;
    }

    public Texture getCurrentTexture(){
        if(_currentMoveState >= _enStatesRight.size)
            _currentMoveState = 0;
        if(_directionMove)
            return _enStatesRight.get((_currentMoveState < _enStatesRight.size)? _currentMoveState : 0);
        else
            return _enStatesLeft.get((_currentMoveState < _enStatesRight.size)? _currentMoveState : 0);
    }

    public Rectangle getRectangle(){
        return _enRect;
    }
}
