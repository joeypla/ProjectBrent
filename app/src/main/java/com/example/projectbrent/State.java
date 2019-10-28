package com.example.projectbrent;

public class State {
    String introMessage;
    String stateName;

    boolean isComplete = false;

    int reps = 0;
    int weight = 0;

    State nextState;

    public State(String name, String intro)
    {
        stateName = name;
        introMessage = intro;
    }

    public void complete(int reps, int weight)
    {
        isComplete = true;
        this.reps = reps;
        this.weight = weight;
    }
}
