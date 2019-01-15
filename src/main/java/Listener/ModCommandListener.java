package Listener;

import config.SanConfiguration;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.awt.*;
import java.util.List;

public class ModCommandListener extends ListenerAdapter {

    final String prefix = new SanConfiguration().PREFIX;

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getGuild() == null) {
            event.getChannel().sendMessage("You must run this command in a server").queue();
            return;
        }
        Member author = event.getMember();
        // if () return; // if author is bot

        Message message = event.getMessage();
        String[] command = message.getRawContent().split(" ", 2);
        if (command[0].equals(prefix + "kick")){

            if (!author.hasPermission(Permission.KICK_MEMBERS)){
                event.getChannel().sendMessage("You don't have permissions to kick people").queue();
                return;
            }
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                event.getChannel().sendMessage("You must mention who you want to be kicked").queue();
                return;
            }

            GuildController guildController = event.getGuild().getController();
            Member selfMember = event.getGuild().getSelfMember();
            MessageChannel channel = event.getChannel();
            for (User user : mentionedUsers){
                Member member = event.getGuild().getMember(user);
                if (!selfMember.canInteract(member)) {
                    channel.sendMessage("Cannot kick member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                guildController.kick(member).queue(
                        success -> channel.sendMessage("Kicked " + member.getEffectiveName() + "! Cya!").queue(),
                        error ->
                        {
                            if (error instanceof PermissionException) {
                                PermissionException pe = (PermissionException) error;
                                Permission missingPermission = pe.getPermission();
                                channel.sendMessage("PermissionError kicking [" + member.getEffectiveName() + "]: " + error.getMessage()).queue();
                            } else {
                                channel.sendMessage("Unknown error while kicking [" + member.getEffectiveName() + "]: <" + error.getClass().getSimpleName() + ">: " + error.getMessage());
                            }
                        }
                );
            }
        }

        else if (command[0].equals(prefix + "ban")){
            if (!author.hasPermission(Permission.BAN_MEMBERS)){
                event.getChannel().sendMessage("You don't have permissions to ban people").queue();
                return;
            }
            Member selfMember = event.getGuild().getSelfMember();
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                event.getChannel().sendMessage("You must mention who you want to be banned").queue();
                return;
            }
            for(User user : mentionedUsers){
                Member member = event.getGuild().getMember(user);
                if (!selfMember.canInteract(member)){
                    event.getChannel().sendMessage("Cannot ban member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                event.getGuild().getController().ban(member, 1, "No Reason").queue(
                        success -> event.getChannel().sendMessage("Ban " + member.getEffectiveName() + " for 1 days").queue(),
                        error ->
                        {
                            if (error instanceof PermissionException) {
                                PermissionException pe = (PermissionException) error;
                                Permission missingPermission = pe.getPermission();
                                event.getChannel().sendMessage("PermissionError Banning [" + member.getEffectiveName() + "]: " + error.getMessage()).queue();
                            } else {
                                event.getChannel().sendMessage("Unknown error while Banning [" + member.getEffectiveName() + "]: <" + error.getClass().getSimpleName() + ">: " + error.getMessage()).queue();
                            }
                        }
                );
            }
        }

        else if (command[0].equals(prefix + "add_role")) {
            if (!author.hasPermission(Permission.MANAGE_ROLES)){
                event.getChannel().sendMessage("You don't have permissions to manage roles").queue();
                return;
            }
            Member selfMember = event.getGuild().getSelfMember();
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            List<Role> mentionedRoles = event.getMessage().getMentionedRoles();
            if (mentionedRoles == null || mentionedRoles.isEmpty()){
                event.getChannel().sendMessage("You must mention the roles you want to add to").queue();
                return; // handle
            }
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                event.getChannel().sendMessage("You must mention the users you want to add roles to").queue();
                return; // handle
            }
            for (User user : mentionedUsers){
                Member member = event.getGuild().getMember(user);
                if (!selfMember.canInteract(member)){
                    event.getChannel().sendMessage("Cannot add role to member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                for (Role role : mentionedRoles){
                    try {
                        event.getGuild().getController().addSingleRoleToMember(member, role).queue(
                                success -> { event.getChannel().sendMessage("added role **" + role.getName() + "** to " + member.getEffectiveName()).queue(); },
                                error ->
                                {
                                    if (error instanceof HierarchyException) {
                                        event.getChannel().sendMessage(error.getMessage()).queue();
                                    } else {
                                        event.getChannel().sendMessage("Unknown error while adding role to [" + member.getEffectiveName() + "]: <" + error.getClass().getSimpleName() + ">: " + error.getMessage()).queue();
                                    }
                                }
                        );
                    } catch (Exception e) {
                        event.getChannel().sendMessage(e.getMessage()).queue();
                    }
                }
            }
        }

        else if (command[0].equals(prefix + "c_text")){
            if (command.length < 2) {
                event.getChannel().sendMessage("usage: "+prefix+"c_text new_text_channel\ncreates new text channel named new_text_channel").queue();
                return;
            }
            if (!author.hasPermission(Permission.MANAGE_CHANNEL)){
                event.getChannel().sendMessage("You don't have permissions to manage channel").queue();
                return;
            }
            event.getGuild().getController().createTextChannel(command[1]).queue(
                    success -> event.getChannel().sendMessage("created text channel #" + command[1]).queue(),
                    error ->
                    {
                        event.getChannel().sendMessage(error.getMessage()).queue();
                    }
            );
        }

        else if (command[0].equals(prefix + "c_category")){
            if (command.length < 2) {
                event.getChannel().sendMessage("usage: " + prefix + "c_category new_category\ncreates new category named new_category").queue();
                return;
            }
            if (!author.hasPermission(Permission.MANAGE_CHANNEL)) {
                event.getChannel().sendMessage("You don't have permissions to manage channel").queue();
                return;
            }
            event.getGuild().getController().createCategory(command[1]).queue(
                    success -> event.getChannel().sendMessage("created new Category: " + command[1]).queue(),
                    error -> event.getChannel().sendMessage(error.getMessage()).queue()
            );
        }

        else if (command[0].equals(prefix + "c_voice")){
            if (command.length < 2) {
                event.getChannel().sendMessage("usage: "+prefix+"c_voice new_voice\ncreates new voice channel named new_voice").queue();
                return;
            }
            if (!author.hasPermission(Permission.MANAGE_CHANNEL)){
                event.getChannel().sendMessage("You don't have permissions to manage channel").queue();
                return;
            }
            event.getGuild().getController().createVoiceChannel(command[1]).queue(
                    success -> event.getChannel().sendMessage("created new voice channel: " + command[1]).queue(),
                    error -> event.getChannel().sendMessage(error.getMessage()).queue()
            );
        }

        else if (command[0].equals(prefix + "rm_role")){
            if (!author.hasPermission(Permission.MANAGE_ROLES)){
                event.getChannel().sendMessage("You don't have permissions to manage roles").queue();
                return;
            }
            List <User> mentionedUsers = event.getMessage().getMentionedUsers();
            List <Role> mentionedRoles = event.getMessage().getMentionedRoles();
            if (mentionedRoles == null || mentionedRoles.isEmpty()){
                event.getChannel().sendMessage("You must mention the role you want to remove from").queue();
                return;
            }
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                event.getChannel().sendMessage("You must mention the user").queue();
                return;
            }
            Guild guild = event.getGuild();
            Member selfMember = guild.getSelfMember();

            for(User user : mentionedUsers){
                Member member = guild.getMember(user);
                if (!selfMember.canInteract(member)){
                    event.getChannel().sendMessage("Cannot removes role to member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                for (Role role : mentionedRoles){
                    guild.getController().removeSingleRoleFromMember(member, role).queue(
                            success -> event.getChannel().sendMessage("Removed role **" + role.getName() + "** from " + member.getEffectiveName()).queue(),
                            error -> event.getChannel().sendMessage(error.getMessage()).queue()
                    );
                }
            }
        }

//        else if (command[0].equals(prefix + "nick")){
//            if (!author.hasPermission(Permission.NICKNAME_MANAGE)){
//                return;
//            }
//            List<User> mentionedUsers = message.getMentionedUsers();
//            // Member member = event.getGuild().getMember(mentionedUsers.get(selectedIndex));
//            // event.getGuild().getController().setNickname(member, )
//            Member selfMember = event.getGuild().getSelfMember();
//            for ( User user : mentionedUsers ){
//                Member member = event.getGuild().getMember(user);
//                if (!selfMember.canInteract(member)){
//                    event.getChannel().sendMessage("Cannot set nickname to member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
//                    continue;
//                }
//                event.getGuild().getController().setNickname(member, command[1]).queue(
//                        success -> event.getChannel().sendMessage(member.getAsMention() + " 's nickname: " + command[1] + " Set by: " + author.getEffectiveName()).queue(),
//                        error -> event.getChannel().sendMessage(error.getMessage()).queue()
//                );
//            }
//        }

        else if (command[0].equals(prefix + "mute")){
            if (!author.hasPermission(Permission.VOICE_MUTE_OTHERS)){
                return;
            }
            List <User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                return;
            }
            Member selfMember = event.getGuild().getSelfMember();
            for (User user : mentionedUsers) {
                Member member = event.getGuild().getMember(user);
                if (!selfMember.canInteract(member)){
                    event.getChannel().sendMessage("Cannot mute member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                event.getGuild().getController().setMute(member, true).queue(
                        success -> event.getChannel().sendMessage("muted " + user.getName()).queue(),
                        error -> event.getChannel().sendMessage(error.getMessage()).queue()
                );
            }
        }

        else if (command[0].equals(prefix + "unmute")){
            if (!author.hasPermission(Permission.VOICE_MUTE_OTHERS)){
                return;
            }
            List <User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers == null || mentionedUsers.isEmpty()){
                return;
            }
            Member selfMember = event.getGuild().getSelfMember();
            for (User user : mentionedUsers) {
                Member member = event.getGuild().getMember(user);
                if (!selfMember.canInteract(member)){
                    event.getChannel().sendMessage("Cannot mute member: " + member.getEffectiveName() + ", they are higher in the hierarchy").queue();
                    continue;
                }
                event.getGuild().getController().setMute(member, false).queue(
                        success -> event.getChannel().sendMessage("unmuted " + user.getName()).queue(),
                        error -> event.getChannel().sendMessage(error.getMessage()).queue()
                );
            }
        }

    }
}
