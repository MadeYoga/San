package games.werewolf;

import net.dv8tion.jda.core.EmbedBuilder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class WerewolfGameState {

    public Queue<WerewolfGamePlayer> queue;

    public Queue<String> roleQueue;

    public boolean openRegistration = false;

    private boolean theNight = false;

    public WerewolfGameState(){
        queue = new LinkedList<>();
        roleQueue = new LinkedList<>();

        Random random = new Random();
        int count_s = 0;
        while (roleQueue.size() <= 5 ){
            int rand = random.nextInt(5);
            if (rand == 0) {
                if (!roleQueue.contains("Werewolf"))
                    roleQueue.offer("Werewolf");
            } else if (rand == 1) {
                if (!roleQueue.contains("Guardian"))
                    roleQueue.offer("Guardian");
            } else if (rand == 2) {
                if (!roleQueue.contains("Hunter"))
                    roleQueue.offer("Hunter");
            } else if (rand == 3) {
                if (!roleQueue.contains("Seer"))
                    roleQueue.offer("Seer");
            } else {
                if (count_s < 2){
                    roleQueue.offer("Villager");
                    count_s++;
                }
            }
        }
    }

    public boolean JoinGame(WerewolfGamePlayer player){
//        if (queue.contains(player)) /////////////////////////////////// HANDLE ERROR, UNCOMMENT
//            return false; // the player was already registered
        queue.offer(player);
        if (queue.size() >= 5){
            openRegistration = false;
        }
        return true;
    }

    public boolean isTheNight(){
        return theNight;
    }

    public EmbedBuilder getEmbeddedPlayers(){
        EmbedBuilder embed = new EmbedBuilder();
        int count = 1;
        for (Object player : queue.toArray()){
            WerewolfGamePlayer p = (WerewolfGamePlayer) player;
            embed.addField(count++ + ". " + p.user.getName(), "a", false);
        }
        embed.setThumbnail("https://static1.squarespace.com/static/532a4886e4b0e5f755112794/t/5840712ab8a79b24536d6471/1518480850434/");
        return embed;
    }

}
