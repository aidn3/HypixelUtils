package com.aidn5.hypixelutils.v1.common.unused;

import java.util.Objects;
import java.util.regex.Pattern;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author aidn5
 * @deprecated
 */
@Deprecated
public class ChatReader {
  private Pattern startPattern;
  private Pattern endPattern;
  private onChatCompleted listener;

  private StringBuilder chatString;

  public ChatReader setStartPattern(Pattern regex) {
    this.startPattern = Objects.requireNonNull(regex);
    return this;
  }

  public Pattern getStartPattern() {
    return startPattern;
  }

  public ChatReader setEndPattern(Pattern regex) {
    this.endPattern = Objects.requireNonNull(regex);
    return this;
  }

  public Pattern getEndPattern() {
    return endPattern;
  }

  public ChatReader setListener(onChatCompleted listener) {
    this.listener = listener;
    return this;
  }

  public onChatCompleted getListener() {
    return listener;
  }

  @SubscribeEvent
  public void onPlayerChatEvent(ClientChatReceivedEvent event) {
    if (event == null || event.type != 0) return;

    String message = event.message.getUnformattedText();

    if (isStartPattern(message)) {
      chatString = new StringBuilder();
    }

    if (chatString != null) chatString.append(message);

    if (isEndPattern(message)) {
      if (chatString != null && chatString.length() > 0 && listener != null)
        listener.get(chatString.toString());

      chatString = null;
    }
  }

  protected boolean isStartPattern(String message) {
    return startPattern.matcher(message).find();
  }

  protected boolean isEndPattern(String message) {
    return endPattern.matcher(message).find();
  }

  public interface onChatCompleted {
    public void get(String chat);
  }
}
