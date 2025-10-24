import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import android.graphics.Matrix
import android.util.Base64
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Data class to store the information for a single continuous stroke
data class DrawingPath(
    val path: Path,
    val color: Color = Color.Black,
    val strokeWidth: Dp = 4.dp
)

// --- Helper Extension Function ---
/**
 * Converts a Compose Path (androidx.compose.ui.graphics.Path) to an
 * Android native Path (android.graphics.Path) for use with Bitmap/Canvas APIs.
 * This function exists in the Compose runtime but needs explicit import.
 */
fun Path.toAndroidPath(): AndroidPath {
    return this.asAndroidPath()
}

/**
 * Main composable screen containing the signature pad and control buttons.
 */
@Composable
fun SignaturePadScreen() {
    // Get context to access file system and resources
    val context = LocalContext.current

    // State to hold all the drawing paths (strokes)
    var paths by remember { mutableStateOf(emptyList<DrawingPath>()) }

    // The currently active path being drawn
    var currentPath by remember { mutableStateOf(Path()) }

    // State to store the actual pixel size of the SignaturePad Composable (Source Size)
    var signaturePadSizePx by remember { mutableStateOf(IntSize(0, 0)) }

    // Style settings for the signature
    val signatureColor = Color.Black
    val signatureStrokeWidth = 4.dp

    // Define the fixed dimensions (in DP) of the drawing area for later bitmap scaling
    val signaturePadHeightDp = 300.dp

    // Target width in pixels for the saved image (fixed high quality width)
    val targetBitmapWidth = 800

    // Target height will be calculated dynamically to match the canvas aspect ratio

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)) // Light background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Below",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // The Signature Drawing Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(signaturePadHeightDp), // Use the defined height DP
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            SignaturePad(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // Signature area background
                    // Capture the size of the Composable in pixels
                    .onGloballyPositioned { layoutCoordinates ->
                        signaturePadSizePx = layoutCoordinates.size
                    },
                paths = paths,
                currentPath = currentPath,
                onSignatureDrawn = { path, dragEnd ->
                    if (dragEnd) {
                        // Drag ended, save the current path to the list of finished paths
                        paths = paths + DrawingPath(path, signatureColor, signatureStrokeWidth)
                        currentPath = Path() // Start a new current path
                    } else {
                        // FIX: Create a new Path object by copying the content of the mutated 'path'
                        // This forces Compose to see a state change (new object reference) and recompose the Canvas.
                        currentPath = Path().apply { addPath(path) }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.size(32.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    paths = emptyList()
                    currentPath = Path()
                },
                shape = RoundedCornerShape(8.dp),
                colors = getButtonColors()
            ) {
                Text("Clear Signature")
            }

            Button(
                onClick = {
                    // Combine all paths for final capture
                    val finalPaths = paths + if (!currentPath.isEmpty) {
                        listOf(DrawingPath(currentPath, signatureColor, signatureStrokeWidth))
                    } else {
                        emptyList()
                    }

                    if (finalPaths.isNotEmpty() && signaturePadSizePx.width > 0) {

                        // FIX: Dynamically calculate target height based on measured aspect ratio
                        val ratio = signaturePadSizePx.height.toFloat() / signaturePadSizePx.width.toFloat()
                        val calculatedTargetHeight = (targetBitmapWidth.toFloat() * ratio).toInt()

                        AppUtils.saveSignatureImage(
                            context,
                            finalPaths,
                            targetBitmapWidth,
                            calculatedTargetHeight, // Use calculated height for correct proportion
                            signaturePadSizePx.width,
                            signaturePadSizePx.height
                        )
                    } else {
                        Log.w("SignaturePad", "Cannot save: No signature drawn or size not yet measured.")
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = getButtonColors()
            ) {
                Text("Save Signature")
            }
        }
    }
}

/**
 * The core composable handling the touch input and drawing on a Canvas.
 */
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    paths: List<DrawingPath>,
    currentPath: Path,
    onSignatureDrawn: (Path, Boolean) -> Unit // (Path, isDragEnd)
) {
    var previousOffset by remember { mutableStateOf(Offset.Unspecified) }

    // Use Box to contain the Canvas and apply the pointerInput modifier.
    // Changing the key to paths.size ensures this lambda is recreated when
    // paths list is cleared or a new stroke is finalized, fixing the stale state issue.
    Box(
        modifier = modifier
            .pointerInput(paths.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        previousOffset = offset
                        // Start a new path by moving to the offset
                        // currentPath is the latest empty Path() instance after a clear
                        val newPath = currentPath.apply { moveTo(offset.x, offset.y) }
                        onSignatureDrawn(newPath, false)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val current = change.position

                        // FIX: Revert to direct mutation on the currentPath reference for guaranteed continuity.
                        // The recomposition is now forced in SignaturePadScreen.
                        val newPath = currentPath.apply {
                            val x1 = previousOffset.x
                            val y1 = previousOffset.y
                            val x2 = (current.x + x1) / 2
                            val y2 = (current.y + y1) / 2
                            quadraticBezierTo(x1, y1, x2, y2)
                        }

                        previousOffset = current
                        // Pass the mutated object back (Screen will clone it)
                        onSignatureDrawn(newPath, false)
                    },
                    onDragEnd = {
                        // Signal that the drag has finished
                        onSignatureDrawn(currentPath, true)
                        previousOffset = Offset.Unspecified
                    }
                )
            }
    ) {
        // The canvas is where all the actual drawing occurs
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Draw all completed paths
            paths.forEach { drawingPath ->
                drawPath(
                    path = drawingPath.path,
                    color = drawingPath.color,
                    // Convert Dp to Pixels for the stroke width
                    style = Stroke(width = drawingPath.strokeWidth.toPx())
                )
            }

            // 2. Draw the currently active path
            drawPath(
                path = currentPath,
                color = Color.Black,
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}

