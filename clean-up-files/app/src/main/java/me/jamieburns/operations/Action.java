package me.jamieburns.operations;

public sealed interface Action<T>
    permits KeepAction, KeepWithRenameAction, RemoveAction {

    T data();
}
