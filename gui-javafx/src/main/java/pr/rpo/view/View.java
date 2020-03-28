package pr.rpo.view;

import javafx.scene.layout.Pane;

import java.lang.reflect.InvocationTargetException;

public abstract class View {

    protected static View view;
    
    public Pane pane;
    
    public static View getInstance(Class<? extends View> clazz) {
        if(view == null) {
            try {
                view = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            view.init();
            return view;
        }else {
            return view;
        }
    }

    abstract void init();

}
