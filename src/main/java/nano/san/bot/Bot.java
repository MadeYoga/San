package nano.san.bot;

import Listener.FunCommandListener;
import Listener.MessageListener;
import Listener.ModCommandListener;
import Listener.MusicCommandListener;

import config.SanConfiguration;
import config.Tokens;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;

public class Bot extends ListenerAdapter {

    static JDA jda;
    //public static SanConfiguration CONFIG = new SanConfiguration();
    public static String BOT_TOKEN = Tokens.BOT_TOKEN;

    public static void main(String args[]){
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(BOT_TOKEN).buildBlocking();
            // jda.addEventListener(new Bot());
            // jda.addEventListener(new MessageListener(jda, CONFIG));
            jda.addEventListener(new MusicCommandListener());
            jda.addEventListener(new FunCommandListener());
            jda.addEventListener(new ModCommandListener());
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        }
//        try {
//            System.setOut(new PrintStream(new File("output-file.txt")));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // handle
        User author = event.getAuthor();
        if (author.isBot()) return;
        Message message = event.getMessage();
        String msg_content = message.getContent();
        String[] command = message.getRawContent().split(" ", 2);
        if (command[0].equals("!help")) {
            if (command.length == 1) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setAuthor("New Music's Commands");
                embed.setColor(Color.pink);
                embed.addField("!help", "!help <command> , to get the command's detail", false);
                String music_commands = "summon, leave, play, skip, s, p, playlist, np, repeat";
                embed.addField("Music Command", music_commands, false);
                embed.setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl());
                event.getChannel().sendMessage(embed.build()).queue();
            } else {
                if (command[1].equals("help")){
                    event.getChannel().sendMessage("usage: !help -> shows this message").queue();
                } else if (command[1].equals("s")){
                    event.getChannel().sendMessage("usage: !s dog -> search 'dog's videos from youtube\nresults would be stored into playlist").queue();
                } else if (command[1].equals("p")){
                    event.getChannel().sendMessage("usage: !p 4 -> picks entries number 4, !playlist to see entries").queue();
                } else if (command[1].equals("playlist")){
                    event.getChannel().sendMessage("usage: !playlist -> shows available entries").queue();
                } else if(command[1].equals("np")){
                    event.getChannel().sendMessage("usage: !np -> display info's about currently playing song").queue();
                } else if (command[1].equals("summon") || command[1].equals("leave")){
                    event.getChannel().sendMessage("usage:\n!summon -> summons bot to join your current voice channel\n!leave -> stop playing song and leaves voice channel").queue();
                } else if (command[1].equals("skip")){
                    event.getChannel().sendMessage("usage: !skip -> skips currently playing song").queue();
                } else if (command[1].equals("repeat")){
                    event.getChannel().sendMessage("usage: !repeat -> repeats currently song when done playing\n(to the last entry if there's a queue)").queue();
                } else if (command[0].equals("music_prefix")){
                    event.getChannel().sendMessage("usage: !music_prefix $ -> change all music commands prefix to $").queue();
                } else if (command[0].equals("queue")){
                    event.getChannel().sendMessage("usage: !queue -> shows queue").queue();
                } else if (command[0].equals("play")){
                    event.getChannel().sendMessage("usage: !play (youtube url) -> youtube url can be a video url or youtube list url").queue();
                }
            }
        }

    }

    public JDA getJDA(){
        return jda;
    }
}
