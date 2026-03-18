// ui/widgets/BottomNavBar.kt

package com.techsolutions.worqee.ui.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.platform.LocalContext
import com.techsolutions.worqee.ui.screens.GradesScreen.GradesActivity

enum class NavBarItem {
    SCHEDULE, FRIENDS, GRADES, STUDY
}

@Composable
fun BottomNavBar(
    selectedItem: NavBarItem,
    onItemSelected: (NavBarItem) -> Unit
) {


    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = selectedItem == NavBarItem.SCHEDULE,
            onClick = { onItemSelected(NavBarItem.SCHEDULE) },
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Schedule") },
            label = { Text("Schedule", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedItem == NavBarItem.FRIENDS,
            onClick = { onItemSelected(NavBarItem.FRIENDS) },
            icon = { Icon(Icons.Outlined.People, contentDescription = "Friends") },
            label = { Text("Friends", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedItem == NavBarItem.GRADES,
            onClick = { onItemSelected(NavBarItem.GRADES) },
            icon = { Icon(Icons.Outlined.School, contentDescription = "Grades") },
            label = { Text("Grades", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedItem == NavBarItem.STUDY,
            onClick = { onItemSelected(NavBarItem.STUDY) },
            icon = { Icon(Icons.Outlined.AutoStories, contentDescription = "Study") },
            label = { Text("Study", style = MaterialTheme.typography.labelSmall) }
        )
    }
}