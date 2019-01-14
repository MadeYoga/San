package Listener;

import config.SanConfiguration;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.List;


public class FunCommandListener extends ListenerAdapter {
    final String prefix = new SanConfiguration().PREFIX;
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        User author = event.getAuthor();
        if(author.isBot()) return;

        Message message = event.getMessage();
        String[] command = message.getRawContent().split(" ", 2);

        if (command[0].equals(prefix+"lenny")) {
            event.getChannel().sendMessage("( ͡° ͜ʖ ͡°)").queue();
        }

        else if(command[0].equals(prefix+"flip")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setImage("https://decollins1969.files.wordpress.com/2016/03/flipping-coin-animated-gif-5.gif");
            embed.setColor(Color.gray);
            embed.setTitle("Flip", "https://decollins1969.files.wordpress.com/2016/03/flipping-coin-animated-gif-5.gif");
            event.getChannel().sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(prefix+"avatar")){
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.BLUE);
                embed.setImage(event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(embed.build()).queue();
                return;
            }
            for (User user : mentionedUsers){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.BLUE);
                embed.setImage(user.getAvatarUrl());
                event.getChannel().sendMessage(embed.build()).queue();
            }
        }

    }
}