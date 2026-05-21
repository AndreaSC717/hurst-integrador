package com.husrt.service;

import com.husrt.model.UsuarioSistema;

public sealed interface LoginOutcome permits LoginOutcome.Success, LoginOutcome.Failure {

    record Success(UsuarioSistema usuario) implements LoginOutcome {
    }

    record Failure(String mensaje) implements LoginOutcome {
    }
}
