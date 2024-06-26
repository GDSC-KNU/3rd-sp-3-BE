package gdsc.comunity.controller.chat;

import gdsc.comunity.dto.chat.Chatting;
import gdsc.comunity.dto.chat.PagingChatting;
import gdsc.comunity.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/{channelId}/{userNickname}")
    public void enter(@DestinationVariable Long channelId,
                      @DestinationVariable String userNickname){
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , userNickname);
    }

    @MessageMapping("/{channelId}")
    public void chat(Chatting chatting, @DestinationVariable Long channelId) {
        chatService.saveChat(chatting, channelId);
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , chatting);
    }

    @GetMapping("/api/chatLog/{channelId}")
    public ResponseEntity<PagingChatting> getChatLog(@PathVariable Long channelId,
                                                     @RequestParam int page){
        return ResponseEntity.ok(chatService.getChatLog(channelId, page));
    }

}
