package me.jamieburns.operations;

public record KeepAction<T>( T data ) implements Action<T> {}
