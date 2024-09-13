package me.jamieburns.actions;

public record KeepAction<T>( T data ) implements Action<T> {}
