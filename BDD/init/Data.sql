-- Asegúrate de estar en la DB correcta
USE `DBB_DATALAB`;

-- Crea usuarios demo solo si no existen (evita fallos por claves únicas y usa el nombre de tabla correcto).
INSERT IGNORE INTO `Usuario` (id_rol, nombre_completo, correo, contrasenia, estado) VALUES
((SELECT id_rol FROM Rol WHERE nombre_rol = 'Administrador'),
 'Usuario Administrador',
 'userTest@administrador.com',
 '$2a$10$oPkyb6yGZE.5pwiYj7sl.u7ARPGDydkHtfiwRXp53kJKmkFuoWrDC',
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Medico'),
 'Usuario Medico',
 'userTest@medico.com',
 '$2a$10$.A/zmn9cM.FDH4qoZOCdA.zEHALvv3rYEShvYBykDkAvNeLgUN53a',
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigadora Principal'),
 'Usuario Investigadora Principal',
 'userTest@investigadora-principal.com',
 '$2a$10$4sBJ1XNonv8byNKO7C5Q9Ot.fiJ4ARBAgU4quLzDpH4h6lcudRuem',
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigador que recluta'),
 'Usuario Investigador que Recluta',
 'userTest@investigador-que-recluta.com',
 '$2a$10$WF2x3HLgJvXzItR5TJCz6uVLuuwl86tmS5s5YDE7ZY3q9FdaZQNgm',
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Investigador sin reclutamiento'),
 'Usuario Investigador sin Reclutamiento',
 'userTest@investigador-sin-reclutamiento.com',
 '$2a$10$DWYk7t2w33d8hxn0D8qDzOu.xUO/lPQnntaOOtKU8EXdQsIFvnC1q',
 'ACTIVO'),

((SELECT id_rol FROM Rol WHERE nombre_rol = 'Estudiante'),
 'Usuario Estudiante',
 'userTest@estudiante.com',
 '$2a$10$3Qv4ylBYgjhAtDWzMrRU9OaN.phD0MEmXtc7BNksMCeooV.mDGKjS',
 'ACTIVO');
