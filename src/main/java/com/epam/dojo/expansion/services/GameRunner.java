package com.epam.dojo.expansion.services;

/*-
 * #%L
 * iCanCode - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 EPAM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.services.settings.Parameter;
import com.epam.dojo.expansion.model.*;
import com.epam.dojo.expansion.model.levels.Levels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codenjoy.dojo.services.settings.SimpleParameter.v;

/**
 * Генератор игор - реализация {@see GameType}
 * Обрати внимание на {@see GameRunner#SINGLE} - там реализовано переключение в режимы "все на одном поле"/"каждый на своем поле"
 */
public class GameRunner extends AbstractGameType implements GameType  {

    private static Logger logger = DLoggerFactory.getLogger(GameRunner.class);

    private MultipleGameFactory gameFactory;

    public GameRunner() {
        new Scores(0, settings);
        gameFactory = new MultipleGameFactory(
                Levels.collectSingle(),
                Levels.collectMultiple()
        );
        gameFactory.setWaitingOthers(false);
    }

    @Override
    public PlayerScores getPlayerScores(int score) {
        return new Scores(score, settings);
    }

    @Override
    public Game newGame(EventListener listener, PrinterFactory factory, String save) {
        boolean isTrainingMode = false; // TODO load from game_settings via GameDataController
        if (!isTrainingMode) {
            int total = Levels.collectSingle().get().size();
            save = "{'total':" + total + ",'current':0,'lastPassed':" + (total - 1) + ",'multiple':true}";
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Starting new game with save {}", save);
        }
        Game single = new Single(gameFactory, listener, factory, save);
        single.newGame();
        return single;
    }

    @Override
    public Parameter<Integer> getBoardSize() {
        return v(Levels.size());
    }

    @Override
    public String name() {
        return "expansion";
    }

    @Override
    public Enum[] getPlots() {
        return Elements.values();
    }

}
