package chess.pkgfinal;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 *
 * @author Jan Praks
 *
 * 2
 *
 * asi jeden z mych nejvetsich projektu, vytvoreno po tetrise, trvalo mi to
 * zhruba 3 mesice obrazky od Adama Tomsu, nejvetsi problem: pohyb nekterych
 * figurek, otaceni plochy po zahrani zabraneni sachu (figurka kdyz je sach muze
 * jit jen tak, aby sachu zabranila) a poznani patu od matu nebo sachu :D, 
 * pozdeji na zacatku roku 2019 jsem pridal ukladani do souboru appdata za velke pomoci
 * MWGS knihovny od Matyase Wotavi.
 */
public class ChessFinal extends Application {

    public static Group root = new Group();
    Scene scene = new Scene(root, Settings.SIZE * 8, Settings.SIZE * 8);
    Field[][] fields = Controller.getBoard();

    @Override
    public void start(Stage primaryStage) {
        Controller.inicializator();
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    primaryStage.close();
                }
            }
        });

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metoda provede se vzdy po zmacknuti tlacitka X. Nehcame at ulozi stav hry
     * do souboru
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        if (!Controller.hraJeUKonce) {
            Controller.ulozitStavhry();
        }
    }

}
