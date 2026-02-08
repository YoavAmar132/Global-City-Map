package gcm.server.bot.tools;

import java.util.Map;

public class GuideMapPurchase implements BotTool {
    private String guide=
            "At first,go to the menu and click on 'buy a map'.Then clicked on the buy button on the city you want to but a map from.\n" +
            "Select the maps you want and choose if you want to purchase the map or subscribe to them.\n" +
            "In the new screen,fill the relevant payment details.\n" +
             "and that's it!.now you should be able to see it after clicking on 'my maps' \n";
    @Override
    public String getName() {
        return "guideMapPurchase";
    }

    @Override
    public String getDescription() {
        return "- guideMapPurchase dont requires any argument,return a guide for how to buy a map";
    }

    @Override
    public String execute(Map<String, String> args) {
        return guide;
    }

}
