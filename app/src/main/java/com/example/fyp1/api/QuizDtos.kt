package com.example.fyp1.api

data class QuizListData(
    val quizzes: List<BackendQuiz>
)

data class QuizData(
    val quiz: BackendQuiz
)

data class QuizAttemptData(
    val attempt: BackendQuizAttempt,
    val result: BackendQuizResult,
    val review: BackendQuizReview
)

data class ProgressData(
    val progress: BackendLearningProgress
)

data class SubmitQuizAttemptRequest(
    val answers: Map<String, String>,
    val timeSpentSeconds: Int? = null
)

data class BackendQuiz(
    val id: String,
    val contentId: String,
    val slug: String,
    val title: String,
    val passingScore: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val questions: List<BackendQuizQuestion> = emptyList()
)

data class BackendQuizQuestion(
    val id: String,
    val quizId: String? = null,
    val code: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String? = null,
    val points: Int? = null
)

data class BackendQuizAttempt(
    val id: String,
    val quizId: String,
    val userId: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val passed: Boolean,
    val answers: Map<String, String> = emptyMap(),
    val timeSpentSeconds: Int? = null,
    val attemptedAt: String? = null
)

data class BackendQuizResult(
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Int,
    val passed: Boolean,
    val timeSpentSeconds: Int? = null,
    val bestScore: Int? = null,
    val previousBestScore: Int? = null,
    val isNewBestScore: Boolean = false
)

data class BackendQuizReview(
    val questions: List<BackendQuizReviewQuestion>
)

data class BackendQuizReviewQuestion(
    val id: String,
    val code: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val selectedAnswer: String? = null,
    val isCorrect: Boolean,
    val wasSkipped: Boolean
)

data class BackendLearningProgress(
    val id: String? = null,
    val userId: String? = null,
    val contentId: String,
    val completed: Boolean = false,
    val completionCount: Int = 0,
    val quizAttemptsCount: Int = 0,
    val passedQuizCount: Int = 0,
    val latestScore: Int? = null,
    val bestScore: Int? = null,
    val lastActivityAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CompletedQuizSession(
    val contentId: String,
    val contentTitle: String,
    val quiz: BackendQuiz,
    val result: BackendQuizResult,
    val review: BackendQuizReview
)
