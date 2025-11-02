--
-- SECCIÓN 1: DDL - CREACIÓN DE TABLAS
--

CREATE DATABASE IF NOT EXISTS `DBB_DATALAB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `DBB_DATALAB`;

CREATE TABLE `Rol` (
  `id_rol` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `nombre_rol` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `Usuario` (
  `id_usuario` INT(11) NOT NULL AUTO_INCREMENT,
  `id_rol` INT(11) NOT NULL,
  `nombre_completo` varchar(255) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `contrasenia` varchar(255) NOT NULL COMMENT 'Almacenar como hash',
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `correo` (`correo`),
  KEY `id_rol` (`id_rol`),
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol`) REFERENCES `Rol` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `Participante` (
  `id_participante` INT(11) NOT NULL AUTO_INCREMENT,
  `id_reclutador` INT(11) NOT NULL,
  `codigo_participante` varchar(50) COMMENT 'Se asigna en runtime',
  `nombre_completo` varchar(255) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `grupo` enum('CASO','CONTROL') NOT NULL,
  `estado_ficha` enum('COMPLETA','INCOMPLETA','NO COMPLETABLE') NOT NULL DEFAULT 'INCOMPLETA',
  `fecha_inclusion` date NOT NULL,
  `observacion` text DEFAULT NULL,
  PRIMARY KEY (`id_participante`),
  UNIQUE KEY `codigo_participante` (`codigo_participante`),
  KEY `id_reclutador` (`id_reclutador`),
  CONSTRAINT `fk_participante_usuario` FOREIGN KEY (`id_reclutador`) REFERENCES `Usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `Variable` (
  `id_variable` INT(11) NOT NULL AUTO_INCREMENT,
  `enunciado` text NOT NULL,
  `codigo_variable` varchar(100) NOT NULL,
  `tipo_dato` varchar(50) NOT NULL COMMENT 'Ej: Texto, Numero, SeleccionUnica',
  `opciones` text DEFAULT NULL COMMENT 'Para SeleccionUnica, separado por comas',
  `aplica_a` varchar(50) NOT NULL DEFAULT 'Ambos' COMMENT 'Ej: Ambos, Caso, Control',
  `seccion` varchar(100) DEFAULT NULL,
  `orden_enunciado` int(11) DEFAULT 0,
  `es_obligatoria` tinyint(1) NOT NULL DEFAULT 0,
  `regla_validacion` text DEFAULT NULL,
  PRIMARY KEY (`id_variable`),
  UNIQUE KEY `codigo_variable` (`codigo_variable`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `Respuesta` (
  `id_respuesta` INT(11) NOT NULL AUTO_INCREMENT,
  `id_participante` INT(11) NOT NULL,
  `id_variable` INT(11) NOT NULL,
  `valor_ingresado` text DEFAULT NULL,
  PRIMARY KEY (`id_respuesta`),
  UNIQUE KEY `participante_variable_unique` (`id_participante`,`id_variable`),
  KEY `id_variable` (`id_variable`),
  CONSTRAINT `fk_respuesta_participante` FOREIGN KEY (`id_participante`) REFERENCES `Participante` (`id_participante`) ON DELETE CASCADE,
  CONSTRAINT `fk_respuesta_variable` FOREIGN KEY (`id_variable`) REFERENCES `Variable` (`id_variable`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `Auditoria` (
  `id_auditoria` INT(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` INT(11) NOT NULL,
  `id_participante` INT(11) NOT NULL,
  `tabla_afectada` varchar(100) DEFAULT NULL,
  `accion` varchar(100) NOT NULL,
  `detalle_cambio` text DEFAULT NULL,
  `fecha_cambio` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_auditoria`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_participante` (`id_participante`),
  CONSTRAINT `fk_auditoria_participante` FOREIGN KEY (`id_participante`) REFERENCES `Participante` (`id_participante`) ON DELETE CASCADE,
  CONSTRAINT `fk_auditoria_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- SECCIÓN 2: DML - INSERCIÓN DE DATOS INICIALES
--

INSERT INTO `Rol` (`nombre_rol`, `descripcion`) VALUES
('Investigadora Principal', 'Acceso total: CRF, auditoria, exportaciones, gestión de usuarios.'),
('Medico', 'Crear/editar CRF de sus casos; sin exportar.'),
('Investigador que recluta', 'Crear/editar CRF de sus casos; sin exportar.'),
('Investigador sin reclutamiento', 'Puede exportar datasets, no ve CRF individuales.'),
('Estudiante', 'Puede ingresar y editar datos con acceso restringido.'),
('Administrador', 'Control total del sistema.');
INSERT INTO `Usuario` (`id_rol`, `nombre_completo`, `correo`, `contrasenia`, `estado`) VALUES
((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigadora Principal'), 'Dra. María González', 'maria.g@investigacion.cl', 'un_hash_muy_seguro_aqui', 'ACTIVO'),
((SELECT id_rol FROM Rol WHERE nombre_rol = 'Administrador'), 'Admin del Sistema', 'admin@sistema.cl','administradorEstudiantesDatalab', 'ACTIVO');
INSERT INTO `Variable` (`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `es_obligatoria`) VALUES
('Edad', 'edad', 'Numero', NULL, 'Ambos', 'Datos sociodemográficos', 1),
('Sexo', 'sexo', 'SeleccionUnica', 'Hombre,Mujer', 'Ambos', 'Datos sociodemográficos', 1),
('Peso (kg)', 'peso_kg', 'Decimal', NULL, 'Ambos', 'Variables antropométricas', 1),
('Estatura (m)', 'estatura_m', 'Decimal', NULL, 'Ambos', 'Variables antropométricas', 1),
('Estado de tabaquismo', 'estado_tabaquismo', 'SeleccionUnica', 'Nunca fumó,Exfumador,Fumador actual', 'Ambos', 'Tabaquismo', 1),
('Genotipificación TLR9 rs5743836', 'tlr9_rs5743836', 'SeleccionUnica', 'TT,TC,CC', 'Ambos', 'Muestras biológicas y genéticas', 1),
('Genotipificación TLR9 rs187084', 'tlr9_rs187084', 'SeleccionUnica', 'TT,TC,CC', 'Ambos', 'Muestras biológicas y genéticas', 1),
('Genotipificación miR-146a rs2910164', 'mir146a_rs2910164', 'SeleccionUnica', 'GG,GC,CC', 'Ambos', 'Muestras biológicas y genéticas', 1);

INSERT INTO `Usuario` (id_rol, nombre_completo, correo, contrasenia, estado) VALUES
((SELECT id_rol FROM Rol WHERE nombre_rol = 'Administrador'),
 'Usuario Administrador',
 'userTest@administrador.com',
 '$2a$10$oPkyb6yGZE.5pwiYj7sl.u7ARPGDydkHtfiwRXp53kJKmkFuoWrDC', -- Versión encriptada de 'passwordUsuarioAdministrador'
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Medico'),
 'Usuario Medico',
 'userTest@medico.com',
 '$2a$10$.A/zmn9cM.FDH4qoZOCdA.zEHALvv3rYEShvYBykDkAvNeLgUN53a', -- Versión encriptada de 'passwordUsuarioMedico'
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigadora Principal'),
 'Usuario Investigadora Principal',
 'userTest@investigadora-principal.com',
 '$2a$10$4sBJ1XNonv8byNKO7C5Q9Ot.fiJ4ARBAgU4quLzDpH4h6lcudRuem', -- Versión encriptada de 'passwordUsuarioInvestigadoraPrincipal'
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigador que recluta'),
 'Usuario Investigador que Recluta',
 'userTest@investigador-que-recluta.com',
 '$2a$10$WF2x3HLgJvXzItR5TJCz6uVLuuwl86tmS5s5YDE7ZY3q9FdaZQNgm', -- Versión encriptada de 'passwordUsuarioInvestigadorQueRecluta'
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigador sin reclutamiento'),
 'Usuario Investigador sin Reclutamiento',
 'userTest@investigador-sin-reclutamiento.com',
 '$2a$10$DWYk7t2w33d8hxn0D8qDzOu.xUO/lPQnntaOOtKU8EXdQsIFvnC1q', -- Versión encriptada de 'passwordUsuarioInvestigadorSinReclutamiento'
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Estudiante'),
 'Usuario Estudiante',
 'userTest@estudiante.com',
 '$2a$10$3Qv4ylBYgjhAtDWzMrRU9OaN.phD0MEmXtc7BNksMCeooV.mDGKjS', -- Versión encriptada de 'passwordUsuarioEstudiante'
 'ACTIVO');
