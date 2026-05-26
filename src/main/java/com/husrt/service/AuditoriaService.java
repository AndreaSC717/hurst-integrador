package com.husrt.service;

import com.husrt.model.UsuarioSistema;
import com.husrt.repository.AuditoriaRepository;
import com.husrt.session.SessionContext;

public class AuditoriaService {

    private final AuditoriaRepository repo = new AuditoriaRepository();

    public void registrar(String modulo, String accion, String detalle) {
        registrar(modulo, accion, detalle, null);
    }

    public void registrar(String modulo, String accion, String detalle, String entidadRef) {
        try {
            UsuarioSistema u = SessionContext.getCurrent();
            Long id = u != null ? u.idUsuario() : null;
            String nombre = u != null ? u.nombreUsuario() : null;
            String rol = u != null ? u.rol().name() : null;
            repo.insert(id, nombre, rol, modulo, accion, detalle, entidadRef);
        } catch (Exception ignored) {
            // La auditoría no debe interrumpir la operación principal.
        }
    }
}
