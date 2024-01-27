package chess.pkgfinal;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 *
 * @author Jan
 */
public class FieldView extends ImageView {
    private Field field;
    
    public FieldView(Field field) {
        this.field = field;
    }

    public FieldView(String url, Field field) {
        super(url);
        this.field = field;
    }
    
}
