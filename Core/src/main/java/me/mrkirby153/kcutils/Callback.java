package me.mrkirby153.kcutils;

/**
 * Generic class for handling callbacks of Async tasks
 */
public abstract class Callback<T> {

    public abstract void call(T data);
}
