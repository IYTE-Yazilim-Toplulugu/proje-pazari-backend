# Kibana Dashboards for Proje Pazari

This directory contains pre-configured Kibana dashboards for monitoring and analytics.

## Available Dashboards

### 1. Project Search Analytics (`project-analytics-dashboard.ndjson`)
**Purpose**: Monitor project discovery and search patterns

**Visualizations**:
- Projects Created Over Time - Line chart showing project creation trends
- Projects by Status - Pie chart showing distribution (OPEN, IN_PROGRESS, COMPLETED, CLOSED)
- Popular Project Tags - Tag cloud showing most used project tags

**Use Cases**:
- Track project creation velocity
- Identify trending project categories
- Monitor project lifecycle distribution

### 2. User Activity Metrics (`user-activity-dashboard.ndjson`)
**Purpose**: Track user engagement and registration patterns

**Visualizations**:
- User Registration Timeline - Histogram showing new user signups over time (based on `joinedAt`)
- Users by Email Domain - Pie chart showing distribution of users by email domain
- Total Users - Metric showing total user count
- Users with Profile Description - Metric showing users who have filled in their description

**Use Cases**:
- Monitor user growth
- Track user profile completion
- Identify user engagement patterns

### 3. System Health Monitoring (`system-health-dashboard.ndjson`)
**Purpose**: Monitor Elasticsearch indices and system metrics

**Visualizations**:
- Total Projects - Metric showing total project count
- Total Users - Metric showing total user count
- Active Projects - Metric showing open projects
- Projects Created Over Time - Area chart showing project creation trends

**Use Cases**:
- Monitor system health
- Track data growth
- Quick overview of system status

## How to Import Dashboards

### Method 1: Using Kibana UI

1. Start the application with docker-compose:
   ```bash
   docker-compose up -d
   ```

2. Access Kibana at http://localhost:5601

3. Navigate to **Management** → **Stack Management** → **Saved Objects**

4. Click **Import** button

5. Select a dashboard file (e.g., `project-analytics-dashboard.ndjson`)

6. Click **Import**

7. Repeat for other dashboards

### Method 2: Using curl

```bash
# Import project analytics dashboard
curl -X POST "localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deployment/kibana/dashboards/project-analytics-dashboard.ndjson

# Import user activity dashboard
curl -X POST "localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deployment/kibana/dashboards/user-activity-dashboard.ndjson

# Import system health dashboard
curl -X POST "localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deployment/kibana/dashboards/system-health-dashboard.ndjson
```

## Data Requirements

These dashboards work with the existing Elasticsearch indices:
- **projects** - Created from `ProjectDocument` entities
- **users** - Created from `UserDocument` entities

### Ensure Data is Synced

Before using dashboards, sync your PostgreSQL data to Elasticsearch:

```bash
# Reindex projects into Elasticsearch
curl -X POST http://localhost:8080/api/v1/admin/elasticsearch/reindex/projects \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
# Reindex users into Elasticsearch
curl -X POST http://localhost:8080/api/v1/admin/elasticsearch/reindex/users \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## Customization

Each dashboard can be customized through Kibana UI:

1. Open the dashboard
2. Click **Edit** button
3. Modify visualizations:
   - Change time ranges
   - Add filters
   - Modify aggregations
   - Change visualization types
4. Click **Save**

## Index Patterns

Make sure these index patterns exist in Kibana:
- `projects` - for project data
- `users` - for user data

They should be created automatically when you sync data to Elasticsearch.

## Troubleshooting

### No data appearing in dashboards

1. Check if Elasticsearch is running:
   ```bash
   curl http://localhost:9200/_cluster/health
   ```

2. Verify indices exist:
   ```bash
   curl http://localhost:9200/_cat/indices
   ```

3. Sync data from PostgreSQL:
   ```bash
   curl -X POST http://localhost:8080/api/v1/admin/elasticsearch/reindex/projects \
     -H "Authorization: Bearer YOUR_TOKEN"
   curl -X POST http://localhost:8080/api/v1/admin/elasticsearch/reindex/users \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

### Dashboard import fails

- Ensure Kibana version matches (8.11.0)
- Check if index patterns exist
- Try importing one visualization at a time

## Best Practices

1. **Refresh interval**: Set appropriate refresh intervals based on data update frequency
2. **Time range**: Use appropriate time ranges for different use cases
3. **Filters**: Create saved searches with common filters
4. **Alerts**: Set up alerts for important metrics (requires Elastic Stack subscription)

## Future Enhancements

Consider adding:
- Application metrics dashboard (API response times, error rates)
- Project collaboration metrics
- Search query analytics (if logging is added)
- User engagement funnel
