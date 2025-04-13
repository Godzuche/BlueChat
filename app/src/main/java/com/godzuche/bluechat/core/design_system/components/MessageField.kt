package com.godzuche.bluechat.core.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme

@Composable
fun MessageField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
        .copy(color = MaterialTheme.colorScheme.onSurface),
    error: (@Composable () -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.None,
        autoCorrectEnabled = false,
    ),
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val isHintVisible by remember(value) { mutableStateOf(value.isEmpty()) }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
    )

    Column(
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            BasicTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                cursorBrush = SolidColor(
                    if (error != null) {
                        MaterialTheme.colorScheme.error
                    } else MaterialTheme.colorScheme.primary
                ),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                textStyle = textStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(containerColor),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = verticalAlignment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                    ) {
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(7.dp))
                        }
                        Box {
                            if (isHintVisible) {
                                placeholder?.invoke()
                            }
                            ProvideTextStyle(textStyle) {
                                innerTextField()
                            }
                        }
                        if (trailingIcon != null) {
                            if (value.isNotEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(6.dp))
                                trailingIcon()
                            }
                        }
                    }
                }
            )
        }

        error?.invoke()
    }
}

@PreviewLightDark
@Composable
private fun MessageFieldPreview() = BlueChatTheme {
    MessageField(
        value = "Message",
        onValueChange = {},
    )
}
