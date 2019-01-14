package Listener;

import Audio.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import config.Tokens;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import ytube.Auth;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MusicCommandListener extends ListenerAdapter {

    // SERVERS HASH
    Map<String, ServerVoiceState> voice_states;

    public MusicCommandListener(){
        voice_states = new HashMap<String, ServerVoiceState>();
//        try {
////            // String pattern = "dd-MM-yyyy";
////            // SimpleDateFormat format = new SimpleDateFormat(pattern);
////            // String date = format.format(new Date());
////            // System.setOut(new PrintStream(new File(date+"output-file.txt")));
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
    }

    public ServerVoiceState get_voice_state(MessageReceivedEvent event){
        ServerVoiceState state = voice_states.get(event.getGuild().getId());
        if (state == null){
            AudioManager audioManager = event.getGuild().getAudioManager();
            state = new ServerVoiceState(audioManager, event.getGuild(), event.getChannel());
            voice_states.put(event.getGuild().getId(), state);
        }
        return state;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        User author = event.getAuthor();

        if (author.isBot()) return;

        Message message = event.getMessage();
        String[] command = message.getRawContent().split(" ", 2);

        ServerVoiceState state = get_voice_state(event);

        if (!state.commands.contains(command[0])) return;

        String msg_content = message.getContent();

        String prefix = state.config.PREFIX;

        if (command.length < 2 && isNumeric(command[0]))
        {
            // input number
            if (state.getTrackScheduler().getQueue().isEmpty())
            {
                // HANDLE EMPTY QUEUE
                return;
            }
            int index = Integer.valueOf(command[0]);
            if (index > state.entry_limit)
            {
                // HANDLE INVALID INPUT NUMBER
                return;
            }
            LoadAndPlay(event, index);
        }

        else if (command[0].equals(prefix+"summon"))
        {
            Summon(event);
        }

        else if (command[0].equals(prefix+"leave"))
        {
            Leave(event);
        }

        else if (command[0].equals(prefix+"play"))
        {
            System.out.println("play: " + msg_content + ", server: " + event.getGuild().getName());

            if (command.length < 2) return; // handle null

            if (command[1].startsWith("https://www.youtu")){
                LoadAndPlay(event, command[1]);
                return;
            }

            if ( !isNumeric(command[1]) )
            {
                Iterator<SearchResult> iteratorSearchResults = YouTubeSearch(command[1], 4, Tokens.YOUTUBE_DATA_API_KEY);
                String url = "";
                while (iteratorSearchResults.hasNext())
                {
                    ResourceId id = iteratorSearchResults.next().getId();
                    if (id.getKind().equals("youtube#video"))
                    {
                        url = "https://www.youtube.com/watch?v=" + id.getVideoId();
                        break;
                    }
                }
                LoadAndPlay(event, url, command[1]);
                return;
            }

            String []indexes = command[1].split(" ", 25);
            for (String i : indexes)
            {
                int index = Integer.valueOf(i);
                if (index > state.entry_limit) continue;
                LoadAndPlay(event, index - 1); // -1 bcs index videos starts from 0
            }

        }

        else if (command[0].equals(prefix+"volume"))
        {
            if (command.length < 2)
            {
                event.getChannel().sendMessage("`n>volume 50` to change current volume to 50%").queue();
                return;
            }
            if (!isNumeric(command[1]))
            {
                event.getChannel().sendMessage("`n>volume 50` to change current volume to 50%").queue();
                return;
            }
            Volume(event, command[1]);
        }

        else if (command[0].equals(prefix+"s") || command[0].equals(prefix+"search"))
        {

            System.out.println("Search: " + msg_content + ", server: " + event.getGuild().getName() );
            state.videos = new YTVideo[state.entry_limit];

            String search_key = getMessageContent(command[0].length(), msg_content);
            Iterator<SearchResult> iteratorSearchResults = YouTubeSearch(search_key, state.entry_limit, Tokens.YOUTUBE_DATA_API_KEY);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Searching for: " + search_key);
            embed.setColor(Color.pink);
            embed.setTitle("use **" + prefix + "p (entry_number)** to pick a song to play");
            // embed.setDescription("Prefix: **" + prefix + "**, search_limit: **" + state.entry_limit + "**");
            int index = 0;
            String search_results = "";
            while (iteratorSearchResults.hasNext())
            {
                SearchResult singleVideo = iteratorSearchResults.next();
                ResourceId rId = singleVideo.getId();
                if (rId.getKind().equals("youtube#video"))
                {

                    state.videos[index] = new YTVideo();

                    Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                    state.videos[index].id = rId.getVideoId();
                    state.videos[index].url = "https://www.youtube.com/watch?v=" + rId.getVideoId();
                    state.videos[index].title = singleVideo.getSnippet().getTitle();
                    state.videos[index].thumbnail_url = thumbnail.getUrl();
                    search_results += "**" + String.valueOf(index + 1) + ". [" + state.videos[index].title + "](" + state.videos[index].url + ")**\n";
                    // embed.addField(String.valueOf(index + 1) + ". " + state.videos[index].title, state.videos[index].url, false);

                    index++;

                }
            }

            embed.addField(
                    "**Results**",
                    search_results,
                    false
            );

            embed.setThumbnail(state.videos[0].thumbnail_url);
            embed.setFooter("use " + prefix + "p (entry number) to pick a song to play", null);
            MessageChannel channel = event.getChannel();
            channel.sendMessage(embed.build()).queue();

        }

        else if (command[0].equals(prefix+"p"))
        {
            System.out.println("play: " + msg_content + ", server: " + event.getGuild().getName());

            if (command.length < 2 ) return; // handle null

            if (command[1].startsWith("https://www.youtu"))
            {
                LoadAndPlay(event, command[1]);
                return;
            }

            if ( !isNumeric(command[1]) )
            {
                Iterator<SearchResult> iteratorSearchResults = YouTubeSearch(command[1], 5, Tokens.YOUTUBE_DATA_API_KEY);
                String url = "";
                while (iteratorSearchResults.hasNext())
                {
                    ResourceId id = iteratorSearchResults.next().getId();
                    if (id.getKind().equals("youtube#video"))
                    {
                        url = "https://www.youtube.com/watch?v=" + id.getVideoId();
                        break;
                    }
                }
                LoadAndPlay(event, url, command[1]);
                return;
            }

            String []indexes = command[1].split(" ", 25);
            for (String i : indexes)
            {
                int index = Integer.valueOf(i);
                if (index > state.entry_limit) continue;
                LoadAndPlay(event, index - 1); // -1 bcs index videos starts from 0
            }

        }

        else if (command[0].equals(prefix+"np"))
        {
            VoiceEntry entry = state.getTrackScheduler().currentlyPlayingEntry();
            if (entry == null)
            {
                MessageChannel channel = event.getChannel();
                channel.sendMessage("not playing anything").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed = state.getTrackScheduler().getEmbeddedNowPlay();

            MessageChannel channel = event.getChannel();
            channel.sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(prefix+"repeat")){
            MessageChannel channel = event.getChannel();
            if (state.getTrackScheduler().repeat) {
                state.getTrackScheduler().repeat = false;
                channel.sendMessage("Repeat: off").queue();
            } else {
                state.getTrackScheduler().repeat = true;
                channel.sendMessage("Repeat: on").queue();
            }
        }

        else if (command[0].equals(prefix+"queue"))
        {
            Object[] obj = state.getTrackScheduler().getQueueArray();
            VoiceEntry []list = new VoiceEntry[obj.length];
            for (int i = 0; i < obj.length; i++)
            {
                list[i] = (VoiceEntry) obj[i];
            }
            VoiceEntry nowPlaying = state.getTrackScheduler().currentlyPlayingEntry();
            if (nowPlaying == null)
            {
                event.getChannel().sendMessage("queue is empty").queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("**" + event.getGuild().getName() + "**");
            embed.setDescription("Prefix: **" + prefix + "**, search_limit: **" + state.entry_limit + "**");
            if (nowPlaying.picked_from_list)
                embed.setThumbnail(nowPlaying.video.thumbnail_url);
            embed.setColor(Color.pink);

            // ADDS QUEUE VALUE TO EMBED
            int count = 1;
            String queue_values = "";
            for (VoiceEntry entry : list)
            {
//                embed.addField(
//                        String.valueOf(count) +". "+ entry.track.getInfo().title + "["+getMinuteFormat(entry.track.getDuration())+"]",
//                        "Requested by " + entry.requester,
//                        false
//                );
                queue_values += String.valueOf(count) +". **"+ entry.track.getInfo().title + " ["+getMinuteFormat(entry.track.getDuration())+"]**\n";
                queue_values += "Requested by " + entry.requester + "\n";

                count+=1;
                if (count == 25) break;
            }
            embed.addField(
                    ":notes: **Queues**",
                    queue_values,
                    false
                    );

            if (state.getTrackScheduler().repeat) {
                embed.addField(
                        ":repeat: **Repeat**",
                        "ON",
                        true
                );
            } else {
                embed.addField(
                        ":repeat: **Repeat**",
                        "OFF",
                        true
                );
            }

            embed.addField(":speaker: **Volume**", String.valueOf(state.getPlayer().getVolume()) + "%", true);
            embed.addField(
                    ":musical_note: **Now Playing**",
                    "**" + nowPlaying.track.getInfo().title + " [" + getMinuteFormat(nowPlaying.track.getPosition()) + "/" + getMinuteFormat(nowPlaying.track.getDuration()) + "]**\nRequested by **" + nowPlaying.requester + "**",
                    false
            );
            embed.setFooter(
                    "Only requester can skip current playing song",
                    event.getGuild().getSelfMember().getUser().getAvatarUrl()
            );
            MessageChannel channel = event.getChannel();
            channel.sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(prefix+"skip"))
        {
            if (state.getTrackScheduler().currentlyPlayingEntry() == null)
            {
                return; // handle
            }

            if (state.getTrackScheduler().currentlyPlayingEntry().requester.equals(author.getName()))
            {
                if (state.getTrackScheduler().SkipTrack()){
                    EmbedBuilder embed = new EmbedBuilder();
                    embed = state.getTrackScheduler().getEmbeddedNowPlay();
                    event.getChannel().sendMessage(embed.build()).queue();
                } else {
                    Leave(event);
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("**Finished playing music**");
                    embed.addField(
                            "Support",
                            "Report bug [Join Support Server](https://discord.gg/Y8sB4ay)\n[Vote](https://discordbots.org/bot/458298539517411328/vote):hearts:",
                            true
                    );
                    event.getChannel().sendMessage(embed.build()).queue();
                    return;
                }
            }
        }

        else if (command[0].equals(prefix+"playlist"))
        {
            if (state.videos == null)
            {
                event.getChannel().sendMessage("theres no entries, **" + prefix + "s <search_key>** to search, then try !playlist command again").queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.pink);
            embed.setTitle("use **" + prefix + "p (entry_number)** to pick a song to play");
            embed.setDescription("Prefix: **" + prefix + "**, search_limit: **" + state.entry_limit + "**");
            int count = 1;
            for (YTVideo video : state.videos)
            {
                embed.addField(String.valueOf(count) + ". " + video.title, video.url, false);
                count++;
            }
            embed.setFooter("use **" + prefix + "p (entry number)** to pick a song to play", null);
            event.getChannel().sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(prefix+"pause"))
        {
            AudioPlayer player = state.getPlayer();
            if (player == null) return;
            if (!player.isPaused())
                state.getPlayer().setPaused(true);
        }

        else if (command[0].equals(prefix+"resume"))
        {
            AudioPlayer player = state.getPlayer();
            if (player == null) return;
            if (player.isPaused())
            {
                player.setPaused(false);
                EmbedBuilder embed = new EmbedBuilder();
                embed = state.getTrackScheduler().getEmbeddedNowPlay();
                event.getChannel().sendMessage(embed.build()).queue();
            }
        }

        else if (command[0].equals(prefix+"s_limit"))
        {
            if (command.length < 2 || !isNumeric(command[1]))
            {
                event.getChannel().sendMessage("usage: **" + prefix+"s_limit 5**\nto change search limit to 5").queue();
                return;
            }
            state.entry_limit = Integer.valueOf(command[1]);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.pink);
            embed.setTitle("**:mega: changed search limit to " + Integer.valueOf(command[1]) + "**");
            event.getChannel().sendMessage(embed.build()).queue();
        }

        else if (command[0].equals(prefix+"music_prefix")){
            if (msg_content.equals("") || msg_content == null || command.length <= 1)
            {
                event.getChannel().sendMessage("not a valid prefix").queue();
                return;
            }
            state.commands.clear();
            state.commands.add(command[1] + "summon");
            state.commands.add(command[1] + "leave");
            state.commands.add(command[1] + "play");
            state.commands.add(command[1] + "volume");
            state.commands.add(command[1] + "s");
            state.commands.add(command[1] + "search");
            state.commands.add(command[1] + "p");
            state.commands.add(command[1] + "np");
            state.commands.add(command[1] + "repeat");
            state.commands.add(command[1] + "skip");
            state.commands.add(command[1] + "queue");
            state.commands.add(command[1] + "playlist");
            state.commands.add(command[1] + "pause");
            state.commands.add(command[1] + "resume");
            state.commands.add(command[1] + "s_limit");
            state.commands.add(command[1] + "music_prefix");
            state.config.PREFIX = command[1];
            event.getChannel().sendMessage("prefix changed to ' " + command[1] + " '").queue();
        }

    }

    public boolean isNumeric(String content){
        for (char character : content.toCharArray()){
            if (character == ' ') continue;
            if (!Character.isDigit(character)) {
                System.out.println("Character not number: " + character);
                return false;
            }
        }
        return true;
    }

    public String getMinuteFormat(long duration_millis){
        String duration_minute = String.valueOf( (int)(duration_millis/1000)/60 );
        String duration_second = String.valueOf( (int)(duration_millis/1000)%60 );
        if (duration_minute.length() == 1) duration_minute = "0" + duration_minute;
        if (duration_second.length() == 1) duration_second = "0" + duration_second;
        String result = duration_minute + ":" + duration_second;
        return result;
    }

    public Iterator<SearchResult> YouTubeSearch(String search_key, int s_limit, String api){
        Iterator<SearchResult> searchResultIterator = null;

        YouTube youtube;
        try {

            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            String queryTerm = search_key;

            YouTube.Search.List search = youtube.search().list("id,snippet");

            String apiKey = api;                                  /// API KEY !!!!!!!!!
            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults((long) s_limit);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            searchResultIterator = searchResultList.iterator();

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return searchResultIterator;
    }

    public void Leave(MessageReceivedEvent event){
        /*
        * CLEARS QUEUE AND LEAVE VOICE CHANNEL
        * */
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        ServerVoiceState state = get_voice_state(event);
        state.getTrackScheduler().Stop();
        voice_states.remove(event.getGuild().getId());
    }

    public boolean Summon(MessageReceivedEvent event){
        Member member = event.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            event.getChannel().sendMessage("Are you sure you're in voice channel ?").queue();
            return false;
        }
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
        return true;
    }

    // AUTO LOAD AND PLAY
    public void LoadAndPlay(MessageReceivedEvent event, String msg_content, String search_key){

        Member san = event.getGuild().getSelfMember();
        /*
         * IF SAN IS NOT IN VOICE CHANNEL
         */
        if (!san.getVoiceState().inVoiceChannel()){
            if (!Summon(event)){
                return;
            }
        }

        ServerVoiceState state = get_voice_state(event);
        AudioPlayerManager playerManager = state.getPlayerManager();
        TrackScheduler trackScheduler = state.getTrackScheduler();

        String identifier = msg_content;//getMessageContent(5, msg_content); // url

        final MessageChannel channel = event.getChannel();

        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName());
                trackScheduler.queue(entry);
                // channel.sendMessage(":musical_note: Enqueued **" + track.getInfo().title + "**\nnot the song you want? try `n>s "+ search_key +"` to search the song").queue();
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(":musical_note: Enqueued **" + track.getInfo().title + "**");
                embed.setColor(Color.pink);
                embed.setDescription("not the song you want? try `"+state.config.PREFIX+"s "+search_key+"` to search the song");
                embed.addField("Requester", entry.requester, true);
                embed.addField("Duration", getMinuteFormat(track.getPosition()) + "/" + getMinuteFormat(track.getDuration()), true);
                embed.addField("Volume", String.valueOf(state.getPlayer().getVolume()) + "%", true);
                channel.sendMessage(embed.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName());
                    trackScheduler.queue(entry);
                }
                channel.sendMessage("Enqueued " + identifier).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("noMatches: " + identifier+"\nplease use !s " + identifier + " to search").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("loadFailed: " + identifier).queue();
            }
        });

    }

    // LOAD AND PLAY YOUTUBE LINK
    public void LoadAndPlay(MessageReceivedEvent event, String msg_content){

        Member san = event.getGuild().getSelfMember();
        /*
         * IF SAN IS NOT IN VOICE CHANNEL
         */
        if (!san.getVoiceState().inVoiceChannel()){
            if (!Summon(event)){
                return;
            }
        }

        ServerVoiceState state = get_voice_state(event);
        AudioPlayerManager playerManager = state.getPlayerManager();
        TrackScheduler trackScheduler = state.getTrackScheduler();

        String identifier = msg_content;//getMessageContent(5, msg_content); // url

        final MessageChannel channel = event.getChannel();

        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName());
                trackScheduler.queue(entry);
                channel.sendMessage(":musical_note: Enqueued **" + track.getInfo().title + "**").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName());
                    trackScheduler.queue(entry);
                }
                channel.sendMessage("Enqueued " + identifier).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("noMatches: " + identifier+"\nplease use !s " + identifier + " to search").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("loadFailed: " + identifier).queue();
            }
        });

    }

    // LOAD AND PLAY ENTRY
    public void LoadAndPlay(MessageReceivedEvent event, int picked_index){

        Member san = event.getGuild().getSelfMember();
        /*
         * IF SAN IS NOT IN VOICE CHANNEL
         */
        if (!san.getVoiceState().inVoiceChannel()){
            if (!Summon(event)){
                return;
            }
        }

        ServerVoiceState state = get_voice_state(event);
        AudioPlayerManager playerManager = state.getPlayerManager();
        TrackScheduler trackScheduler = state.getTrackScheduler();

        String identifier = state.videos[picked_index].url;

        final MessageChannel channel = event.getChannel();

        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName(), state.videos[picked_index]);
                entry.picked_from_list = true;
                trackScheduler.queue(entry);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(":musical_note: Enqueued " + state.videos[picked_index].title);
                embed.addField("Requester", entry.requester, true);
                String duration_fmted = getMinuteFormat(entry.track.getDuration());
                String position_fmted = getMinuteFormat(entry.track.getPosition());
                embed.addField("Duration", position_fmted + "/" + duration_fmted , true);
                embed.addField("Volume", String.valueOf(state.getPlayer().getVolume()) + "%", true);
                embed.setColor(Color.pink);
                embed.setThumbnail(state.videos[picked_index].thumbnail_url);

                channel.sendMessage(embed.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    VoiceEntry entry = new VoiceEntry(track, event.getAuthor().getName());
                    // picked_from_list
                    trackScheduler.queue(entry);
                }
                channel.sendMessage("Enqueued " + identifier).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("noMatches: " + identifier).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("loadFailed: " + identifier).queue();
            }
        });

    }

    public void Volume(MessageReceivedEvent event, String msg_content){

        int volume = Integer.valueOf(msg_content);

        ServerVoiceState state = get_voice_state(event);
        AudioPlayer audioPlayer = state.getPlayer();
        int volume_before = state.getPlayer().getVolume();
        audioPlayer.setVolume(volume);

        MessageChannel channel = event.getChannel();
        channel.sendMessage("Volume before: " + volume_before + "%, after: " + volume + "%").queue();

    }

    public String getMessageContent(int command_length, String message_content){
        String result = message_content.substring(command_length);
        //result = result.replaceFirst(" ", "");
        result = result.trim();
        return  result;
    }

}
