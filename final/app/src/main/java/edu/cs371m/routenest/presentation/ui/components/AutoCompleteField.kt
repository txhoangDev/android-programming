package edu.cs371m.routenest.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import edu.cs371m.routenest.data.api.PlacesApi
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutoCompleteField(
    searchQuery: String,
    textFieldText: String,
    onQueryChange: (String) -> Unit,
    onItemClick: (AutocompletePrediction) -> Unit,
    isPlace: Boolean,
    location: String = "",
) {
    var expanded by remember { mutableStateOf(false) }
    var queryList by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    val showMenu = expanded && searchQuery.isNotBlank() && queryList.isNotEmpty()
    val sessionToken = AutocompleteSessionToken.newInstance()

    ExposedDropdownMenuBox(
        expanded = showMenu,
        onExpandedChange = {
            expanded = if (expanded) false else searchQuery.isNotBlank()
        }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            value = searchQuery,
            onValueChange = {
                onQueryChange(it)
                expanded = it.isNotBlank()
            },
            label = { Text(text = textFieldText) },
            singleLine = true,
        )

        LaunchedEffect(searchQuery) {
            if (searchQuery.length > 2) {
                delay(300)
                Log.d("AutoCompleteField", "searchQuery: $searchQuery")
                if (isPlace) {
                    PlacesApi.getAutoCompletePredictionsPlaces(searchQuery, sessionToken, location) {
                        Log.d("AutoCompleteField", "queryList: $it")
                        queryList = it
                    }
                } else {
                    PlacesApi.getAutoCompletePredictions(query = searchQuery, token = sessionToken) { it ->
                        Log.d("AutoCompleteField", "queryList: $it")
                        queryList = it
                    }
                }
            }
        }

        // Keep the dropdown from stealing focus; otherwise typing can stop when it opens.
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(),
            properties = PopupProperties(focusable = false),
        ) {
            queryList.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.getFullText(null).toString()) },
                    onClick = {
                        onItemClick(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
