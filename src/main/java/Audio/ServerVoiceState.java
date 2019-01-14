package Audio;

import Listener.MusicCommandListener;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import config.SanConfiguration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;

public class ServerVoiceState {

    AudioPlayerManager playerManager;
    AudioManager audioManager;
    TrackScheduler trackScheduler;

    MessageChannel channel;
    Guild guild;

    public SanConfiguration config;

    public YTVideo []videos;

    public ArrayList<String> commands = new ArrayList<String>();

    public int entry_limit = 7; // default

    public ServerVoiceState(AudioManager audioManager){

        config = new SanConfiguration();

        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        AudioPlayer player = playerManager.createPlayer();
        player.setVolume(25); // DEFAULT

        this.audioManager = audioManager;
        this.audioManager.setSendingHandler(new AudioPlayerSendHandler(player));

        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);

        setCommands();

    }

    public ServerVoiceState(AudioManager audioManager, Guild guild, MessageChannel channel){

        config = new SanConfiguration();

        this.channel = channel;
        this.guild = guild;

        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        AudioPlayer player = playerManager.createPlayer();
        player.setVolume(25); // DEFAULT

        this.audioManager = audioManager;
        this.audioManager.setSendingHandler(new AudioPlayerSendHandler(player));

        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        trackScheduler.channel = channel;

        setCommands();

    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setPlayerManager(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public AudioPlayer getPlayer() {
        return trackScheduler.player;
    }

    public void setPlayer(AudioPlayer player) {
        this.trackScheduler.player = player;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public void setTrackScheduler(TrackScheduler trackScheduler) {
        this.trackScheduler = trackScheduler;
    }

    public void setCommands(){
        commands.add(config.PREFIX + "summon");
        commands.add(config.PREFIX + "leave");
        commands.add(config.PREFIX + "play");
        commands.add(config.PREFIX + "volume");
        commands.add(config.PREFIX + "s");
        commands.add(config.PREFIX + "search");
        commands.add(config.PREFIX + "p");
        commands.add(config.PREFIX + "np");
        commands.add(config.PREFIX + "repeat");
        commands.add(config.PREFIX + "skip");
        commands.add(config.PREFIX + "queue");
        commands.add(config.PREFIX + "playlist");
        commands.add(config.PREFIX + "pause");
        commands.add(config.PREFIX + "resume");
        commands.add(config.PREFIX + "music_prefix");
        commands.add(config.PREFIX + "s_limit");
    }
}
