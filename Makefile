.PHONY: help build up down restart logs clean test db-shell app-shell ps rebuild stop start dev prod tools

# Default target
.DEFAULT_GOAL := help

# Colors for output
YELLOW := \033[1;33m
GREEN := \033[1;32m
CYAN := \033[1;36m
RED := \033[1;31m
NC := \033[0m # No Color

##@ Help

help: ## Display this help message
	@echo "$(CYAN)Proje Pazari Backend - Development Commands$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make $(YELLOW)<target>$(NC)\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  $(CYAN)%-15s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(GREEN)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development

dev: ## Start development environment (app + database)
	@echo "$(GREEN)Starting development environment...$(NC)"
	docker compose up -d

dev-build: ## Build and start development environment
	@echo "$(GREEN)Building and starting development environment...$(NC)"
	docker compose up -d --build

dev-logs: ## Follow development logs
	docker compose logs -f

stop: ## Stop all services
	@echo "$(YELLOW)Stopping all services...$(NC)"
	docker compose stop

down: ## Stop and remove all containers
	@echo "$(RED)Stopping and removing containers...$(NC)"
	docker compose down

down-volumes: ## Stop and remove all containers and volumes (⚠️  deletes database)
	@echo "$(RED)Stopping and removing containers and volumes...$(NC)"
	docker compose down -v

restart: ## Restart all services
	@echo "$(YELLOW)Restarting all services...$(NC)"
	docker compose restart

##@ Build & Deploy

build: ## Build Docker images
	@echo "$(GREEN)Building Docker images...$(NC)"
	docker compose build

rebuild: ## Rebuild Docker images from scratch (no cache)
	@echo "$(GREEN)Rebuilding Docker images from scratch...$(NC)"
	docker compose build --no-cache

up: ## Start all services in detached mode
	@echo "$(GREEN)Starting all services...$(NC)"
	docker compose up -d

start: ## Start existing containers
	docker compose start

##@ Database

db-shell: ## Open PostgreSQL shell
	@echo "$(CYAN)Opening PostgreSQL shell...$(NC)"
	docker compose exec postgres psql -U yazilim -d proje_pazari_db

db-logs: ## View database logs
	docker compose logs -f postgres

db-migrate: ## Run database migrations (if using Flyway/Liquibase)
	@echo "$(YELLOW)Running database migrations...$(NC)"
	docker compose exec app ./gradlew flywayMigrate

db-reset: down-volumes dev ## Reset database (⚠️  deletes all data)
	@echo "$(GREEN)Database reset complete$(NC)"

##@ Application

app-shell: ## Open shell in application container
	@echo "$(CYAN)Opening application shell...$(NC)"
	docker compose exec app sh

app-logs: ## View application logs
	docker compose logs -f app

app-restart: ## Restart application container
	docker compose restart app

test: ## Run tests locally
	@echo "$(GREEN)Running tests locally...$(NC)"
	./gradlew test

test-docker: ## Run tests in Docker
	@echo "$(GREEN)Running tests in Docker...$(NC)"
	docker compose exec app ./gradlew test

clean: ## Clean build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	./gradlew clean

##@ Tools

tools: ## Start additional tools (pgAdmin)
	@echo "$(GREEN)Starting additional tools...$(NC)"
	docker compose --profile tools up -d

tools-down: ## Stop additional tools
	docker compose --profile tools down

pgadmin: tools ## Open pgAdmin in browser
	@echo "$(CYAN)pgAdmin is running at: http://localhost:5050$(NC)"
	@echo "Email: admin@proje-pazari.com"
	@echo "Password: admin"

##@ Monitoring

ps: ## List running containers
	docker compose ps

logs: ## View logs from all services
	docker compose logs

logs-tail: ## Tail logs from all services
	docker compose logs -f --tail=100

status: ## Show status of all services
	@echo "$(CYAN)Service Status:$(NC)"
	@docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

##@ Cleanup

prune: ## Remove unused Docker resources
	@echo "$(YELLOW)Removing unused Docker resources...$(NC)"
	docker system prune -f

prune-all: ## Remove all unused Docker resources including volumes
	@echo "$(RED)Removing all unused Docker resources...$(NC)"
	docker system prune -af --volumes

##@ Local Development

local-dev: ## Run application locally (without Docker)
	@echo "$(GREEN)Starting PostgreSQL container...$(NC)"
	docker compose up -d postgres
	@echo "Waiting for PostgreSQL to be ready..."
	@sleep 5
	@echo "$(GREEN)Starting Spring Boot application locally...$(NC)"
	./gradlew bootRun

local-test: ## Run tests locally
	@echo "$(GREEN)Running tests locally...$(NC)"
	./gradlew test

local-build: ## Build application locally
	@echo "$(GREEN)Building application locally...$(NC)"
	./gradlew build

