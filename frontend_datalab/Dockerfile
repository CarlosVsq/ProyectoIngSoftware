# ---------- Build stage ----------
FROM node:22-alpine AS build

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY . .

# Build de producción
RUN npm run build

# ---------- Runtime stage con Nginx ----------
FROM nginx:1.25-alpine

# Copiar archivos build de Angular al directorio de Nginx
COPY --from=build /app/dist/datalab-frontend/browser /usr/share/nginx/html

# Copiar configuración personalizada de Nginx para Angular (routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto 80
EXPOSE 80

# Comando de inicio
CMD ["nginx", "-g", "daemon off;"]
