package com.appgdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Rectangle _plRect;
    private Array<Texture> _plStatesRight;   //текстуры движения вправо
    private Array<Texture> _plStatesLeft;    //текстуры движения влево
    private Array<Texture> _plStatesJumpRight; //текстуры прыжка вверх
    private Array<Texture> _plStatesJumpLeft; //текстуры прыжка влево
    private boolean _directionMove = true;      //направление: true - вправо, false - влево
    private boolean _directionJump = false;     //направление прыжка: true - вверх, false - вниз
    private int _currentMoveState = 0;           //текущая текстура движения вправо/влево
    private int _currentJumpState = 0;      //текущая текстура прыжка
    private boolean _jump = false;

    private int _minY = 0;
    private int _maxY = 0;

    public Player(Rectangle pos, Array<Texture> right, Array<Texture> left, Array<Texture> rightJump, Array<Texture> leftJump, int maxY){
        this._plRect = pos;
        this._maxY = maxY;
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

    public void moveRight(float value){
        _plRect.x = _plRect.x + value;
    }

    public void moveLeft(float value){
        _plRect.x = _plRect.x - value;
    }

    public void setJump(boolean jamp){
        _jump = jamp;
    }

    public boolean getJump(){
        return _jump;
    }

    public void moveUp(float value){
        if(_jump == false)
            return;
        if((_plRect.y + value) > _maxY){
            _jump = false;
            return;
        }
        _plRect.y = _plRect.y + value;
    }

    public void moveDown(float value){
        _plRect.y = _plRect.y - value;
    }

    public Texture getCurrentTexture(){
        if(_jump){
            if(_directionMove)
                return _plStatesJumpRight.get(1);
            else
                return _plStatesJumpLeft.get(1);
        }else if((!_jump) && (_plRect.y > 100)){
            if(_directionMove)
                return _plStatesJumpRight.get(0);
            else
                return _plStatesJumpLeft.get(0);
        }

        if(_currentMoveState >= _plStatesRight.size)
            _currentMoveState = 0;
        if(_directionMove)
            return _plStatesRight.get(_currentMoveState);
        else
            return _plStatesLeft.get(_currentMoveState);
    }

    public Rectangle getRectangle(){
        return _plRect;
    }

    public boolean isDispose(){
        return ((_plStatesRight == null) || (_plStatesRight.size == 0)
        || (_plStatesLeft == null) || (_plStatesLeft.size == 0));
    }

    public void dispose(){
        for(int i = 0; i < _plStatesRight.size; i++)
            _plStatesRight.get(i).dispose();

        for(int i = 0; i < _plStatesLeft.size; i++)
            _plStatesLeft.get(i).dispose();
    }
}
