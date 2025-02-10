package org.pvp.pvponedoteight.GameUtils;

public class MyPlayer {

    private boolean isInArena=false;
    private boolean isnewPVP = true;
    private boolean isPlayingPvp=true;
    private ArenaConfig myArena;

    public MyPlayer() {}

    public boolean UpdateArena (ArenaConfig Arena) {

        if (Arena == null) {
            isInArena=false;
            return false;
        } else if (!isInArena || myArena==null || !myArena.equals(Arena)) {
            isInArena=true;
            myArena=Arena;
            isnewPVP=Arena.isNewPvp();
            return true;
        }
        return false;
    }

    public void updatePlaying(boolean val) {
        isPlayingPvp=val;
    }

    public boolean isPlayingPVP() {
        return isPlayingPvp;
    }

    public boolean isInPVPArena() {
        return isInArena;
    }

    public boolean isNewPVPArena() {
        return isnewPVP;
    }
}
