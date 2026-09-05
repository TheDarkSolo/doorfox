import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/** JWT is validated on the STOMP CONNECT frame server-side (see JwtChannelInterceptor). */
export function createStompClient(token: string): Client {
  return new Client({
    webSocketFactory: () => new SockJS("/ws"),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
  });
}
