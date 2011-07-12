package controllers;

import play.*;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.*;

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
            JSONObject data = new JSONObject(payload);
            JSONObject page = data.getJSONObject("page");
            if (page == null) {
                error("app only usable within a fb page");
                return;
            }

            boolean liked = page.getBoolean("liked");

            if (liked) {
                fan();
            } else {
                notfan();
            }

        } catch (UnsupportedEncodingException e) {
            error("faied to parse signed_request");
        } catch (JSONException e) {
            error(e);
        }
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
        
        if (p != null) Logger.debug("mailAddress from db: %s", p.mailAddress);
        
        if (!validation.equals(p,null).ok) {
          params.flash();  
          validation.addError("mailAddress", "duplicate");  
          validation.keep();
          fan();
        }
        
        p = new Participant();
        p.mailAddress = mailAddress;
        p.insert();

        /* save mail address, get active question */
        question(1);

    }

    public static void notfan() {
        render();
    }

    public static void fan() {
        render();
    }

    public static void question(int id) {
        render(id);
    }

    public static void answer(int qId, int selection) {
        
        Logger.debug("question: " + qId + " answered with selection: " + selection);
        
        render();
    }

}
