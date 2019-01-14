package Listener;

import games.werewolf.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WerewolfCommandListener extends ListenerAdapter {

    Map<String, WerewolfGameState> game_states;

    public WerewolfCommandListener(){
        game_states = new HashMap<String, WerewolfGameState>();
    }

    public WerewolfGameState get_game_state(GuildMessageReceivedEvent event){
        WerewolfGameState state = game_states.get(event.getGuild().getId());
        if (state == null){
            state = new WerewolfGameState();
            game_states.put(event.getGuild().getId(), state);
        }
        return state;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        User author = event.getAuthor();
        if (author.isBot()) return;

        Message message = event.getMessage();
        String[] command = message.getRawContent().split(" ", 2);

        if (command[0].equals(".start_wolf")){
            WerewolfGameState state = get_game_state(event);
            state.openRegistration = true;
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.gray);
            embed.setTitle(":mega: Registration is now OPEN! type .join_wolf to register!!");
            event.getChannel().sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(".join_wolf")){
            WerewolfGameState state = get_game_state(event);

            WerewolfGamePlayer player;
            if (state.roleQueue.peek().equals("Werewolf")){
                player = new Werewolf();
            } else if (state.roleQueue.peek().equals("Seer")){
                player = new Seer();
            } else if (state.roleQueue.peek().equals("Hunter")){
                player = new Hunter();
            } else if (state.roleQueue.peek().equals("Guardian")){
                player = new Guardian();
            } else {
                player = new WerewolfGamePlayer(); // villager
            }
            player.user = author;

            if (state.JoinGame(player)){
                state.roleQueue.poll();
                EmbedBuilder embed = state.getEmbeddedPlayers();
                event.getChannel().sendMessage(embed.build()).queue();
                if (state.queue.size() >= 5){
                    EmbedBuilder embed2 = new EmbedBuilder();
                    embed.setTitle(":mega: Close Registration and The Game is Now Start!");
                    embed.addField("i will send the role immediately!", ".wolf_role if you want me to send your role again", false);
                    event.getChannel().sendMessage(embed2.build()).queue();
                    for (Object p : state.queue.toArray()){
                        WerewolfGamePlayer wgp = (WerewolfGamePlayer) p;
                        String msg = "";
//                        if (wgp.role.equals("Werewolf")){
//                            // wgp.getFirstMessage() //
//                        }
                        wgp.user.openPrivateChannel().queue((channel) ->
                        {
                            channel.sendMessage(msg).queue();
                        });
                    }
                }
            }



        }
    }
}
