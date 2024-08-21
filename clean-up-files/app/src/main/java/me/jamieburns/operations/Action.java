package me.jamieburns.operations;

public sealed interface Action<T> 
    permits KeepAction, RemoveAction {

    T data();
}
