package com.proyecto.datalab.service;

import java.time.LocalDate;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.proyecto.datalab.entity.Participante;
import com.proyecto.datalab.enums.EstadoFicha;
import com.proyecto.datalab.repository.AuditoriaRepository;
import com.proyecto.datalab.repository.ParticipanteRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReminderService {

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // Configurable days, default to 3 if not present
    @Value("${datalab.reminder.days:3}")
    private int reminderDays;

    // Run every day at 09:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendRemindersForIncompleteCrfs() {
        log.info("Starting scheduled task: Check for incomplete CRFs > {} days", reminderDays);

        List<Participante> incomplete = participanteRepository.findByEstadoFicha(EstadoFicha.INCOMPLETA);
        LocalDate today = LocalDate.now();

        for (Participante p : incomplete) {
            if (p.getFechaInclusion() == null)
                continue;

            long daysBetween = ChronoUnit.DAYS.between(p.getFechaInclusion(), today);

            if (daysBetween > reminderDays) {
                // Check if we already sent a reminder EVER for this participant
                boolean alreadySent = auditoriaRepository.existsByParticipanteAndAccion(
                        p,
                        "ENVIO_RECORDATORIO");

                if (!alreadySent) {
                    sendReminder(p, daysBetween);
                }
            }
        }

        log.info("Finished scheduled task.");
    }

    private void sendReminder(Participante p, long daysOverdue) {
        if (p.getReclutador() == null || p.getReclutador().getCorreo() == null) {
            log.warn("Cannot send reminder for participant {}: Reclutador or email is missing", p.getIdParticipante());
            return;
        }

        String to = p.getReclutador().getCorreo();
        String subject = "Recordatorio: CRF Incompleto - " + p.getCodigoParticipante();

        String text = String.format(
                """
                        Estimado/a %s,

                        Le recordamos que la ficha del participante %s (Grupo: %s) lleva %d días incompleta desde su inclusión (%s).

                        Por favor, ingrese al sistema DataLAB para completar la información faltante.

                        Atentamente,
                        Equipo DataLAB
                        """,
                p.getReclutador().getNombreCompleto(),
                p.getCodigoParticipante(),
                p.getGrupo(),
                daysOverdue,
                p.getFechaInclusion());

        try {
            emailService.sendEmail(to, subject, text);
            log.info("Reminder sent to {} for participant {}", to, p.getCodigoParticipante());

            // Log the action to avoid resending immediately
            auditoriaService.registrarAccion(
                    p.getReclutador(),
                    p,
                    "ENVIO_RECORDATORIO",
                    "Participante",
                    "Notificación por email: Incompleta por " + daysOverdue + " días");
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
