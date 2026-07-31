# Backend API And Change Record

Compared against untouched original backend:

- Original: `C:\Users\User\Downloads\sdg-recycling-ecosystem-dev\backend`
- Updated: `C:\Users\User\Desktop\UOW Class\Sem 7\Final Year Project 2\sdg-recycling-ecosystem-dev\backend`

Legend:

- `New added`: endpoint, enum value, database field, migration, script, or behavior did not exist in the original backend.
- `Updated`: endpoint/file existed in the original backend, but behavior or returned data changed.
- `Original`: endpoint already existed and the core behavior is unchanged.

## Auth

Login/register system using PostgreSQL, bcrypt, and JWT.

### POST `/auth/register` - Original

- Validates `name`, `email`, and `password`.
- Email must be valid format.
- Password must be at least 8 characters.
- Checks whether email already exists.
- Hashes password using bcrypt.
- bcrypt automatically handles salt generation.
- Creates a new user with `STUDENT` role.
- Returns JWT token and user data.

### POST `/auth/login` - Original

- Validates `email` and `password`.
- Finds user by email in PostgreSQL.
- Compares typed password with stored bcrypt hash.
- Returns JWT token and user data if correct.

### GET `/auth/me` - Original

- Requires `Authorization: Bearer <JWT token>`.
- Verifies token.
- Returns current logged-in user data.

## Missions

Mission creation, listing, update, archive, join, image upload, and submission.

### POST `/missions` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Creates a new mission.
- Validates title, description, type, start/end date, points, and existing original mission fields.
- `New added`: accepts `longDescription`.
- `New added`: accepts `imageUrl`.
- `New added`: accepts `guide` JSON array.
- `New added`: accepts `targetQuantity`.
- `New added`: accepts `targetDays`.
- Generates mission slug from title.
- Checks mission time window does not overlap with an existing mission of the same type.
- Returns created mission data.

### GET `/missions` - logged-in user - Updated

- Requires `Authorization: Bearer <JWT token>`.
- Retrieves missions from PostgreSQL.
- For normal student browsing without query filters, returns:
  - active missions where `isActive = true` and `status = ACTIVE`
  - `New added`: archived missions where `status = ARCHIVED`
- Admin or explicit query filters can filter by `status`, `isActive`, or `type`.
- `New added`: returned mission data now includes `longDescription`, `imageUrl`, `guide`, `targetQuantity`, and `targetDays`.

### GET `/missions/:id` - logged-in user - Updated

- Requires `Authorization: Bearer <JWT token>`.
- Finds mission by mission id in PostgreSQL.
- Returns error if mission does not exist.
- `New added`: returned mission detail now includes `longDescription`, `imageUrl`, `guide`, `targetQuantity`, and `targetDays`.

### PATCH `/missions/:id` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Finds mission by id.
- Validates update payload.
- If title changes, regenerates slug.
- Checks updated time window does not overlap with another mission of the same type.
- `New added`: can update `longDescription`, `imageUrl`, `guide`, `targetQuantity`, and `targetDays`.
- Returns updated mission data.

### DELETE `/missions/:id` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Does not permanently delete the mission.
- Archives mission by setting `isActive = false` and `status = ARCHIVED`.
- Returns archived mission data.

### POST `/missions/:id/join` - STUDENT only - New added

- Requires JWT token and `STUDENT` role.
- Checks mission exists.
- Checks mission is accepting student action:
  - `isActive = true`
  - `status = ACTIVE`
  - current date is within `startAt` and `endAt`
- Prevents joining if the student already has an `ONGOING` or `PENDING_REVIEW` submission for that mission.
- Checks `submissionCap` against non-rejected submissions.
- Creates a `MissionSubmission` row with `status = ONGOING`.
- Returns created submission.

### POST `/missions/:id/submit` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Finds mission by id.
- Checks mission is active and inside the submission window.
- Validates `proofText`, `proofImageUrl`, `quantity`, and optional `uploadId`.
- Validates `uploadId` belongs to current student if provided.
- `Updated`: if student already joined, updates the existing `ONGOING` submission instead of always creating a new row.
- Prevents submission if an existing submission is already `PENDING_REVIEW`.
- Checks `submissionCap` when no ongoing submission exists.
- Sets status to `PENDING_REVIEW`, or `APPROVED` if mission `autoApprove = true`.
- Attaches uploaded proof file to the submission when `uploadId` is provided.
- `New added`: response converts private mission proof upload into temporary readable SAS URL when possible.
- `Updated`: when a submission becomes `APPROVED`, points are awarded only if that approved progress completes the mission target.
- `Updated`: auto-approved submissions with uploaded proof now still run mission-completion points and badge checks.
- Returns submission data.

### POST `/missions/:id/image` - ADMIN only - New added

- Requires JWT token and `ADMIN` role.
- Accepts `multipart/form-data` with form key `file`.
- Uses existing multer image upload middleware.
- Accepts JPEG, PNG, WEBP, max 5MB.
- Checks mission exists.
- Uploads the image to blob storage using purpose `MISSION_IMAGE`.
- Creates an `UploadedFile` row.
- Automatically updates the mission's `imageUrl`.
- Returns both updated mission and upload record.

### GET `/missions/:id/submissions` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Finds mission by id.
- Returns all submissions for that mission.
- `New added`: includes attached uploads.
- `New added`: mission proof image URLs are returned as temporary SAS URLs when upload metadata exists.

## Submissions

Admin/student mission submission management.

### GET `/submissions/me` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Retrieves submissions made by the current student.
- Can filter by `status` or `missionId` query parameters.
- Includes related mission summary: `id`, `title`, `slug`, `points`.
- Includes submission status such as `ONGOING`, `PENDING_REVIEW`, `APPROVED`, or `REJECTED`.
- `New added`: includes attached upload metadata.
- `New added`: returns temporary readable proof image URL when the proof is stored in private blob storage.
- Used by Android app to decide mission card status.

### GET `/submissions/:id` - logged-in user - Updated

- Requires JWT token.
- Finds submission by submission id.
- Admin can view any submission.
- Student can only view their own submission.
- Returns error if submission does not exist.
- Returns error if student tries to view another user's submission.
- Returns submission detail such as mission id, user id, proof text, proof image URL, quantity, status, review note, submitted date, and reviewed date.
- `New added`: includes attached uploads and temporary proof image SAS URL when upload metadata exists.

### GET `/submissions` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Retrieves all submissions.
- Can filter by `status`, `missionId`, or `userId`.
- Includes user and mission summary data.
- `New added`: includes attached uploads and temporary proof image SAS URL when available.

### PATCH `/submissions/:id/review` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Validates review payload.
- Allows admin to set status to `APPROVED` or `REJECTED`.
- Saves `reviewNote`, `reviewedById`, and `reviewedAt`.
- `Updated`: if status becomes `APPROVED`, checks whether the mission is now completed, then creates one mission-completion points event if needed and evaluates badges.

## Content

Educational content CRUD.

### GET `/content` - logged-in user - Updated

- Requires JWT token.
- Retrieves content records from PostgreSQL.
- For `STUDENT`, returns only `PUBLISHED` content.
- For `ADMIN`, can filter by status.
- Can filter by fixed tag using `?tag=plastic`, `?tag=paper`, etc.
- `New added`: fixed backend tag validation.
- `New added`: returned data supports `summary`, `imageUrl`, `estimatedReadMinutes`, and `contentBlocks`.
- Used by Android content browsing screen.

### GET `/content/:id` - logged-in user - Updated

- Requires JWT token.
- Finds content by id.
- Student can only retrieve `PUBLISHED` content.
- Admin can retrieve draft/published/archived content.
- Returns error if content does not exist or student requests non-published content.
- `New added`: returned detail supports `summary`, `imageUrl`, `estimatedReadMinutes`, and structured `contentBlocks`.

### POST `/content` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Validates title, body, tags, status, and optional article fields.
- `New added`: tags must use fixed backend values:
  - `plastic`
  - `paper`
  - `ewaste`
  - `food-waste`
  - `sorting`
  - `cleanliness`
  - `safety`
  - `general`
- `New added`: accepts `summary`.
- `New added`: accepts `imageUrl`.
- `New added`: accepts `estimatedReadMinutes`.
- `New added`: accepts `contentBlocks`.
- Creates new `Content` row.
- Generates slug from title.
- Returns created content data.

### PUT `/content/:id` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Finds content by id.
- Validates updated fields.
- Updates content data in PostgreSQL.
- If title changes, regenerates slug.
- Creates a `ContentRevision` record before updating.
- `New added`: revision now stores `summary`, `imageUrl`, `estimatedReadMinutes`, and `contentBlocks`.
- Returns updated content data.

### DELETE `/content/:id` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Finds content by id.
- Archives content by setting `status = ARCHIVED`.
- Does not permanently remove the row.
- Returns archived content data.

### GET `/content/:id/revisions` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Finds content by id.
- Retrieves previous content versions from `ContentRevision`.
- `Updated`: revisions now include title, body, summary, imageUrl, estimatedReadMinutes, contentBlocks, tags, and status.

## Quizzes

Quiz creation, quiz questions, and quiz attempts.

### POST `/quizzes` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Creates a quiz for one content/article.
- Requires `contentId` and `title`.
- Optional `passingScore`.
- `Updated`: `passingScore` now means number of correct answers needed, not percentage.
- Example: 5 questions and passing score 4 means 4 correct answers are needed.

### GET `/quizzes` - logged-in user - Original

- Requires JWT token.
- Returns quiz list.
- Can filter by `contentId`.
- Android uses this to find quiz for selected article.
- Does not return quiz question answers.

### GET `/quizzes/:id` - logged-in user - Updated

- Requires JWT token.
- Returns one quiz by quiz id.
- For `STUDENT`, returns questions without `correctAnswer`.
- For `ADMIN`, returns questions including `correctAnswer`.
- Checks related content is `PUBLISHED` for students.
- `New added`: students cannot retrieve/attempt a quiz unless it has 5 to 10 questions.
- `New added`: checks `passingScore` fits the question count.

### PATCH `/quizzes/:id` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Updates quiz title or passing score.
- `Updated`: passing score must fit the number of questions.
- Example: if quiz has 5 questions, passing score cannot be above 5.

### POST `/quizzes/:id/questions` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Adds a question to a quiz.
- Question must have options and one correct answer.
- Correct answer must match one of the options.
- `Updated`: each question is always worth 1 point.
- `New added`: quiz cannot exceed 10 questions.

### PATCH `/quizzes/:id/questions/:questionId` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Updates a quiz question.
- Can update question text, options, or correct answer.
- Correct answer must still match one of the options.
- `Updated`: points remain fixed at 1.

### DELETE `/quizzes/:id/questions/:questionId` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Deletes a question from a quiz.
- Question must belong to that quiz.

### POST `/quizzes/:id/attempts` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Student submits final quiz answers.
- Attempt is only stored after this endpoint is called.
- `Updated`: score is correct-answer count, not percentage.
- `Updated`: accuracy is percentage.
- `Updated`: passed means `score >= quiz.passingScore`.
- `New added`: accepts optional `timeSpentSeconds`.
- Updates `LearningProgress`:
  - increments `quizAttemptsCount`
  - updates `latestScore`
  - updates `bestScore`
  - increments `passedQuizCount` if passed
  - marks `completed = true` if passed
  - increments `completionCount` only when content becomes completed for the first time
- `Updated`: returns full payload with `attempt`, `result`, and `review`.
- `New added`: review includes correct answer, selected answer, skipped status, and correctness per question.
- Evaluates badges after attempt.

### GET `/quizzes/:id/attempts/me` - STUDENT only - Original

- Requires JWT token and `STUDENT` role.
- Returns logged-in student's attempts for that quiz.
- Ordered newest first.
- Useful for future attempt history.

### GET `/quizzes/:id/attempts` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Returns all student attempts for that quiz.
- Includes student basic info.
- Also returns attempt stats:
  - attempt count
  - pass count
  - pass rate
  - average score

## Progress

Student learning progress.

### GET `/progress/me` - STUDENT only - Original

- Requires JWT token and `STUDENT` role.
- Returns all learning progress records for logged-in student.
- Shows progress across all content/articles.

### GET `/progress/content/:contentId/me` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Returns logged-in student's progress for one content/article.
- Android uses this to show best score before quiz.
- If no progress exists, returns default empty progress.
- `New added`: default response includes `bestScore`.

### PATCH `/progress/content/:contentId/complete` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Manually marks content/article as completed.
- Sets `completed = true`.
- Increments `completionCount`.
- Evaluates badges after completion.
- This is separate from quiz completion.

### GET `/progress/content/:contentId` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Returns all students' progress for one content/article.
- Useful for admin analytics later.

## Badges

Badge creation, admin badge management, and user badge progress.

### POST `/badges` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Creates a badge.
- Validates name, description, tier, criteria type, and criteria value.
- Generates slug from badge name.
- Prevents duplicate badge slug.
- `Updated`: accepted criteria are now:
  - `MISSIONS_COMPLETED`
  - `QUIZZES_PASSED`
  - `CONTENT_COMPLETED`
  - `APPROVED_SUBMISSIONS`
- `New added`: `APPROVED_SUBMISSIONS` replaces the old placeholder `ACTIVITY_METRIC`.

### GET `/badges` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Returns all badges ordered by created date.

### GET `/badges/progress` - logged-in user - Updated

- Requires JWT token.
- Evaluates badges for current user.
- Issues new `BadgeAward` rows when the user meets badge criteria.
- Returns `earned` and `locked` badge progress.
- Each badge progress item includes:
  - badge id, slug, name, description, tier
  - criteria type and criteria value
  - current progress
  - progress percentage
  - status
  - awarded date when earned
- `Updated`: criteria logic now means:
  - `MISSIONS_COMPLETED`: number of missions fully completed by approved progress
  - `QUIZZES_PASSED`: count of passed quiz attempts
  - `CONTENT_COMPLETED`: count of completed content items; one content item only counts once per user
  - `APPROVED_SUBMISSIONS`: count of approved mission submission rows

### GET `/badges/:id` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Finds badge by id.
- Returns badge data or error if not found.

### PATCH `/badges/:id` - ADMIN only - Updated

- Requires JWT token and `ADMIN` role.
- Updates badge data.
- If name changes, regenerates slug and checks duplicate slug.
- `Updated`: criteria validation uses the new accepted criteria list and no longer accepts `ACTIVITY_METRIC`.

### DELETE `/badges/:id` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Does not delete badge row permanently.
- Deactivates badge by setting `isActive = false`.

### GET `/badges/:id/awards` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Returns users who earned a selected badge.
- Includes basic user info.

## Points

Points event history.

### GET `/points/me` - STUDENT only - Original

- Requires JWT token and `STUDENT` role.
- Returns current student's points events and total point sum.
- `Updated`: points come from `PointsEvent` rows.
- `New added`: mission points now use `MISSION_COMPLETED` events, created only when approved progress completes the mission.
- `Updated`: old/deprecated `MISSION_APPROVED` rows are no longer counted in the active student point total.
- `Updated`: each student can only have one active `MISSION_COMPLETED` points event per mission.
- `Updated`: for `MISSION_COMPLETED`, `submissionId` refers to the final approved submission that made the mission reach its target.
- `New added`: PostgreSQL trigger creates missing `MISSION_COMPLETED` point events when a `MissionSubmission` row becomes `APPROVED`.
- Completion rules:
  - `QUANTITY_BASED`: approved submitted quantity reaches `Mission.targetQuantity`.
  - `STREAK_BASED`: approved submission count reaches `Mission.targetDays`.
  - `TIME_LIMITED`: at least one approved submission.

### GET `/points` - ADMIN only - Original

- Requires JWT token and `ADMIN` role.
- Returns all points events.
- Can filter by `status`, `userId`, or `missionId`.
- Includes user and mission summary info.
- `Updated`: mission completion point events use `eventType = MISSION_COMPLETED`.
- `Updated`: default listing now focuses on active point events instead of old approval-based events.

## Uploads

File upload support using Azure Blob Storage or Azurite.

### POST `/uploads/mission-proof` - STUDENT only - Updated

- Requires JWT token and `STUDENT` role.
- Accepts image upload using `multipart/form-data`.
- Form-data key is `file`.
- Uses multer memory storage.
- Accepts JPEG, PNG, WEBP, max 5MB.
- Uploads file buffer to Azure Blob Storage or local Azurite.
- Creates unique blob name using user id, timestamp, and original file name.
- Stores actual file in blob storage.
- Stores upload metadata in PostgreSQL `UploadedFile` table.
- Metadata includes user id, container name, blob name, file URL, mime type, file size, and purpose.
- Purpose is `MISSION_PROOF`.
- Returns uploaded file record with upload id and file URL.
- Upload id can later be used in `POST /missions/:id/submit`.
- `New added`: shared upload helper now also supports other upload purposes.

### POST `/uploads/content-image` - ADMIN only - New added

- Requires JWT token and `ADMIN` role.
- Uploads an education/content article image.
- Form-data key is `file`.
- Accepts JPEG, PNG, WEBP, max 5MB.
- Stores file in blob storage.
- Creates `UploadedFile` row with `purpose = CONTENT_IMAGE`.
- Returns `data.upload.fileUrl`.
- Admin can use returned URL in `Content.imageUrl`.

### GET `/uploads/mine` - STUDENT only - Original

- Requires JWT token and `STUDENT` role.
- Retrieves uploaded files created by current student.
- Reads upload metadata from `UploadedFile` table.
- Returns uploads ordered by `createdAt` descending.
- Includes upload id, file URL, mime type, file size, purpose, and related submission id if attached.

### GET `/uploads/:id` - logged-in user - Original

- Requires JWT token.
- Finds upload by upload id.
- Admin can view any upload.
- Student can only view their own upload.
- Returns error if upload does not exist.
- Returns upload metadata from `UploadedFile`.

## Database And Prisma Changes

### `prisma/schema.prisma` - Updated

- `New added`: `SubmissionStatus.ONGOING`.
- `New added`: `PointsEventType.MISSION_COMPLETED`.
- `Updated`: `PointsEvent.submissionId` is no longer unique by itself.
- `Updated`: added code comment that `submissionId` is the final/completing approved submission for `MISSION_COMPLETED`.
- `New added`: `BadgeCriteriaType.APPROVED_SUBMISSIONS`.
- `Updated`: `BadgeCriteriaType.ACTIVITY_METRIC` kept only as deprecated compatibility value.
- `New added`: `UploadPurpose.CONTENT_IMAGE`.
- `New added`: `UploadPurpose.MISSION_IMAGE`.
- `New added`: `Mission.longDescription String?`.
- `New added`: `Mission.imageUrl String?`.
- `New added`: `Mission.guide Json?`.
- `New added`: `Mission.targetQuantity Int?`.
- `New added`: `Mission.targetDays Int?`.
- `New added`: `Content.summary String?`.
- `New added`: `Content.imageUrl String?`.
- `New added`: `Content.estimatedReadMinutes Int @default(5)`.
- `New added`: `Content.contentBlocks Json?`.
- `New added`: `ContentRevision.summary String?`.
- `New added`: `ContentRevision.imageUrl String?`.
- `New added`: `ContentRevision.estimatedReadMinutes Int?`.
- `New added`: `ContentRevision.contentBlocks Json?`.
- `Updated`: `Quiz.passingScore` default changed from `70` to `4`.
- `New added`: `QuizAttempt.timeSpentSeconds Int?`.
- `New added`: `LearningProgress.bestScore Int?`.

### Prisma migrations - New added

- `20260724063030_add_mission_details_fields`: adds mission `guide`, `imageUrl`, `longDescription`.
- `20260726000000_add_mission_progress_targets`: adds mission `targetQuantity`, `targetDays`.
- `20260726010000_add_ongoing_submission_status`: adds `ONGOING` submission status.
- `20260728000000_add_content_detail_fields`: adds content detail fields and temporary `category`.
- `20260728010000_remove_content_category`: removes unused `category`.
- `20260728020000_add_learning_progress_best_score`: adds `bestScore`, normalizes question points to 1, adds points check constraint.
- `20260728030000_add_quiz_attempt_time_spent`: adds `QuizAttempt.timeSpentSeconds`.
- `20260728040000_use_question_count_quiz_scores`: changes quiz scoring data from percentage style to question-count style and backfills progress.
- `20260728050000_add_content_image_upload_purpose`: adds `CONTENT_IMAGE`.
- `20260728060000_add_mission_image_upload_purpose`: adds `MISSION_IMAGE`.
- `20260729021000_add_approved_submissions_badge_criteria`: adds `APPROVED_SUBMISSIONS`.
- `20260730090000_add_mission_completed_points_event_type`: adds `MISSION_COMPLETED`.
- `20260730100000_add_mission_completed_unique_index`: removes unique constraint from `PointsEvent.submissionId` and adds a PostgreSQL partial unique index so only one `MISSION_COMPLETED` event can exist per user and mission.
- `20260730110000_add_mission_completion_points_trigger`: adds database trigger/function to create `MISSION_COMPLETED` point events whenever an approved submission completes a mission.

## Backend File Changes

### `.env.example` / `.env.docker` - Updated

- `New added`: `AZURE_STORAGE_CONTAINER_CONTENT_IMAGES`.
- `New added`: `AZURE_STORAGE_CONTAINER_MISSION_IMAGES`.
- Existing mission proof container remains `AZURE_STORAGE_CONTAINER_MISSION_PROOFS`.

### `src/utils/config.js` - Updated

- `New added`: reads `AZURE_STORAGE_CONTAINER_CONTENT_IMAGES`, default `content-images`.
- `New added`: reads `AZURE_STORAGE_CONTAINER_MISSION_IMAGES`, default `mission-images`.

### `src/routes/mission.routes.js` - Updated

- `New added`: `POST /missions/:id/join`.
- `New added`: `POST /missions/:id/image`.

### `src/controllers/mission.controller.js` - Updated

- `New added`: `joinMissionHandler`.
- `New added`: `uploadMissionImageHandler`.
- Mission image handler checks file exists, calls mission service, and returns updated mission plus upload record.

### `src/services/mission.service.js` - Updated

- `New added`: saves `longDescription`, `imageUrl`, `guide`, `targetQuantity`, `targetDays` during mission creation.
- `New added`: `uploadMissionImage()` checks mission exists, uploads file through upload service, updates mission `imageUrl`, and returns mission plus upload.

### `src/repositories/mission.repository.js` - Updated

- `New added`: explicit mission field selection.
- `New added`: `GET /missions` and `GET /missions/:id` return mission detail fields.
- `Updated`: student default mission list includes active missions and archived missions.

### `src/validators/mission.validator.js` - Updated

- `New added`: validates `longDescription`.
- `New added`: validates `imageUrl`.
- `New added`: validates structured `guide`.
- `New added`: validates `targetQuantity`.
- `New added`: validates `targetDays`.

### `src/services/submission.service.js` - Updated

- `New added`: `joinMission()`.
- `Updated`: `submitMission()` can finalize an existing `ONGOING` submission.
- `New added`: mission proof upload responses can use temporary SAS read URLs.
- `Updated`: approval side effects now check mission completion before awarding points.
- `Updated`: creates a points event only once per user and mission after the mission target is completed.
- `Updated`: auto-approved submissions with `uploadId` no longer skip completion points/badge checks.

### `src/repositories/submission.repository.js` - Updated

- `New added`: includes attached uploads in submission queries.
- `New added`: `findUserSubmissionForMissionByStatuses`.
- `New added`: `getApprovedMissionProgressForUser`, used to calculate approved count and approved quantity for mission completion.
- Existing `findActiveUserSubmissionForMission` remains available.

### `src/services/points.service.js` - Updated

- `New added`: `createPointsEventForMissionCompletion()`.
- `Updated`: mission points are no longer created for every approved submission.
- `Updated`: prevents duplicate mission-completion point events for the same user and mission.
- `Updated`: stores the completing/final approved submission id directly on the mission-completion point event.
- `Updated`: duplicate protection is backed by database partial unique index, not only JavaScript checks.
- `Updated`: if the database trigger already created a pending/failed completion point event, the service can still dispatch it when `POINTS_LEDGER_URL` is configured.

### `src/repositories/points.repository.js` - Updated

- `New added`: `findPointsEventByUserAndMission()`, used to detect whether mission-completion points were already awarded.
- `Updated`: `GET /points/me` totals and point event list now use active point event types, currently `MISSION_COMPLETED`.
- `Updated`: old `MISSION_APPROVED` rows are kept for compatibility/history but are no longer counted as active mission points.
- `Updated`: removed submission-id-based lookup from normal mission-completion point creation because `submissionId` is now a reference, not the uniqueness rule.

### Existing database reset note

- The temporary mission-completion points backfill script was removed.
- Existing old local database rows should be cleared by resetting the Docker volume before reseeding.
- Recommended local reset command: `docker compose down -v`, then rebuild and seed again.

### `src/routes/upload.routes.js` - Updated

- `New added`: `POST /uploads/content-image`.

### `src/controllers/upload.controller.js` - Updated

- `New added`: `uploadContentImageHandler`.
- Checks file exists and passes file metadata to upload service.

### `src/services/upload.service.js` - Updated

- `Updated`: refactored common blob upload logic into shared `uploadToBlobStorage`.
- Existing mission proof upload still works.
- `New added`: `uploadContentImage`.
- `New added`: `uploadMissionImage`.
- `New added`: `createMissionProofReadUrl`, which generates temporary read-only SAS URLs for private mission proof blobs. Default expiry is 60 minutes.

### `src/validators/content.validator.js` - Updated

- `New added`: supports `summary`.
- `New added`: supports `imageUrl`.
- `New added`: supports `estimatedReadMinutes`.
- `New added`: supports `contentBlocks`.
- `Updated`: tags are fixed to allowed backend values.

### `src/services/content.service.js` - Updated

- `New added`: content create/update supports `summary`, `imageUrl`, `estimatedReadMinutes`, and `contentBlocks`.
- `Updated`: tag filtering validates fixed tags.

### `src/repositories/content.repository.js` - Updated

- `New added`: revision history stores `summary`, `imageUrl`, `estimatedReadMinutes`, and `contentBlocks`.

### `src/validators/quiz.validator.js` - Updated

- `Updated`: `passingScore` validation changed from percentage-style 0-100 to question-count style 1-10.
- `New added`: submit attempt accepts optional `timeSpentSeconds`.
- `Updated`: question points can only be 1 if sent.

### `src/services/quiz.service.js` - Updated

- `New added`: minimum quiz questions = 5.
- `New added`: maximum quiz questions = 10.
- `Updated`: students cannot view/submit quiz unless it has 5 to 10 questions.
- `Updated`: score is number of correct answers.
- `Updated`: accuracy is percentage.
- `Updated`: every question is worth 1 point.
- `Updated`: passing score must fit question count.
- `New added`: attempt result includes `timeSpentSeconds`, `bestScore`, `previousBestScore`, and `isNewBestScore`.
- `New added`: returns review questions with correct answer, selected answer, skipped status, and correctness.
- `Updated`: evaluates badges after quiz attempt.

### `src/controllers/quiz.controller.js` - Updated

- `Updated`: submit attempt returns full submit result payload instead of only `{ attempt }`.

### `src/services/progress.service.js` - Updated

- `New added`: tracks `bestScore`.
- `New added`: tracks whether latest attempt is a new best score.
- `Updated`: passed quiz marks content as completed.
- `Updated`: `completionCount` increments only when content is completed for the first time through a passed quiz.
- `Updated`: manual content complete still increments `completionCount`.
- `Updated`: badge evaluation runs after content complete.

### `src/repositories/badge.repository.js` - Updated

- `New added`: `countCompletedMissions(userId)`.
- `Updated`: mission completion now means:
  - `QUANTITY_BASED`: approved submitted quantity reaches `Mission.targetQuantity`.
  - `STREAK_BASED`: approved submission count reaches `Mission.targetDays`.
  - `TIME_LIMITED`: at least one approved submission.
- `New added`: keeps `countApprovedMissionSubmissions(userId)` for `APPROVED_SUBMISSIONS`.
- `Updated`: removed old `sumActivityMetric()` placeholder logic.

### `src/services/badge.service.js` - Updated

- `Updated`: criteria mapping:
  - `MISSIONS_COMPLETED` -> `countCompletedMissions()`
  - `QUIZZES_PASSED` -> `countPassedQuizAttempts()`
  - `CONTENT_COMPLETED` -> `countCompletedLearningProgress()`
  - `APPROVED_SUBMISSIONS` -> `countApprovedMissionSubmissions()`

### `src/validators/badge.validator.js` - Updated

- `Updated`: admin badge create/update accepts:
  - `MISSIONS_COMPLETED`
  - `QUIZZES_PASSED`
  - `CONTENT_COMPLETED`
  - `APPROVED_SUBMISSIONS`
- `Updated`: no longer accepts `ACTIVITY_METRIC`.

### `scripts/uploadMissionImages.js` - New added

- Helper script to upload local mission images to blob storage and update mission `imageUrl` values.

### `README.md` - Updated

- Updated environment/config notes for new upload containers.

### `postman_environment_local.json` / `postman_environment_shared.json` - Updated

- `Updated`: badge criteria example changed from `ACTIVITY_METRIC` to `APPROVED_SUBMISSIONS`.

## Seed Data Changes

### `prisma/seed.js` - Updated

- `New added`: mission seed data includes:
  - `longDescription`
  - `imageUrl`
  - `guide`
  - `targetQuantity`
  - `targetDays`
- `New added`: content seed data includes:
  - `summary`
  - `imageUrl`
  - `estimatedReadMinutes`
  - `contentBlocks`
  - fixed `tags`
- `Updated`: seeded quizzes expanded from 3 questions to 5 questions.
- `New added`: article-specific quiz questions.
- `Updated`: quiz `passingScore` is calculated with `Math.ceil(questionCount * 0.7)`.
- With 5 questions, passing score becomes 4.
- `Updated`: badge seed data now seeds 12 badges:
  - 3 for `MISSIONS_COMPLETED`
  - 3 for `QUIZZES_PASSED`
  - 3 for `CONTENT_COMPLETED`
  - 3 for `APPROVED_SUBMISSIONS`
- `New added`: badge names/descriptions clarify the trigger:
  - mission badges = finished mission progress
  - quiz badges = passed quiz attempts
  - content badges = completed content item, one per content
  - approval badges = approved mission submission count
- `Updated`: seed demo submission `SUB001` now uses 20 bottles so it actually completes `MIS001`.
- `Updated`: seed demo point event `PEV001` now uses `MISSION_COMPLETED` instead of `MISSION_APPROVED`.

## Important Clarifications

- `ACTIVITY_METRIC` is no longer used by seed, validator, or badge service logic.
- It remains in `schema.prisma` only to avoid breaking existing local database rows before reseeding/migrating away.
- The badge endpoints themselves already existed in the original backend. The new work changed badge criteria logic and seed data, not the route list.
- The only new route URLs compared to the original backend are:
  - `POST /missions/:id/join`
  - `POST /missions/:id/image`
  - `POST /uploads/content-image`

## Latest Point Event Debugging Fix

### Problem found

- Point events were not consistently created when a mission was truly completed.
- The old behavior was too submission-approval based.
- That caused problems for mission types where completion depends on progress:
  - `QUANTITY_BASED`: should complete only when approved quantity reaches `targetQuantity`.
  - `STREAK_BASED`: should complete only when approved submission count reaches `targetDays`.
  - `TIME_LIMITED`: should complete once one submission is approved.
- Directly changing submission status in the database could also skip JavaScript service logic if points were only handled in `submission.service.js`.

### Fix summary

- `New added`: mission-completion point creation is now backed by a PostgreSQL trigger.
- The trigger runs when `MissionSubmission.status` becomes `APPROVED`.
- The trigger checks the mission type and only creates a `MISSION_COMPLETED` point event when the mission target is fully reached.
- `submissionId` on `PointEvent` now means the final approved submission that completed the mission.
- Duplicate point events are prevented by a partial unique index: one active `MISSION_COMPLETED` event per user and mission.
- JavaScript service logic still supports normal API approval flow, but the database trigger protects direct database updates too.

### Backend files involved

- `prisma/schema.prisma`
  - Added/uses `PointsEventType.MISSION_COMPLETED`.
  - Changed `PointEvent.submissionId` from the uniqueness rule into a reference to the final/completing submission.
  - Added comment explaining `submissionId` meaning.

- `prisma/migrations/20260730090000_add_mission_completed_points_event_type/migration.sql`
  - Adds `MISSION_COMPLETED` point event type.

- `prisma/migrations/20260730100000_add_mission_completed_unique_index/migration.sql`
  - Removes the old unique constraint on `submissionId`.
  - Adds partial unique index to prevent duplicate `MISSION_COMPLETED` points per user and mission.

- `prisma/migrations/20260730110000_add_mission_completion_points_trigger/migration.sql`
  - Adds PostgreSQL function and trigger for mission-completion point creation.

- `src/services/submission.service.js`
  - Normal API approval path checks mission completion.
  - Auto-approved submissions with uploaded proof now still run completion points/badge checks.

- `src/services/points.service.js`
  - Creates mission-completion points.
  - Avoids duplicate completion points.
  - Dispatches point events to external ledger if `POINTS_LEDGER_URL` is configured.

- `src/repositories/points.repository.js`
  - Finds existing mission-completion point event by user and mission.
  - `GET /points/me` now counts active `MISSION_COMPLETED` rows instead of old approval-based mission rows.

- `src/repositories/submission.repository.js`
  - Provides approved mission progress data for quantity/streak/time-limited completion checks.

- `prisma/seed.js`
  - Demo point event uses `MISSION_COMPLETED`.
  - Demo submission quantity was adjusted so seeded sample data can represent a completed quantity mission.

### Local database note

- Old local Docker volume data can still contain older point-event rows.
- For clean testing, reset Docker volume and reseed:
  - `docker compose down -v`
  - `docker compose up --build`
  - run Prisma migrate/seed as needed based on the setup.
