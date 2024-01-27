/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.pkgfinal;

import javafx.scene.image.ImageView;


/**
 *
 * @author Jan
 */
public class Field {

    private FigureType figureType;
    private Player player;
    private FieldView fieldView;
    private moveableView moveableView;
    private int x;
    private int y;

    public Field(FigureType figureType, Player player) {
        this.figureType = figureType;
        this.player = player;
    }
    
    public Field(FigureType figureType) {
        this.figureType = figureType;
    }
    
    public Field() {
        
    }
    public Field(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public moveableView getMoveableView() {
        return moveableView;
    }

    public void setMoveableView(moveableView moveable) {
        this.moveableView = moveable;
    }

    public void setFieldView(FieldView fieldView) {
        this.fieldView = fieldView;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public Player getEnemy() {
        if (player ==  Player.BLACK) {
            return Player.WHITE;
        }
        return Player.BLACK;
    }
    public FigureType getFigureType() {
        return figureType;
    }

    public void setFigureType(FigureType figureType) {
        this.figureType = figureType;
        
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public FieldView getFieldView() {
        return fieldView;
    }

    
}
