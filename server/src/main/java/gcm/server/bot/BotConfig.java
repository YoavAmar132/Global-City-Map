package gcm.server.bot;

public final class BotConfig {


    private static BotConfig instance = null;
    private boolean enabled = false;
    private String model = "qwen2.5:1.5b";

    private BotConfig() {}

    public static synchronized BotConfig getInstance(){
        if (instance == null)
            instance = new BotConfig();

        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
