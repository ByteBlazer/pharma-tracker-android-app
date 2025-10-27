package com.deltasoft.pharmatracker.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.ui.theme.AppPrimary
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getCenterAlignedTopAppBarColors
import com.deltasoft.pharmatracker.utils.AppUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.deltasoft.pharmatracker.ui.theme.AppTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App_CommonTopBar(title : String="", onBackClick: () -> Unit ={},backButtonVisibility : Boolean = true,btnTxt:String? = null,onBtnTxtClick: () -> Unit ={},
                     useDefaultColor : Boolean = false){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (backButtonVisibility) {
                IconButton(onClick = {
                    onBackClick.invoke()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back"
                    )
                }
            }
        },
        colors = if (useDefaultColor) TopAppBarDefaults.centerAlignedTopAppBarColors() else getCenterAlignedTopAppBarColors()
    )
}

fun Modifier.drawOneSideBorder(
    width: Dp,
    color: Color,
    side: BorderSide,
    shape: Shape = RectangleShape
) = this
    .clip(shape)
    .drawWithContent {
        val widthPx = width.toPx()
        drawContent()

        val start = when (side) {
            BorderSide.LEFT -> Offset(widthPx / 2, 0f)
            BorderSide.RIGHT -> Offset(size.width - widthPx / 2, 0f)
        }
        val end = when (side) {
            BorderSide.LEFT -> Offset(widthPx / 2, size.height)
            BorderSide.RIGHT -> Offset(size.width - widthPx / 2, size.height)
        }

        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = widthPx
        )
    }


val Int.nonScaledSp
    @Composable
    get() = (this / LocalDensity.current.fontScale).sp


fun Color.blendWith(other: Color, ratio: Float): Color {
    return Color(
        red = lerp(this.red, other.red, ratio),
        green = lerp(this.green, other.green, ratio),
        blue = lerp(this.blue, other.blue, ratio),
        alpha = this.alpha
    )
}


enum class BorderSide {
    LEFT, RIGHT
}


@Composable
fun AppConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm" ,
    dismissButtonText: String = "Cancel",
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                androidx.compose.material.Text(
                    text = title, style = MaterialTheme.typography.bodyLarge,
                    color = AppPrimary, fontWeight = FontWeight.Bold
                )
            },
            text = {
                androidx.compose.material.Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    androidx.compose.material.Text(
                        confirmButtonText,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    androidx.compose.material.Text(
                        dismissButtonText,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}


@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    leadingIcon: Int?=null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = getButtonColors(),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                painter = painterResource(id = leadingIcon?:0),
                tint = AppUtils.getTextColorBasedOnColortype(AppPrimary),
                contentDescription = "Button Start Icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun TripIdAnnotatedText(tripId: String,
                                 style: TextStyle = MaterialTheme.typography.titleLarge,
                                 color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    val hashIconId = "hashIconId"

    val inlineContentMap: Map<String, InlineTextContent> = mapOf(
        hashIconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hash),
                contentDescription = "hash",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        }
    )

    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = color)) {
            append("$tripId  ") // Added padding spaces for separation
        }
    }

    val annotatedTextIcon = buildAnnotatedString {
        appendInlineContent(hashIconId, "[ID]")
        withStyle(SpanStyle(color = color)) {
            append("") // Added padding spaces for separation
        }
    }
    Row(Modifier.padding(vertical = 0.dp)) {
        Text(
            text = annotatedTextIcon,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
        Text(
            text = annotatedText,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
    }
}

@Composable
fun TripIdWithRouteAnnotatedText(tripId: String,
                                         route: String,
                                         style: TextStyle = MaterialTheme.typography.titleLarge,
                                         color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
                                         fontWeight: FontWeight = FontWeight.Normal,
                                         itemsSpace: Dp = 4.dp) {
    val hashIconId = "hashIconId"
    val routeIconId = "routeIconId"

    val inlineContentMap: Map<String, InlineTextContent> = mapOf(
        hashIconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hash),
                contentDescription = "hash",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        },

        routeIconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_route),
                contentDescription = "route",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        }
    )

    val annotatedText = buildAnnotatedString {
//        appendInlineContent(hashIconId, "[ID]")
        withStyle(SpanStyle(color = color)) {
            append("$tripId  ") // Added padding spaces for separation
        }
        appendInlineContent(routeIconId, "[Route]")
        withStyle(SpanStyle(color = color)) {
            append(" $route")
        }
    }

    val annotatedTextIcon = buildAnnotatedString {
        appendInlineContent(hashIconId, "[ID]")
        withStyle(SpanStyle(color = color)) {
            append("") // Added padding spaces for separation
        }
    }
    Row(Modifier.padding(vertical = 0.dp)) {
        Text(
            text = annotatedTextIcon,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
        Text(
            text = annotatedText,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
    }
}

@Composable
fun SingleIconWithTextAnnotatedItem(icon: Int, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal,
                                itemsSpace: Dp = 4.dp,searchQuery:String="") {
    val highlightColor = AppTertiary
    val iconId = "IconId"

    val inlineContentMap: Map<String, InlineTextContent> = mapOf(
        iconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "icon",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        },
    )

    val annotatedText = if (searchQuery.isBlank()) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = color)) {
                append("$value  ") // Added padding spaces for separation
            }
        }
    }else{
//        buildAnnotatedString {
//            withStyle(SpanStyle(color = color)) {
//                append("$value  ") // Added padding spaces for separation
//            }
//        }
        buildAnnotatedString {
            val lowerCaseText = value.lowercase()
            val lowerCaseQuery = searchQuery.lowercase()

            var currentPosition = 0 // Tracks the position we last left off in the fullText

            while (currentPosition < value.length) {
                // Find the next occurrence of the query string
                val matchIndex = lowerCaseText.indexOf(lowerCaseQuery, currentPosition)

                if (matchIndex >= 0) {
                    // Match found!

                    // 1. Append the text *before* the match (unstyled)
                    append(value.substring(currentPosition, matchIndex))

                    // 2. Append the match itself, with the highlight style
                    val matchEnd = matchIndex + searchQuery.length
                    withStyle(style = SpanStyle(background = highlightColor, color = color)) {
                        append(value.substring(matchIndex, matchEnd))
                    }

                    // 3. Update the starting position for the next search to be AFTER the match
                    currentPosition = matchEnd
                } else {
                    // No more matches found. Append the rest of the string and break the loop.
                    withStyle(SpanStyle(color = color)) {
                        append("${value.substring(currentPosition)}  ") // Added padding spaces for separation
                    }
//                    append(value.substring(currentPosition))
                    break
                }
            }
        }
    }




    val annotatedTextIcon = buildAnnotatedString {
        appendInlineContent(iconId, "[ID]")
        withStyle(SpanStyle(color = color)) {
            append(" ") // Added padding spaces for separation
        }
    }
    Row(Modifier.padding(vertical = 0.dp)) {
        Text(
            text = annotatedTextIcon,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
        Text(
            text = annotatedText,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
    }
}

@Composable
fun SingleIconWithTextAnnotatedItemWithOnCLick(icon: Int, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal,
                                    itemsSpace: Dp = 4.dp,
                                               onClick: () -> Unit,) {

    val iconId = "IconId"

    val inlineContentMap: Map<String, InlineTextContent> = mapOf(
        iconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "icon",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        },
    )

    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = color)) {
            append("$value  ") // Added padding spaces for separation
        }
    }

    val annotatedTextIcon = buildAnnotatedString {
        appendInlineContent(iconId, "[ID]")
        withStyle(SpanStyle(color = color)) {
            append(" ") // Added padding spaces for separation
        }
    }
    Row(Modifier
        .padding(vertical = 0.dp)
        .clickable { onClick.invoke() }) {
        Text(
            text = annotatedTextIcon,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
        Text(
            text = annotatedText,
            inlineContent = inlineContentMap,
            modifier = Modifier.padding(0.dp),
            style = style,
            color = color
        )
    }
}


@Composable
fun ButtonContentCompose(
    icon: Int,
    text: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = Color.White
) {
    val iconId = "hashIconId"

    val inlineContentMap: Map<String, InlineTextContent> = mapOf(
        iconId to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "hash",
                tint = color,
                modifier = Modifier.fillMaxSize()
            )
        }
    )

    val annotatedText = buildAnnotatedString {
        appendInlineContent(iconId, "IC")
        withStyle(SpanStyle(color = color)) {
            append("  $text") // Added padding spaces for separation
        }
    }

    Text(
        text = annotatedText,
        inlineContent = inlineContentMap,
        modifier = Modifier.padding(0.dp),
        style = style,
        color = color,
//        maxLines = 1,
//        overflow = TextOverflow.Ellipsis,
    )
    //                    Text(text)
}

@Composable
fun SimpleSearchView(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        placeholder = { Text("Search") },
        singleLine = true,
        // Left side icon
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        },
        // Right side icon (Clear button)
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        // Apply Material 3 shapes (e.g., rounded corners)
        shape = MaterialTheme.shapes.extraLarge
    )
}


