package com.example.restaurantwaiting;

public class ReservationManager {
    public enum State { IDLE, RESERVED, DINING }

    private static ReservationManager instance;
    private State currentState = State.IDLE;
    private String restaurantName = "";
    private int waitingTeams = 0;

    private ReservationManager() {}

    public static synchronized ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    public State getCurrentState() { return currentState; }
    public void setCurrentState(State state) { this.currentState = state; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String name) { this.restaurantName = name; }

    public int getWaitingTeams() { return waitingTeams; }
    public void setWaitingTeams(int teams) { this.waitingTeams = teams; }

    public void reset() {
        currentState = State.IDLE;
        restaurantName = "";
        waitingTeams = 0;
    }
}
