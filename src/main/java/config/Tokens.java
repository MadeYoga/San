package config;

import java.util.Map;

public class Tokens
{

    public static String YOUTUBE_DATA_API_KEY = "";
    public static String BOT_TOKEN = "";

    public Tokens()
    {
        Map<String, String> env = System.getenv();
        if (!env.containsKey("YOUTUBE_DATA_API_KEY") || !env.containsKey("BOT_TOKEN"))
        {
            Hidden hidden = new Hidden();
            this.YOUTUBE_DATA_API_KEY = hidden.YOUTUBE_DATA_API_KEY;
            this.BOT_TOKEN = hidden.BOT_TOKEN;
        }
        else
        {
            this.YOUTUBE_DATA_API_KEY = env.get("YOUTUBE_DATA_API_KEY");
            this.BOT_TOKEN = env.get("BOT_TOKEN");
        }
    }

}
