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
public class moveableView extends ImageView{
    private Field field;

    public moveableView(Field field) {
        this.field = field;
    }

    public moveableView(String url, Field field) {
        super(url);
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
    
    
    
}
