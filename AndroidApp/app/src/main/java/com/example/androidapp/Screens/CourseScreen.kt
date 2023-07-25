package com.example.androidapp.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.androidapp.models.Course
import com.example.androidapp.models.Schedule
import com.example.androidapp.viewModels.ScheduleViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Composable
fun CourseScreen(
    course: Course?,
    navController: NavController,
    index: Int,
    term: String,
    schedule: Schedule,
    position: Int
) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
        ) {
            course?.courseID?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            course?.courseName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (course != null) {
                RatingBar(rating = course.easyRating)
                Spacer(modifier = Modifier.size(16.dp))
                Description(description = course.description)
            }
        }

        SwapAndDelete(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            onSwap = {
                course?.let {
                    navController.currentBackStackEntry?.arguments?.putParcelable("schedule", schedule)
                    navController.currentBackStackEntry?.arguments?.putString("term", term)
                    navController.currentBackStackEntry?.arguments?.putInt("position", position)
                    navController.currentBackStackEntry?.arguments?.putBoolean("swap", true)
                    navController.currentBackStackEntry?.arguments?.putInt("index", index)
                    navController.navigate(Screen.SearchCourse.route)
                }

            },
            onDelete = {
                course?.let {
                    var updatedSchedule = schedule
                    updatedSchedule.termSchedule[term]?.removeAt(index)
                    updatedSchedule.time = Date()
                    val sharedPreferences = context.getSharedPreferences("MySchedules", Context.MODE_PRIVATE)
                    val existingList = sharedPreferences.getString("scheduleList", "[]")
                    val type = object : TypeToken<MutableList<Schedule>>() {}.type
                    val scheduleList : MutableList<Schedule> = Gson().fromJson(existingList, type)
                    scheduleList.removeAt(position)
                    scheduleList.add(0, updatedSchedule)

                    val jsonList = Gson().toJson(scheduleList)
                    val editor = sharedPreferences.edit()
                    editor.putString("scheduleList", jsonList)
                    editor.apply()

                    navController.navigate(route = Screen.ViewSchedule.route)
                }
            }
        )
    }
}

@Composable
fun RatingBar(rating: Float) {
    Column {
        Text(
            text = "Rating",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row {
            repeat(5) {index ->
                if (index < (rating * 5).toInt()){
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun Description(description: String) {
    Column {
        Text(
            text = "Course Description",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 0.dp)
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Composable
fun SwapAndDelete(modifier: Modifier = Modifier, onSwap: () -> Unit, onDelete: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.height(56.dp),
                onClick = { /* Handle button 1 click */ }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Swap"
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Swap Course")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.height(56.dp),
                onClick = { onDelete() }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Delete Course")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}