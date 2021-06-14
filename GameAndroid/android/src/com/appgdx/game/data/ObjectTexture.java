package com.appgdx.game.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class ObjectTexture{                         //объект с текстурой
    public Texture texture; //текстура
    public Rectangle rect;  //прямоугольник, в который вписана текстура

    public ObjectTexture(Texture texture, Rectangle rect) {
        this.texture = texture;
        this.rect = rect;
    }
}
