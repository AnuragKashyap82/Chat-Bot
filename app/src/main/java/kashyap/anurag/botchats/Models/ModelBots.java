package kashyap.anurag.botchats.Models;

public class ModelBots {

    String botId, botName;

    public ModelBots() {
    }

    public ModelBots(String botId, String botName) {
        this.botId = botId;
        this.botName = botName;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
