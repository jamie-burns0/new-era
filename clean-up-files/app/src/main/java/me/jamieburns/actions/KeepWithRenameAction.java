package me.jamieburns.actions;

public record KeepWithRenameAction<T>( T data ) implements Action<T> {}