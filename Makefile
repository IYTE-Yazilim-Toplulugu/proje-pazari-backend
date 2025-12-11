PHONY: dev up

up:
	@echo "Starting all services..."
	docker compose up -d

down:
	@echo "Stopping all services..."
	docker compose down

logs:
	docker compose logs -f

dev:
	@echo "Starting PostgreSQL container..."
	docker compose up -d postgres

	@echo "Waiting for PostgreSQL to be ready..."
	sleep 5

	@echo "Starting Spring Boot application..."
	./gradlew bootRun

	@echo "Application started. Press Ctrl+C to stop."

