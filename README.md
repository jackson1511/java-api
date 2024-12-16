# Spring Boot Kubernetes Deployment Guide

## Project Setup

This project demonstrates a Spring Boot application deployment on Kubernetes with automated CI/CD using GitHub Actions.

### Prerequisites
- Docker Desktop with Kubernetes enabled
- kubectl CLI
- Git
- GitHub account
- Docker Hub account

## Infrastructure Components

1. **Spring Boot Application**
- Java 17
- Spring Boot 3.3.5
- PostgreSQL database
- JPA for data persistence
- Actuator for health checks
- REST API endpoints

2. **Database**
- PostgreSQL 16
- Deployed as a Kubernetes service

3. **Kubernetes Resources**
- Deployments for application and database
- Services for routing
- Environment variables for configuration

## Deployment Configuration

### Kubernetes Configuration (docker-java-kubernetes.yaml)
```yaml
# PostgreSQL deployment
apiVersion: apps/v1
kind: Deployment
metadata:
name: postgres
spec:
replicas: 1
selector:
    matchLabels:
    app: postgres
template:
    metadata:
    labels:
        app: postgres
    spec:
    containers:
        - name: postgres
        image: postgres:16
        env:
            - name: POSTGRES_DB
            value: "demo"
            - name: POSTGRES_USER
            value: "postgres"
            - name: POSTGRES_PASSWORD
            value: "postgres"
        ports:
            - containerPort: 5432
---
# PostgreSQL service
apiVersion: v1
kind: Service
metadata:
name: db
spec:
selector:
    app: postgres
ports:
    - port: 5432
    targetPort: 5432
---
# Application deployment
apiVersion: apps/v1
kind: Deployment
metadata:
name: docker-java-demo
spec:
replicas: 1
selector:
    matchLabels:
    service: server
template:
    metadata:
    labels:
        service: server
    spec:
    containers:
        - name: server-service
        image: jackson1115/springboot
        imagePullPolicy: Always
        env:
            - name: SPRING_DATASOURCE_URL
            value: "jdbc:postgresql://db:5432/demo"
            - name: SPRING_DATASOURCE_USERNAME
            value: "postgres"
            - name: SPRING_DATASOURCE_PASSWORD
            value: "postgres"
---
# Application service
apiVersion: v1
kind: Service
metadata:
name: service-entrypoint
spec:
type: NodePort
selector:
    service: server
ports:
    - port: 8080
    targetPort: 8080
    nodePort: 30001
```

### GitHub Actions Workflow (.github/workflows/main.yml)
```yaml
name: CI/CD Pipeline

on:
push:
    branches: [ main ]
    tags: [ "v*" ]

env:
DOCKER_USERNAME: ${{ vars.DOCKER_USERNAME }}
REPO_NAME: ${{ github.event.repository.name }}

jobs:
build-and-deploy:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
        uses: actions/checkout@v4
        with:
        fetch-depth: 0

    - name: Generate version
        id: version
        run: |
        VERSION=$(echo $GITHUB_SHA | cut -c1-7)
        echo "VERSION=${VERSION}" >> $GITHUB_ENV

    - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
        username: ${{ env.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKERHUB_TOKEN }}

    - name: Build and push Docker image
        uses: docker/build-push-action@v6
        with:
        platforms: linux/amd64,linux/arm64
        push: true
        target: final
        tags: |
            ${{ env.DOCKER_USERNAME }}/${{ env.REPO_NAME }}:latest
            ${{ env.DOCKER_USERNAME }}/${{ env.REPO_NAME }}:${{ env.VERSION }}
```

## Deployment Steps

1. **Initial Setup**
```bash
# Enable Kubernetes in Docker Desktop
# Clone repository
git clone <repository-url>
cd <repository-directory>
```

2. **Deploy to Kubernetes**
```bash
# Apply Kubernetes configuration
kubectl apply -f docker-java-kubernetes.yaml

# Verify deployments
kubectl get pods
kubectl get services
```

3. **Update Application**
```bash
# Make code changes
git add .
git commit -m "Update message"
git push

# GitHub Actions will automatically:
# - Build new Docker image
# - Push to Docker Hub
# - Update deployment

# Or manually update deployment
kubectl rollout restart deployment docker-java-demo
```

4. **Monitoring and Troubleshooting**
```bash
# Check pod status
kubectl get pods

# View logs
kubectl logs <pod-name>

# Check deployment status
kubectl describe deployment docker-java-demo

# Test application
curl http://localhost:30001/actuator/health
```

5. **Managing Versions**
```bash
# List Docker image tags
curl -s "https://registry.hub.docker.com/v2/repositories/jackson1115/springboot/tags"

# Update to specific version
kubectl set image deployment/docker-java-demo server-service=jackson1115/springboot:<tag>
```

## Accessing the Application

- Health Check: http://localhost:30001/actuator/health
- API Endpoints: http://localhost:30001/api/v1/*

## Maintenance

1. **Scale Application**
```bash
kubectl scale deployment docker-java-demo --replicas=3
```

2. **Update Database Configuration**
- Edit environment variables in docker-java-kubernetes.yaml
- Reapply configuration:
    ```bash
    kubectl apply -f docker-java-kubernetes.yaml
    ```

3. **Rollback to Previous Version**
```bash
kubectl rollout undo deployment docker-java-demo
```

## Notes
- Keep secrets in Kubernetes secrets (not in plain text)
- Monitor application logs for issues
- Regularly update dependencies
- Use specific version tags in production
