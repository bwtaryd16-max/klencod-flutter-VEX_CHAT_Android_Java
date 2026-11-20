package com.vexchat.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.*;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(18,19,30), PANEL = Color.rgb(31,32,48);
    private final int ACCENT = Color.rgb(105,126,255), MUTED = Color.rgb(155,158,178);
    private LinearLayout root, content, tabs;
    private String page = "المحادثات";
    private String folder = "الكل";
    private final ArrayList<Chat> chats = new ArrayList<>();

    static class Chat {
        String name, message, time, folder;
        int color, unread;
        Chat(String n, String m, String t, String f, int c, int u) {
            name=n; message=m; time=t; folder=f; color=c; unread=u;
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        seed();
        showMain();
    }

    private void seed() {
        chats.add(new Chat("أحمد محمد","شلونك؟ إن شاء الله بخير","11:42","الشخصية",0xff4d83c7,2));
        chats.add(new Chat("مجموعة الأصدقاء","تم إرسال صورة","10:18","المجموعات",0xff8a61c2,5));
        chats.add(new Chat("سارة","تمام، شكراً ❤️","أمس","الشخصية",0xffbd668e,0));
        chats.add(new Chat("قناة التقنية","أهلاً بكم في القناة","أمس","القنوات",0xff438e83,0));
        chats.add(new Chat("المفضلة","رسالة محفوظة","الثلاثاء","المفضلة",0xffc18a45,0));
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v, int a) { v.setPadding(a,a,a,a); }
    private void showMain() {
        root = column(); root.setBackgroundColor(BG); setContentView(root);
        LinearLayout header = row(); header.setPadding(20,18,20,12);
        LinearLayout titles = column();
        titles.addView(text("VEX CHAT",22,Color.WHITE,true));
        titles.addView(text("مساحة تجمع محادثاتك",12,MUTED,false));
        header.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        TextView plus = text("＋",30,Color.WHITE,true); plus.setGravity(Gravity.CENTER);
        plus.setBackground(shape(ACCENT,50)); header.addView(plus,new LinearLayout.LayoutParams(48,48));
        plus.setOnClickListener(v -> newChat());
        root.addView(header);

        TextView welcome = text("مرحباً بك 👋",21,Color.WHITE,true);
        welcome.setPadding(20,8,20,4); root.addView(welcome);
        TextView subtitle = text("ابدأ يومك بمحادثة جميلة",13,MUTED,false);
        subtitle.setPadding(20,0,20,14); root.addView(subtitle);

        EditText search = new EditText(this); search.setSingleLine(true);
        search.setTextColor(Color.WHITE); search.setHintTextColor(MUTED);
        search.setHint("⌕  ابحث عن محادثة...");
        search.setTextSize(14); search.setPadding(16,0,16,0);
        search.setBackground(shape(PANEL,28));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,48);
        sp.setMargins(16,0,16,12); root.addView(search,sp);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout folders = row(); folders.setPadding(12,2,12,10);
        String[] fs={"الكل","غير المقروءة","الشخصية","المجموعات","القنوات","المفضلة","العمل"};
        for(String f:fs) {
            TextView chip=text(f,13, f.equals(folder)?Color.WHITE:MUTED,true);
            chip.setGravity(Gravity.CENTER); chip.setPadding(16,10,16,10);
            chip.setBackground(shape(f.equals(folder)?ACCENT:PANEL,40));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,38); cp.setMargins(4,0,4,0);
            folders.addView(chip,cp);
            chip.setOnClickListener(v->{folder=f; showMain();});
        }
        hsv.addView(folders); root.addView(hsv);

        ScrollView scroll = new ScrollView(this);
        content=column(); content.setPadding(12,2,12,8); scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ renderChats(s.toString()); }
            public void afterTextChanged(Editable e){}
        });
        renderChats("");

        tabs=row(); tabs.setPadding(8,8,8,12); tabs.setBackground(shape(PANEL,28));
        String[] names={"المحادثات","جهات الاتصال","الإعدادات","الملف الشخصي"};
        String[] icons={"◉","♙","⚙","●"};
        for(int i=0;i<names.length;i++){
            final String n=names[i];
            LinearLayout item=column(); item.setGravity(Gravity.CENTER);
            TextView ic=text(icons[i],21,n.equals(page)?ACCENT:MUTED,true); ic.setGravity(Gravity.CENTER);
            TextView label=text(n,10,n.equals(page)?ACCENT:MUTED,true); label.setGravity(Gravity.CENTER);
            item.addView(ic); item.addView(label);
            tabs.addView(item,new LinearLayout.LayoutParams(0,52,1));
            item.setOnClickListener(v->{page=n; if(n.equals("المحادثات"))showMain(); else showSimple(n);});
        }
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);
        tp.setMargins(12,6,12,8); root.addView(tabs,tp);
    }

    private void renderChats(String query) {
        content.removeAllViews();
        int count=0;
        for(Chat c:chats) {
            boolean folderMatch=folder.equals("الكل") || (folder.equals("غير المقروءة")?c.unread>0:c.folder.equals(folder));
            boolean queryMatch=c.name.toLowerCase().contains(query.toLowerCase()) || c.message.toLowerCase().contains(query.toLowerCase());
            if(!folderMatch || !queryMatch) continue;
            count++;
            LinearLayout item=row(); item.setPadding(12,12,12,12);
            item.setBackground(shape(PANEL,22));
            LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2); ip.setMargins(0,5,0,5);
            content.addView(item,ip);
            TextView avatar=text(c.name.substring(0,1),21,Color.WHITE,true); avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(shape(c.color,60)); item.addView(avatar,new LinearLayout.LayoutParams(52,52));
            LinearLayout details=column(); details.setPadding(12,0,8,0);
            details.addView(text(c.name,15,Color.WHITE,true));
            details.addView(text(c.message,12,MUTED,false));
            item.addView(details,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout right=column(); right.setGravity(Gravity.CENTER);
            right.addView(text(c.time,10,MUTED,false));
            if(c.unread>0) {
                TextView badge=text(String.valueOf(c.unread),10,Color.WHITE,true);
                badge.setGravity(Gravity.CENTER); badge.setBackground(shape(ACCENT,40));
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(22,22); bp.topMargin=5; right.addView(badge,bp);
            }
            item.addView(right);
            item.setOnClickListener(v->openChat(c));
        }
        if(count==0) {
            TextView empty=text("لا توجد محادثات هنا حالياً ✨",15,MUTED,true);
            empty.setGravity(Gravity.CENTER); empty.setPadding(10,50,10,50); content.addView(empty);
        }
    }

    private void openChat(Chat c) {
        LinearLayout screen=column(); screen.setBackgroundColor(BG); setContentView(screen);
        LinearLayout top=row(); top.setPadding(16,18,16,18);
        TextView back=text("‹",34,Color.WHITE,true); top.addView(back);
        TextView title=text("   "+c.name,18,Color.WHITE,true); top.addView(title);
        screen.addView(top); back.setOnClickListener(v->showMain());
        TextView intro=text("هذه بداية محادثتك مع "+c.name,13,MUTED,false);
        intro.setGravity(Gravity.CENTER); screen.addView(intro,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout composer=row(); composer.setPadding(12,10,12,10);
        EditText msg=new EditText(this); msg.setSingleLine(true); msg.setHint("اكتب رسالة...");
        msg.setTextColor(Color.WHITE); msg.setHintTextColor(MUTED); msg.setBackground(shape(PANEL,30));
        composer.addView(msg,new LinearLayout.LayoutParams(0,48,1));
        TextView send=text("➤",22,Color.WHITE,true); send.setGravity(Gravity.CENTER);
        send.setBackground(shape(ACCENT,40)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(48,48); bp.setMargins(8,0,0,0);
        composer.addView(send,bp); screen.addView(composer);
        send.setOnClickListener(v->{String s=msg.getText().toString().trim(); if(!s.isEmpty()){
            c.message=s; c.time="الآن"; c.unread=0;
            Toast.makeText(this,"تمت إضافة الرسالة محلياً (نسخة تجريبية)",Toast.LENGTH_SHORT).show();
            msg.setText(""); }});
    }

    private void newChat() {
        EditText input=new EditText(this); input.setHint("اسم جهة الاتصال");
        new AlertDialog.Builder(this).setTitle("محادثة جديدة").setView(input)
            .setPositiveButton("إنشاء",(d,w)->{
                String n=input.getText().toString().trim();
                if(!n.isEmpty()){chats.add(0,new Chat(n,"ابدأ المحادثة الآن","الآن","الشخصية",0xff6576d9,0)); folder="الكل"; showMain();}
            }).setNegativeButton("إلغاء",null).show();
    }

    private void showSimple(String name) {
        root=column(); root.setBackgroundColor(BG); setContentView(root);
        TextView heading=text(name,23,Color.WHITE,true); heading.setPadding(22,28,22,20); root.addView(heading);
        String message=name.equals("جهات الاتصال")?"ستظهر جهات اتصالك هنا عند ربط التطبيق بخدمة جهات الاتصال":
            name.equals("الإعدادات")?"إعدادات الإشعارات والمظهر والخصوصية ستتوفر هنا":
            "ملفك الشخصي وإعدادات حسابك";
        TextView body=text(message,15,MUTED,false); body.setGravity(Gravity.CENTER); root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        TextView back=text("العودة إلى المحادثات",15,Color.WHITE,true); back.setGravity(Gravity.CENTER);
        back.setBackground(shape(ACCENT,30)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,50); p.setMargins(20,10,20,30); root.addView(back,p);
        back.setOnClickListener(v->{page="المحادثات";showMain();});
    }
}
