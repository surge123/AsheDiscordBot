package harry.AsheBot.Events;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import harry.AsheBot.AsheBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class memberAFK extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        //System.out.println("I got called");

        //start of AFK command
        if(args[0].equalsIgnoreCase("~AFK")){
            String afk = "";
            if(args.length == 1){
                event.getChannel().sendMessage("Bruh enter your AFK!").queue();
            }
            else{
                for (int i = 1; i < args.length; i++) {
                    afk += args[i] + " ";
                }
                afk = afk.substring(0, afk.length()-1);
                User temp = new User();
                String memberID = event.getMember().getId();
                DBObject query = new BasicDBObject("memberID", memberID);
                DBCursor cursor = AsheBot.users.find(query);
                temp.setMemberID(memberID);
                temp.setXp((int)cursor.one().get("XP"));
                temp.setTimer((int)cursor.one().get("Timer"));
                temp.setAfk(afk);
                AsheBot.users.findAndModify(query, AsheBot.convert(temp));
                System.out.println(AsheBot.users.find(new BasicDBObject("memberID", memberID)).one());
                event.getChannel().sendMessage("You are now afk for: " + afk).queue();
            }
        }

        if(!args[0].equalsIgnoreCase("~AFK")){
            String member = event.getMember().getId();
            DBObject queryAFK = new BasicDBObject("memberID", member);
            DBCursor cursor1 = AsheBot.users.find(queryAFK);
            String afkMess = (String)cursor1.one().get("AFK");
            System.out.println(afkMess);
            if(!afkMess.equalsIgnoreCase("")){
                event.getChannel().sendMessage("Welcome back " + event.getAuthor().getAsMention() + ", I have removed your AFK").queue();
                removeAFK(member);
            }
        }


        //AFK call code
        if(event.getMessage().getContentRaw().contains("<@")){
            List<String> memberIds = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                String temp = args[i];
                if(temp.contains("<@")){
                    memberIds.add(temp.substring(temp.indexOf("<@"), temp.indexOf(">")));
                }
            }
            for (int i = 0; i < memberIds.size(); i++) {
                System.out.println(memberIds.get(i));
                if(memberIds.get(i).contains("!")){
                    Member member = event.getGuild().getMemberById(memberIds.get(i).replace("<@!", "").replace(">", ""));
                    System.out.println(member.getAsMention());
                    DBObject query = new BasicDBObject("memberID", member.getId());
                    DBCursor cursor = AsheBot.users.find(query);
                    String afk = (String)cursor.one().get("AFK");
                    if(!afk.equals("")){

                        EmbedBuilder afkEmbed = new EmbedBuilder();
                        afkEmbed.setTitle(member.getEffectiveName() + " Is Currently AFK");
                        afkEmbed.setDescription("[" + member.getAsMention() + "] is currently " + afk);
                        afkEmbed.setFooter("Contact Them Later!", event.getAuthor().getAvatarUrl());
                        event.getChannel().sendMessage(afkEmbed.build()).queue();
                    }
                }
                else{
                    Member member = event.getGuild().getMemberById(memberIds.get(i).replace("<@", "").replace(">", ""));
                    System.out.println(member.getAsMention());
                    DBObject query = new BasicDBObject("memberID", member.getId());
                    DBCursor cursor = AsheBot.users.find(query);
                    String afk = (String)cursor.one().get("AFK");
                    if(!afk.equals("")){

                        EmbedBuilder afkEmbed = new EmbedBuilder();
                        afkEmbed.setTitle(member.getEffectiveName() + " Is Currently AFK");
                        afkEmbed.setDescription("[" + member.getAsMention() + "] is currently " + afk);
                        afkEmbed.setFooter("Contact Them Later!", event.getAuthor().getAvatarUrl());
                        event.getChannel().sendMessage(afkEmbed.build()).queue();
                    }
                }
            }
        }
        //End of checking for @

    }

    public static void removeAFK(String memberID){
        DBObject queryAFK = new BasicDBObject("memberID", memberID);
        DBCursor cursor1 = AsheBot.users.find(queryAFK);
        User temp = new User();
        temp.setAfk("");
        temp.setTimer((int)cursor1.one().get("Timer"));
        temp.setXp((int)cursor1.one().get("XP"));
        temp.setMemberID(memberID);
        AsheBot.users.findAndModify(queryAFK, AsheBot.convert(temp));
    }
}