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


import com.codenjoy.dojo.services.LengthToXY;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.utils.TestUtils;
import com.epam.dojo.expansion.model.Expansion;
import com.epam.dojo.expansion.model.Player;
import com.epam.dojo.expansion.model.interfaces.ICell;
import com.epam.dojo.expansion.model.interfaces.IItem;
import com.epam.dojo.expansion.model.items.HeroForces;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class Printer {

    private static final int BOUND_DEFAULT = 4;
    public static final int LAYERS_TOTAL = 2;
    public static final int COUNT_NUMBERS = TestUtils.COUNT_NUMBERS;

    private int size;
    private Expansion game;

    private int viewSize;
    private int vx;
    private int vy;
    private int bound;

    private boolean needToCenter;

    public Printer(Expansion game, int viewSize) {
        this.game = game;
        this.viewSize = Math.min(game.size(), viewSize);

        if (this.viewSize == viewSize) {
            bound = BOUND_DEFAULT;
        }

        needToCenter = bound != 0;
    }

    public PrinterData getBoardAsString(Player player) {
        int layers = LAYERS_TOTAL;
        size = game.size();

        centerPositionOnStart(player);

        StringBuilder[] builders = prepareLayers(layers + 1);
        fillLayers(player, builders);
        PrinterData result = getPrinterData(layers, builders);

        return result;
    }

    private void fillLayers(Player player, StringBuilder[] builders) {
        LengthToXY xy = new LengthToXY(size);
        ICell[] cells = game.getCurrentLevel().getCells();
        for (int y = vy + viewSize - 1; y >= vy; --y) {
            for (int x = vx; x < vx + viewSize; ++x) {
                int index = xy.getLength(x, y);

                IItem item1 = cells[index].getItem(0);
                builders[0].append(makeState(item1, player));

                IItem item2 = cells[index].getItem(1);
                builders[1].append(makeState(item2, player));
                builders[2].append(makeForceState(item2, player));
            }
        }
    }

    @NotNull
    private PrinterData getPrinterData(int layers, StringBuilder[] builders) {
        PrinterData result = new PrinterData();
        result.setOffset(new PointImpl(vx, vy));
        for (int i = 0; i < layers; ++i) {
            result.addLayer(builders[i].toString());
        }
        result.setForces(builders[layers].toString());
        return result;
    }

    @NotNull
    private StringBuilder[] prepareLayers(int layers) {
        StringBuilder[] builders = new StringBuilder[layers];
        for (int i = 0; i < layers; ++i) {
            builders[i] = new StringBuilder(viewSize * viewSize + viewSize);
        }
        return builders;
    }

    // If it is the first start that we will must to center position
    private void centerPositionOnStart(Player player) {
        Point pivot = player.getHero().getPosition();
        if (needToCenter) {
            needToCenter = false;
            moveToCenter(pivot);
        } else if (pivot != null) {
            moveTo(pivot);
        }
        adjustView(size);
    }

    private String makeState(IItem item, Player player) {
        if (item != null) {
            return String.valueOf(item.state(player, item.getItemsInSameCell().toArray()).ch());
        } else {
            return "-";
        }
    }

    public static String makeForceState(IItem item, Player player) {
        if (item instanceof HeroForces) {
            HeroForces forces = (HeroForces) item;
            int count = forces.getForces().getCount();
            String result = Integer.toString(count, Character.MAX_RADIX).toUpperCase();
            if (result.length() < COUNT_NUMBERS) { // TODO оптимизировать
                return StringUtils.leftPad(result, COUNT_NUMBERS, '0');
            } else if (result.length() > COUNT_NUMBERS) {
                return result.substring(result.length() - COUNT_NUMBERS, result.length());
            }
            return result;
        } else {
            return "-=#";
        }
    }

    private void moveTo(Point point) {
        int left = point.getX() - (vx + bound);
        left = fixToNegative(left);

        int right = point.getX() - (vx + viewSize - bound - 1);
        right = fixToPositive(right);

        int bottom = point.getY() - (vy + bound);
        bottom = fixToNegative(bottom);

        int up = point.getY() - (vy + viewSize - bound - 1);
        up = fixToPositive(up);

        vx += left + right;
        vy += up + bottom;
    }

    private int fixToPositive(int value) {
        if (value < 0) {
            return 0;
        }

        return value;
    }

    private int fixToNegative(int value) {
        if (value > 0) {
            return 0;
        }

        return value;
    }

    private void moveToCenter(Point point) {
        vx = (int) (point.getX() - Math.round((double) viewSize / 2));
        vy = (int) (point.getY() - Math.round((double) viewSize / 2));
    }

    private void adjustView(int size) {
        vx = fixToPositive(vx);
        if (vx + viewSize > size) {
            vx = size - viewSize;
        }

        vy = fixToPositive(vy);
        if (vy + viewSize > size) {
            vy = size - viewSize;
        }
    }
}