# ==============================================================================
# PROJECT CODEOWNERS
#
# GitHub will automatically request a code review from the users listed below
# for Pull Requests that modify the specified files.
# ==============================================================================

# 1. GLOBAL RULE (Entire Codebase)
# Any file change in the project will notify both @uncorrupt0 and @ygt-erensoy.
# If only 1 approval is required in your branch protections, @ygt-erensoy can 
# approve and merge the PR if @uncorrupt0 is unavailable.
* @uncorrupt0 @ygt-erensoy

# ==============================================================================
# 2. ABSOLUTE OWNERSHIP RULES (Critical Infrastructure)
# The rules below override the global rule above. Any changes to these files
# can ONLY be approved by @uncorrupt0.
# ==============================================================================

# GitHub Actions (CI/CD) workflows and automated test pipelines
/.github/workflows/ @uncorrupt0

# Project dependencies and package manager lock files 
# (To prevent malicious packages from infiltrating the project)
requirements.txt    @uncorrupt0
Pipfile* @uncorrupt0
package.json        @uncorrupt0
package-lock.json   @uncorrupt0
yarn.lock           @uncorrupt0

# Docker, database schemas, or server configurations
Dockerfile          @uncorrupt0
docker-compose.yml  @uncorrupt0