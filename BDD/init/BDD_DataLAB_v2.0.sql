--
-- SECCIÓN 1: DDL - CREACIÓN DE TABLAS
--
SET NAMES 'utf8mb4';

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
  `estado_ficha` enum('COMPLETA','INCOMPLETA','NO_COMPLETABLE') NOT NULL DEFAULT 'INCOMPLETA',
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
  `id_participante` INT(11) DEFAULT NULL,
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

 /* 1. Identificacion del participante */
INSERT INTO `Variable` 
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Nombre completo', 'nombre_completo', 'Texto', NULL, 'Ambos', 'Identificacion del participante', 1, 1),
('Telefono', 'telefono', 'Texto', NULL, 'Ambos', 'Identificacion del participante', 2, 1),
('Correo electronico', 'correo_electronico', 'Texto', NULL, 'Ambos', 'Identificacion del participante', 3, 0),
('Codigo del participante', 'codigo_participante', 'Texto', NULL, 'Ambos', 'Identificacion del participante', 4, 1),
('Fecha de inclusion', 'fecha_inclusion', 'Fecha', NULL, 'Ambos', 'Identificacion del participante', 6, 1);

/* 2. Datos sociodemograficos */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Edad', 'edad', 'Numero', NULL, 'Ambos', 'Datos sociodemograficos', 7, 0),
('Sexo', 'sexo', 'SeleccionUnica', 'Hombre,Mujer', 'Ambos', 'Datos sociodemograficos', 8, 0),
('Nacionalidad', 'nacionalidad', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 9, 0),
('Direccion', 'direccion', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 10, 0),
('Comuna', 'comuna', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 11, 0),
('Ciudad', 'ciudad', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 12, 0),
('Zona', 'zona', 'SeleccionUnica', 'Urbana,Rural', 'Ambos', 'Datos sociodemograficos', 13, 0),
('Vive usted en esta zona desde hace mas de 5 años?', 'vive_mas_5_anios_zona', 'SeleccionUnica', 'Si,No', 'Ambos', 'Datos sociodemograficos', 14, 0),
('Nivel educacional', 'nivel_educacional', 'SeleccionUnica', 'Basico,Medio,Superior', 'Ambos', 'Datos sociodemograficos', 15, 0),
('Ocupacion actual', 'ocupacion_actual', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 16, 0),
('Prevision de salud actual', 'prevision_salud', 'SeleccionUnica', 'Fonasa,Isapre,Capredena / Dipreca,Sin prevision,Otra', 'Ambos', 'Datos sociodemograficos', 17, 0),
('Prevision de salud actual - Otra (especificar)', 'prevision_salud_otra', 'Texto', NULL, 'Ambos', 'Datos sociodemograficos', 18, 0);

/* 3. Antecedentes clinicos */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Diagnostico histologico de adenocarcinoma gastrico (solo casos)', 'diag_adenocarcinoma_gastrico', 'SeleccionUnica', 'Si,No', 'Caso', 'Antecedentes clinicos', 19, 0),
('Fecha de diagnostico (solo casos)', 'fecha_diagnostico', 'Fecha', NULL, 'Caso', 'Antecedentes clinicos', 20, 0),
('Antecedentes familiares de cancer gastrico', 'anteced_fam_cancer_gastrico', 'SeleccionUnica', 'Si,No', 'Ambos', 'Antecedentes clinicos', 21, 0),
('Antecedentes familiares de otros tipos de cancer', 'anteced_fam_otro_cancer', 'SeleccionUnica', 'Si,No', 'Ambos', 'Antecedentes clinicos', 22, 0),
('Antecedentes familiares de otros tipos de cancer - Cual(es)?', 'anteced_fam_otro_cancer_detalle', 'Texto', NULL, 'Ambos', 'Antecedentes clinicos', 23, 0),
('Otras enfermedades relevantes (ej. gastritis cronica, ulcera peptica, anemia)', 'otras_enfermedades_relevantes', 'Texto', NULL, 'Ambos', 'Antecedentes clinicos', 24, 0),
('Uso cronico de medicamentos gastrolesivos (AINES u otros)', 'uso_cronico_gastrolesivos', 'SeleccionUnica', 'Si,No', 'Ambos', 'Antecedentes clinicos', 25, 0),
('Uso cronico de medicamentos gastrolesivos - Especificar cual', 'uso_cronico_gastrolesivos_detalle', 'Texto', NULL, 'Ambos', 'Antecedentes clinicos', 26, 0),
('Cirugia gastrica previa (gastrectomia parcial)', 'cirugia_gastrica_previa', 'SeleccionUnica', 'Si,No', 'Ambos', 'Antecedentes clinicos', 27, 0);

/* 4. Variables antropometricas */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Peso (kg)', 'peso_kg', 'Decimal', NULL, 'Ambos', 'Variables antropometricas', 28, 0),
('Estatura (m)', 'estatura_m', 'Decimal', NULL, 'Ambos', 'Variables antropometricas', 29, 0),
('Indice de masa corporal (IMC = peso/estatura^2)', 'imc', 'Decimal', NULL, 'Ambos', 'Variables antropometricas', 30, 0);

/* 5. Tabaquismo */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Estado de tabaquismo', 'estado_tabaquismo', 'SeleccionUnica', 'Nunca fumo (<100 cigarrillos vida),Exfumador,Fumador actual', 'Ambos', 'Tabaquismo', 31, 0),
('Cantidad promedio fumada', 'cantidad_cigarrillos_dia', 'SeleccionUnica', '1-9 cigarrillos/dia (poco),10-19 cigarrillos/dia (moderado),>=20 cigarrillos/dia (mucho)', 'Ambos', 'Tabaquismo', 32, 0),
('Tiempo total fumando', 'tiempo_total_fumando', 'SeleccionUnica', '<10 años,10-20 años,>20 años', 'Ambos', 'Tabaquismo', 33, 0),
('Si exfumador: tiempo desde que dejo de fumar', 'tiempo_desde_dejo_fumar', 'SeleccionUnica', '<5 años,5-10 años,>10 años', 'Ambos', 'Tabaquismo', 34, 0);

/* 6. Consumo de alcohol */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Estado de consumo', 'estado_consumo_alcohol', 'SeleccionUnica', 'Nunca,Exconsumidor,Consumidor actual', 'Ambos', 'Consumo de alcohol', 35, 0),
('Frecuencia', 'frecuencia_consumo_alcohol', 'SeleccionUnica', 'Ocasional (<1 vez/semana),Regular (1-3 veces/semana),Frecuente (>=4 veces/semana)', 'Ambos', 'Consumo de alcohol', 36, 0),
('Cantidad tipica por ocasion', 'cantidad_tragos_ocasion', 'SeleccionUnica', '1-2 tragos (poco),3-4 tragos (moderado),>=5 tragos (mucho)', 'Ambos', 'Consumo de alcohol', 37, 0),
('Años de consumo habitual', 'anios_consumo_alcohol', 'SeleccionUnica', '<10 años,10-20 años,>20 años', 'Ambos', 'Consumo de alcohol', 38, 0),
('Si exconsumidor: tiempo desde que dejo de beber regularmente', 'tiempo_desde_dejo_beber', 'SeleccionUnica', '<5 años,5-10 años,>10 años', 'Ambos', 'Consumo de alcohol', 39, 0);

/* 7. Factores dietarios y ambientales */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Consumo de carnes procesadas/cecinas', 'consumo_carnes_procesadas', 'SeleccionUnica', '<=1/sem,2/sem,>=3/sem', 'Ambos', 'Factores dietarios y ambientales', 40, 0),
('Consumo de alimentos muy salados (agrega sal a la comida sin probar)', 'consumo_alimentos_muy_salados', 'SeleccionUnica', 'Si,No', 'Ambos', 'Factores dietarios y ambientales', 41, 0),
('Consumo de porciones de frutas y verduras frescas (1 porcion = 1 fruta pequena, 1/2 taza de fruta picada, 1 taza de verduras de hoja cruda o 1/2 taza de verduras cocidas aprox 80 g)', 'consumo_frutas_verduras', 'SeleccionUnica', '>=5 porciones/dia (adecuado/protector),3-4 porciones/dia (intermedio),<=2 porciones/dia (bajo/insuficiente - riesgo)', 'Ambos', 'Factores dietarios y ambientales', 42, 0),
('Consumo frecuente de frituras (>=3 veces por semana)', 'consumo_frituras', 'SeleccionUnica', 'Si,No', 'Ambos', 'Factores dietarios y ambientales', 43, 0),
('Consumo de alimentos muy condimentados (aji, salsas picantes, etc.)', 'consumo_alimentos_condimentados', 'SeleccionUnica', 'Casi nunca / Rara vez,1 a 2 veces por semana,3 o mas veces por semana', 'Ambos', 'Factores dietarios y ambientales', 44, 0),
('Consumo de infusiones o bebidas muy calientes (tomadas sin dejar entibiar)', 'consumo_bebidas_muy_calientes', 'SeleccionUnica', 'Nunca/Rara vez,1-2/sem,>=3/sem', 'Ambos', 'Factores dietarios y ambientales', 45, 0),
('Exposicion ocupacional a pesticidas', 'exposicion_pesticidas_ocupacional', 'SeleccionUnica', 'Si,No', 'Ambos', 'Factores dietarios y ambientales', 46, 0),
('Exposicion a otros compuestos quimicos', 'exposicion_otros_quimicos', 'SeleccionUnica', 'Si,No', 'Ambos', 'Factores dietarios y ambientales', 47, 0),
('Exposicion a otros compuestos quimicos - Cual(es)?', 'exposicion_otros_quimicos_detalle', 'Texto', NULL, 'Ambos', 'Factores dietarios y ambientales', 48, 0),
('Humo de lena en el hogar (cocina/calefaccion)', 'humo_lena_hogar', 'SeleccionUnica', 'Nunca/Rara vez,Estacional,Diario', 'Ambos', 'Factores dietarios y ambientales', 49, 0),
('Fuente principal de agua en el hogar', 'fuente_agua_hogar', 'SeleccionUnica', 'Red publica,Pozo,Camion aljibe,Otra', 'Ambos', 'Factores dietarios y ambientales', 50, 0),
('Fuente principal de agua en el hogar - Otra (especificar)', 'fuente_agua_hogar_otra', 'Texto', NULL, 'Ambos', 'Factores dietarios y ambientales', 51, 0),
('Tratamiento del agua en casa', 'tratamiento_agua_hogar', 'SeleccionUnica', 'Ninguno,Hervir,Filtro,Cloro', 'Ambos', 'Factores dietarios y ambientales', 52, 0);

/* 8. Infeccion por Helicobacter pylori */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Resultado del examen para Helicobacter pylori', 'hp_resultado_actual', 'SeleccionUnica', 'Positivo,Negativo,Desconocido', 'Ambos', 'Infeccion por Helicobacter pylori', 53, 0),
('Ha tenido alguna vez un resultado POSITIVO para H. pylori en el pasado?', 'hp_resultado_prev_positivo', 'SeleccionUnica', 'Si,No,No recuerda', 'Ambos', 'Infeccion por Helicobacter pylori', 54, 0),
('Año aproximado del resultado positivo previo para H. pylori', 'hp_resultado_prev_anio', 'Numero', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 55, 0),
('Tipo de examen del resultado positivo previo para H. pylori', 'hp_resultado_prev_tipo_examen', 'Texto', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 56, 0),
('Recibio tratamiento para erradicacion de H. pylori?', 'hp_tratamiento_erradicacion', 'SeleccionUnica', 'Si,No,No recuerda', 'Ambos', 'Infeccion por Helicobacter pylori', 57, 0),
('Año del tratamiento para erradicacion de H. pylori', 'hp_tratamiento_erradicacion_anio', 'Numero', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 58, 0),
('Esquema de tratamiento para erradicacion de H. pylori', 'hp_tratamiento_erradicacion_esquema', 'Texto', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 59, 0),
('Tipo de examen realizado para H. pylori', 'hp_tipo_examen_actual', 'SeleccionUnica', 'Test de aliento (urea-C13/C14),Antigeno en deposiciones,Serologia (IgG),Test rapido de ureasa,Histologia / Biopsia,Otro', 'Ambos', 'Infeccion por Helicobacter pylori', 60, 0),
('Tipo de examen realizado para H. pylori - Otro (especificar)', 'hp_tipo_examen_otro', 'Texto', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 61, 0),
('Hace cuanto tiempo se realizo el test para H. pylori?', 'hp_tiempo_desde_test', 'SeleccionUnica', '<1 año,1-5 años,>5 años', 'Ambos', 'Infeccion por Helicobacter pylori', 62, 0),
('Uso de antibioticos o inhibidores de bomba de protones (IBP) en las 4 semanas previas al examen de H. pylori', 'hp_uso_antibioticos_ibp_4s', 'SeleccionUnica', 'Si,No,No recuerda', 'Ambos', 'Infeccion por Helicobacter pylori', 63, 0),
('Ha repetido el examen para H. pylori posteriormente?', 'hp_repite_examen', 'SeleccionUnica', 'Si,No', 'Ambos', 'Infeccion por Helicobacter pylori', 64, 0),
('Fecha del examen para H. pylori mas reciente', 'hp_repite_examen_fecha_reciente', 'Fecha', NULL, 'Ambos', 'Infeccion por Helicobacter pylori', 65, 0),
('Resultado del examen para H. pylori mas reciente', 'hp_repite_examen_resultado_reciente', 'SeleccionUnica', 'Positivo,Negativo,Desconocido', 'Ambos', 'Infeccion por Helicobacter pylori', 66, 0);

/* 9. Histopatologia (solo casos) */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Tipo histologico', 'tipo_histologico', 'SeleccionUnica', 'Intestinal,Difuso,Mixto,Otro', 'Caso', 'Histopatologia', 67, 0),
('Tipo histologico - Otro (especificar)', 'tipo_histologico_otro', 'Texto', NULL, 'Caso', 'Histopatologia', 68, 0),
('Localizacion tumoral', 'localizacion_tumoral', 'SeleccionUnica', 'Cardias,Cuerpo,Antro,Difuso', 'Caso', 'Histopatologia', 69, 0),
('Estadio clinico (TNM)', 'estadio_clinico_tnm', 'Texto', NULL, 'Caso', 'Histopatologia', 70, 0);

/* 10. Muestras biologicas y geneticas */
INSERT INTO `Variable`
(`enunciado`, `codigo_variable`, `tipo_dato`, `opciones`, `aplica_a`, `seccion`, `orden_enunciado`, `es_obligatoria`) VALUES
('Genotipificacion TLR9 rs5743836', 'tlr9_rs5743836', 'SeleccionUnica', 'TT,TC,CC', 'Ambos', 'Muestras biologicas y geneticas', 71, 0),
('Genotipificacion TLR9 rs187084', 'tlr9_rs187084', 'SeleccionUnica', 'TT,TC,CC', 'Ambos', 'Muestras biologicas y geneticas', 72, 1),
('Genotipificacion miR-146a rs2910164', 'mir146a_rs2910164', 'SeleccionUnica', 'GG,GC,CC', 'Ambos', 'Muestras biologicas y geneticas', 73, 0);