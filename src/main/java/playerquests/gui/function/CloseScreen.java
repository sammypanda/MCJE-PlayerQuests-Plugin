package playerquests.gui.function;

public class CloseScreen extends GUIFunction {

    @Override
    public void execute() {
        this.parentGui.close();
    }
}