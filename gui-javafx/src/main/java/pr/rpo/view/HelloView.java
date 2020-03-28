package pr.rpo.view;

import javafx.scene.control.Label;

public class HelloView extends View {

    protected static HelloView view;

    public static HelloView getInstance() {
        if(view == null) {
            view = new HelloView();
            view.init();
            return view;
        }else {
            return view;
        }
    }

    @Override
    public void init() {
    }
}
