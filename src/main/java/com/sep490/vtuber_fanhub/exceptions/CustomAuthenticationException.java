package com.sep490.vtuber_fanhub.exceptions;

public class CustomAuthenticationException extends RuntimeException{
    public CustomAuthenticationException(String message){super(message);}

    public CustomAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomAuthenticationException(Throwable cause) {
        super(cause);
    }
}
