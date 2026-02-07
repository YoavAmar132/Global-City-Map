package gcm.server.bot;

/*
 * a singleton class that track the configuration of the bot
 * e.i:
 * whether the bot is enabled or not
 * what's the model of the bot
 * */

public final class BotConfig {


    private static BotConfig instance = null;
    private boolean enabled = false;
    private String model = "mistral";

    private BotConfig() {}

    public static synchronized BotConfig getInstance(){
        if (instance == null)
            instance = new BotConfig();

        return instance;
    }


    /*
     * return whether the bot is enabled
     * */
    public boolean isEnabled() {
        return enabled;
    }


    /*
     * set the bot enabled status base on the argument
     * */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /*
     * return the model of the bot
     */
    public String getModel() {
        return model;
    }

    /*
     * set the model of the bot
     */
    public void setModel(String model) {
        this.model = model;
    }
}
