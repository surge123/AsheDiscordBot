package harry.AsheBot.Events;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import harry.AsheBot.AsheBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Time;
import java.util.*;

public class XPSystem extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split(" ");

        //System.out.println(AsheBot.users.find().one());

        DBObject query = new BasicDBObject("memberID", event.getAuthor().getId());
        DBCursor cursor = AsheBot.users.find(query);
        //System.out.println(cursor.count());
        if(cursor.count() == 0){
            AsheBot.addNew(event.getAuthor().getId());
        }
        else if (canGetXP(Objects.requireNonNull(event.getMember()))){
            assert event.getMember() != null;
            setXP(event.getMember());
            setTimer(event.getMember().getId());
        }

        if(args[0].equalsIgnoreCase("~XP")){
            if(args.length == 1){
                String xp = getXp(event.getMember().getId())+"";
                EmbedBuilder xpInfo = new EmbedBuilder();
                xpInfo.setColor(0x000000);
                xpInfo.setTitle(event.getMember().getEffectiveName() + "'s XP in Ashe!");
                xpInfo.setDescription("[" + event.getMember().getAsMention() + "] you have: " + xp + " points!\nKeep sending messages!");
                xpInfo.setFooter("Enjoy!", event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(xpInfo.build()).queue();
            }
            else if(args.length == 2){
                Member member;
                StringBuilder id = new StringBuilder();
                for (int i = 0; i < args[1].length(); i++) {
                    char te = args[1].charAt(i);
                    if(te >= '0' && te <= '9'){
                        id.append(te);
                    }
                }

                member = event.getGuild().getMemberById(id.toString());

                DBObject memQuery = new BasicDBObject("memberID", id);
                DBCursor cursor1 = AsheBot.users.find(memQuery);
                if(cursor1.count() == 0){
                    EmbedBuilder noInfo = new EmbedBuilder();
                    noInfo.setTitle("Your Friend Has Not Chatted Yet...");
                    noInfo.setDescription("Tell " + member.getAsMention() + " to start chatting!");
                    noInfo.setFooter("Enjoy", event.getAuthor().getAvatarUrl());
                    event.getChannel().sendMessage(noInfo.build()).queue();
                }
                else{
                    String xp = getXp(member.getId())+"";
                    EmbedBuilder xpInfo = new EmbedBuilder();
                    xpInfo.setColor(0x000000);
                    xpInfo.setTitle(member.getEffectiveName() + "'s XP in Ashe!");
                    xpInfo.setDescription(member.getAsMention() + " you have: " + xp + " points!\nKeep sending messages!");
                    xpInfo.setFooter("Enjoy!", event.getAuthor().getAvatarUrl());
                    event.getChannel().sendMessage(xpInfo.build()).queue();
                }

            }
        }
    }

    public static int getXp(String member){
        String id = member;
        DBObject query = new BasicDBObject("memberID", id);
        DBCursor cursor = AsheBot.users.find(query);
        return (int)cursor.one().get("XP");
    }

    public int getTimer(String memberID){
        String id = memberID;
        DBObject query = new BasicDBObject("memberID", id);
        DBCursor cursor = AsheBot.users.find(query);
        return (int)cursor.one().get("Timer");
    }

    public void setXP(Member member){
        String id = member.getId();
        DBObject query = new BasicDBObject("memberID", id);
        DBCursor cursor = AsheBot.users.find(query);
        int newXp = (int)cursor.one().get("XP") + randXP();
        User temp = new User();
        temp.setBalance((int)cursor.one().get("Bal"));
        temp.setMemberID(id);
        temp.setAfk((String)cursor.one().get("AFK"));
        temp.setXp(newXp);
        temp.setTimer((int)cursor.one().get("Timer"));
        temp.setCurTimer((int)cursor.one().get("curTimer"));
        List<String> warns = (List<String>)cursor.one().get("Warns");
        Warn warn = new Warn(warns);
        AsheBot.users.findAndModify(query, AsheBot.convert(temp, warn));
    }

    public void setTimer(String memberID){
        String id = memberID;
        DBObject query = new BasicDBObject("memberID", id);
        DBCursor cursor = AsheBot.users.find(query);
        User temp = new User();
        temp.setBalance((int)cursor.one().get("Bal"));
        temp.setMemberID(memberID);
        temp.setAfk((String)cursor.one().get("AFK"));
        temp.setXp((int)cursor.one().get("XP"));
        temp.setTimer(0);
        temp.setCurTimer((int)cursor.one().get("curTimer"));
        List<String> warns = (List<String>)cursor.one().get("Warns");
        Warn warn = new Warn(warns);
        AsheBot.users.findAndModify(query, AsheBot.convert(temp, warn));
        new java.util.Timer().schedule(
                new java.util.TimerTask(){
                    public void run(){
                        temp.setBalance((int)cursor.one().get("Bal"));
                        temp.setMemberID(memberID);
                        temp.setAfk((String)cursor.one().get("AFK"));
                        temp.setXp((int)cursor.one().get("XP"));
                        temp.setTimer(1);
                        temp.setCurTimer((int)cursor.one().get("curTimer"));
                        List<String> warns = (List<String>)cursor.one().get("Warns");
                        Warn warn = new Warn(warns);
                        AsheBot.users.findAndModify(query, AsheBot.convert(temp, warn));
                        //System.out.println("Modified");
                    }
                },
                60*1000
        );
    }

    public int randXP(){
        Random r = new Random();
        return r.nextInt(5)+5;
    }

    public boolean canGetXP(Member member){
        System.out.println(member + " can");
        return getTimer(member.getId()) == 1;
    }
//
//    public void startTimer(){
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            DBCursor cursor = AsheBot.users.find();
//            @Override
//            public void run() {
//                while (cursor.hasNext()){
//                    DBObject next = cursor.next();
//                    setTimer((String)next.get("memberID"), getTimer((String)next.get("memberID"))-1);
//                }
//            }
//        };
//        timer.schedule(task, 1000, 1000);
//    }
}
