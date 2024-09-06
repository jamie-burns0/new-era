package me.jamieburns.operations;

public record KeepWithRenameAction<T>( T data ) implements Action<T> {}