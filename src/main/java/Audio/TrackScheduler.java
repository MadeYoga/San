package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TrackScheduler extends AudioEventAdapter {

    AudioPlayer player;
    Queue<VoiceEntry> queue;
    MessageChannel channel;
    public boolean repeat = false;
    VoiceEntry NowPlay;

    public TrackScheduler(AudioPlayer audioPlayer){
        super();
        this.player = audioPlayer;
        this.queue = new LinkedList<>();
    }

    public void queue(VoiceEntry entry){
        if (!player.startTrack(entry.track, true)) {
            queue.offer(entry);
        } else {
            NowPlay = entry;
        }
    }

    public VoiceEntry currentlyPlayingEntry(){
        return NowPlay;
    }

    String getMinuteFormat(long duration_millis){
        String duration_minute = String.valueOf( (int)(duration_millis/1000)/60 );
        String duration_second = String.valueOf( (int)(duration_millis/1000)%60 );
        if (duration_minute.length() == 1) duration_minute = "0" + duration_minute;
        if (duration_second.length() == 1) duration_second = "0" + duration_second;
        String result = duration_minute + ":" + duration_second;
        return result;
    }

    public Object[] getQueueArray(){
        return (Object[]) queue.toArray();
    }

    public Queue<VoiceEntry> getQueue(){ return queue; }

    public EmbedBuilder getEmbeddedNowPlay(){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.pink);
        embed.setTitle(":musical_note: Now Playing " + NowPlay.track.getInfo().title);

        if (NowPlay.picked_from_list){
            embed.setThumbnail(NowPlay.video.thumbnail_url);
        }

        String duration_fmted = getMinuteFormat(NowPlay.track.getDuration());
        String position_fmted = getMinuteFormat(NowPlay.track.getPosition());

        embed.addField("Requester", NowPlay.requester, true);
        embed.addField("Duration", position_fmted + "/" + duration_fmted, true);
        embed.addField("Volume", String.valueOf(player.getVolume()) + "%", true);

        return embed;
    }

    public boolean SkipTrack(){
        if (queue.isEmpty()) {
            return false;
        }
        NowPlay = queue.peek();
        player.startTrack(queue.poll().track, false);
        return true;
    }

    public void Stop(){
        player.stopTrack();
        queue.clear();
        NowPlay = null;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        super.onPlayerPause(player);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        super.onPlayerResume(player);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        super.onTrackStart(player, track);
        System.out.println("playing: " + track.getInfo().title);
        // channel.sendMessage(getEmbeddedNowPlay().build()); /**************************************************************/
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        super.onTrackEnd(player, track, endReason);

        if (repeat) {
            VoiceEntry entry = new VoiceEntry(track.makeClone(),NowPlay.requester, NowPlay.video);
            entry.picked_from_list = NowPlay.picked_from_list;
            queue.offer(entry);
            System.out.println("Repeated");
        }
        if (endReason.mayStartNext){
            NowPlay = queue.peek();
            player.startTrack(queue.poll().track, false);
        }

    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        super.onTrackStuck(player, track, thresholdMs);
    }

    @Override
    public void onEvent(AudioEvent event) {
        super.onEvent(event);
    }
}
