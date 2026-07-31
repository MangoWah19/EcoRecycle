package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.api.BackendQuizReviewQuestion
import com.example.fyp1.api.QuizSessionCache
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import kotlin.math.roundToInt

@Composable
fun QuizReviewScreen(navController: NavController) {
    val session = QuizSessionCache.completedSession

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ReviewBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ReviewTopBar(onBack = { navController.popBackStack() })
            }

            if (session == null) {
                item {
                    ReviewInfoCard("No quiz result is available. Complete a quiz first to review your answers.")
                }
                return@LazyColumn
            }

            item {
                ReviewSummaryHeader(
                    correctAnswers = session.result.correctAnswers,
                    totalQuestions = session.result.totalQuestions,
                    bestScore = session.result.bestScore
                )
            }

            itemsIndexed(session.review.questions, key = { _, question -> question.id }) { index, question ->
                ReviewQuestionCard(questionNumber = index + 1, question = question)
            }

            item {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ReviewPrimary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Back to Results", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ReviewTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ReviewPrimary)
        }
        Text(
            text = "Review Answers",
            color = ReviewPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun ReviewSummaryHeader(correctAnswers: Int, totalQuestions: Int, bestScore: Int?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = CircleShape, color = Color(0xFFDDF8E2)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Summarize, contentDescription = null, tint = ReviewPrimary, modifier = Modifier.size(14.dp))
                Text("Quiz Summary", color = ReviewPrimary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(
            text = "$correctAnswers/$totalQuestions Correct",
            color = ReviewText,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Surface(shape = CircleShape, color = Color(0xFFEAFCEF), border = BorderStroke(1.dp, Color(0x3330A152))) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ReviewPrimary, modifier = Modifier.size(14.dp))
                Text(
                    text = "Personal Best: ${scoreToCorrectCount(bestScore, totalQuestions) ?: correctAnswers}/$totalQuestions",
                    color = ReviewPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ReviewQuestionCard(questionNumber: Int, question: BackendQuizReviewQuestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE6E9E7)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "QUESTION $questionNumber",
                    color = ReviewMuted.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(shape = CircleShape, color = if (question.isCorrect) Color(0xFFEAFCEF) else Color(0xFFFFE9E4)) {
                    Icon(
                        imageVector = if (question.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (question.isCorrect) ReviewPrimary else ReviewError,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
            }
            Text(
                text = question.questionText,
                color = ReviewText,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    question.wasSkipped -> {
                        ReviewAnswerRow(
                            text = "No answer selected",
                            caption = "Your Answer - Skipped",
                            icon = Icons.Default.RadioButtonUnchecked,
                            background = Color(0xFFF2F4F3),
                            border = Color(0xFFD6DBD8),
                            tint = ReviewMuted
                        )
                        ReviewAnswerRow(
                            text = question.correctAnswer,
                            caption = "Correct Answer",
                            icon = Icons.Default.Check,
                            background = Color(0xFFD9FFD9),
                            border = Color(0xFF9CEFA5),
                            tint = ReviewPrimary
                        )
                    }
                    question.isCorrect -> {
                        ReviewAnswerRow(
                            text = question.correctAnswer,
                            caption = "Your Answer - Correct",
                            icon = Icons.Default.CheckCircle,
                            background = Color(0xFFD9FFD9),
                            border = Color(0xFF9CEFA5),
                            tint = ReviewPrimary
                        )
                    }
                    else -> {
                        ReviewAnswerRow(
                            text = question.selectedAnswer ?: "No answer selected",
                            caption = "Your Answer - Incorrect",
                            icon = Icons.Default.Close,
                            background = Color(0xFFFFDAD2),
                            border = Color(0xFFFFB4A5),
                            tint = ReviewError
                        )
                        ReviewAnswerRow(
                            text = question.correctAnswer,
                            caption = "Correct Answer",
                            icon = Icons.Default.Check,
                            background = Color(0xFFD9FFD9),
                            border = Color(0xFF9CEFA5),
                            tint = ReviewPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewAnswerRow(
    text: String,
    caption: String,
    icon: ImageVector,
    background: Color,
    border: Color,
    tint: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Column {
                Text(text, color = tint, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text(caption, color = tint.copy(alpha = 0.74f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReviewInfoCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E5E1))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            color = ReviewMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun scoreToCorrectCount(score: Int?, totalQuestions: Int): Int? {
    if (score == null || totalQuestions <= 0) return null
    return score.coerceIn(0, totalQuestions)
}

private val ReviewBackground = Color(0xFFF5F7F5)
private val ReviewPrimary = Color(0xFF006B1B)
private val ReviewText = Color(0xFF2C2F2E)
private val ReviewMuted = Color(0xFF666D68)
private val ReviewError = Color(0xFFB02500)
