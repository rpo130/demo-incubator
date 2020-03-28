package pr.rpo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pr.rpo.view.Md5View;
import pr.rpo.view.ResponderView;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Button buttonA = new Button("md5util");
        Button buttonB = new Button("responder");

        Pane root = new FlowPane();
//        root.setStyle("-fx-background-color:blue");
        root.getChildren().add(buttonA);
        root.getChildren().add(buttonB);

        buttonA.setOnMouseClicked(e -> {
            root.getChildren().clear();
            Pane p = Md5View.getInstance(Md5View.class).pane;
            p.setPrefSize(400, 400);
            root.getChildren().add(p);
        });
        buttonB.setOnMouseClicked(e -> {
            root.getChildren().clear();
            root.getChildren().add(ResponderView.getInstance(ResponderView.class).pane);
        });

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.setTitle("toolkit");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
