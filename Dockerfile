# Frontend Dockerfile
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files first for better caching
COPY ./package.json .
COPY ./package-lock.json .

# Install dependencies
RUN npm install

# Copy source files
COPY ./client ./client
COPY ./tsconfig.json .
COPY ./vite.config.ts .
COPY ./postcss.config.js .
COPY ./tailwind.config.ts .
COPY ./theme.json .
COPY ./components.json .

# Build the application
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80