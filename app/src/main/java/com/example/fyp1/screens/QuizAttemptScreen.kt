package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendContent
import com.example.fyp1.api.BackendLearningProgress
import com.example.fyp1.api.BackendQuiz
import com.example.fyp1.api.BackendQuizQuestion
import com.example.fyp1.api.ContentRepository
import com.example.fyp1.api.ContentSelectionCache
import com.example.fyp1.api.QuizAttemptData
import com.example.fyp1.api.QuizRepository
import com.example.fyp1.api.QuizSessionCache
import com.example.fyp1.api.CompletedQuizSession
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun QuizAttemptScreen(navController: NavController, contentId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val contentRepository = remember { ContentRepository(context) }
    val quizRepository = remember { QuizRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var content by remember(contentId) {
        mutableStateOf(ContentSelectionCache.selectedContent?.takeIf { it.id == contentId })
    }
    var quiz by remember(contentId) { mutableStateOf<BackendQuiz?>(null) }
    var progress by remember(contentId) { mutableStateOf<BackendLearningProgress?>(null) }
    var completedAttempt by remember(contentId) { mutableStateOf<QuizAttemptData?>(null) }
    var selectedAnswers by remember(contentId) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var currentIndex by remember(contentId) { mutableIntStateOf(0) }
    var isLoading by remember(contentId) { mutableStateOf(true) }
    var isSubmitting by remember(contentId) { mutableStateOf(false) }
    var errorMessage by remember(contentId) { mutableStateOf<String?>(null) }
    var actionMessage by remember(contentId) { mutableStateOf<String?>(null) }
    var startedAtMillis by remember(contentId) { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(contentId) {
        isLoading = true
        errorMessage = null
        actionMessage = null
        completedAttempt = null
        selectedAnswers = emptyMap()
        currentIndex = 0
        startedAtMillis = System.currentTimeMillis()

        if (content == null) {
            when (val result = contentRepository.getContentById(contentId)) {
                is AuthResult.Success -> content = result.value
                is AuthResult.Error -> errorMessage = result.message
            }
        }

        when (val progressResult = quizRepository.getMyProgressForContent(contentId)) {
            is AuthResult.Success -> progress = progressResult.value
            is AuthResult.Error -> progress = null
        }

        when (val quizResult = quizRepository.getQuizForContent(contentId)) {
            is AuthResult.Success -> quiz = quizResult.value
            is AuthResult.Error -> errorMessage = quizResult.message
        }

        isLoading = false
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(QuizBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 18.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                QuizTopBar(
                    title = if (completedAttempt == null) "Quiz" else "Quiz Results",
                    onBack = {
                        if (completedAttempt == null) {
                            navController.popBackStack()
                        } else {
                            navigateBackToContent(navController, contentId, content)
                        }
                    }
                )
            }

            if (isLoading) {
                item { QuizLoadingMessage() }
                return@LazyColumn
            }

            val loadedQuiz = quiz
            if (errorMessage != null || loadedQuiz == null) {
                item {
                    QuizInfoCard(
                        message = errorMessage ?: "Quiz is not available yet."
                    )
                }
                return@LazyColumn
            }

            val result = completedAttempt
            if (result != null) {
                item {
                    QuizProgressHeader(
                        currentQuestion = loadedQuiz.questions.size.coerceAtLeast(1),
                        totalQuestions = loadedQuiz.questions.size.coerceAtLeast(1),
                        completedQuestions = loadedQuiz.questions.size.coerceAtLeast(1)
                    )
                }
                item {
                    QuizCompletedCard(
                        content = content,
                        quiz = loadedQuiz,
                        result = result,
                        onReviewAnswers = {
                            QuizSessionCache.completedSession = CompletedQuizSession(
                                contentId = contentId,
                                contentTitle = content?.title ?: loadedQuiz.title,
                                quiz = loadedQuiz,
                                result = result.result,
                                review = result.review
                            )
                            navController.navigate("quiz_review")
                        },
                        onBackToLearn = { navigateBackToContent(navController, contentId, content) }
                    )
                }
                return@LazyColumn
            }

            val questions = loadedQuiz.questions
            if (questions.isEmpty()) {
                item { QuizInfoCard(message = "This quiz does not have questions yet.") }
                return@LazyColumn
            }

            val safeIndex = currentIndex.coerceIn(0, questions.lastIndex)
            val question = questions[safeIndex]

            item {
                QuizIntroSection(
                    content = content,
                    quiz = loadedQuiz,
                    progress = progress,
                    totalQuestions = questions.size
                )
            }
            item {
                QuizProgressHeader(
                    currentQuestion = safeIndex + 1,
                    totalQuestions = questions.size,
                    completedQuestions = safeIndex
                )
            }
            item {
                QuizQuestionCard(
                    question = question,
                    selectedAnswer = selectedAnswers[question.code],
                    onSelectAnswer = { selected ->
                        selectedAnswers = selectedAnswers + (question.code to selected)
                        actionMessage = null
                    }
                )
            }
            actionMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = QuizError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                QuizActionSection(
                    isLastQuestion = safeIndex == questions.lastIndex,
                    canGoPrevious = safeIndex > 0,
                    isSubmitting = isSubmitting,
                    onSubmitOrNext = {
                        val selected = selectedAnswers[question.code]
                        if (selected.isNullOrBlank()) {
                            actionMessage = "Choose an answer or skip this question."
                        } else if (safeIndex == questions.lastIndex) {
                            isSubmitting = true
                            actionMessage = null
                            coroutineScope.launch {
                                val timeSpentSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L)
                                    .toInt()
                                    .coerceAtLeast(1)
                                when (val submitResult = quizRepository.submitAttempt(
                                    quizId = loadedQuiz.id,
                                    answers = selectedAnswers,
                                    timeSpentSeconds = timeSpentSeconds
                                )) {
                                    is AuthResult.Success -> {
                                        completedAttempt = submitResult.value
                                        progress = progress?.copy(bestScore = submitResult.value.result.bestScore)
                                        QuizSessionCache.completedSession = CompletedQuizSession(
                                            contentId = contentId,
                                            contentTitle = content?.title ?: loadedQuiz.title,
                                            quiz = loadedQuiz,
                                            result = submitResult.value.result,
                                            review = submitResult.value.review
                                        )
                                    }
                                    is AuthResult.Error -> actionMessage = submitResult.message
                                }
                                isSubmitting = false
                            }
                        } else {
                            currentIndex = safeIndex + 1
                            actionMessage = null
                        }
                    },
                    onPrevious = {
                        currentIndex = (safeIndex - 1).coerceAtLeast(0)
                        actionMessage = null
                    },
                    onSkip = {
                        selectedAnswers = selectedAnswers - question.code
                        actionMessage = null
                        if (safeIndex == questions.lastIndex) {
                            isSubmitting = true
                            coroutineScope.launch {
                                val timeSpentSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L)
                                    .toInt()
                                    .coerceAtLeast(1)
                                when (val submitResult = quizRepository.submitAttempt(
                                    quizId = loadedQuiz.id,
                                    answers = selectedAnswers - question.code,
                                    timeSpentSeconds = timeSpentSeconds
                                )) {
                                    is AuthResult.Success -> {
                                        completedAttempt = submitResult.value
                                        progress = progress?.copy(bestScore = submitResult.value.result.bestScore)
                                        QuizSessionCache.completedSession = CompletedQuizSession(
                                            contentId = contentId,
                                            contentTitle = content?.title ?: loadedQuiz.title,
                                            quiz = loadedQuiz,
                                            result = submitResult.value.result,
                                            review = submitResult.value.review
                                        )
                                    }
                                    is AuthResult.Error -> actionMessage = submitResult.message
                                }
                                isSubmitting = false
                            }
                        } else {
                            currentIndex = safeIndex + 1
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuizTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = QuizPrimary)
        }
        Text(
            text = title,
            color = QuizPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun QuizIntroSection(
    content: BackendContent?,
    quiz: BackendQuiz,
    progress: BackendLearningProgress?,
    totalQuestions: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Quiz", color = QuizText, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
        QuizTagRow(content?.tags?.takeIf { it.isNotEmpty() } ?: listOf("general"))
        Surface(shape = CircleShape, color = Color(0xFFE7F6EA), border = BorderStroke(1.dp, Color(0x3330A152))) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = QuizPrimary, modifier = Modifier.size(15.dp))
                Text(
                    text = bestScoreLabel(progress?.bestScore, totalQuestions),
                    color = QuizPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Text(
            text = content?.summary?.takeIf { it.isNotBlank() }
                ?: "Test your knowledge from this learning article.",
            color = QuizMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        Text(
            text = quiz.title,
            color = QuizMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuizTagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(shape = CircleShape, color = Color(0xFFDDF8E2)) {
                Text(
                    text = contentTagLabel(tag).uppercase(),
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    color = QuizPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun QuizProgressHeader(currentQuestion: Int, totalQuestions: Int, completedQuestions: Int) {
    val safeTotal = totalQuestions.coerceAtLeast(1)
    val fraction = (completedQuestions.coerceIn(0, safeTotal)).toFloat() / safeTotal.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Question $currentQuestion of $safeTotal",
                color = QuizText,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${(fraction * 100).roundToInt()}% Complete",
                color = QuizMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = QuizPrimary,
            trackColor = Color(0xFFDDE2DF)
        )
    }
}

@Composable
private fun QuizQuestionCard(
    question: BackendQuizQuestion,
    selectedAnswer: String?,
    onSelectAnswer: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE6E9E7)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = question.questionText,
                color = QuizText,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            question.options.forEach { option ->
                QuizOptionRow(
                    option = option,
                    selected = selectedAnswer == option,
                    onClick = { onSelectAnswer(option) }
                )
            }
        }
    }
}

@Composable
private fun QuizOptionRow(option: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = if (selected) Color(0xFFEAFCEF) else QuizBackground,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) QuizPrimary else Color(0xFFC8CECA))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                color = if (selected) QuizPrimary else QuizText,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) QuizPrimary else Color(0xFF9DA49F),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QuizActionSection(
    isLastQuestion: Boolean,
    canGoPrevious: Boolean,
    isSubmitting: Boolean,
    onSubmitOrNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = onSubmitOrNext,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = QuizPrimary, contentColor = Color.White)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (isLastQuestion) "Submit Quiz" else "Submit Answer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPrevious, enabled = canGoPrevious && !isSubmitting) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Previous", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onSkip, enabled = !isSubmitting) {
                Text("Skip Question", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuizCompletedCard(
    content: BackendContent?,
    quiz: BackendQuiz,
    result: QuizAttemptData,
    onReviewAnswers: () -> Unit,
    onBackToLearn: () -> Unit
) {
    val total = result.result.totalQuestions
    val bestCorrect = scoreToCorrectCount(result.result.bestScore, total)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE3FBE5),
        border = BorderStroke(1.dp, Color(0x3330A152))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0x3330A152)) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = QuizPrimary,
                    modifier = Modifier.padding(12.dp).size(30.dp)
                )
            }
            Text("Quiz Completed!", color = QuizText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "You scored ${result.result.correctAnswers} out of $total on ${content?.title ?: quiz.title}.",
                color = QuizMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            if (result.result.isNewBestScore) {
                Surface(shape = CircleShape, color = Color(0xFFB9F7C4)) {
                    Text(
                        text = "New best score",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        color = QuizPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultMetricRow("Accuracy", "${result.result.accuracy}%")
                    ResultMetricRow("Time Spent", formatDuration(result.result.timeSpentSeconds ?: 0))
                    ResultMetricRow("Best Score", "${bestCorrect ?: result.result.correctAnswers}/$total")
                }
            }
            Button(
                onClick = onReviewAnswers,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = QuizPrimary, contentColor = Color.White)
            ) {
                Text("Review Answers", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = onBackToLearn,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF86FAAC), contentColor = QuizPrimary)
            ) {
                Text("Back to Learn", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ResultMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), color = QuizMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text(value, color = QuizPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun QuizLoadingMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(color = QuizPrimary)
        Text("Loading quiz...", color = QuizMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuizInfoCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E5E1))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            color = QuizMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun navigateBackToContent(navController: NavController, contentId: String, content: BackendContent?) {
    if (content != null) {
        ContentSelectionCache.selectedContent = content
    }
    if (!navController.popBackStack("content_detail/$contentId", false)) {
        navController.navigate("content_detail/$contentId")
    }
}

private fun contentTagLabel(tag: String): String = when (tag) {
    "ewaste" -> "E-Waste"
    "food-waste" -> "Food Waste"
    else -> tag.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private fun bestScoreLabel(score: Int?, totalQuestions: Int): String {
    val correct = scoreToCorrectCount(score, totalQuestions)
    return if (correct == null) "Best Score: --/$totalQuestions" else "Best Score: $correct/$totalQuestions"
}

private fun scoreToCorrectCount(score: Int?, totalQuestions: Int): Int? {
    if (score == null || totalQuestions <= 0) return null
    return score.coerceIn(0, totalQuestions)
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private val QuizBackground = Color(0xFFF5F7F5)
private val QuizPrimary = Color(0xFF006B1B)
private val QuizText = Color(0xFF2C2F2E)
private val QuizMuted = Color(0xFF666D68)
private val QuizError = Color(0xFFB02500)
