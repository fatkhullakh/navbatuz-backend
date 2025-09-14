package uz.navbatuz.backend.common;

public class AccountDeletedOrDisabledException extends RuntimeException {
    public AccountDeletedOrDisabledException(String msg) { super(msg); }
}
