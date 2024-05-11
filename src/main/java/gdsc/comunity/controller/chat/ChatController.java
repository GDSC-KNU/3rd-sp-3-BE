package gdsc.comunity.controller.chat;

import gdsc.comunity.dto.chat.ChatStateDto;
import gdsc.comunity.dto.chat.Chatting;
import gdsc.comunity.dto.chat.PagingChatting;
import gdsc.comunity.exception.CustomException;
import gdsc.comunity.exception.ErrorCode;
import gdsc.comunity.service.ChatService;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/{channelId}/state")
    public void connection(ChatStateDto chatStateDto, @DestinationVariable Long channelId){
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , chatStateDto);
    }

    @MessageMapping("/{channelId}")
    public void chat(Chatting chatting, @DestinationVariable Long channelId) {
        chatService.saveChat(chatting, channelId);
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , chatting);
    }

    @PostMapping("/{channelId}/{userNickname}/file")
    public ResponseEntity<Void> chatFileUpload(@PathVariable Long channelId, @PathVariable String userNickname, @RequestParam("file") MultipartFile file) {
        Chatting chatting = chatService.saveChatFile(channelId, userNickname, file);
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , chatting);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{channelId}/{userNickname}/image")
    public ResponseEntity<Void> chatImageUpload(@PathVariable Long channelId, @PathVariable String userNickname, @RequestParam("images") MultipartFile[] images) {
        Chatting chatting = chatService.saveChatImage(channelId, userNickname, images);
        messagingTemplate.convertAndSend("/api/sub/" + channelId
                , chatting);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file/{fileCode}")
    public ResponseEntity<ByteArrayResource> chatFileDownload(@PathVariable String fileCode) {
        try {
            File file = chatService.getChatFile(fileCode);
            byte[] fileData = Files.readAllBytes(file.toPath());
            String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);

            ByteArrayResource resource = new ByteArrayResource(fileData);

            ResponseEntity<ByteArrayResource> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);

            file.delete();

            return responseEntity;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DOWNLOAD_ERROR);
        }
    }

    @GetMapping("/api/chatLog/{channelId}")
    public ResponseEntity<PagingChatting> getChatLog(@PathVariable Long channelId,
                                                     @RequestParam int page){
        return ResponseEntity.ok(chatService.getChatLog(channelId, page));
    }

}
