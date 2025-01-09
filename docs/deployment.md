# Deployment Guide

## Prerequisites

- Java 17 or higher
- PostgreSQL 15+
- Redis (optional)
- AWS Account (for cloud deployment)
- Docker (optional)

## Local Deployment

### 1. Database Setup

1. Install PostgreSQL:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib

# Windows
# Download and install from https://www.postgresql.org/download/windows/
```

2. Create database:
```sql
CREATE DATABASE chatgenius;
CREATE USER chatgenius_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE chatgenius TO chatgenius_user;
```

### 2. Redis Setup (Optional)

1. Install Redis:
```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# Windows
# Download and install from https://redis.io/download
```

2. Start Redis:
```bash
# Linux
sudo systemctl start redis

# Windows
redis-server
```

### 3. Application Deployment

1. Build the application:
```bash
./mvnw clean package -DskipTests
```

2. Configure application:
```bash
# Create application-prod.yml
cp src/main/resources/application.yml src/main/resources/application-prod.yml
# Edit application-prod.yml with production settings
```

3. Run the application:
```bash
java -jar target/chatgenius-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Docker Deployment

### 1. Build Docker Image

1. Create Dockerfile:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/chatgenius-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

2. Build image:
```bash
docker build -t chatgenius-backend .
```

3. Run container:
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/chatgenius \
  -e SPRING_DATASOURCE_USERNAME=chatgenius_user \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  chatgenius-backend
```

### 2. Docker Compose

1. Create docker-compose.yml:
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/chatgenius
      - SPRING_DATASOURCE_USERNAME=chatgenius_user
      - SPRING_DATASOURCE_PASSWORD=your_password
      - SPRING_REDIS_HOST=redis
    depends_on:
      - db
      - redis

  db:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=chatgenius
      - POSTGRES_USER=chatgenius_user
      - POSTGRES_PASSWORD=your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

2. Run with Docker Compose:
```bash
docker-compose up -d
```

## AWS Deployment

### 1. Prerequisites

- AWS CLI installed and configured
- AWS RDS instance running
- AWS ElastiCache instance running (optional)
- AWS S3 bucket created (for file uploads)

### 2. Deploy to Elastic Beanstalk

1. Create Elastic Beanstalk application:
```bash
eb init chatgenius-backend --platform java --region us-east-1
```

2. Create .elasticbeanstalk/config.yml:
```yaml
branch-defaults:
  main:
    environment: chatgenius-prod
    group_suffix: null

global:
  application_name: chatgenius-backend
  branch: null
  default_ec2_keyname: null
  default_platform: Java 17
  default_region: us-east-1
  include_git_submodules: true
  instance_profile: null
  platform_name: null
  platform_version: null
  profile: null
  repository: null
  sc: git
  workspace_type: Application
```

3. Deploy:
```bash
eb create chatgenius-prod --single --instance-type t2.micro
```

### 3. Configure Environment Variables

Set environment variables in Elastic Beanstalk:
```bash
eb setenv \
  SPRING_PROFILES_ACTIVE=prod \
  SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-instance.region.rds.amazonaws.com:5432/chatgenius \
  SPRING_DATASOURCE_USERNAME=chatgenius_user \
  SPRING_DATASOURCE_PASSWORD=your_password \
  SPRING_REDIS_HOST=your-elasticache-instance.region.cache.amazonaws.com
```

## Monitoring & Maintenance

### 1. Health Checks

Monitor application health:
```bash
# Local
curl http://localhost:8080/actuator/health

# AWS
curl http://your-eb-environment.region.elasticbeanstalk.com/actuator/health
```

### 2. Logging

View application logs:
```bash
# Local
tail -f logs/application.log

# Docker
docker logs -f container_id

# AWS
eb logs
```

### 3. Backup

1. Database backup:
```bash
# Local
pg_dump -U chatgenius_user chatgenius > backup.sql

# AWS RDS
aws rds create-db-snapshot \
  --db-instance-identifier your-instance-id \
  --db-snapshot-identifier backup-name
```

2. Application backup:
```bash
# Configuration files
cp -r src/main/resources/application*.yml backup/

# Uploaded files
cp -r uploads/ backup/
```

## Security Considerations

1. SSL/TLS Configuration:
```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: your_keystore_password
    key-store-type: PKCS12
    key-alias: tomcat
```

2. Firewall Rules:
```bash
# Allow only necessary ports
sudo ufw allow 8080/tcp
sudo ufw allow 5432/tcp
sudo ufw allow 6379/tcp
```

3. Security Headers:
```yaml
security:
  headers:
    content-security-policy: "default-src 'self'"
    x-frame-options: DENY
    x-content-type-options: nosniff
```

## Troubleshooting

1. Application won't start:
- Check logs for errors
- Verify database connection
- Ensure correct Java version
- Check port availability

2. Database connection issues:
- Verify credentials
- Check network connectivity
- Validate database status

3. Performance issues:
- Monitor CPU/memory usage
- Check database query performance
- Review connection pool settings
- Analyze garbage collection logs 