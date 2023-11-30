package org.teacon.slides.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
import org.teacon.slides.data.IGui;
import org.teacon.slides.data.WidgetBetterTextField;
import org.teacon.slides.data.WidgetShorterSlider;
import org.teacon.slides.mappings.ScreenMapper;
import org.teacon.slides.mappings.Text;
import org.teacon.slides.mappings.UtilitiesClient;

public class ConfigScreen extends ScreenMapper implements IGui {
    private boolean proxySwitch;
    private boolean discordUrlVisualized;
    private final Button buttonProxySwitch;
    private final Button discordVisualizerSwitch;
    private final WidgetBetterTextField textFieldHost;
    private final WidgetBetterTextField textFieldPort;
    private final WidgetShorterSlider sliderViewDistance;
    private static final int MAX_HOST_LENGTH = 256;
    private static final int MAX_PORT_LENGTH = 5;
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = TEXT_HEIGHT + TEXT_PADDING;


    public ConfigScreen() {
        super(Text.literal(""));
        this.buttonProxySwitch = UtilitiesClient.newButton(BUTTON_HEIGHT, Text.literal(""), button -> {
            this.proxySwitch = Config.setProxySwitch(!proxySwitch);
            setButtonText(button, proxySwitch);
        });
        this.discordVisualizerSwitch = UtilitiesClient.newButton(BUTTON_HEIGHT, Text.literal(""), button -> {
            this.discordUrlVisualized = Config.setDiscordVisualizerEnabled(!discordUrlVisualized);
           setButtonText(button, discordUrlVisualized);
        });
        this.textFieldHost = new WidgetBetterTextField("localhost", MAX_HOST_LENGTH);
        this.textFieldPort = new WidgetBetterTextField("8080", MAX_PORT_LENGTH);
        this.sliderViewDistance = new WidgetShorterSlider(0, 0, Config.MAX_VIEW_DISTANCE, num -> String.format("%d", num < 4 ? 4 : num), null);
    }

    @Override
    protected void init() {
        super.init();
        Config.refreshProperties();
        this.proxySwitch = Config.isProxySwitch();
        this.discordUrlVisualized = Config.getDiscordVisualizerEnabled();
        int i = 1;
        setPositionAndWidth(buttonProxySwitch, width - SQUARE_SIZE - BUTTON_WIDTH, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE, BUTTON_WIDTH);
        setPositionAndWidth(discordVisualizerSwitch, width - SQUARE_SIZE - BUTTON_WIDTH, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE, BUTTON_WIDTH);
        setPositionAndWidth(textFieldHost, width - (SQUARE_SIZE * 10) - BUTTON_WIDTH, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE, BUTTON_WIDTH - TEXT_PADDING - font.width("256") + (SQUARE_SIZE * 9));
        setPositionAndWidth(textFieldPort, width - (SQUARE_SIZE * 10) - BUTTON_WIDTH, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE, BUTTON_WIDTH - TEXT_PADDING - font.width("256") + (SQUARE_SIZE * 9));
        setPositionAndWidth(sliderViewDistance, width - (SQUARE_SIZE * 10) - BUTTON_WIDTH, (SQUARE_SIZE + TEXT_FIELD_PADDING) * i + SQUARE_SIZE, BUTTON_WIDTH - TEXT_PADDING - font.width("256") + (SQUARE_SIZE * 9));
        setButtonText(buttonProxySwitch, proxySwitch);
        setButtonText(discordVisualizerSwitch, discordUrlVisualized);
        textFieldHost.setValue(Config.getHost());
        textFieldPort.setValue(String.valueOf(Config.getPort()));
        sliderViewDistance.setHeight(BUTTON_HEIGHT);
        sliderViewDistance.setValue(Config.getViewDistance());
        addDrawableChild(buttonProxySwitch);
        addDrawableChild(discordVisualizerSwitch);
        addDrawableChild(textFieldHost);
        addDrawableChild(textFieldPort);
        addDrawableChild(sliderViewDistance);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        try {
            renderBackground(matrices);
            drawCenteredString(matrices, font, Text.translatable("gui.slide_show.options"), width / 2, TEXT_PADDING, ARGB_WHITE);
            int i = 1;
            drawString(matrices, font, Text.translatable("options.slide_show.proxy_switch"), SQUARE_SIZE, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE + TEXT_PADDING, ARGB_WHITE);
            drawString(matrices, font, Text.translatable("options.slide_show.discord_visualized"), SQUARE_SIZE, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE + TEXT_PADDING, ARGB_WHITE);
            drawString(matrices, font, Text.translatable("options.slide_show.host"), SQUARE_SIZE, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE + TEXT_PADDING, ARGB_WHITE);
            drawString(matrices, font, Text.translatable("options.slide_show.port"), SQUARE_SIZE, (SQUARE_SIZE + TEXT_FIELD_PADDING) * (i++) + SQUARE_SIZE + TEXT_PADDING, ARGB_WHITE);
            drawString(matrices, font, Text.translatable("options.slide_show.view_distance"), SQUARE_SIZE, (SQUARE_SIZE + TEXT_FIELD_PADDING) * i + SQUARE_SIZE + TEXT_PADDING, ARGB_WHITE);
            super.render(matrices, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Config.setHost(textFieldHost.getValue());
        Config.setPort(Integer.parseInt(textFieldPort.getValue()));
        Config.setViewDistance(Math.max(sliderViewDistance.getIntValue(), 4));
        Config.refreshProperties();
    }

    private static void setButtonText(Button button, boolean state) {
        button.setMessage(Text.translatable(state ? "options.slider_show.on" : "options.slider_show.off"));
    }

    static void setPositionAndWidth(AbstractWidget widget, int x, int y, int widgetWidth) {
        UtilitiesClient.setWidgetX(widget, x);
        UtilitiesClient.setWidgetY(widget, y);
        widget.setWidth(Mth.clamp(widgetWidth, 0, 380 - (widget instanceof WidgetBetterTextField ? TEXT_FIELD_PADDING : 0)));
    }
}