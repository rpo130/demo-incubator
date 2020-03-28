package pr.rpo.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import pr.rpo.helper.MD5Helper;

import java.io.File;
import java.io.IOException;

public class Md5View extends View{

    @Override
    public void init() {

        try {

            VBox root = new VBox();

            Label fileLabel = new Label("点击选择文件");
            fileLabel.setPrefSize(30, 30);
//            fileLabel.setStyle("-fx-background-color:gray");
            fileLabel.setOnMouseClicked(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("文件选择");
                File file = fileChooser.showOpenDialog(null);
                if(file != null) {
                    fileLabel.setText(file.toString());
                }
            });



            Label md5Text = new Label();
            md5Text.prefWidth(400);
//            md5Text.setStyle("-fx-background-color:gray");

            Button button = new Button();
            button.setText("确认");
            button.setOnAction(e -> {

                try {
                    md5Text.setText(MD5Helper.getMD5CheckSum(new File(fileLabel.getText())));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            root.getChildren().add(fileLabel);
            
            root.getChildren().add(button);
            root.getChildren().add(md5Text);
//            root.setStyle("-fx-background-color:red");

            super.pane = root;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
