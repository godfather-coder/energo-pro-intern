package com.example.mssqll.utiles.exceptions;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(){
        super("მომხმარებელი უკვე არსებობს");
    }
}
