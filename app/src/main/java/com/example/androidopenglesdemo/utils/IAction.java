package com.example.androidopenglesdemo.utils;

public interface IAction<P> {

    void doAction(P p);

    void onDenied(P p);
}
