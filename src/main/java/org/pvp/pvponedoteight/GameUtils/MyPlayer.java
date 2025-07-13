package org.pvp.pvponedoteight.GameUtils;

public class MyPlayer {

    private boolean isInArena=false;
    private boolean isNewPVPModeArena = true;
    private boolean isPlayingPvp=true;
    private int kills;
    private int deaths;
    private ArenaConfig myArena;

    public MyPlayer() {}

    public boolean UpdateArena (ArenaConfig Arena) {

        if (Arena == null) {
            isInArena=false;
            return false;
        } else if (!isInArena || myArena==null || !myArena.equals(Arena)) {
            isInArena=true;
            myArena=Arena;
            isNewPVPModeArena =Arena.isNewPvpMode();
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
        return isNewPVPModeArena;
    }

    public void setKills() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public void setDeaths() {
        deaths++;
    }

    public int getDeaths() {
        return deaths;
    }
}
