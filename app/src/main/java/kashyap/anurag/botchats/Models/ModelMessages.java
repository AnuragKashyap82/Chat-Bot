package kashyap.anurag.botchats.Models;

public class ModelMessages {

    String message, messageId, response, messageType, imageUrl;

    public ModelMessages() {
    }

    public ModelMessages(String message, String messageId, String response, String messageType, String imageUrl) {
        this.message = message;
        this.messageId = messageId;
        this.response = response;
        this.messageType = messageType;
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

