package controllers;

import play.*;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.mvc.*;
import play.mvc.Router.Route;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.jboss.netty.handler.codec.base64.Base64Decoder;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gson.JsonObject;

import models.Participant;
import net.sf.oval.constraint.NotEmpty;

public class Application extends Controller {

    public static void dispatch(
            @Required(message = "signed_request required") String signed_request) {

        if (validation.hasErrors()) {
            error("only usable within fb context");
        }

        String[] tmp = signed_request.split("\\.");
        String sig = tmp[0];/* ignored for now */
        String payload = "";
        try {
            payload = new String(Base64.decodeBase64(tmp[1]), "utf-8");
            Logger.debug("payload: %s", payload);
            JSONObject data = new JSONObject(payload);
            JSONObject page = data.getJSONObject("page");
            if (page == null) {
                Logger.error("app only usable within a fb page");
                notfan();
                return;
            }

            boolean liked = page.getBoolean("liked");

            if (liked) {
                fan();
            } else {
                notfan();
            }

        } catch (UnsupportedEncodingException e) {
            Logger.error("faied to parse signed_request");
        } catch (JSONException e) {
            Logger.error(e.getMessage());
        }
        //default
        fan();
    }

    public static void participate(@Required @Email String mailAddress, 
            @Required String userName) {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            fan();
        }
        
        /*store mailAddress*/
        Participant p = Participant.all().filter("mailAddress", mailAddress).get();
        
        if (!validation.equals(p,null).ok) {
          params.flash();  
          validation.addError("mailAddress", "duplicate");  
          validation.keep();
          fan();
        }
        
        p = new Participant();
        p.mailAddress = mailAddress;
        p.name = userName;
        p.insert();
        
        /*keep mail address in session*/
        session.put("id", p.id);

        question();

    }

    public static void notfan() {
        render();
    }

    public static void fan() {
        render();
    }

    public static void question() {
        render();
    }
    
    public static void test(int aId) {
        
        String feed_name = Messages.get("answer_" + aId);
        String feed_picture = Messages.get("feed_picture", aId);
        
        render("Application/answer.html",feed_name,feed_picture);
    }

    public static void answer(int selection) {
        
        String userId = session.get("id");
        if (userId == null) {
            error("not registered");
        } 
        
        Participant p = new Participant();
        p.id = Long.parseLong(userId);
        p.get();
        if (p.mailAddress == null) {
            error("not registered");
        }
        
        p.answer = selection;
        p.update();
        session.clear();
        
        Logger.debug( p.name + " answered with selection: " + selection);
        
        //lookup correct answerText (used in FB 'feed' dialog)
        String feed_name = Messages.get("answer_" + selection);
        String feed_picture = Messages.get("feed_picture", selection);
        
        
        render(feed_name,feed_picture);
    }

}
