package com.td.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.td.game.TowerDefenseGame;
import com.td.game.map.GameMap;
import com.td.game.screens.GameScreen;
import com.td.game.systems.OptionsManager;

public class KeyBindings {
    public enum Action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        CONSOLE_TOGGLE,
        BUY_FIRE,
        BUY_WATER,
        BUY_EARTH,
        BUY_AIR,
        SLOT_1,
        SLOT_2,
        SLOT_3,
        SLOT_4,
        SLOT_5,
        SLOT_6,
        MOVE_TO_MERGE,
        MOVE_TO_CHARM,
        START_WAVE
    }

    public static int getDefaultKey(Action action) {
        switch (action) {
            case MOVE_UP: return Input.Keys.W;
            case MOVE_DOWN: return Input.Keys.S;
            case MOVE_LEFT: return Input.Keys.A;
            case MOVE_RIGHT: return Input.Keys.D;
            case CONSOLE_TOGGLE: return Input.Keys.GRAVE;
            case BUY_FIRE: return Input.Keys.NUM_6;
            case BUY_WATER: return Input.Keys.NUM_7;
            case BUY_EARTH: return Input.Keys.NUM_8;
            case BUY_AIR: return Input.Keys.NUM_9;
            case SLOT_1: return Input.Keys.NUMPAD_1;
            case SLOT_2: return Input.Keys.NUMPAD_2;
            case SLOT_3: return Input.Keys.NUMPAD_3;
            case SLOT_4: return Input.Keys.NUMPAD_4;
            case SLOT_5: return Input.Keys.NUMPAD_5;
            case SLOT_6: return Input.Keys.NUMPAD_6;
            case MOVE_TO_MERGE: return Input.Keys.M;
            case MOVE_TO_CHARM: return Input.Keys.C;
            case START_WAVE: return Input.Keys.SPACE;
            default: return Input.Keys.UNKNOWN;
        }
    }

    public static int getKey(Action action) {
        Integer key = OptionsManager.get().bindings.get(action.name());
        if (key == null) {
            return getDefaultKey(action);
        }
        return key;
    }

    public static void setKey(Action action, int keycode) {
        OptionsManager.get().bindings.put(action.name(), keycode);
        OptionsManager.save();
    }

    public static Action getActionForKey(int keycode) {
        for (Action action : Action.values()) {
            if (getKey(action) == keycode) {
                return action;
            }
        }
        return null;
    }

    public static boolean isPressed(Action action) {
        return Gdx.input.isKeyPressed(getKey(action));
    }

    public static boolean handleShortcutKeys(int keycode, TowerDefenseGame game, GameMap.MapType mapType,
            Screen currentScreen) {
        if (keycode == getKey(Action.CONSOLE_TOGGLE)) {
            if (currentScreen instanceof GameScreen) {
                ((GameScreen) currentScreen).toggleConsole();
            }
            return true;
        }
        if (currentScreen instanceof GameScreen) {
            GameScreen gs = (GameScreen) currentScreen;
            if (keycode == getKey(Action.BUY_FIRE)) {
                gs.buyOrbByIndex(0);
                return true;
            }
            if (keycode == getKey(Action.BUY_WATER)) {
                gs.buyOrbByIndex(1);
                return true;
            }
            if (keycode == getKey(Action.BUY_EARTH)) {
                gs.buyOrbByIndex(2);
                return true;
            }
            if (keycode == getKey(Action.BUY_AIR)) {
                gs.buyOrbByIndex(3);
                return true;
            }
            if (keycode == getKey(Action.SLOT_1)) {
                gs.selectInventorySlot(0);
                return true;
            }
            if (keycode == getKey(Action.SLOT_2)) {
                gs.selectInventorySlot(1);
                return true;
            }
            if (keycode == getKey(Action.SLOT_3)) {
                gs.selectInventorySlot(2);
                return true;
            }
            if (keycode == getKey(Action.SLOT_4)) {
                gs.selectInventorySlot(3);
                return true;
            }
            if (keycode == getKey(Action.SLOT_5)) {
                gs.selectInventorySlot(4);
                return true;
            }
            if (keycode == getKey(Action.SLOT_6)) {
                gs.selectInventorySlot(5);
                return true;
            }
            if (keycode == getKey(Action.MOVE_TO_MERGE)) {
                gs.moveSelectedToMerge();
                return true;
            }
            if (keycode == getKey(Action.MOVE_TO_CHARM)) {
                gs.moveSelectedToCharm();
                return true;
            }
            if (keycode == getKey(Action.START_WAVE)) {
                gs.startNextWaveHotkey();
                return true;
            }
        }
        return false;
    }

}
