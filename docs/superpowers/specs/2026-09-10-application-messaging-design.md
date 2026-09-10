# Application-Scoped Messaging Design

## Scope

Each project application is one message thread. Its only participants are the applicant and the
owner of the associated project. The feature uses authenticated REST calls and deliberately adds
no inbox, realtime transport, read state, attachments, or rich text.

## Backend contract

- `GET /api/v1/applications/{applicationId}/messages?page=0&size=20`
- `POST /api/v1/applications/{applicationId}/messages` with `{ "body": "..." }`
- The server derives the requester/sender identifier from `Authentication`; client-supplied user
  identifiers are ignored because they are not part of the request schema.
- Bodies are plain text, trimmed before persistence, non-blank, and limited to 2,000 characters.
- Pages default to 20 messages and accept sizes from 1 through 100.
- Ordering is ascending by `(createdAt, id)` so pagination is deterministic even when timestamps
  are equal.
- Missing applications return 404. Authenticated non-participants return 403 for both operations.
  Existing security rejects anonymous requests.

The response uses the existing `ApiResponse` envelope. Lists contain a `PagedResponse` whose
`content` items expose only `id`, `applicationId`, `senderId`, `body`, and `createdAt`.

## Persistence and deletion

V6 creates `application_messages` with explicit foreign keys to `project_applications` and `users`.
Deleting an application cascades to its messages. Deleting a sender is restricted while a parent
application and its thread exist. A composite `(application_id, created_at, id)` index supports the
documented ordering.

V6 is merged after #166 establishes Flyway V1-V4 and #163 adds V5.

## Logging and rendering safety

Handlers and pipeline logging identify only the request type and never stringify the command.
Validation logging records rejected field names without rejected values. Message bodies are not
added to audit records or exception messages. The frontend renders body strings through React text
nodes and never injects HTML.

## Frontend integration

A shared `ApplicationMessageThread` component is embedded in each applicant item on
`/applications` and beside each owner review card on `/my-projects/[project_id]`. Shared Zod models,
API functions, and React Query hooks implement the same contract. A successful send invalidates the
application-specific message query, updating the visible thread without navigation or reload.
