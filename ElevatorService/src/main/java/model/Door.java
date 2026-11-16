package model;

import enums.DoorState;

public class Door {
    DoorState doorState;

    public Door() {
        this.doorState = DoorState.CLOSED;
    }

    public Door(DoorState doorState) {
        this.doorState = doorState;
    }

    public DoorState getDoorState() {
        return doorState;
    }

    public void setDoorState(DoorState doorState) {
        this.doorState = doorState;
    }
}
