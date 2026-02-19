# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files for better caching
COPY package*.json ./
RUN npm ci

# Copy the rest of the source
COPY . .

# Build the application
# Note: VITE_ environment variables can be injected here via --build-arg if needed
RUN npm run build

# Stage 2: Production runtime
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts from stage 1
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port (default for nginx)
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -q --spider http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
