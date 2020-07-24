package harry.AsheBot.Events;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import harry.AsheBot.AsheBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Time;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class XPSystem extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split(" ");

        //System.out.println(AsheBot.users.find().one());

        DBObject query = new BasicDBObject("memberID", event.getMember().getId());
        DBCursor cursor = AsheBot.users.find(query);
        //System.out.println(cursor.count());
        if(cursor.count() == 0){
            User newUser = new User();
            newUser.setAfk("");
            newUser.setMemberID(event.getMember().getId());
            newUser.setXp(0);
            newUser.setTimer(1);
            AsheBot.users.insert(AsheBot.convert(newUser));
        }
        else if (canGetXP(event.getMember())){
            assert event.getMember() != null;
            setXP(event.getMember());
            setTimer(event.getMember().getId());
        }

        if(args[0].equalsIgnoreCase("~XP")){
            if(args.length == 1){
                String xp = getXp(event.getMember())+"";
                event.getChannel().sendMessage("Here is ur XP: " + xp).queue();
            }
            else if(args.length == 2){
                Member member = event.getGuild().getMemberById(args[1].replace("<@!", "").replace(">", ""));
                assert member != null;
                String xp = getXp(member)+"";
                event.getChannel().sendMessage("Here is " + args[1] + "'s XP: " + xp).queue();
            }
        }
    }

    public int getXp(Member member){
        String id = member.getId();
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
        temp.setMemberID(id);
        temp.setAfk((String)cursor.one().get("AFK"));
        temp.setXp(newXp);
        temp.setTimer((int)cursor.one().get("Timer"));
        AsheBot.users.findAndModify(query, AsheBot.convert(temp));
    }

    public void setTimer(String memberID){
        String id = memberID;
        DBObject query = new BasicDBObject("memberID", id);
        DBCursor cursor = AsheBot.users.find(query);
        User temp = new User();
        temp.setMemberID(memberID);
        temp.setAfk((String)cursor.one().get("AFK"));
        temp.setXp((int)cursor.one().get("XP"));
        temp.setTimer(0);
        AsheBot.users.findAndModify(query, AsheBot.convert(temp));
        new java.util.Timer().schedule(
                new java.util.TimerTask(){
                    public void run(){
                        temp.setTimer(1);
                        AsheBot.users.findAndModify(query, AsheBot.convert(temp));
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
