package me.jamieburns.actions;

public sealed interface Action<T>
    permits KeepAction, KeepWithRenameAction, RemoveAction {

    T data();
}
