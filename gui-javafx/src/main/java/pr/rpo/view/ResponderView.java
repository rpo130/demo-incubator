package pr.rpo.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pr.rpo.entity.ProjectDb;
import pr.rpo.entity.TitleAnswerInfo;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ResponderView extends View {
//    private static ResponderView view;

    ProjectDb data1 = new ProjectDb();
    LocalDateTime titleBeginTime;
    LocalDateTime titleEndTime;

    @Override
    public void init() {

        try {
            Button changeModeButton = new Button(data1.getModeName());
            changeModeButton.setOnAction(e -> {
                data1.triggerMode();
                changeModeButton.setText(data1.getModeName());
            });

            Button changeAnswerTypeButton = new Button(data1.getAnswerTypeName());
            changeAnswerTypeButton.setOnAction(e -> {
                data1.changeAnswerType();
                changeAnswerTypeButton.setText(data1.getAnswerTypeName());
            });

            Label projectNameText = new Label("请输入题目");
            TextField projectNameField = new TextField("");

            Label indexText = new Label( "请输入题号");
            TextField inputField = new TextField(String.valueOf(data1.getTitleIndex()));

            Button preTitleButton = new Button("上一题");
            Button nextTitleButton = new Button("下一题");
            preTitleButton.setOnAction(e -> {
                data1.pre();
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });
            nextTitleButton.setOnAction(e -> {
                data1.next();
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });

            Button buttonA = new Button("A");
            buttonA.setPrefSize(80,80);
            Button buttonB = new Button("B");
            buttonB.setPrefSize(80,80);
            Button buttonC = new Button("C");
            buttonC.setPrefSize(80,80);
            Button buttonD = new Button("D");
            buttonD.setPrefSize(80,80);


            TextArea chooseLog = new TextArea();

            inputField.textProperty().addListener(e -> {
                String t = inputField.getText();
                if(t.isBlank()) {
                }else {
//                    no = Integer.parseInt(inputField.getText());
                }
            });

            buttonA.setOnMouseClicked(e -> {
                optionButtonClickHandler(inputField, chooseLog, e);
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });

            buttonB.setOnMouseClicked(e -> {
                optionButtonClickHandler(inputField, chooseLog, e);
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });

            buttonC.setOnMouseClicked(e -> {
                optionButtonClickHandler(inputField, chooseLog, e);
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });

            buttonD.setOnMouseClicked(e -> {
                optionButtonClickHandler(inputField, chooseLog, e);
                inputField.setText(String.valueOf(data1.getTitleIndex()));
            });

            Button buttonStart = new Button();
            buttonStart.setText("开始");
            buttonStart.setOnAction(e -> {
                titleBeginTime = LocalDateTime.now();
                buttonStart.setVisible(false);
                chooseLog.setText("开始:" + titleBeginTime);
            });

            Button buttonFinish = new Button();
            buttonFinish.setText("确认");
            buttonFinish.setOnAction(e -> {
                if(!isStart()) {
                    chooseLog.appendText("未开始");
                    return;
                }

                saveFile(projectNameField.getText());
                chooseLog.clear();
                chooseLog.appendText("结束:" + LocalDateTime.now() + " 输出至 " + projectNameField.getText() + ".csv");
            });

            Button buttonClear = new Button("clear");
            buttonClear.setOnAction(e -> {
                data1.clear();
                if(data1.getModeName().equals("校正")) {
                    data1.triggerMode();
                }
                inputField.setText(String.valueOf(data1.getTitleIndex()));
                chooseLog.clear();
                buttonStart.setVisible(true);
                titleBeginTime = null;
                titleEndTime = null;
            });

            VBox root = new VBox();
            root.getChildren().add(changeModeButton);
            root.getChildren().add(changeAnswerTypeButton);
            root.getChildren().add(projectNameText);
            root.getChildren().add(projectNameField);
            root.getChildren().add(indexText);
            root.getChildren().add(inputField);

            HBox hBox0 = new HBox();
            hBox0.getChildren().addAll(preTitleButton,nextTitleButton);
            root.getChildren().add(hBox0);

            HBox hBox = new HBox();

            hBox.getChildren().add(buttonA);
            hBox.getChildren().add(buttonB);
            hBox.getChildren().add(buttonC);
            hBox.getChildren().add(buttonD);
            root.getChildren().add(hBox);

            HBox hBox2 = new HBox();
            hBox2.getChildren().add(buttonStart);
            hBox2.getChildren().add(buttonFinish);
            hBox2.getChildren().add(buttonClear);
            root.getChildren().add(hBox2);
            root.getChildren().add(chooseLog);

            // view.getChildren().add(root);
            pane  = root;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private void saveFile() {
        try {
            FileWriter fw = new FileWriter("output.csv", false);
            fw.write(options2CSV(data1.getData()));
            fw.flush();
            fw.close();
        }catch (Exception err) {
            err.printStackTrace();
        }
    }
    private void saveFile(String fileName) {
        if(fileName==null || fileName.isBlank()) {
            fileName = "output-unname";
        }
        try {
            FileWriter fw = new FileWriter(fileName+".csv", false);
            //TODO
            fw.write(map2CSVWithCheck(list2map(data1.getData())));
            fw.flush();
            fw.close();
        }catch (Exception err) {
            err.printStackTrace();
        }
    }

    private boolean isStart() {
        if(titleBeginTime != null) {
            return true;
        }else {
            return false;
        }
    }

    private void optionButtonClickHandler(TextField inputField, TextArea chooseLog, MouseEvent e) {
        if(!isStart()) {
            return;
        }

        titleEndTime = LocalDateTime.now();
        Button b = (Button) e.getSource();
        if(e.getButton() == MouseButton.PRIMARY) {
            data1.add(data1.getTitleIndex(), b.getText());
            data1.next();
        }else if(e.getButton() == MouseButton.SECONDARY) {
            data1.add(data1.getTitleIndex(), data1.getCurrentAnswer()+b.getText());
        }
        chooseLog.setText(options2CSV(data1.getData()));
        titleBeginTime = LocalDateTime.now();
        saveFile();
        inputField.setText(String.valueOf(data1.getTitleIndex()));
    }

    private String map2CSV(Map<Integer, TitleAnswerInfo> m) {
        Set<Integer> l = new TreeSet<>();
        l = m.keySet();
        StringBuilder sb = new StringBuilder();
        for(Integer i : l) {
            sb.append(i);
            sb.append(",");
            sb.append(m.get(i).getChoiceAnswerInString());
            sb.append(",");
            sb.append(m.get(i).getRightChoiceAnswerInString());
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    private String map2CSVWithCheck(Map<Integer, TitleAnswerInfo> m) {
        Set<Integer> l = new TreeSet<>();
        l = m.keySet();
        StringBuilder sb = new StringBuilder();
        for(Integer i : l) {
            sb.append(i);
            sb.append(",");
            sb.append(m.get(i).getAnswer());
            if(m.get(i).getRightAnswer() != null && !m.get(i).getRightAnswer().equals(m.get(i).getAnswer())) {
                sb.append(",");
                sb.append(m.get(i).getRightAnswer());
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    private String options2CSV(List<TitleAnswerInfo> l) {
        return map2CSV(list2map(l));
    }

    private Map<Integer, TitleAnswerInfo> list2map(List<TitleAnswerInfo> l) {

        Map<Integer, TitleAnswerInfo> map = l.stream().collect(Collectors.toMap(e-> e.getTitleIndex(), e -> e, (o1, o2) -> {
            if(o1.getStartTime().compareTo(o2.getStartTime()) > 0) {
                return o1;
            }else {
                return o2;
            }
        }));
        return map;
    }

}
