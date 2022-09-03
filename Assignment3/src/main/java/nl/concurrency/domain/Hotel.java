package nl.concurrency.domain;

import java.io.Serializable;
import java.util.HashMap;

public class Hotel implements Serializable {
    public final String name;

    /**
     * Key -> Integer: The room number
     * Value -> Boolean: Is the room reserved (TRUE = RESERVED)
     */
    public final HashMap<Integer, Boolean> rooms = new HashMap<>();

    public Hotel(String name, int roomCount) {
        this.name = name;
        for (int i = 1; i <= roomCount; i++){
            rooms.put(i,false);
        }
    }
}
