package me.jamieburns.operations;

public record RemoveAction<T>( T data ) implements Action<T> {}
