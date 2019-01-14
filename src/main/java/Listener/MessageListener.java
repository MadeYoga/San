package Listener;

import Audio.AudioPlayerSendHandler;
import Audio.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import config.SanConfiguration;
import nano.san.bot.Bot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;

import java.awt.*;
import java.util.Random;

public class MessageListener extends ListenerAdapter {

    JDA SAN_JDA;
    SanConfiguration config;

    public MessageListener(JDA SAN_JDA, SanConfiguration config){
        this.SAN_JDA = SAN_JDA;
        this.config = config;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        User author = event.getAuthor();

        if (author.isBot()) return;

        Message message = event.getMessage();
        String msg_content = message.getContent();
        String[] command = event.getMessage().getRawContent().split(" ", 2);

        if (command[0].equals("!echo")){
            MessageChannel channel = event.getChannel();
            String echo = getMessageContent(5, msg_content);
            channel.sendMessage(echo).queue();
        }

        else if (command[0].equals("!random")){
            MessageChannel channel = event.getChannel();
            Random random = new Random();
            int random_number = random.nextInt(100) + 1;
            channel.sendMessage(author.getName() + "'s random number " + String.valueOf(random_number)).queue();
        }

        else if (command[0].equals("!status")){
            if (!config.OWNER.equals(author.getId())){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.pink);
                embed.setImage("http://i.imgur.com/aF13v7A.gif");
                event.getChannel().sendMessage(embed.build()).queue();
                return;
            }
            MessageChannel channel = event.getChannel();
            String status = getMessageContent(7, msg_content);
            SAN_JDA.getPresence().setGame(Game.playing(status));
            channel.sendMessage("Hay Master, i have changed my status to '"+status+"'").queue();
        }

    }

    public String getMessageContent(int command_length, String message_content){
        String result = message_content.substring(command_length);
        result = result.replaceFirst(" ", "");
        return  result;
    }

}
