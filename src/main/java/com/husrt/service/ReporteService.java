package com.husrt.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.husrt.model.Estudiante;
import com.husrt.repository.EstudianteRepository;
import com.husrt.repository.ProgramaRequisitoRepository;
import com.husrt.repository.RegistroAccesoRepository;
import com.husrt.util.PeriodoAcademico;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ReporteService {

    private final RegistroAccesoRepository registros = new RegistroAccesoRepository();
    private final EstudianteRepository estudiantes = new EstudianteRepository();
    private final ProgramaRequisitoRepository requisitos = new ProgramaRequisitoRepository();

    public List<RegistroAccesoRepository.HistorialRow> historialEstudiante(long idEstudiante, LocalDate desde, LocalDate hasta) throws SQLException {
        LocalDateTime d0 = desde.atStartOfDay();
        LocalDateTime d1 = hasta.plusDays(1).atStartOfDay();
        return registros.historialEstudianteRango(idEstudiante, d0, d1);
    }

    public record HorasVsRequeridas(String cedula, String nombre, String programa, double horasCumplidas, int horasRequeridas, double pct) {
    }

    public List<HorasVsRequeridas> horasVsRequeridasTodos(int anio, int periodo) throws SQLException {
        List<Estudiante> all = estudiantes.findAll();
        List<HorasVsRequeridas> out = new ArrayList<>();
        for (Estudiante e : all) {
            double sum = registros.sumHorasAprobadasSemestre(e.idEstudiante(), anio, periodo);
            int req = requisitos.findHorasRequeridas(e.programaAcademico()).orElse(160);
            double pct = req > 0 ? Math.min(100, sum * 100.0 / req) : 0;
            out.add(new HorasVsRequeridas(e.cedula(), e.nombre() + " " + e.apellido(), e.programaAcademico(), sum, req, pct));
        }
        return out;
    }

    public List<RegistroAccesoRepository.RechazoRow> intentosRechazados(LocalDate desde, LocalDate hasta) throws SQLException {
        return registros.listRechazados(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    public List<RegistroAccesoRepository.OcupacionRow> ocupacion(LocalDate desde, LocalDate hasta) throws SQLException {
        return registros.ocupacionHistorica(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    public List<EstudianteRepository.ArlAlertaRow> arlProximos15() throws SQLException {
        return estudiantes.listArlProximaVencer(15);
    }

    public void exportHistorialExcel(long idEstudiante, LocalDate desde, LocalDate hasta, Path out) throws SQLException, IOException {
        List<RegistroAccesoRepository.HistorialRow> rows = historialEstudiante(idEstudiante, desde, hasta);
        List<String> headers = List.of("ID", "Cédula", "Nombre", "Entrada", "Salida", "Horas", "Resultado", "Motivo");
        List<List<String>> data = new ArrayList<>();
        for (RegistroAccesoRepository.HistorialRow r : rows) {
            data.add(List.of(
                    String.valueOf(r.idRegistro()),
                    r.cedula(),
                    r.nombreCompleto(),
                    String.valueOf(r.entrada()),
                    String.valueOf(r.salida()),
                    r.horas() != null ? String.valueOf(r.horas()) : "",
                    r.resultado(),
                    r.motivo() != null ? r.motivo() : ""));
        }
        writeExcel(headers, data, out);
    }

    public void exportRechazosExcel(LocalDate desde, LocalDate hasta, Path out) throws SQLException, IOException {
        List<RegistroAccesoRepository.RechazoRow> rows = intentosRechazados(desde, hasta);
        List<String> headers = List.of("Fecha", "Cédula", "Nombre", "Motivo");
        List<List<String>> data = new ArrayList<>();
        for (RegistroAccesoRepository.RechazoRow r : rows) {
            data.add(List.of(String.valueOf(r.ts()), r.cedula(), r.nombre(), r.motivo() != null ? r.motivo() : ""));
        }
        writeExcel(headers, data, out);
    }

    public void exportOcupacionExcel(LocalDate desde, LocalDate hasta, Path out) throws SQLException, IOException {
        List<RegistroAccesoRepository.OcupacionRow> rows = ocupacion(desde, hasta);
        List<String> headers = List.of("Servicio", "Día", "Ingresos aprobados");
        List<List<String>> data = new ArrayList<>();
        for (RegistroAccesoRepository.OcupacionRow r : rows) {
            data.add(List.of(r.servicio(), r.dia() != null ? r.dia().toLocalDate().toString() : "", String.valueOf(r.ingresos())));
        }
        writeExcel(headers, data, out);
    }

    public void exportHorasPdf(int anio, int periodo, Path out) throws SQLException, IOException {
        List<HorasVsRequeridas> rows = horasVsRequeridasTodos(anio, periodo);
        try (OutputStream os = Files.newOutputStream(out)) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, os);
            doc.open();
            doc.add(new Paragraph("Horas cumplidas vs requeridas — " + anio + " periodo " + periodo,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(5);
            table.addCell("Cédula");
            table.addCell("Nombre");
            table.addCell("Programa");
            table.addCell("Horas");
            table.addCell("%");
            for (HorasVsRequeridas r : rows) {
                table.addCell(r.cedula());
                table.addCell(r.nombre());
                table.addCell(r.programa());
                table.addCell(String.valueOf(r.horasCumplidas()));
                table.addCell(String.format("%.1f", r.pct()));
            }
            doc.add(table);
            doc.close();
        }
    }

    public void exportArlPdf(Path out) throws SQLException, IOException {
        List<EstudianteRepository.ArlAlertaRow> rows = arlProximos15();
        try (OutputStream os = Files.newOutputStream(out)) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, os);
            doc.open();
            doc.add(new Paragraph("ARL próxima a vencer (15 días)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(4);
            table.addCell("Cédula");
            table.addCell("Nombre");
            table.addCell("Apellido");
            table.addCell("ARL fin");
            for (EstudianteRepository.ArlAlertaRow r : rows) {
                table.addCell(r.cedula());
                table.addCell(r.nombre());
                table.addCell(r.apellido());
                table.addCell(r.arlFin() != null ? r.arlFin().toString() : "");
            }
            doc.add(table);
            doc.close();
        }
    }

    private static void writeExcel(List<String> headers, List<List<String>> data, Path out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Reporte");
            Row h = sh.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                h.createCell(i).setCellValue(headers.get(i));
            }
            int r = 1;
            for (List<String> row : data) {
                Row xr = sh.createRow(r++);
                for (int c = 0; c < row.size(); c++) {
                    xr.createCell(c).setCellValue(row.get(c));
                }
            }
            Path parent = out.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
        }
    }

    public static LocalDate hoy() {
        return LocalDate.now();
    }

    public static int anioActual() {
        return PeriodoAcademico.anioDeFecha(LocalDate.now());
    }

    public static int periodoActual() {
        return PeriodoAcademico.periodoDeFecha(LocalDate.now());
    }
}
