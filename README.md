# CafeOJ - Online Judge Platform

A Spring Boot-based online judge platform for problem solving.

## 🚀 Quick Start with Docker

### Prerequisites

- Docker (20.10+)
- Docker Compose (2.0+)

### Setup Environment Variables

Before starting, create your environment file:

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your preferred values (optional for development)
nano .env
```

The `.env` file contains sensitive configuration:

```bash
# Database credentials
POSTGRES_DB=cafeoj
POSTGRES_USER=cafeoj_user
POSTGRES_PASSWORD=your_secure_password

# Admin account password (created on first startup)
ADMIN_PASSWORD=your_admin_password
```

> ⚠️ **Important**: Never commit `.env` to version control. It's already in `.gitignore`.

### Run Using Docker Compose

```bash
# Start the application
docker compose up --build -d

# View logs
docker compose logs -f

# Stop the application
docker compose down
```

## 📦 What Gets Created

When you run the Docker setup, the following components are created:

1. **PostgreSQL Container** (`cafeoj-postgres`)

   - Database: configured via `POSTGRES_DB`
   - User: configured via `POSTGRES_USER`
   - Password: configured via `POSTGRES_PASSWORD`
   - Port: `5432`

2. **Spring Boot Application Container** (`cafeoj-app`)

   - Port: `8080`
   - Profile: `docker`
   - Admin user created with password from `ADMIN_PASSWORD`

3. **Persistent Volume** (`postgres-data`)

   - Stores all database data
   - Survives container restarts

4. **Network** (`cafeoj-network`)
   - Allows containers to communicate

## 🌐 Access the Application

Once started, access the application at:

- **Homepage**: http://localhost:8080

## 📋 Available Commands

### Docker Compose Commands

```bash
docker compose up -d              # Start in detached mode
docker compose up --build -d      # Build and start
docker compose down               # Stop services
docker compose down -v            # Stop and remove volumes
docker compose logs -f            # Follow logs
docker compose logs -f app        # Follow app logs only
docker compose ps                 # Show status
docker compose restart            # Restart services
```

## 🛠️ Development Workflow

### Making Code Changes

1. Edit your code in the `src` directory
2. Rebuild and restart:
   ```bash
    docker compose up --build -d
   ```

### Database Access

To access the PostgreSQL database directly:

```bash
docker exec -it cafeoj-postgres psql -U cafeoj_user -d cafeoj
```

Common PostgreSQL commands:

```sql
\dt              -- List tables
\d users         -- Describe users table
SELECT * FROM users;  -- Query users
\q               -- Quit
```

## 🔧 Configuration

### Environment Variables

All sensitive configuration is managed via the `.env` file. Available variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `cafeoj` |
| `POSTGRES_USER` | Database username | `cafeoj_user` |
| `POSTGRES_PASSWORD` | Database password | *(required)* |
| `ADMIN_PASSWORD` | Admin account password | `admin` |

To customize:

```bash
# Create .env from template
cp .env.example .env

# Edit with your values
nano .env
```

### Port Conflicts

If ports 8080 or 5432 are already in use, edit `docker compose.yml`:

```yaml
services:
  app:
    ports:
      - "8081:8080" # Change external port
  postgres:
    ports:
      - "5433:5432" # Change external port
```

## 📁 Project Structure

```
CafeOJ/
├── docker compose.yml          # Docker Compose configuration
├── Dockerfile                  # Application container definition
├── .dockerignore              # Files to exclude from Docker build
├── .env.example               # Environment variables template
├── README-DOCKER.md           # Detailed Docker documentation
├── src/
│   └── main/
│       ├── java/              # Java source code
│       └── resources/
│           ├── application.properties        # Default config
│           └── application-docker.properties # Docker config
└── pom.xml                    # Maven configuration
```

## 🔒 Security Notes

For production deployment:

1. **Change default passwords** in your `.env` file (never use defaults in production)
2. **Keep `.env` secret** - it's gitignored and should never be committed
3. **Use secrets management** (Docker secrets, Kubernetes secrets, etc.) for production
4. **Enable HTTPS** with proper SSL certificates
5. **Configure firewall** rules
6. **Set up monitoring** and alerting
7. **Implement backup** strategy for the database

## 📚 Additional Documentation

- Detailed Docker Documentation: https://docs.docker.com/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- PostgreSQL Documentation: https://www.postgresql.org/docs/

## 🆘 Getting Help

If you encounter issues:

1. Check logs: `docker compose logs -f`
2. Check container status: `docker compose ps`
3. Try a clean restart: `docker compose down -v` then `docker compose up --build -d`

## 📝 License

See [LICENSE](LICENSE) file for details.
