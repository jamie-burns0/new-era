package me.jamieburns.actions;

public record RemoveAction<T>( T data ) implements Action<T> {}
