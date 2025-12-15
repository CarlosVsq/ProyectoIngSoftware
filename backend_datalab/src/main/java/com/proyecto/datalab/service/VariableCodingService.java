package com.proyecto.datalab.service;

import org.springframework.stereotype.Service;
import com.proyecto.datalab.entity.Variable;
import java.util.Arrays;
import java.util.List;

@Service
public class VariableCodingService {

    /**
     * Transforma un valor crudo a su codigo segun reglas definidas.
     */
    public String encodeValue(Variable variable, String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return "";
        }

        String codigo = variable.getCodigoVariable().toLowerCase();

        // 1. EDAD (Dicotomica: <45=0, >=45=1)
        if (codigo.equals("edad")) {
            try {
                double edad = Double.parseDouble(rawValue);
                return edad >= 45 ? "1" : "0";
            } catch (Exception e) {
                return "";
            }
        }

        // 2. IMC (Ordinal: 0=Bajo, 1=Normal, 2=Sobrepeso, 3=Obesidad)
        if (codigo.equals("imc")) {
            try {
                double imc = Double.parseDouble(rawValue.replace(",", "."));
                if (imc < 18.5)
                    return "0";
                if (imc < 25.0)
                    return "1";
                if (imc < 30.0)
                    return "2";
                return "3";
            } catch (Exception e) {
                return "";
            }
        }

        // 3. Variables con Opciones (Select) -> Index mapping
        // Se asume el orden de opciones define el codigo (0, 1, 2...)
        if (variable.getOpciones() != null && !variable.getOpciones().isEmpty()) {
            List<String> opciones = Arrays.asList(variable.getOpciones().split(","));
            for (int i = 0; i < opciones.size(); i++) {
                if (opciones.get(i).trim().equalsIgnoreCase(rawValue.trim())) {
                    return String.valueOf(i);
                }
            }
        }

        // 4. Fallback: Si/No standard (Si opsi no esta explícito)
        if (rawValue.equalsIgnoreCase("Si") || rawValue.equalsIgnoreCase("Sí"))
            return "1";
        if (rawValue.equalsIgnoreCase("No"))
            return "0";

        // Default: return raw if no rule applies (e.g. text fields, dates, or
        // Peso/Estatura raw)
        // User requested Peso/Estatura NOT dichotomized, so they flow through here.
        return rawValue;
    }

    /**
     * Retorna descripcion textual de la regla de codificacion para la leyenda.
     */
    public String getEncodingDescription(Variable variable) {
        String codigo = variable.getCodigoVariable().toLowerCase();

        if (codigo.equals("edad")) {
            return "0: < 45 años\n1: >= 45 años";
        }
        if (codigo.equals("imc")) {
            return "0: Bajo peso (<18.5)\n1: Normal (18.5-24.9)\n2: Sobrepeso (25-29.9)\n3: Obesidad (>=30)";
        }

        if (variable.getOpciones() != null && !variable.getOpciones().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            String[] opts = variable.getOpciones().split(",");
            for (int i = 0; i < opts.length; i++) {
                if (i > 0)
                    sb.append("\n");
                sb.append(i).append(": ").append(opts[i].trim());
            }
            return sb.toString();
        }

        // Tipos numericos puros sin reglas especiales
        if (variable.getTipoDato().equalsIgnoreCase("Numero") || variable.getTipoDato().equalsIgnoreCase("Decimal")) {
            return "Valor numérico real";
        }

        // Texto / Fecha
        return "Texto / Fecha (Sin codificar)";
    }

    /**
     * Determina si una variable es "binaria" en su output (0/1) para el calculo de
     * stats.
     * Esto ayuda a saber si contar ceros y unos tiene sentido o si es mejor contar
     * solo "Llenos/Vacios".
     */
    public boolean isBinaryOrCategorical(Variable variable) {
        String code = variable.getCodigoVariable().toLowerCase();
        if (code.equals("edad") || code.equals("imc"))
            return true;
        if (variable.getOpciones() != null && !variable.getOpciones().isEmpty())
            return true;
        return false;
    }
}
