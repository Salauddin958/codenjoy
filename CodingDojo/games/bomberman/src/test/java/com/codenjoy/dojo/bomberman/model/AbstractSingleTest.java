package com.codenjoy.dojo.bomberman.model;

import com.codenjoy.dojo.bomberman.services.Events;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.EventListener;
import com.codenjoy.dojo.services.Game;
import com.codenjoy.dojo.services.multiplayer.Single;
import com.codenjoy.dojo.services.printer.PrinterFactory;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.services.round.RoundSettingsWrapper;
import com.codenjoy.dojo.services.settings.Parameter;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.stubbing.OngoingStubbing;

import java.util.LinkedList;
import java.util.List;

import static com.codenjoy.dojo.services.settings.SimpleParameter.v;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public abstract class AbstractSingleTest {

    public static final int SIZE = 5;
    protected Walls walls = emptyWalls();
    private List<Hero> heroes = new LinkedList<>();
    private List<Game> games = new LinkedList<>();
    private List<EventListener> listeners = new LinkedList<>();
    protected GameSettings settings;
    protected Level level;
    private Bomberman board;
    protected int bombsCount = 1;
    protected Parameter<Integer> playersPerRoom = v(Integer.MAX_VALUE);
    protected Dice meatDice = mock(Dice.class);
    protected Dice heroDice = mock(Dice.class);
    private PrinterFactory printerFactory = new PrinterFactoryImpl();

    public void givenBoard(int count) {
        settings = mock(GameSettings.class);

        level = mock(Level.class);
        when(level.bombsCount()).thenReturn(bombsCount);
        when(level.bombsPower()).thenReturn(1);

        for (int i = 0; i < count; i++) {
            dice(heroDice,  0, 0);
            heroes.add(new Hero(level, heroDice));
        }

        OngoingStubbing<Hero> when = when(settings.getBomberman(any(Level.class)));
        for (Hero h : heroes) {
            when = when.thenReturn(h);
        }

        when(settings.getLevel()).thenReturn(level);
        when(settings.getBoardSize()).thenReturn(v(SIZE));
        when(settings.getWalls(any(Bomberman.class))).thenReturn(walls);
        when(settings.getRoundSettings()).thenReturn(getRoundSettings());
        when(settings.getPlayersPerRoom()).thenReturn(playersPerRoom);
        when(settings.killOtherBombermanScore()).thenReturn(v(200));
        when(settings.killMeatChopperScore()).thenReturn(v(100));
        when(settings.killWallScore()).thenReturn(v(10));

        board = new Bomberman(settings);

        for (int i = 0; i < count; i++) {
            listeners.add(mock(EventListener.class));
            games.add(new Single(new Player(listener(i), getRoundSettings().roundsEnabled()), printerFactory));
        }

        games.forEach(g -> {
            g.on(board);
            g.newGame();
        });
    }

    protected void meatChopperAt(int x, int y) {
        dice(meatDice, x, y);
        Field temp = mock(Field.class);
        when(temp.size()).thenReturn(SIZE);
        MeatChoppers meatchoppers = new MeatChoppers(new WallsImpl(), temp, v(1), meatDice);
        meatchoppers.regenerate();
        walls = meatchoppers;
    }

    protected void asrtBrd(String board, Game game) {
        assertEquals(board, game.getBoardAsString());
    }

    protected void verifyEvents(EventListener events, String expected) {
        if (expected.equals("[]")) {
            try {
                verify(events, never()).event(any(Events.class));
            } catch (NeverWantedButInvoked e) {
                assertEquals(expected, getEvents(events));
            }
        } else {
            assertEquals(expected, getEvents(events));
        }
        reset(events);
    }

    protected String getEvents(EventListener events) {
        ArgumentCaptor<Events> captor = ArgumentCaptor.forClass(Events.class);
        verify(events, atLeast(1)).event(captor.capture());
        return captor.getAllValues().toString();
    }

    protected Hero hero(int index) {
        return heroes.get(index);
    }

    protected Game game(int index) {
        return games.get(index);
    }

    protected EventListener listener(int index) {
        return listeners.get(index);
    }

    protected void tick() {
        board.tick();
    }

    protected abstract RoundSettingsWrapper getRoundSettings();

    protected void dice(Dice dice, int... values) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int value : values) {
            when = when.thenReturn(value);
        }
    }

    private Walls emptyWalls() {
        Walls walls = mock(WallsImpl.class);
        when(walls.iterator()).thenReturn(new LinkedList<Wall>().iterator());
        return walls;
    }

}