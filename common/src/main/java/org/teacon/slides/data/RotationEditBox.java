package org.teacon.slides.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.teacon.slides.mappings.Text;

import java.util.function.Consumer;

public class RotationEditBox extends EditBox implements IGui {
    public RotationEditBox(int i, int j, int k, int l) {
        super(Minecraft.getInstance().font, i, j, k, l, Text.literal(""));
        setResponder(text -> {
        });
    }

    @Override
    public void setResponder(Consumer<String> changedListener) {
        super.setResponder(text -> {
            changedListener.accept(text);
        });
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if(active) {
            float val;
            try {
                val = Float.parseFloat(getValue());
            } catch (Exception ex) {
                val = 0f;
            }
            val += f;
            if(val > -360 && val < 360) {
                setValue(String.valueOf(val));
                setCursorPosition(0);
                setHighlightPos(0);
            }
        }
        return super.mouseScrolled(d, e, f);
    }
}