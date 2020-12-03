package caro.client;

import caro.common.KMessage;
import java.io.IOException;

public interface IReceiveMessage {
    public void receiveMessage(KMessage msg) throws IOException;
    
}
