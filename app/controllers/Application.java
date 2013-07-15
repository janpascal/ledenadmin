package controllers;

import java.util.*;

import models.Lid;
import models.Persoon;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.db.ebean.*;

import views.html.*;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.core.models.Role;

public class Application extends Controller {

@Restrict({@Group(Persoon.LID_ROLE)})
    public static Result index() {
        Configuration conf = Play.application().configuration();
        String naamVereniging = conf.getString("ledenadmin.vereniging.naam");
        return ok(index.render(naamVereniging));
    }

   // -- Authentication
    
    public static class Login extends Model {
        
        public String account;
        public String password;
        
        public String validate() {
            try {
                if(Persoon.authenticate(account, password) == null) {
                    return "Invalid user or password";
                }
            } catch (Exception e) {
                Logger.info("Error verifying password", e);
                return "Invalid user or password";
            }
            return null;
        }
        
    }

    public static Result login() {
        return ok(login.render(form(Login.class)));
    }

    public static Result authenticate() throws Exception {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if(loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            String account = loginForm.get().account;
            String password = loginForm.get().password;
            Persoon p = Persoon.authenticate(account, password);
            if (p!=null) {
                session("account", account);
                return redirect(routes.Application.index());
            } else {
                flash("error", "Illegal password");
                return redirect(routes.Application.login());
            }
        }
    }
    
    public static Result logout() {
        session("account", null);
        return redirect(routes.Application.index());
    }

    public static class AccountSettings extends Model {
        
        public String account;
        public String oldPassword;
        public String newPassword;
        public String verifyPassword;

        public Map<String,String> roles;
        
        public String validate() {
            if(!newPassword.isEmpty()) {
                if(!getCurrentAccount().hasRole(Persoon.ADMIN_ROLE)) {
                    // ADMIN is allowed to change password
                    // without knowing old
                    try {
                        if(Persoon.authenticate(account, oldPassword) == null) {
                            return "Oud password niet correct";
                        }
                    } catch (Exception e) {
                        Logger.info("Error verifying password", e);
                        return "Invalid user or password";
                    }
                }
                // When changing password
                if(!newPassword.equals(verifyPassword)) {
                    return "Password komt niet overeen";
                }
            }
            return null;
        }
        
    }

    public static List<String> allRoles() {
        return Persoon.allRoles();
    }

    @Restrict({@Group(Persoon.LID_ROLE)})
    public static Result accountSettings(Long id) {
        // alleen toegestaan voor eigen account of
        // als admin
        Persoon subject = Persoon.find.byId(id);
        if(session("account").equals(subject.accountName) ||
           getCurrentAccount().hasRole(Persoon.ADMIN_ROLE)) {
            AccountSettings settings = new AccountSettings();
            settings.account = subject.accountName;
            settings.roles = new HashMap<String,String>();
            for(Role role: subject.getRoles()) {
                settings.roles.put(role.getName(), "yes");
            }
            return ok(editaccount.render(id, form(AccountSettings.class).fill(settings), subject));
        } else {
            flash("error", "Niet toegestane actie");
            return forbidden();
        }
    }

    @Restrict({@Group(Persoon.LID_ROLE)})
    public static Result saveAccount(Long id) throws Exception {
        // alleen toegestaan voor eigen account of
        // als admin
        java.util.Map<java.lang.String,java.lang.String[]> map = request().body().asFormUrlEncoded();
        for(String k: map.keySet()) {
          String[] v = map.get(k);
          for(String s: v) {
            Logger.debug(k+": "+s);
          }
        }
        Logger.debug(request().uri());
        Logger.debug(request().body().asText());
        Persoon subject = Persoon.find.byId(id);
        if(session("account").equals(subject.accountName) ||
           getCurrentAccount().hasRole(Persoon.ADMIN_ROLE)) {
            Form<AccountSettings> settingsForm = form(AccountSettings.class).bindFromRequest();
            if(settingsForm.hasErrors()) {
                return badRequest(editaccount.render(id,settingsForm, subject));
            } else {
                AccountSettings settings = settingsForm.get();
                // Change account name?
                if(!settings.account.equals(subject.accountName)) {
                    Persoon other = Persoon.findByAccountName(settings.account);
                    if (other==null) {
                        if(session("account").equals(subject.accountName)) {
                            session("account", settings.account);
                        }
                        subject.accountName = settings.account;
                    } else {
                        flash("error", "Account naam al in gebruik");
                        return badRequest(editaccount.render(id,settingsForm,subject));
                    }
                }
                // Change password?
                if(!settings.newPassword.isEmpty()) {
                    subject.setPassword(settings.newPassword);
                }
                // Change security roles?
                if(getCurrentAccount().hasRole(Persoon.ADMIN_ROLE)) {
                    for(String k: settings.roles.keySet()) {
                        Logger.debug("Role: " + k + ": " + settings.roles.get(k));
                    }
                    subject.setRoles(settings.roles.keySet());
                }
                    
                subject.update();
                return redirect(routes.Leden.lijst());
            }
        } else {
            flash("error", "Niet toegestane actie");
            return forbidden();
        }
    }

    public static Persoon getCurrentAccount() {
        return Persoon.findByAccountName(session("account"));
    }

    public static boolean currentRole(String role) {
        Persoon account = getCurrentAccount();
        return account!=null && account.hasRole(role);
    }

    public static String currentAccount() {
        return session("account");
    }
}
