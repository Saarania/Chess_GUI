package chess.pkgfinal;

import java.io.IOException;
import java.util.ArrayList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Jan
 */
public class Controller { //nova informace: ve switch nesmi byt v zavorce null, jinak nullPointerException

    private static Field[][] fields;
    private static final Group group = ChessFinal.root;

    private static FigureType lastFigure;
    private static Player activePlayer = Player.WHITE;

    private static boolean black = false; //pomaha pri vytvareni plochy
    private static boolean smer = true; //pri otaceni
    private static ImageView sachImage = new ImageView("images/sachImage.png"); //cerveny okraj kdyz ma nekdo sach
    public static boolean hraJeUKonce = false; //nastaveno na true pokud jsme dohrali, tudiz se nema stav hry jakkoliv ukladat

    private static boolean rBr = true; //rosada black right
    private static boolean rBl = true;
    private static boolean rWr = true;
    private static boolean rWl = true;

    private static ImageView mark1 = new ImageView("images/mark.png");//pro predchozi figuru
    private static ImageView mark2 = new ImageView("images/mark.png");// pro soucasnou figuru
    private static int pescovaSouradnice = 100; //pro brani mimochodem, 100 slouzi jako null

    static {
        fields = new Field[8][8];
    }

    public static void inicializator() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                fields[j][i] = new Field();

                Rectangle r = new Rectangle(Settings.SIZE, Settings.SIZE); //vytvoreni pole
                r.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        eraseAllMoveable();
                    }
                });
                if (black == false) {
                    r.setFill(Color.WHITE);
                } else {
                    r.setFill(Color.GRAY);
                }
                r.setTranslateX(j * Settings.SIZE);
                r.setTranslateY(i * Settings.SIZE);
                group.getChildren().add(r);

                black = !black;
            }
            black = !black;
        }

        //pokud je ulozeny nejaky stav hry, ted se nastavi
        ArrayList<int[]> figurkyZMinula = new ArrayList<>();
        int[] array = null;
        int count = 0; //kontroluje, jestli nahodou ulozena hra obsahuje alespon jednu figurku
        int delkaFigurekZMinula = 0; //cislo se bude zvetsovat, obsahuje indexy pro pole figurkyZMinula
        ArrayList<int[]> poleFielduZeSouboru = null;
        //PODMINKA POKDU MAME ULOZENOU HRU
        if (poleFielduZeSouboru != null) {
            for (int i = 0; i < poleFielduZeSouboru.size(); i++) {
                array = poleFielduZeSouboru.get(i);
                figurkyZMinula.add(array);
            }
            for (int i = 0; i < figurkyZMinula.size(); i++) {
                //System.out.println("X = " + figurkyZMinula[i][2] + ", Y = " + figurkyZMinula[i][3]);
                if (figurkyZMinula.get(i)[0] != -1) {
                    fields[figurkyZMinula.get(i)[2]][figurkyZMinula.get(i)[3]].setFigureType(FigureType.values()[figurkyZMinula.get(i)[0]]);
                    fields[figurkyZMinula.get(i)[2]][figurkyZMinula.get(i)[3]].setPlayer(Player.values()[figurkyZMinula.get(i)[1]]);
                }
            }
        } else {
            fillBoard(Player.WHITE, 6, 7);
            fillBoard(Player.BLACK, 1, 0);
        }
        //nastavit zobrazovadla
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                fields[j][i].setX(j);   //nastavy x a y
                fields[j][i].setY(i);
                if (fields[j][i].getFigureType() != null) { //nastavovani obrazku figurek
                    FieldView figureView = new FieldView("images/" + fields[j][i].getFigureType().toString() + fields[j][i].getPlayer().toString() + ".png", fields[j][i]);
                    figureView.setTranslateX(j * Settings.SIZE);
                    figureView.setTranslateY(i * Settings.SIZE);
                    fields[j][i].setFieldView(figureView);
                    group.getChildren().add(figureView);
                }
            }
        }
        mark1.setVisible(false);
        group.getChildren().add(mark1);
        mark2.setVisible(false);
        group.getChildren().add(mark2);

        group.getChildren().add(sachImage);  //inicializace obrazku kdyz je sach
        sachImage.setVisible(false);

        setImageOnAction();
    }

    private static moveableView createMoveable(int x, int y) {
        moveableView moveable = new moveableView("images/moveable.png", fields[x][y]);
        moveable.setTranslateX(x * Settings.SIZE);
        moveable.setTranslateY(y * Settings.SIZE);
        fields[x][y].setMoveableView(moveable);
        group.getChildren().add(moveable);
        return moveable;
    }

    private static Player unActivePlayer() {
        if (activePlayer == Player.BLACK) {
            return Player.WHITE;
        }
        return Player.BLACK;
    }

    public static boolean moznostZachrany(Player p) { //prokontroluje jestli hrac p nema mat
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() != null) {
                    if (fields[j][i].getPlayer() == p) {
                        switch (fields[j][i].getFigureType()) {
                            case PAWN:
                                if (fields[j][i].getY() == 6 && fields[j][i].getFigureType() == FigureType.PAWN) {
                                    if (fields[fields[j][i].getX()][fields[j][i].getY() - 2].getFigureType() == null) { //nemoznost chodit do sachu
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][fields[j][i].getY() - 2], p)) {
                                            return true;
                                        }
                                    }
                                }

                                if (fields[fields[j][i].getX()][fields[j][i].getY() - 1].getFigureType() == null) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][fields[j][i].getY() - 1], p)) {
                                        return true;
                                    }
                                }

                                if (fields[j][i].getX() != 0) {
                                    if (fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1].getFigureType() != null) {
                                        if (fields[fields[j][i].getX()][fields[j][i].getY()].getPlayer() == fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1].getEnemy()) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1], p)) {
                                                return true;
                                            }
                                        }
                                    } else {
                                        if (fields[j][i].getX() - 1 == pescovaSouradnice && fields[j][i].getY() == 3) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1])) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 7) {
                                    if (fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1].getFigureType() != null) {
                                        if (fields[fields[j][i].getX()][fields[j][i].getY()].getPlayer() == fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1].getEnemy()) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1], p)) {
                                                return true;
                                            }
                                        }
                                    } else {
                                        if (fields[j][i].getX() + 1 == pescovaSouradnice && fields[j][i].getY() == 3) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1])) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                break;

                            case ROOK:
                                for (int k = fields[j][i].getY(); k > -1; k--) {
                                    if (fields[fields[j][i].getX()][k].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][k], fields[fields[j][k].getX()][k], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getY()) {
                                            if (fields[fields[j][i].getX()][k].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][k], fields[fields[j][i].getX()][k], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getY(); k < 8; k++) {
                                    if (fields[fields[j][i].getX()][k].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][k], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getY()) {
                                            if (fields[fields[j][i].getX()][k].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][k], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getX(); k < 8; k++) {
                                    if (fields[k][fields[j][i].getY()].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getX()) {
                                            if (fields[k][fields[j][i].getY()].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getX(); k > -1; k--) {
                                    if (fields[k][fields[j][i].getY()].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getX()) {
                                            if (fields[k][fields[j][i].getY()].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                break;
                            case KING:
                                if (fields[j][i].getY() != 0) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][fields[j][i].getY() - 1], p) && fields[fields[j][i].getX()][fields[j][i].getY() - 1].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getY() != 0 && fields[j][i].getX() != 0) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1], p) && fields[fields[j][i].getX() - 1][fields[j][i].getY() - 1].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getY() != 0 && fields[j][i].getX() != 7) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1], p) && fields[fields[j][i].getX() + 1][fields[j][i].getY() - 1].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getX() != 0) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY()], p) && fields[fields[j][i].getX() - 1][fields[j][i].getY()].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getX() != 7) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY()], p) && fields[fields[j][i].getX() + 1][fields[j][i].getY()].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getY() != 7 && fields[j][i].getX() != 7) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() + 1], p) && fields[fields[j][i].getX() + 1][fields[j][i].getY() + 1].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getY() != 7 && fields[j][i].getX() != 0) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() + 1], p) && fields[fields[j][i].getX() - 1][fields[j][i].getY() + 1].getPlayer() != fields[j][i].getPlayer()) {
                                        return true;
                                    }
                                }
                                if (fields[j][i].getY() != 7) {
                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][fields[j][i].getY() + 1], p) && fields[fields[j][i].getX()][fields[j][i].getY() + 1].getPlayer() != fields[j][i].getPlayer()) { //dole
                                        return true;
                                    }
                                }
                                break;
                            case BISHOPS:
                                int l = 0;
                                for (int s = 0; s <= fields[j][i].getX(); s++) { //sikmo nahoru doleva
                                    if (l <= fields[j][i].getY()) {
                                        if (fields[fields[j][i].getX() - s][fields[j][i].getY() - l].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - s][fields[j][i].getY() - l], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (s != 0) {
                                                if (fields[fields[j][i].getX() - s][fields[j][i].getY() - l].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - s][fields[j][i].getY() - l], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        l++;
                                    }
                                }
                                int op = 0;
                                for (int m = 0; m + fields[j][i].getX() < 8; m++) {  //sikmo nahoru doprava
                                    if (op <= fields[j][i].getY()) {
                                        if (fields[fields[j][i].getX() + m][fields[j][i].getY() - op].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + m][fields[j][i].getY() - op], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (m != 0) {
                                                if (fields[fields[j][i].getX() + m][fields[j][i].getY() - op].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + m][fields[j][i].getY() - op], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        op++;
                                    }
                                }

                                int g = 0;
                                for (int q = 0; q + fields[j][i].getX() < 8; q++) {  //sikmo dolu doprava
                                    if (g + fields[j][i].getY() < 8) {
                                        if (fields[fields[j][i].getX() + q][fields[j][i].getY() + g].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + q][fields[j][i].getY() + g], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (q != 0) {
                                                if (fields[fields[j][i].getX() + q][fields[j][i].getY() + g].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + q][fields[j][i].getY() + g], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        g++;
                                    }
                                }

                                int m = 0;
                                for (int f = 0; f + fields[j][i].getX() > -1; f--) {  //sikmo dolu doleva
                                    if (m + fields[j][i].getY() < 8) {
                                        if (fields[fields[j][i].getX() + f][fields[j][i].getY() + m].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + f][fields[j][i].getY() + m], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (f != 0) {
                                                if (fields[fields[j][i].getX() + f][fields[j][i].getY() + m].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + f][fields[j][i].getY() + m], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        m++;
                                    }
                                }
                                break;
                            case KNIGHT:
                                if (fields[j][i].getX() != 0 && fields[j][i].getY() != 1 && fields[j][i].getY() != 0) {
                                    if (fields[fields[j][i].getX() - 1][fields[j][i].getY() - 2].getFigureType() == null || fields[fields[j][i].getX() - 1][fields[j][i].getY() - 2].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() - 2], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 7 && fields[j][i].getY() != 1 && fields[j][i].getY() != 0) {
                                    if (fields[fields[j][i].getX() + 1][fields[j][i].getY() - 2].getFigureType() == null || fields[fields[j][i].getX() + 1][fields[j][i].getY() - 2].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() - 2], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 0 && fields[j][i].getX() != 1 && fields[j][i].getY() != 7) {
                                    if (fields[fields[j][i].getX() - 2][fields[j][i].getY() + 1].getFigureType() == null || fields[fields[j][i].getX() - 2][fields[j][i].getY() + 1].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 2][fields[j][i].getY() + 1], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 0 && fields[j][i].getX() != 1 && fields[j][i].getY() != 0) {
                                    if (fields[fields[j][i].getX() - 2][fields[j][i].getY() - 1].getFigureType() == null || fields[fields[j][i].getX() - 2][fields[j][i].getY() - 1].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 2][fields[j][i].getY() - 1], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 7 && fields[j][i].getX() != 6 && fields[j][i].getY() != 0) {
                                    if (fields[fields[j][i].getX() + 2][fields[j][i].getY() - 1].getFigureType() == null || fields[fields[j][i].getX() + 2][fields[j][i].getY() - 1].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 2][fields[j][i].getY() - 1], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getX() != 7 && fields[j][i].getX() != 6 && fields[j][i].getY() != 7 && fields[fields[j][i].getX() + 2][fields[j][i].getY() + 1].getPlayer() != fields[j][i].getPlayer()) {
                                    if (fields[fields[j][i].getX() + 2][fields[j][i].getY() + 1].getFigureType() == null || fields[fields[j][i].getX() + 2][fields[j][i].getY() + 1].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 2][fields[j][i].getY() + 1], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getY() != 7 && fields[j][i].getY() != 6 && fields[j][i].getX() != 0) {
                                    if (fields[fields[j][i].getX() - 1][fields[j][i].getY() + 2].getFigureType() == null || fields[fields[j][i].getX() - 1][fields[j][i].getY() + 2].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - 1][fields[j][i].getY() + 2], p)) {
                                            return true;
                                        }
                                    }
                                }
                                if (fields[j][i].getY() != 7 && fields[j][i].getY() != 6 && fields[j][i].getX() != 7) {
                                    if (fields[fields[j][i].getX() + 1][fields[j][i].getY() + 2].getFigureType() == null || fields[fields[j][i].getX() + 1][fields[j][i].getY() + 2].getEnemy() == fields[j][i].getPlayer()) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + 1][fields[j][i].getY() + 2], p)) {
                                            return true;
                                        }
                                    }
                                }
                                break;

                            case QUEEN:
                                int b = 0;
                                for (int s = 0; s <= fields[j][i].getX(); s++) { //sikmo nahoru doleva
                                    if (b <= fields[j][i].getY()) {
                                        if (fields[fields[j][i].getX() - s][fields[j][i].getY() - b].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - s][fields[j][i].getY() - b], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (s != 0) {
                                                if (fields[fields[j][i].getX() - s][fields[j][i].getY() - b].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() - s][fields[j][i].getY() - b], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        b++;
                                    }
                                }
                                int h = 0;
                                for (int ii = 0; ii + fields[j][i].getX() < 8; ii++) {  //sikmo nahoru doprava
                                    if (h <= fields[j][i].getY()) {
                                        if (fields[fields[j][i].getX() + ii][fields[j][i].getY() - h].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + ii][fields[j][i].getY() - h], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (ii != 0) {
                                                if (fields[fields[j][i].getX() + ii][fields[j][i].getY() - h].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + ii][fields[j][i].getY() - h], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        h++;
                                    }
                                }

                                int ij = 0;
                                for (int q = 0; q + fields[j][i].getX() < 8; q++) {  //sikmo dolu doprava
                                    if (ij + fields[j][i].getY() < 8) {
                                        if (fields[fields[j][i].getX() + q][fields[j][i].getY() + ij].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + q][fields[j][i].getY() + ij], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (q != 0) {
                                                if (fields[fields[j][i].getX() + q][fields[j][i].getY() + ij].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + q][fields[j][i].getY() + ij], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        ij++;
                                    }
                                }

                                int ik = 0;
                                for (int f = 0; f + fields[j][i].getX() > -1; f--) {  //sikmo dolu doleva
                                    if (ik + fields[j][i].getY() < 8) {
                                        if (fields[fields[j][i].getX() + f][fields[j][i].getY() + ik].getFigureType() == null) {
                                            if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + f][fields[j][i].getY() + ik], p)) {
                                                return true;
                                            }
                                        } else {
                                            if (f != 0) {
                                                if (fields[fields[j][i].getX() + f][fields[j][i].getY() + ik].getEnemy() != fields[j][i].getEnemy()) {
                                                    if (pohybKontrola(fields[j][i], fields[fields[j][i].getX() + f][fields[j][i].getY() + ik], p)) {
                                                        return true;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        ik++;
                                    }
                                }
                                for (int k = fields[j][i].getY(); k > -1; k--) {
                                    if (fields[fields[j][i].getX()][k].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][k], fields[fields[j][k].getX()][k], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getY()) {
                                            if (fields[fields[j][i].getX()][k].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][k], fields[fields[j][i].getX()][k], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getY(); k < 8; k++) {
                                    if (fields[fields[j][i].getX()][k].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][k], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getY()) {
                                            if (fields[fields[j][i].getX()][k].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[fields[j][i].getX()][k], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getX(); k < 8; k++) {
                                    if (fields[k][fields[j][i].getY()].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getX()) {
                                            if (fields[k][fields[j][i].getY()].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                for (int k = fields[j][i].getX(); k > -1; k--) {
                                    if (fields[k][fields[j][i].getY()].getFigureType() == null) {
                                        if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                            return true;
                                        }
                                    } else {
                                        if (k != fields[j][i].getX()) {
                                            if (fields[k][fields[j][i].getY()].getEnemy() != fields[j][i].getEnemy()) {
                                                if (pohybKontrola(fields[j][i], fields[k][fields[j][i].getY()], p)) {
                                                    return true;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                break;

                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean kralOhrozen(Player player, Field kral) { //player miri na sach
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() != null && fields[j][i].getPlayer() == player) { //jen kvuli vyjimce nullPointerException

                    switch (fields[j][i].getFigureType()) {
                        case ROOK:
                            for (int k = fields[j][i].getY(); k < 8; k++) {
                                if (fields[fields[j][i].getX()][k] == kral) {
                                    return true;
                                }
                                if (fields[fields[j][i].getX()][k].getFigureType() != null && k != fields[j][i].getY()) {
                                    break;
                                }
                            }
                            for (int k = fields[j][i].getY(); k > -1; k--) {
                                if (fields[fields[j][i].getX()][k] == kral) {
                                    return true;
                                }
                                if (fields[fields[j][i].getX()][k].getFigureType() != null && k != fields[j][i].getY()) {
                                    break;
                                }
                            }
                            for (int k = fields[j][i].getX(); k > -1; k--) {
                                if (fields[k][fields[j][i].getY()] == kral) {
                                    return true;
                                }
                                if (fields[k][fields[j][i].getY()].getFigureType() != null && k != fields[j][i].getX()) {
                                    break;
                                }
                            }
                            for (int k = fields[j][i].getX(); k < 8; k++) {
                                if (fields[k][fields[j][i].getY()] == kral) {
                                    return true;
                                }
                                if (fields[k][fields[j][i].getY()].getFigureType() != null && k != fields[j][i].getX()) {
                                    break;
                                }
                            }
                            break;
                        case KING:
                            if (i != 0 && fields[j][i - 1] == kral) { //primo pred
                                return true;
                            }
                            if (j != 0 && i != 0 && fields[j - 1][i - 1] == kral) { //pred vlevo
                                return true;
                            }
                            if (j != 7 && i != 0 && fields[j + 1][i - 1] == kral) { //pred vpravo
                                return true;
                            }
                            if (j != 7 && fields[j + 1][i] == kral) { //vpravo
                                return true;
                            }
                            if (j != 0 && fields[j - 1][i] == kral) { //vlevo
                                return true;
                            }
                            if (i != 7 && j != 7 && fields[j + 1][i + 1] == kral) { //vpravo dole
                                return true;
                            }
                            if (i != 7 && fields[j][i + 1] == kral) { //dole
                                return true;
                            }
                            if (i != 7 && j != 0 && fields[j - 1][i + 1] == kral) { //vpravo dole
                                return true;
                            }
                            break;
                        case BISHOPS:
                            int l = 0;
                            for (int p = 0; p + fields[j][i].getX() < 8; p++) {  //sikmo dolu doprava
                                if (l + fields[j][i].getY() < 8) {
                                    if (fields[fields[j][i].getX() + p][fields[j][i].getY() + l] == kral) {
                                        return true;
                                    } else {
                                        if (p != 0) {
                                            if (fields[fields[j][i].getX() + p][fields[j][i].getY() + l].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    l++;
                                }
                            }

                            int k = 0;
                            for (int u = 0; fields[j][i].getX() - u > -1; u++) {  //sikmo dolu doleva
                                if (k + fields[j][i].getY() < 8) {
                                    if (fields[fields[j][i].getX() - u][fields[j][i].getY() + k] == kral) {
                                        return true;
                                    } else {
                                        if (u != 0) {
                                            if (fields[fields[j][i].getX() - u][fields[j][i].getY() + k].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    k++;
                                }
                            }

                            int q = 0;
                            for (int r = 0; fields[j][i].getX() - r > -1; r++) {  //sikmo nahoru doleva
                                if (fields[j][i].getY() - q > -1) {
                                    if (fields[fields[j][i].getX() - r][fields[j][i].getY() - q] == kral) {
                                        return true;
                                    } else {
                                        if (r != 0) {
                                            if (fields[fields[j][i].getX() - r][fields[j][i].getY() - q].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    q++;
                                }
                            }

                            int b = 0;
                            for (int r = 0; fields[j][i].getX() + r < 8; r++) {  //sikmo nahoru doprava
                                if (fields[j][i].getY() - b > -1) {
                                    if (fields[fields[j][i].getX() + r][fields[j][i].getY() - b] == kral) {
                                        return true;
                                    } else {
                                        if (r != 0) {
                                            if (fields[fields[j][i].getX() + r][fields[j][i].getY() - b].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    b++;
                                }
                            }
                            break;

                        case QUEEN:
                            int x = 0;
                            for (int p = 0; p + fields[j][i].getX() < 8; p++) {  //sikmo dolu doprava
                                if (x + fields[j][i].getY() < 8) {
                                    if (fields[fields[j][i].getX() + p][fields[j][i].getY() + x] == kral) {
                                        return true;
                                    } else {
                                        if (p != 0) {
                                            if (fields[fields[j][i].getX() + p][fields[j][i].getY() + x].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    x++;
                                }
                            }

                            int f = 0;
                            for (int u = 0; fields[j][i].getX() - u > -1; u++) {  //sikmo dolu doleva
                                if (f + fields[j][i].getY() < 8) {
                                    if (fields[fields[j][i].getX() - u][fields[j][i].getY() + f] == kral) {
                                        return true;
                                    } else {
                                        if (u != 0) {
                                            if (fields[fields[j][i].getX() - u][fields[j][i].getY() + f].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    f++;
                                }
                            }

                            int h = 0;
                            for (int r = 0; fields[j][i].getX() - r > -1; r++) {  //sikmo nahoru doleva
                                if (fields[j][i].getY() - h > -1) {
                                    if (fields[fields[j][i].getX() - r][fields[j][i].getY() - h] == kral) {
                                        return true;
                                    } else {
                                        if (r != 0) {
                                            if (fields[fields[j][i].getX() - r][fields[j][i].getY() - h].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    h++;
                                }
                            }

                            int d = 0;
                            for (int r = 0; fields[j][i].getX() + r < 8; r++) {  //sikmo nahoru doprava
                                if (fields[j][i].getY() - d > -1) {
                                    if (fields[fields[j][i].getX() + r][fields[j][i].getY() - d] == kral) {
                                        return true;
                                    } else {
                                        if (r != 0) {
                                            if (fields[fields[j][i].getX() + r][fields[j][i].getY() - d].getFigureType() != null) {
                                                break;
                                            }
                                        }
                                    }
                                    d++;
                                }
                            }

                            for (int s = fields[j][i].getY(); s < 8; s++) {
                                if (fields[fields[j][i].getX()][s] == kral) {
                                    return true;
                                }
                                if (fields[fields[j][i].getX()][s].getFigureType() != null && s != fields[j][i].getY()) {
                                    break;
                                }
                            }
                            for (int s = fields[j][i].getY(); s > -1; s--) {
                                if (fields[fields[j][i].getX()][s] == kral) {
                                    return true;
                                }
                                if (fields[fields[j][i].getX()][s].getFigureType() != null && s != fields[j][i].getY()) {
                                    break;
                                }
                            }
                            for (int s = fields[j][i].getX(); s > -1; s--) {
                                if (fields[s][fields[j][i].getY()] == kral) {
                                    return true;
                                }
                                if (fields[s][fields[j][i].getY()].getFigureType() != null && s != fields[j][i].getX()) {
                                    break;
                                }
                            }
                            for (int s = fields[j][i].getX(); s < 8; s++) {
                                if (fields[s][fields[j][i].getY()] == kral) {
                                    return true;
                                }
                                if (fields[s][fields[j][i].getY()].getFigureType() != null && s != fields[j][i].getX()) {
                                    break;
                                }
                            }
                            break;
                        case KNIGHT:
                            if (fields[j][i].getX() != 0 && fields[j][i].getY() != 1 && fields[j][i].getY() != 0) {
                                if (fields[fields[j][i].getX() - 1][fields[j][i].getY() - 2] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getX() != 7 && fields[j][i].getY() != 1 && fields[j][i].getY() != 0) {
                                if (fields[fields[j][i].getX() + 1][fields[j][i].getY() - 2] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getX() != 0 && fields[j][i].getX() != 1 && fields[j][i].getY() != 7) {
                                if (fields[fields[j][i].getX() - 2][fields[j][i].getY() + 1] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getX() != 0 && fields[j][i].getX() != 1 && fields[j][i].getY() != 0) {
                                if (fields[fields[j][i].getX() - 2][fields[j][i].getY() - 1] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getX() != 7 && fields[j][i].getX() != 6 && fields[j][i].getY() != 0) {
                                if (fields[fields[j][i].getX() + 2][fields[j][i].getY() - 1] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getX() != 7 && fields[j][i].getX() != 6 && fields[j][i].getY() != 7) {
                                if (fields[fields[j][i].getX() + 2][fields[j][i].getY() + 1] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getY() != 7 && fields[j][i].getY() != 6 && fields[j][i].getX() != 0) {
                                if (fields[fields[j][i].getX() - 1][fields[j][i].getY() + 2] == kral) {
                                    return true;
                                }
                            }
                            if (fields[j][i].getY() != 7 && fields[j][i].getY() != 6 && fields[j][i].getX() != 7) {
                                if (fields[fields[j][i].getX() + 1][fields[j][i].getY() + 2] == kral) {
                                    return true;
                                }
                            }
                            break;
                        case PAWN:
                            if (fields[j][i].getY() != 7 && fields[j][i].getX() != 0 && fields[j - 1][i + 1] == kral) {
                                return true;
                            }
                            if (fields[j][i].getY() != 7 && fields[j][i].getX() != 7 && fields[j + 1][i + 1] == kral) {
                                return true;
                            }
                            break;
                    }
                }
            }
        }
        return false;
    }

    public static moveableView[] moznostPohybu(Field field) { //nastavit brani figurek nastavit nehlaseni integer out of bound
        int counter = 0; //pomocna promenna
        moveableView[] moveables = new moveableView[28]; //pole co se vraci
        if (field.getPlayer() == activePlayer) {
            if (field.getFigureType() != null) {
                switch (field.getFigureType()) {
                    case PAWN:
                        if (field.getY() == 6 && field.getFigureType() == FigureType.PAWN) {
                            if (fields[field.getX()][field.getY() - 2].getFigureType() == null) { //nemoznost chodit do sachu
                                if (pohybKontrola(field, fields[field.getX()][field.getY() - 2])) {
                                    moveables[counter] = createMoveable(field.getX(), field.getY() - 2);
                                    counter++;
                                }
                            }
                        }

                        if (fields[field.getX()][field.getY() - 1].getFigureType() == null) {
                            if (pohybKontrola(field, fields[field.getX()][field.getY() - 1])) {
                                moveables[counter] = createMoveable(field.getX(), field.getY() - 1);
                                counter++;
                            }
                        }

                        if (field.getX() != 0) {
                            if (fields[field.getX() - 1][field.getY() - 1].getFigureType() != null) {
                                if (fields[field.getX()][field.getY()].getPlayer() == fields[field.getX() - 1][field.getY() - 1].getEnemy()) {
                                    if (pohybKontrola(field, fields[field.getX() - 1][field.getY() - 1])) {
                                        moveables[counter] = createMoveable(field.getX() - 1, field.getY() - 1);
                                        counter++;
                                    }
                                }
                            } else {
                                if (field.getX() - 1 == pescovaSouradnice && field.getY() == 3) {
                                    if (pohybKontrola(field, fields[field.getX() - 1][field.getY() - 1])) {
                                        moveables[counter] = createMoveable(field.getX() - 1, field.getY() - 1);
                                        counter++;
                                    }
                                }
                            }
                        }
                        if (field.getX() != 7) {
                            if (fields[field.getX() + 1][field.getY() - 1].getFigureType() != null) {
                                if (fields[field.getX()][field.getY()].getPlayer() == fields[field.getX() + 1][field.getY() - 1].getEnemy()) {
                                    if (pohybKontrola(field, fields[field.getX() + 1][field.getY() - 1])) {
                                        moveables[counter] = createMoveable(field.getX() + 1, field.getY() - 1);
                                        counter++;
                                    }
                                }
                            } else {
                                if (field.getX() + 1 == pescovaSouradnice && field.getY() == 3) {
                                    if (pohybKontrola(field, fields[field.getX() + 1][field.getY() - 1])) {
                                        moveables[counter] = createMoveable(field.getX() + 1, field.getY() - 1);
                                        counter++;
                                    }
                                }
                            }
                        }

                        lastFigure = FigureType.PAWN;
                        break;

                    case ROOK:
                        for (int i = field.getY(); i > -1; i--) {
                            if (fields[field.getX()][i].getFigureType() == null) {
                                if (pohybKontrola(field, fields[field.getX()][i])) {
                                    moveables[counter] = createMoveable(field.getX(), i);
                                    counter++;
                                }

                            } else {
                                if (i != field.getY()) {
                                    if (fields[field.getX()][i].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[field.getX()][i])) {
                                            moveables[counter] = createMoveable(field.getX(), i);
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getY(); i < 8; i++) {
                            if (fields[field.getX()][i].getFigureType() == null) {
                                if (pohybKontrola(field, fields[field.getX()][i])) {
                                    moveables[counter] = createMoveable(field.getX(), i);
                                    counter++;
                                }

                            } else {
                                if (i != field.getY()) {
                                    if (fields[field.getX()][i].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[field.getX()][i])) {
                                            moveables[counter] = createMoveable(field.getX(), i);
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getX(); i < 8; i++) {
                            if (fields[i][field.getY()].getFigureType() == null) {
                                if (pohybKontrola(field, fields[i][field.getY()])) {
                                    moveables[counter] = createMoveable(i, field.getY());
                                    counter++;
                                }
                            } else {
                                if (i != field.getX()) {
                                    if (fields[i][field.getY()].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[i][field.getY()])) {
                                            moveables[counter] = createMoveable(i, field.getY());
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getX(); i > -1; i--) {
                            if (fields[i][field.getY()].getFigureType() == null) {
                                if (pohybKontrola(field, fields[i][field.getY()])) {
                                    moveables[counter] = createMoveable(i, field.getY());
                                    counter++;
                                }
                            } else {
                                if (i != field.getX()) {
                                    if (fields[i][field.getY()].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[i][field.getY()])) {
                                            moveables[counter] = createMoveable(i, field.getY());
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        lastFigure = FigureType.ROOK;
                        break;
                    case KING:
                        if (field.getPlayer() == Player.WHITE) {
                            if (rWl && fields[0][7].getFigureType() == FigureType.ROOK && fields[0][7].getPlayer() == Player.WHITE) {
                                if (fields[field.getX() - 1][field.getY()].getFigureType() == null && fields[field.getX() - 2][field.getY()].getFigureType() == null && fields[field.getX() - 3][field.getY()].getFigureType() == null
                                        && pohybKontrola(field, fields[field.getX() - 1][field.getY()], Player.WHITE) && pohybKontrola(field, fields[field.getX() - 2][field.getY()], Player.WHITE) && pohybKontrola(field, fields[field.getX() - 3][field.getY()], Player.WHITE)) {
                                    moveables[counter] = createMoveable(field.getX() - 2, field.getY());
                                    counter++;
                                }
                            }
                            if (rWr && fields[7][7].getFigureType() == FigureType.ROOK && fields[7][7].getPlayer() == Player.WHITE) {
                                if (fields[field.getX() + 1][field.getY()].getFigureType() == null && fields[field.getX() + 2][field.getY()].getFigureType() == null && pohybKontrola(field, fields[field.getX() + 1][field.getY()], Player.WHITE)
                                        && pohybKontrola(field, fields[field.getX() + 2][field.getY()], Player.WHITE)) {
                                    moveables[counter] = createMoveable(field.getX() + 2, field.getY());
                                    counter++;
                                }
                            }
                        }
                        if (field.getPlayer() == Player.BLACK) {
                            if (rBl && fields[0][7].getFigureType() == FigureType.ROOK && fields[0][7].getPlayer() == Player.BLACK) {
                                if (fields[field.getX() - 1][field.getY()].getFigureType() == null && fields[field.getX() - 2][field.getY()].getFigureType() == null && pohybKontrola(field, fields[field.getX() - 1][field.getY()], Player.BLACK)
                                        && pohybKontrola(field, fields[field.getX() - 2][field.getY()], Player.BLACK)) {
                                    moveables[counter] = createMoveable(field.getX() - 2, field.getY());
                                    counter++;
                                }
                            }
                            if (rBr && fields[7][7].getFigureType() == FigureType.ROOK && fields[7][7].getPlayer() == Player.BLACK) {
                                if (fields[field.getX() + 1][field.getY()].getFigureType() == null && fields[field.getX() + 2][field.getY()].getFigureType() == null && fields[field.getX() + 3][field.getY()].getFigureType() == null
                                        && pohybKontrola(field, fields[field.getX() + 1][field.getY()], Player.BLACK) && pohybKontrola(field, fields[field.getX() + 2][field.getY()], Player.BLACK) && pohybKontrola(field, fields[field.getX() + 3][field.getY()], Player.BLACK)) {
                                    moveables[counter] = createMoveable(field.getX() + 2, field.getY());
                                    counter++;
                                }
                            }
                        }
                        if (field.getY() != 0) {
                            if (pohybKontrola(field, fields[field.getX()][field.getY() - 1]) && fields[field.getX()][field.getY() - 1].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX(), field.getY() - 1);
                                counter++;
                            }
                        }
                        if (field.getY() != 0 && field.getX() != 0) {
                            if (pohybKontrola(field, fields[field.getX() - 1][field.getY() - 1]) && fields[field.getX() - 1][field.getY() - 1].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() - 1, field.getY() - 1);
                                counter++;
                            }
                        }
                        if (field.getY() != 0 && field.getX() != 7) {
                            if (pohybKontrola(field, fields[field.getX() + 1][field.getY() - 1]) && fields[field.getX() + 1][field.getY() - 1].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() + 1, field.getY() - 1);
                                counter++;
                            }
                        }
                        if (field.getX() != 0) {
                            if (pohybKontrola(field, fields[field.getX() - 1][field.getY()]) && fields[field.getX() - 1][field.getY()].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() - 1, field.getY());
                                counter++;
                            }
                        }
                        if (field.getX() != 7) {
                            if (pohybKontrola(field, fields[field.getX() + 1][field.getY()]) && fields[field.getX() + 1][field.getY()].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() + 1, field.getY());
                                counter++;
                            }
                        }
                        if (field.getY() != 7 && field.getX() != 7) {
                            if (pohybKontrola(field, fields[field.getX() + 1][field.getY() + 1]) && fields[field.getX() + 1][field.getY() + 1].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() + 1, field.getY() + 1);
                                counter++;
                            }
                        }
                        if (field.getY() != 7 && field.getX() != 0) {
                            if (pohybKontrola(field, fields[field.getX() - 1][field.getY() + 1]) && fields[field.getX() - 1][field.getY() + 1].getPlayer() != field.getPlayer()) {
                                moveables[counter] = createMoveable(field.getX() - 1, field.getY() + 1);
                                counter++;
                            }
                        }
                        if (field.getY() != 7) {
                            if (pohybKontrola(field, fields[field.getX()][field.getY() + 1]) && fields[field.getX()][field.getY() + 1].getPlayer() != field.getPlayer()) { //dole
                                moveables[counter] = createMoveable(field.getX(), field.getY() + 1);
                                counter++;
                            }

                        }
                        lastFigure = FigureType.KING;
                        break;
                    case BISHOPS:
                        int j = 0;
                        for (int i = 0; i <= field.getX(); i++) { //sikmo nahoru doleva
                            if (j <= field.getY()) {
                                if (fields[field.getX() - i][field.getY() - j].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() - i][field.getY() - i])) {
                                        moveables[counter] = createMoveable(field.getX() - i, field.getY() - j);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() - i][field.getY() - j].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() - i][field.getY() - i])) {
                                                moveables[counter] = createMoveable(field.getX() - i, field.getY() - j);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                j++;
                            }
                        }
                        int k = 0;
                        for (int i = 0; i + field.getX() < 8; i++) {  //sikmo nahoru doprava
                            if (k <= field.getY()) {
                                if (fields[field.getX() + i][field.getY() - k].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() - k])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() - k);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() - k].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() + i][field.getY() - k])) {
                                                moveables[counter] = createMoveable(field.getX() + i, field.getY() - k);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                k++;
                            }
                        }

                        int l = 0;
                        for (int i = 0; i + field.getX() < 8; i++) {  //sikmo dolu doprava
                            if (l + field.getY() < 8) {
                                if (fields[field.getX() + i][field.getY() + l].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() + l])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() + l);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() + l].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() + i][field.getY() + l])) {
                                                moveables[counter] = createMoveable(field.getX() + i, field.getY() + l);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                l++;
                            }
                        }

                        int m = 0;
                        for (int i = 0; i + field.getX() > -1; i--) {  //sikmo dolu doleva
                            if (m + field.getY() < 8) {
                                if (fields[field.getX() + i][field.getY() + m].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() + m])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() + m);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() + m].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() + i][field.getY() + m])) {
                                                moveables[counter] = createMoveable(field.getX() + i, field.getY() + m);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                m++;
                            }
                        }
                        lastFigure = FigureType.BISHOPS;
                        break;
                    case KNIGHT:
                        if (field.getX() != 0 && field.getY() != 1 && field.getY() != 0) {
                            if (fields[field.getX() - 1][field.getY() - 2].getFigureType() == null || fields[field.getX() - 1][field.getY() - 2].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() - 1][field.getY() - 2])) {
                                    moveables[counter] = createMoveable(field.getX() - 1, field.getY() - 2);
                                    counter++;
                                }
                            }
                        }
                        if (field.getX() != 7 && field.getY() != 1 && field.getY() != 0) {
                            if (fields[field.getX() + 1][field.getY() - 2].getFigureType() == null || fields[field.getX() + 1][field.getY() - 2].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() + 1][field.getY() - 2])) {
                                    moveables[counter] = createMoveable(field.getX() + 1, field.getY() - 2);
                                    counter++;
                                }
                            }
                        }
                        if (field.getX() != 0 && field.getX() != 1 && field.getY() != 7) {
                            if (fields[field.getX() - 2][field.getY() + 1].getFigureType() == null || fields[field.getX() - 2][field.getY() + 1].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() - 2][field.getY() + 1])) {
                                    moveables[counter] = createMoveable(field.getX() - 2, field.getY() + 1);
                                    counter++;
                                }
                            }
                        }
                        if (field.getX() != 0 && field.getX() != 1 && field.getY() != 0) {
                            if (fields[field.getX() - 2][field.getY() - 1].getFigureType() == null || fields[field.getX() - 2][field.getY() - 1].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() - 2][field.getY() - 1])) {
                                    moveables[counter] = createMoveable(field.getX() - 2, field.getY() - 1);
                                    counter++;
                                }
                            }
                        }
                        if (field.getX() != 7 && field.getX() != 6 && field.getY() != 0) {
                            if (fields[field.getX() + 2][field.getY() - 1].getFigureType() == null || fields[field.getX() + 2][field.getY() - 1].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() + 2][field.getY() - 1])) {
                                    moveables[counter] = createMoveable(field.getX() + 2, field.getY() - 1);
                                    counter++;
                                }
                            }
                        }
                        if (field.getX() != 7 && field.getX() != 6 && field.getY() != 7 && fields[field.getX() + 2][field.getY() + 1].getPlayer() != field.getPlayer()) {
                            if (fields[field.getX() + 2][field.getY() + 1].getFigureType() == null || fields[field.getX() + 2][field.getY() + 1].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() + 2][field.getY() + 1])) {
                                    moveables[counter] = createMoveable(field.getX() + 2, field.getY() + 1);
                                    counter++;
                                }
                            }
                        }
                        if (field.getY() != 7 && field.getY() != 6 && field.getX() != 0) {
                            if (fields[field.getX() - 1][field.getY() + 2].getFigureType() == null || fields[field.getX() - 1][field.getY() + 2].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() - 1][field.getY() + 2])) {
                                    moveables[counter] = createMoveable(field.getX() - 1, field.getY() + 2);
                                    counter++;
                                }
                            }
                        }
                        if (field.getY() != 7 && field.getY() != 6 && field.getX() != 7) {
                            if (fields[field.getX() + 1][field.getY() + 2].getFigureType() == null || fields[field.getX() + 1][field.getY() + 2].getEnemy() == field.getPlayer()) {
                                if (pohybKontrola(field, fields[field.getX() + 1][field.getY() + 2])) {
                                    moveables[counter] = createMoveable(field.getX() + 1, field.getY() + 2);
                                    counter++;
                                }
                            }
                        }
                        lastFigure = FigureType.KNIGHT;
                        break;

                    case QUEEN:
                        int b = 0;
                        for (int i = 0; i <= field.getX(); i++) { //sikmo nahoru doleva
                            if (b <= field.getY()) {
                                if (fields[field.getX() - i][field.getY() - b].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() - i][field.getY() - b])) {
                                        moveables[counter] = createMoveable(field.getX() - i, field.getY() - b);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() - i][field.getY() - b].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() - i][field.getY() - b])) {
                                                moveables[counter] = createMoveable(field.getX() - i, field.getY() - b);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                b++;
                            }
                        }
                        int s = 0;
                        for (int i = 0; i + field.getX() < 8; i++) {  //sikmo nahoru doprava
                            if (s <= field.getY()) {
                                if (fields[field.getX() + i][field.getY() - s].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() - s])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() - s);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() - s].getEnemy() != field.getEnemy()) {
                                            moveables[counter] = createMoveable(field.getX() + i, field.getY() - s);
                                            counter++;
                                        }
                                        break;
                                    }
                                }
                                s++;
                            }
                        }

                        int p = 0;
                        for (int i = 0; i + field.getX() < 8; i++) {  //sikmo dolu doprava
                            if (p + field.getY() < 8) {
                                if (fields[field.getX() + i][field.getY() + p].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() + p])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() + p);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() + p].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() + i][field.getY() + p])) {
                                                moveables[counter] = createMoveable(field.getX() + i, field.getY() + p);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                p++;
                            }
                        }

                        int o = 0;
                        for (int i = 0; i + field.getX() > -1; i--) {  //sikmo dolu doleva
                            if (o + field.getY() < 8) {
                                if (fields[field.getX() + i][field.getY() + o].getFigureType() == null) {
                                    if (pohybKontrola(field, fields[field.getX() + i][field.getY() + o])) {
                                        moveables[counter] = createMoveable(field.getX() + i, field.getY() + o);
                                        counter++;
                                    }
                                } else {
                                    if (i != 0) {
                                        if (fields[field.getX() + i][field.getY() + o].getEnemy() != field.getEnemy()) {
                                            if (pohybKontrola(field, fields[field.getX() + i][field.getY() + o])) {
                                                moveables[counter] = createMoveable(field.getX() + i, field.getY() + o);
                                                counter++;
                                            }
                                        }
                                        break;
                                    }
                                }
                                o++;
                            }
                        }
                        for (int i = field.getY(); i > -1; i--) {
                            if (fields[field.getX()][i].getFigureType() == null) {
                                if (pohybKontrola(field, fields[field.getX()][i])) {
                                    moveables[counter] = createMoveable(field.getX(), i);
                                    counter++;
                                }

                            } else {
                                if (i != field.getY()) {
                                    if (fields[field.getX()][i].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[field.getX()][i])) {
                                            moveables[counter] = createMoveable(field.getX(), i);
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getY(); i < 8; i++) {
                            if (fields[field.getX()][i].getFigureType() == null) {
                                if (pohybKontrola(field, fields[field.getX()][i])) {
                                    moveables[counter] = createMoveable(field.getX(), i);
                                    counter++;
                                }

                            } else {
                                if (i != field.getY()) {
                                    if (fields[field.getX()][i].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[field.getX()][i])) {
                                            moveables[counter] = createMoveable(field.getX(), i);
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getX(); i < 8; i++) {
                            if (fields[i][field.getY()].getFigureType() == null) {
                                if (pohybKontrola(field, fields[i][field.getY()])) {
                                    moveables[counter] = createMoveable(i, field.getY());
                                    counter++;
                                }
                            } else {
                                if (i != field.getX()) {
                                    if (fields[i][field.getY()].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[i][field.getY()])) {
                                            moveables[counter] = createMoveable(i, field.getY());
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        for (int i = field.getX(); i > -1; i--) {
                            if (fields[i][field.getY()].getFigureType() == null) {
                                if (pohybKontrola(field, fields[i][field.getY()])) {
                                    moveables[counter] = createMoveable(i, field.getY());
                                    counter++;
                                }
                            } else {
                                if (i != field.getX()) {
                                    if (fields[i][field.getY()].getEnemy() != field.getEnemy()) {
                                        if (pohybKontrola(field, fields[i][field.getY()])) {
                                            moveables[counter] = createMoveable(i, field.getY());
                                            counter++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        lastFigure = FigureType.QUEEN;
                        break;

                }
            }
        }
        return moveables;
    }

    public static void setImageOnAction() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFieldView() != null) { //predchozi figurka je na miste fields[jFinal][iFinal]
                    final int iFinal = i;
                    final int jFinal = j;
                    fields[j][i].getFieldView().setOnMouseClicked(new EventHandler<MouseEvent>() { //nastaveni pro zmacknuti figurky

                        @Override
                        public void handle(MouseEvent event) { //budouci bude na miste moveables[kFinal].getField
                            eraseAllMoveable();
                            moveableView[] moveables = moznostPohybu(fields[jFinal][iFinal]);
                            for (int k = 0; k < moveables.length; k++) {
                                if (moveables[k] != null) {
                                    final int kFinal = k;
                                    moveables[k].setOnMouseClicked(new EventHandler<Event>() { //nastaveni pro zmacknuti jakehokoliv moveable

                                        @Override
                                        public void handle(Event event) {
                                            mark1.setVisible(true);
                                            mark2.setVisible(true);
                                            mark1.setTranslateX((7 - fields[jFinal][iFinal].getX()) * Settings.SIZE);//nastaveni pro oznacovani kroku
                                            mark1.setTranslateY((7 - fields[jFinal][iFinal].getY()) * Settings.SIZE);
                                            mark2.setTranslateX((7 - moveables[kFinal].getField().getX()) * Settings.SIZE);
                                            mark2.setTranslateY((7 - moveables[kFinal].getField().getY()) * Settings.SIZE);

                                            pescovaSouradnice = 100; //brani mimochodem
                                            if (lastFigure == FigureType.PAWN && fields[jFinal][iFinal].getY() - 2 == moveables[kFinal].getField().getY()) {
                                                pescovaSouradnice = 7 - fields[jFinal][iFinal].getX();
                                            }
                                            if (lastFigure == FigureType.PAWN) {
                                                if (fields[jFinal][iFinal].getX() != 0 && fields[fields[jFinal][iFinal].getX() - 1][fields[jFinal][iFinal].getY() - 1].getFigureType() == null && fields[fields[jFinal][iFinal].getX() - 1][fields[jFinal][iFinal].getY() - 1].getMoveableView() != null) {
                                                    erase(fields[fields[jFinal][iFinal].getX() - 1][fields[jFinal][iFinal].getY()]);
                                                }
                                                if (fields[jFinal][iFinal].getX() != 7 && fields[fields[jFinal][iFinal].getX() + 1][fields[jFinal][iFinal].getY() - 1].getFigureType() == null && fields[fields[jFinal][iFinal].getX() + 1][fields[jFinal][iFinal].getY() - 1].getMoveableView() != null) {
                                                    erase(fields[fields[jFinal][iFinal].getX() + 1][fields[jFinal][iFinal].getY()]);
                                                }
                                            }
                                            if (activePlayer == Player.WHITE) { //rosada
                                                if (fields[jFinal][iFinal].getFigureType() == FigureType.KING) {
                                                    if (moveables[kFinal].getField().getX() == 2 && moveables[kFinal].getField().getY() == 7) {
                                                        setFigure(fields[3][iFinal], FigureType.ROOK, activePlayer);
                                                        erase(fields[0][7]);
                                                        if (activePlayer == Player.WHITE) {
                                                            rWl = false;
                                                            rWr = false;
                                                        }
                                                    }
                                                    if (moveables[kFinal].getField().getX() == 6 && moveables[kFinal].getField().getY() == 7) {
                                                        setFigure(fields[5][iFinal], FigureType.ROOK, activePlayer);
                                                        erase(fields[7][7]);
                                                        if (activePlayer == Player.WHITE) {
                                                            rWl = false;
                                                            rWr = false;
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (fields[jFinal][iFinal].getFigureType() == FigureType.KING) {
                                                    if (moveables[kFinal].getField().getX() == 1 && moveables[kFinal].getField().getY() == 7) {
                                                        setFigure(fields[2][iFinal], FigureType.ROOK, activePlayer);
                                                        erase(fields[0][7]);
                                                        if (activePlayer == Player.BLACK) {
                                                            rBl = false;
                                                            rBr = false;
                                                        }
                                                    }
                                                    if (moveables[kFinal].getField().getX() == 5 && moveables[kFinal].getField().getY() == 7) {
                                                        setFigure(fields[4][iFinal], FigureType.ROOK, activePlayer);
                                                        erase(fields[7][7]);
                                                        if (activePlayer == Player.WHITE) {
                                                            rBl = false;
                                                            rBr = false;
                                                        }
                                                    }
                                                }
                                            }
                                            sachImage.setVisible(false); //odstrani sach obrazek
                                            erase(moveables[kFinal].getField()); //vyhodi figuru
                                            setFigure(moveables[kFinal], activePlayer); //premisti figuru
                                            eraseAllMoveable();
                                            erase(fields[jFinal][iFinal]); //vymaze predchozi (v pohybu)
                                            moveables[kFinal].setVisible(false);
                                            if (lastFigure == FigureType.PAWN && fields[jFinal][iFinal].getY() == 1/* && fields[jFinal][iFinal].getPlayer() == activePlayer*/) {
                                                erase(moveables[kFinal].getField()); //kdyz pesec dojde nakonec zmeni se
                                                ImageView BSW = new ImageView();
                                                ImageView BVW = new ImageView();
                                                ImageView BDW = new ImageView();
                                                ImageView BKW = new ImageView();
                                                if (activePlayer == Player.WHITE) {
                                                    BSW.setImage(new Image("images/BISHOPSWHITEB.png"));
                                                    BVW.setImage(new Image("images/ROOKWHITEB.png"));
                                                    BDW.setImage(new Image("images/QUEENWHITEB.png"));
                                                    BKW.setImage(new Image("images/HORSEWHITEB.png"));
                                                } else {
                                                    BSW.setImage(new Image("images/BISHOPSBLACKB.png"));
                                                    BVW.setImage(new Image("images/ROOKBLACKB.png"));
                                                    BDW.setImage(new Image("images/QUEENBLACKB.png"));
                                                    BKW.setImage(new Image("images/HORSEBLACKB.png"));
                                                }
                                                BSW.setTranslateY(20);
                                                BVW.setTranslateX(100);
                                                BVW.setTranslateY(20);
                                                BDW.setTranslateX(200);
                                                BDW.setTranslateY(20);
                                                BKW.setTranslateX(300);
                                                BKW.setTranslateY(20);
                                                group.getChildren().add(BSW);
                                                group.getChildren().add(BVW);
                                                group.getChildren().add(BDW);
                                                group.getChildren().add(BKW);
                                                BSW.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                    @Override
                                                    public void handle(MouseEvent event) {
                                                        setFigure(fields[7 - moveables[kFinal].getField().getX()][7 - moveables[kFinal].getField().getY()], FigureType.BISHOPS, unActivePlayer());
                                                        BSW.setVisible(false);
                                                        BVW.setVisible(false);
                                                        BDW.setVisible(false);
                                                        BKW.setVisible(false);
                                                        setImageOnAction();
                                                    }
                                                });
                                                BVW.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                    @Override
                                                    public void handle(MouseEvent event) {
                                                        setFigure(fields[7 - moveables[kFinal].getField().getX()][7 - moveables[kFinal].getField().getY()], FigureType.ROOK, unActivePlayer());
                                                        BSW.setVisible(false);
                                                        BVW.setVisible(false);
                                                        BDW.setVisible(false);
                                                        BKW.setVisible(false);
                                                        setImageOnAction();
                                                    }
                                                });
                                                BDW.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                    @Override
                                                    public void handle(MouseEvent event) {
                                                        setFigure(fields[7 - moveables[kFinal].getField().getX()][7 - moveables[kFinal].getField().getY()], FigureType.QUEEN, unActivePlayer());
                                                        BSW.setVisible(false);
                                                        BVW.setVisible(false);
                                                        BDW.setVisible(false);
                                                        BKW.setVisible(false);
                                                        setImageOnAction();
                                                    }
                                                });
                                                BKW.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                    @Override
                                                    public void handle(MouseEvent event) {
                                                        setFigure(fields[7 - moveables[kFinal].getField().getX()][7 - moveables[kFinal].getField().getY()], FigureType.KNIGHT, unActivePlayer());
                                                        BSW.setVisible(false);
                                                        BVW.setVisible(false);
                                                        BDW.setVisible(false);
                                                        BKW.setVisible(false);
                                                        setImageOnAction();
                                                    }
                                                });
                                            }
                                            pretocit(); //pretoci
                                            if (lastFigure == FigureType.ROOK) { //ukonceni rosady
                                                if (fields[jFinal][iFinal].getX() == 0 && fields[jFinal][iFinal].getY() == 7 && fields[jFinal][iFinal].getPlayer() == Player.WHITE) {
                                                    rWl = false;
                                                }
                                                if (fields[jFinal][iFinal].getX() == 0 && fields[jFinal][iFinal].getY() == 7 && fields[jFinal][iFinal].getPlayer() == Player.BLACK) {
                                                    rBl = false;
                                                }
                                                if (fields[jFinal][iFinal].getX() == 7 && fields[jFinal][iFinal].getY() == 7 && fields[jFinal][iFinal].getPlayer() == Player.WHITE) {
                                                    rWr = false;
                                                }
                                                if (fields[jFinal][iFinal].getX() == 7 && fields[jFinal][iFinal].getY() == 7 && fields[jFinal][iFinal].getPlayer() == Player.BLACK) {
                                                    rBr = false;
                                                }
                                            }
                                            if (lastFigure == FigureType.KING) { //zakaze rosadu
                                                if (fields[jFinal][iFinal].getPlayer() == Player.WHITE) {
                                                    rWl = false;
                                                    rWr = false;
                                                }
                                                if (fields[jFinal][iFinal].getPlayer() == Player.BLACK) {
                                                    rBl = false;
                                                    rBr = false;
                                                }
                                            }
                                            if (!moznostZachrany(unActivePlayer()) && kontrolaRemizy()) { //nastaveni pro pat
                                                hraJeUKonce = true;
                                                ImageView patImage = new ImageView("images/pat.png");
                                                group.getChildren().add(patImage);
                                                ImageView zpet = new ImageView("images/zpatky.png");
                                                zpet.setTranslateX(4 * Settings.SIZE - 75);
                                                zpet.setTranslateY(4 * Settings.SIZE);
                                                group.getChildren().add(zpet);
                                                zpet.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                    @Override
                                                    public void handle(MouseEvent event) {
                                                        zpet.setVisible(false);
                                                        patImage.setVisible(false);
                                                    }
                                                });
                                            } else {
                                                if (kralOhrozen(activePlayer, najdiKralovoMisto(unActivePlayer()))) {
                                                    sachImage.setTranslateX(najdiKralovoMisto(unActivePlayer()).getX() * Settings.SIZE);
                                                    sachImage.setTranslateY(najdiKralovoMisto(unActivePlayer()).getY() * Settings.SIZE);
                                                    sachImage.setVisible(true);
                                                    if (!moznostZachrany(unActivePlayer())) { //pokud je sach mat
                                                        //System.out.println("konec hry");
                                                        hraJeUKonce = true;
                                                        ImageView matImage = new ImageView("images/konecHry.png");
                                                        group.getChildren().add(matImage);
                                                        ImageView zpet = new ImageView("images/zpatky.png");
                                                        zpet.setTranslateX(4 * Settings.SIZE - 75);
                                                        zpet.setTranslateY(4 * Settings.SIZE);
                                                        group.getChildren().add(zpet);
                                                        zpet.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                                            @Override
                                                            public void handle(MouseEvent event) {
                                                                zpet.setVisible(false);
                                                                matImage.setVisible(false);
                                                            }
                                                        });
                                                    } else { //tolik elsu proto aby po konci hry se nemohlo ovladat s figurkama
                                                        setImageOnAction(); //nastavi ovladani nanovo
                                                    }
                                                } else {
                                                    setImageOnAction();
                                                }
                                            }
                                            if (activePlayer == Player.BLACK) {   //odkomentovat pro hrani za cerne
                                                activePlayer = Player.WHITE;
                                            } else {
                                                activePlayer = Player.BLACK;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private static boolean kontrolaRemizy() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() == FigureType.BISHOPS || fields[j][i].getFigureType() == FigureType.QUEEN || fields[j][i].getFigureType() == FigureType.BISHOPS || fields[j][i].getFigureType() == FigureType.KNIGHT || fields[j][i].getFigureType() == FigureType.ROOK) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Player opacnaBarva(Player p) {
        if (p == Player.BLACK) {
            return Player.WHITE;
        } else {
            return Player.BLACK;
        }
    }

    private static int pocetFigurVeHre() {
        int pocet = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() != null) {
                    pocet++;
                }
            }
        }
        return pocet;
    }

    static void pretocit() {
        int pocetFigur = pocetFigurVeHre();
        Field[] fieldss = new Field[pocetFigur]; //pro ty co nemaji protejsiho
        Field[] fields2 = new Field[pocetFigur]; //pro ty co ho maji
        int counter = 0; //na fieldss
        int counter2 = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() != null) {
                    fieldss[counter] = fields[j][i];
                    counter++;
                }
            }
        }
        for (int i = 0; i < fieldss.length; i++) {
            if (fieldss[i] != null) {
                if (fields[7 - fieldss[i].getX()][7 - fieldss[i].getY()].getFigureType() == null) {
                    setFigure(fields[7 - fieldss[i].getX()][7 - fieldss[i].getY()], fieldss[i].getFigureType(), fieldss[i].getPlayer());
                    erase(fieldss[i]);
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() != null) {
                    fields2[counter2] = fields[j][i];
                    counter2++;
                }
            }
        }
        for (int i = 0; i < fields2.length; i++) {
            if (fields2[i] != null) {
                if (fields[7 - fields2[i].getX()][7 - fields2[i].getY()].getFigureType() != null) {
                    Field pomocne = new Field();
                    setFigure(pomocne, fields2[i].getFigureType(), fields2[i].getPlayer());
                    erase(fields2[i]);

                    setFigure(fields2[i], fields[7 - fields2[i].getX()][7 - fields2[i].getY()].getFigureType(), fields[7 - fields2[i].getX()][7 - fields2[i].getY()].getPlayer());
                    erase(fields[7 - fields2[i].getX()][7 - fields2[i].getY()]);
                    setFigure(fields[7 - fields2[i].getX()][7 - fields2[i].getY()], pomocne.getFigureType(), pomocne.getPlayer());
                    erase(pomocne);
                }
            }
        }
        setImageOnAction();
    }

    private static Field najdiKralovoMisto(Player player) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[j][i].getFigureType() == FigureType.KING && fields[j][i].getPlayer() == player) {
                    return fields[j][i];
                }
            }
        }
        return null;
    }

    public static boolean pohybKontrola(Field field, Field misto, Player p) { //hrac p nema sach
        FigureType figurka = field.getFigureType();
        Player hrac = field.getPlayer();
        FigureType predchoziFigura = misto.getFigureType();
        Player predchoziHrac = misto.getPlayer();
        setFigureOnField(misto, hrac, figurka);
        setFigureOnField(field, null, null);
        if (kralOhrozen(opacnaBarva(p), najdiKralovoMisto(p))) {
            setFigureOnField(field, hrac, figurka);
            setFigureOnField(misto, predchoziHrac, predchoziFigura);
            return false;
        }
        setFigureOnField(misto, predchoziHrac, predchoziFigura);
        setFigureOnField(field, hrac, figurka);
        return true;
    }

    public static boolean pohybKontrola(Field field, Field misto) {
        FigureType figurka = field.getFigureType();
        Player hrac = field.getPlayer();
        FigureType predchoziFigura = misto.getFigureType();
        Player predchoziHrac = misto.getPlayer();
        setFigureOnField(misto, hrac, figurka);
        setFigureOnField(field, null, null);
        if (kralOhrozen(unActivePlayer(), najdiKralovoMisto(activePlayer))) {
            setFigureOnField(field, hrac, figurka);
            setFigureOnField(misto, predchoziHrac, predchoziFigura);
            return false;
        }
        setFigureOnField(misto, predchoziHrac, predchoziFigura);
        setFigureOnField(field, hrac, figurka);
        return true;
    }

    private static void setFigureOnField(Field field, Player p, FigureType f) {
        field.setFigureType(f);
        field.setPlayer(p);
    }

    private static void setFigure(Field field, FigureType f, Player p) {
        field.setFieldView(new FieldView("images/" + f.toString() + p.toString() + ".png", field));
        field.getFieldView().setTranslateX(field.getX() * Settings.SIZE);
        field.getFieldView().setTranslateY(field.getY() * Settings.SIZE);
        field.setFigureType(f);
        field.setPlayer(p);
        group.getChildren().add(field.getFieldView());
    }

    private static void setFigure(moveableView moveable, Player p) {
        moveable.getField().setFigureType(lastFigure);
        moveable.getField().setPlayer(p);
        moveable.getField().setFieldView(new FieldView("images/" + lastFigure.toString() + p.toString() + ".png", moveable.getField()));
        moveable.getField().getFieldView().setTranslateX(moveable/*.getField()*/.getTranslateX());
        moveable.getField().getFieldView().setTranslateY(moveable/*.getField()*/.getTranslateY());
        group.getChildren().add(moveable.getField().getFieldView());
    }

    private static void setFigure(moveableView moveable, Player p, FigureType f) {
        moveable.getField().setFigureType(f);
        moveable.getField().setPlayer(p);
        moveable.getField().setFieldView(new FieldView("images/" + f.toString() + p.toString() + ".png", moveable.getField()));
        moveable.getField().getFieldView().setTranslateX(moveable/*.getField()*/.getTranslateX());
        moveable.getField().getFieldView().setTranslateY(moveable/*.getField()*/.getTranslateY());
        group.getChildren().add(moveable.getField().getFieldView());
    }

    private static void fillBoard(Player player, int pawnIndex, int kingIndex) {
        for (int i = 0; i < 8; i++) {
            fields[i][pawnIndex] = new Field(FigureType.PAWN, player);
        }
        fields[0][kingIndex] = new Field(FigureType.ROOK, player);
        fields[1][kingIndex] = new Field(FigureType.KNIGHT, player);
        fields[2][kingIndex] = new Field(FigureType.BISHOPS, player);
        fields[3][kingIndex] = new Field(FigureType.QUEEN, player);
        fields[4][kingIndex] = new Field(FigureType.KING, player);
        fields[5][kingIndex] = new Field(FigureType.BISHOPS, player);
        fields[6][kingIndex] = new Field(FigureType.KNIGHT, player);
        fields[7][kingIndex] = new Field(FigureType.ROOK, player);
    }

    public static Field[][] getBoard() {
        return fields;
    }

    public static void eraseAllMoveable() {
        for (int k = 0; k < 8; k++) {           //maze a zapomina vsechny tecky FUNGUJE               
            for (int l = 0; l < 8; l++) {
                if (fields[l][k].getMoveableView() != null) {
                    fields[l][k].getMoveableView().setVisible(false);
                    fields[l][k].setMoveableView(null);
                }
            }
        }
    }

    private static void erase(Field field) {
        if (field.getFieldView() != null) {
            field.getFieldView().setVisible(false);
        }
        if (field.getMoveableView() != null) {
            field.getMoveableView().setVisible(false);
        }
        field.setPlayer(null);
        field.setFigureType(null);
        field.setFieldView(null);
        field.setMoveableView(null); //projistotu
    }

    //ulozit stav hry
    public static void ulozitStavhry() throws IOException {
        ArrayList<int[]> poleFieldu = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[i][j].getFigureType() != null) {
                    poleFieldu.add(fieldToIntArray(fields[i][j]));
                }
            }
        }
    }

    /**
     * int [0] = FigureType int [1] = Player int [2] = getX() int [3] = getY()
     *
     * @param f
     * @return
     */
    public static int[] fieldToIntArray(Field f) {
        int[] array = new int[4];
        //prida na prvni pozici cislo 1 - 6 podle figurky
        array[0] = -1; //pokud je figurka rovna null
        for (int i = 0; i < FigureType.values().length; i++) {
            if (FigureType.values()[i] == f.getFigureType()) {
                array[0] = i;
                break;
            }
        }
        //nastavi hodnotu pro typu hrace
        if (array[0] != -1) {
            switch (f.getPlayer()) {
                case BLACK:
                    array[1] = 0;
                    break;
                case WHITE:
                    array[1] = 1;
                    break;
            }
        }
        //ulozi na int x a y fieldu
        array[2] = f.getX();
        array[3] = f.getY();
        
        return array;
    }
}
