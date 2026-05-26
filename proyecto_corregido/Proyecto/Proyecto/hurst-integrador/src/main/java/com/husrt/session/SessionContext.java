package com.husrt.session;

import com.husrt.model.Rol;
import com.husrt.model.UsuarioSistema;

public final class SessionContext {

    private static UsuarioSistema current;

    private SessionContext() {
    }

    public static void setCurrent(UsuarioSistema user) {
        current = user;
    }

    public static UsuarioSistema getCurrent() {
        return current;
    }

    public static void clear() {
        current = null;
    }

    public static boolean hasRole(Rol... roles) {
        if (current == null) {
            return false;
        }
        for (Rol r : roles) {
            if (current.rol() == r) {
                return true;
            }
        }
        return false;
    }
}
