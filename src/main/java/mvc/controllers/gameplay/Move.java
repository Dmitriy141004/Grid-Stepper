package mvc.controllers.gameplay;

import javafx.scene.input.KeyCode;

public enum Move {
    UP,
    LEFT,
    DOWN,
    RIGHT;

    public static Move getStepFromKeyCode(KeyCode keyCode) {
        Move value;

        switch (keyCode) {
            case W:
            case UP:
                value = Move.UP;
                break;

            case A:
            case LEFT:
                value = Move.LEFT;
                break;

            case S:
            case DOWN:
                value = Move.DOWN;
                break;

            case D:
            case RIGHT:
                value = Move.RIGHT;
                break;

            default:
                throw new IllegalArgumentException(String.format("Can\'t recognize key code \"%s\"", keyCode));
        }

        return value;
    }
}
