package Audio;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class VoiceEntry {
    public String requester;
    public AudioTrack track;
    public boolean picked_from_list = false;
    public YTVideo video = null;
    public VoiceEntry(AudioTrack track, String r){
        this.requester = r;
        this.track = track;
    }
    public VoiceEntry(AudioTrack track, String r, YTVideo video){
        this.requester = r;
        this.video = video;
        this.track = track;
    }
}
