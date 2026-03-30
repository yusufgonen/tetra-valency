package com.td.game.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.td.game.TowerDefenseGame;
import com.td.game.map.GameMap;
import com.td.game.screens.EndgameScreen;

public class KeyBindings {

    public static boolean handleShortcutKeys(int keycode, TowerDefenseGame game, GameMap.MapType mapType,
            Screen currentScreen) {
        if (keycode == Input.Keys.GRAVE) {
            if (currentScreen instanceof com.td.game.screens.GameScreen) {
                ((com.td.game.screens.GameScreen) currentScreen).toggleConsole();
            }
            return true;
        } else if (keycode == Input.Keys.NUM_1) {
            transitionToEndgame(game, mapType, currentScreen, EndgameScreen.EndState.WIN);
            return true;
        }
         else if (keycode == Input.Keys.NUM_2) {
            transitionToEndgame(game, mapType, currentScreen, EndgameScreen.EndState.LOSE);
            return true;
        } else if (keycode == Input.Keys.NUM_3) {
            transitionToEndgame(game, mapType, currentScreen, EndgameScreen.EndState.ENDLESS_FINISH);
            return true;
        } else if (keycode == Input.Keys.NUM_4) {
            if (currentScreen instanceof com.td.game.screens.GameScreen) {
                ((com.td.game.screens.GameScreen) currentScreen).skipToNextWave();
            }
            return true;
        }
        return false;
    }

    private static void transitionToEndgame(TowerDefenseGame game, GameMap.MapType mapType, Screen currentScreen,
            EndgameScreen.EndState state) {
        int randomWave = 0;
        if (state.equals(EndgameScreen.EndState.ENDLESS_FINISH)) {
            randomWave = MathUtils.random(51, 200);
        } else {
            randomWave = MathUtils.random(1, 49);
        }
        float randomTime = MathUtils.random(60f, 7200f);

        if (game != null && game.audio != null) {
            if (state == EndgameScreen.EndState.LOSE) {
                game.audio.playLose();
            } else if (state == EndgameScreen.EndState.WIN || state == EndgameScreen.EndState.ENDLESS_FINISH) {
                game.audio.playVictory();
            }
        }

        game.setScreen(new EndgameScreen(game, state, mapType, randomWave, randomTime));
        if (currentScreen != null) {
            currentScreen.dispose();
        }
    }
}
