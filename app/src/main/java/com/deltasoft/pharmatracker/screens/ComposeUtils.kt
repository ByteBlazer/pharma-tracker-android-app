package com.deltasoft.pharmatracker.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.ui.theme.AppPrimary
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getCenterAlignedTopAppBarColors
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty

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
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
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

