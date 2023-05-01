package org.teacon.slides.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.teacon.slides.mappings.Text;
import org.teacon.slides.mappings.UtilitiesClient;

import java.util.Locale;
import java.util.function.Consumer;

public class WidgetBetterTextField extends EditBox implements IGui {
    private final String filter;
    private final String suggestion;
    private final int newMaxLength;

    public WidgetBetterTextField(String suggestion, int maxLength) {
        this("", suggestion, maxLength);
    }

    public WidgetBetterTextField(String filter, String suggestion, int maxLength) {
        super(Minecraft.getInstance().font, 0, 0, 0, SQUARE_SIZE, Text.literal(""));
        this.filter = filter;
        this.suggestion = suggestion;
        newMaxLength = maxLength;
        setResponder(text -> {
        });
        setMaxLength(0);
    }

    @Override
    public void setResponder(Consumer<String> changedListener) {
        super.setResponder(text -> {
            final String newText;
            if (filter.isEmpty()) {
                newText = trySetLength(text);
            } else {
                newText = trySetLength(text.toUpperCase(Locale.ENGLISH).replaceAll(filter, ""));
                if (!newText.equals(text)) {
                    setValue(newText);
                }
            }
            setSuggestion(newText.isEmpty() && suggestion != null ? suggestion : "");
            changedListener.accept(newText);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isVisible() && isBetween(mouseX, UtilitiesClient.getWidgetX(this), UtilitiesClient.getWidgetX(this) + width) && isBetween(mouseY, UtilitiesClient.getWidgetY(this), UtilitiesClient.getWidgetY(this) + height)) {
            if (button == 1) {
                setValue("");
            }
            return super.mouseClicked(mouseX, mouseY, 0);
        } else {
            setFocused(false);
            return false;
        }
    }

    public static boolean isBetween(double value, double value1, double value2) {
        return isBetween(value, value1, value2, 0);
    }

    public static boolean isBetween(double value, double value1, double value2, double padding) {
        return value >= Math.min(value1, value2) - padding && value <= Math.max(value1, value2) + padding;
    }

    @Override
    public void setMaxLength(int maxLength) {
        super.setMaxLength(Integer.MAX_VALUE);
    }

    private String trySetLength(String text) {
        return text.isEmpty() ? "" : text.substring(0, Math.min(newMaxLength, text.length()));
    }
}