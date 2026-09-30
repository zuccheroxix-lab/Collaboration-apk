package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SensitivityEngine
import com.example.model.PresetType
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SensitivityControls(
    sensX: Float,
    sensY: Float,
    isLinked: Boolean,
    linkRatio: Float,
    preset: PresetType,
    onSensXChange: (Float) -> Unit,
    onSensYChange: (Float) -> Unit,
    onLinkToggle: (Boolean) -> Unit,
    onLinkRatioChange: (Float) -> Unit,
    onPresetSelect: (PresetType) -> Unit,
    onReset: () -> Unit,
    onCalibrateClick: () -> Unit = {},
    onApplyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sensitivity_controls_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Presets & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENSITIVITY",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real input delta scaling engine",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset parameters to default",
                        tint = TextSecondary
                    )
                }
            }

            // Presets row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetType.entries.forEach { p ->
                    val isSelected = preset == p
                    val bg = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant
                    val border = if (isSelected) NeonCyan else Color.Transparent
                    val textCol = if (isSelected) NeonCyan else TextSecondary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                            .clickable { onPresetSelect(p) }
                            .padding(vertical = 8.dp)
                            .testTag("preset_${p.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = p.name,
                            color = textCol,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // GLOBAL X Slider with clear value alignment
            AxisSlider(
                axisLabel = "GLOBAL X",
                axisColor = NeonCyan,
                value = sensX,
                onValueChange = onSensXChange,
                testTagPrefix = "sens_x"
            )

            // GLOBAL Y Slider with clear value alignment
            AxisSlider(
                axisLabel = "GLOBAL Y",
                axisColor = NeonPurple,
                value = sensY,
                onValueChange = onSensYChange,
                testTagPrefix = "sens_y"
            )

            // X/Y LINK toggle bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isLinked) Icons.Default.Link else Icons.Default.LinkOff,
                        contentDescription = "Link X/Y Ratio",
                        tint = if (isLinked) NeonCyan else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "X/Y LINK",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLinked) "Locked at ratio ${String.format(Locale.US, "%.2f", linkRatio)}" else "Independent axes",
                            color = if (isLinked) NeonCyan else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = isLinked,
                    onCheckedChange = onLinkToggle,
                    modifier = Modifier.testTag("link_xy_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            // Action Buttons: [ CALIBRATE ] & [ APPLY ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCalibrateClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("calibrate_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CALIBRATE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onApplyClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("apply_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APPLY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AxisSlider(
    axisLabel: String,
    axisColor: Color,
    value: Float,
    onValueChange: (Float) -> Unit,
    testTagPrefix: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = axisLabel,
                color = axisColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val next = (value - SensitivityEngine.SENS_STEP).coerceIn(SensitivityEngine.MIN_SENS, SensitivityEngine.MAX_SENS)
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("${testTagPrefix}_decrement")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease $axisLabel",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.2f", value),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 44.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        val next = (value + SensitivityEngine.SENS_STEP).coerceIn(SensitivityEngine.MIN_SENS, SensitivityEngine.MAX_SENS)
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("${testTagPrefix}_increment")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase $axisLabel",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = SensitivityEngine.MIN_SENS..SensitivityEngine.MAX_SENS,
            steps = 57,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_slider"),
            colors = SliderDefaults.colors(
                thumbColor = axisColor,
                activeTrackColor = axisColor,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}
