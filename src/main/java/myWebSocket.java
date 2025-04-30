import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/ws")
public class myWebSocket {
    @OnOpen
    public void onOpen(Session session) {
      System.out.println("WS opened: " + session.getId());
    }   
  
    @OnMessage
    public void onMessage(String msg, Session session) throws IOException {
      System.out.println("Received: " + msg);
    }
  
    @OnClose
    public void onClose(Session session, CloseReason reason) {
      System.out.println("WS closed: " + reason);
    }
  
    @OnError
    public void onError(Session session, Throwable t) {
      System.out.println(t);
    }
}
