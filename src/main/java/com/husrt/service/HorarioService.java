package com.husrt.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.husrt.model.Docente;
import com.husrt.model.Estudiante;
import com.husrt.repository.DocenteRepository;
import com.husrt.repository.EstudianteRepository;
import com.husrt.session.SessionContext;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class HorarioService {

    private final DocenteRepository docenteRepo = new DocenteRepository();
    private final EstudianteRepository estudianteRepo = new EstudianteRepository();

    public void generarHorarioDocente(Path out) throws IOException, SQLException {
        try (OutputStream os = Files.newOutputStream(out)) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, os);
            doc.open();

            var usuario = SessionContext.getCurrent();
            if (usuario == null) {
                doc.add(new Paragraph("Error: No hay sesión activa"));
                doc.close();
                return;
            }

            if (usuario.idDocente() == null) {
                doc.add(new Paragraph("Error: La cuenta no está vinculada a un docente"));
                doc.close();
                return;
            }

            Docente docente = docenteRepo.findById(usuario.idDocente()).orElse(null);
            if (docente == null) {
                doc.add(new Paragraph("Error: No se encontró información del docente"));
                doc.close();
                return;
            }

            doc.add(new Paragraph("Horario de clases — Docente",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Docente: " + docente.nombre() + " " + docente.apellido(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Programa: " + docente.programaQueSupervisa(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Día");
            table.addCell("Horario");
            table.addCell("Servicio");
            table.addCell("Actividad");

            // Sample data - in production this would be loaded from the database
            table.addCell("Lunes");
            table.addCell("08:00 - 12:00");
            table.addCell("Medicina Interna");
            table.addCell("Supervisión");

            table.addCell("Martes");
            table.addCell("08:00 - 12:00");
            table.addCell("Cirugía General");
            table.addCell("Supervisión");

            table.addCell("Miércoles");
            table.addCell("08:00 - 12:00");
            table.addCell("Pediatría");
            table.addCell("Supervisión");

            table.addCell("Jueves");
            table.addCell("08:00 - 12:00");
            table.addCell("Medicina Interna");
            table.addCell("Supervisión");

            table.addCell("Viernes");
            table.addCell("08:00 - 12:00");
            table.addCell("Cirugía General");
            table.addCell("Supervisión");

            doc.add(table);
            doc.close();
        }
    }

    public void generarHorarioEstudiante(long idEstudiante, Path out) throws IOException, SQLException {
        try (OutputStream os = Files.newOutputStream(out)) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, os);
            doc.open();

            Estudiante estudiante = estudianteRepo.findById(idEstudiante).orElse(null);
            if (estudiante == null) {
                doc.add(new Paragraph("Error: No se encontró información del estudiante"));
                doc.close();
                return;
            }

            doc.add(new Paragraph("Horario de prácticas — Estudiante",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Estudiante: " + estudiante.nombre() + " " + estudiante.apellido(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Cédula: " + estudiante.cedula(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Programa: " + estudiante.programaAcademico(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Semestre: " + estudiante.semestreAcademico(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Día");
            table.addCell("Horario");
            table.addCell("Servicio");
            table.addCell("Actividad");

            // Sample data - in production this would be loaded from the database
            table.addCell("Lunes");
            table.addCell("08:00 - 12:00");
            table.addCell("Medicina Interna");
            table.addCell("Práctica clínica");

            table.addCell("Martes");
            table.addCell("08:00 - 12:00");
            table.addCell("Cirugía General");
            table.addCell("Práctica clínica");

            table.addCell("Miércoles");
            table.addCell("08:00 - 12:00");
            table.addCell("Pediatría");
            table.addCell("Práctica clínica");

            table.addCell("Jueves");
            table.addCell("08:00 - 12:00");
            table.addCell("Medicina Interna");
            table.addCell("Práctica clínica");

            table.addCell("Viernes");
            table.addCell("08:00 - 12:00");
            table.addCell("Cirugía General");
            table.addCell("Práctica clínica");

            doc.add(table);
            doc.close();
        }
    }
}
